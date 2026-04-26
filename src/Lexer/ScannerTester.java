package Lexer;

import Shared.Token;
import Shared.TokenType;

public class ScannerTester {

    private final String sourceCode;

    public ScannerTester() {
        // Define the sample source string testing all token types
        this.sourceCode =
                "// This is a TinyCalc test program\n" +
                        "read x;\n" +
                        "y := x + 10 * (5 ^ 2);\n" +
                        "print y % 3;\n" +
                        "@ // This unknown character should trigger an ERROR token";
    }

    public void runTest() {
        System.out.println("--- Source Code ---");
        System.out.println(sourceCode);
        System.out.println("\n--- Scanner Output ---");

        Scanner scanner = new Scanner(sourceCode);
        Token token;

        do {
            token = scanner.getNextToken();
            System.out.printf("Token: %-12s | Lexeme: '%s'%n", token.getType(), token.getLexeme());
        } while (token.getType() == TokenType.EOF);
    }


}