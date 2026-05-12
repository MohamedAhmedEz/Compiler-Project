import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import AST.ASTVisualizer;
import AST.Program;
import Lexer.Scanner;
import Parser.Parser;
import Parser.ParseError;
import Shared.Token;
import Shared.TokenType;
import Analysis.SemanticAnalyzer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CompilerServer {

    public static void main(String[] args) throws Exception {
        // Start a server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Define the API route
        server.createContext("/api/compile", new CompileHandler());
        server.setExecutor(null); // Use default executor
        server.start();

        System.out.println("Zero-Dependency Compiler API is running on http://localhost:5173");
    }

    static class CompileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 1. Handle CORS so React can connect
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            // Browser sends an OPTIONS request first to check CORS
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                // 2. Read the request body from React
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                // Hacky but effective way to extract "sourceCode" without a JSON library
                String sourceCode = extractSourceCode(body);

                // 3. Run the compiler
                String jsonResponse = compileToJson(sourceCode);

                // 4. Send the response back to React
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);

                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
            }
        }

        // --- Helper Methods ---

        private String compileToJson(String source) {
            try {
                // 1. Lexical Analysis: Gather Tokens for the frontend grid
                List<Token> allTokens = new ArrayList<>();
                Scanner tokenScanner = new Scanner(source);
                Token t;
                do {
                    t = tokenScanner.getNextToken();
                    allTokens.add(t);
                } while (t.getType() != TokenType.EOF);

                // 2. Syntax Analysis: Build the AST
                Parser parser = new Parser(new Scanner(source));
                Program program = parser.parse();

                // 3. Semantic Analysis: Simple variable initialization check
                // We do this BEFORE visualization to ensure the code is valid
                try {
                    Analysis.SemanticAnalyzer analyzer = new Analysis.SemanticAnalyzer();
                    analyzer.analyze(program);
                } catch (RuntimeException semanticError) {
                    // If initialization check fails, we return success: false
                    // but still send the tokens so the editor can highlight them
                    return String.format(
                            "{\"success\": false, \"messages\": [\"%s\"], \"tokens\": %s, \"astDot\": \"\"}",
                            escapeJson(semanticError.getMessage()),
                            tokensToJsonArray(allTokens)
                    );
                }

                // 4. Visualization: If all checks pass, generate the Graphviz DOT string
                String dotOutput = new ASTVisualizer().toDotFormat(program);

                // Convert the Token list into a JSON Array string
                String tokensJsonArray = tokensToJsonArray(allTokens);

                return String.format(
                        "{\"success\": true, \"messages\": [\"Parsed and Validated successfully\"], \"tokens\": %s, \"astDot\": \"%s\"}",
                        tokensJsonArray,
                        escapeJson(dotOutput)
                );

            } catch (ParseError e) {
                // Handle Syntax Errors (e.g., missing semicolons)
                return String.format(
                        "{\"success\": false, \"messages\": [\"Syntax Error: %s\"], \"tokens\": [], \"astDot\": \"\"}",
                        escapeJson(e.getMessage())
                );
            } catch (Exception e) {
                // Handle unexpected crashes
                return "{\"success\": false, \"messages\": [\"Internal Server Error\"], \"tokens\": [], \"astDot\": \"\"}";
            }
        }

        // --- NEW HELPER METHOD ---
        // Manually builds a JSON array of token objects: [{"type":"...", "lexeme":"..."}, ...]
        private String tokensToJsonArray(List<Token> tokens) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < tokens.size(); i++) {
                Token t = tokens.get(i);

                // Escape the lexeme just in case it contains quotes or slashes
                String safeLexeme = escapeJson(t.getLexeme());

                sb.append(String.format(
                        "{\"type\": \"%s\", \"lexeme\": \"%s\", \"line\": %d, \"column\": %d}",
                        t.getType().toString(), safeLexeme, t.getLine(), t.getColumn()
                ));

                // Add a comma after every token except the last one
                if (i < tokens.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }
        // Extracts the value of "sourceCode" from a basic JSON string: {"sourceCode": "x := 10;"}
        private String extractSourceCode(String jsonBody) {
            String target = "\"sourceCode\":";
            int idx = jsonBody.indexOf(target);
            if (idx == -1) return "";

            int start = jsonBody.indexOf("\"", idx + target.length()) + 1;
            int end = jsonBody.lastIndexOf("\"");

            // Un-escape newlines sent by JSON
            return jsonBody.substring(start, end).replace("\\n", "\n");
        }

        // Escapes quotes and newlines so the DOT string doesn't break our JSON
        private String escapeJson(String raw) {
            return raw.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "");
        }
    }
}