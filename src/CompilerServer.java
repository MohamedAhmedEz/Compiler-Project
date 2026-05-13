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
import Analysis.SemanticError; // NEW IMPORT

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
                List<Token> allTokens = new ArrayList<>();
                List<String> allErrors = new ArrayList<>();

                // 1. Lexical Analysis
                Scanner tokenScanner = new Scanner(source);
                Token t;
                do {
                    t = tokenScanner.getNextToken();
                    allTokens.add(t);
                } while (t.getType() != TokenType.EOF);

                String tokensJsonArray = tokensToJsonArray(allTokens);

                // Ask the Scanner for any errors it found internally
                for (Lexer.LexicalError lexErr : tokenScanner.getErrors()) {
                    // Assuming your LexicalError class has a getMessage() or toString() method
                    // If you added a toString() to LexicalError similar to SemanticError, you can just use lexErr.toString()
                    allErrors.add(escapeJson(lexErr.toString()));
                }

                // 2. Syntax Analysis
                Parser parser = new Parser(new Scanner(source)); // Fresh scanner for the parser
                Program program = parser.parse();

                // Ask the Parser for any errors it found
                for (ParseError err : parser.getErrors()) {
                    allErrors.add("Syntax Error: " + escapeJson(err.getMessage() + " at line " + err.getLine() + ", col " + err.getColumn()));
                }

                // 3. Semantic Analysis
                // We run this every time now, letting it analyze the salvaged AST
                Analysis.SemanticAnalyzer analyzer = new Analysis.SemanticAnalyzer();
                analyzer.analyze(program);

                // FIXED: Read SemanticError objects instead of Strings
                for (SemanticError semErr : analyzer.getErrors()) {
                    allErrors.add(escapeJson(semErr.toString()));
                }

                boolean hasErrors = !allErrors.isEmpty();

                // 4. Visualization (Generate AST for whatever the parser managed to salvage!)
                String dotOutput = "";
                try {
                    dotOutput = new ASTVisualizer().toDotFormat(program);
                } catch (Exception ignored) { }

                // 5. Final Response Routing
                if (hasErrors) {
                    StringBuilder msgs = new StringBuilder("[");
                    for (int i = 0; i < allErrors.size(); i++) {
                        msgs.append(String.format("\"%s\"", allErrors.get(i)));
                        if (i < allErrors.size() - 1) msgs.append(", ");
                    }
                    msgs.append("]");

                    return String.format(
                            "{\"success\": false, \"messages\": %s, \"tokens\": %s, \"astDot\": \"%s\"}",
                            msgs.toString(), tokensJsonArray, escapeJson(dotOutput)
                    );
                }

                return String.format(
                        "{\"success\": true, \"messages\": [\"Parsed and Validated successfully\"], \"tokens\": %s, \"astDot\": \"%s\"}",
                        tokensJsonArray, escapeJson(dotOutput)
                );

            } catch (Exception e) {
                e.printStackTrace(); // Helpful for debugging server crashes
                return "{\"success\": false, \"messages\": [\"Internal Server Error\"], \"tokens\": [], \"astDot\": \"\"}";
            }
        }

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