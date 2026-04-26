import Shared.Token;
import Lexer.Scanner;
import Shared.TokenType;

import java.util.regex.Matcher;


import Shared.Token;
import Shared.TokenType;

public class Main {
    public static void main(String[] args) {
        // 1. Define a sample source string testing all token types
        String sourceCode =
                "// This is a TinyCalc test program\n" +
                        "read x;\n" +
                        "y := x + 10 * (5 ^ 2);\n" +
                        "print y % 3;\n" +
                        "@ // This unknown character should trigger an ERROR token";

        System.out.println("--- Source Code ---");
        System.out.println(sourceCode);
        System.out.println("\n--- Scanner Output ---");

        // 2. Initialize the Scanner (your string-chopping version)
        Scanner scanner = new Scanner(sourceCode);
        Token token;

        // 3. Loop through the tokens until we hit EOF
        do {
            token = scanner.getNextToken();

            // Assuming your Token class has public getters or is a Record.
            // Adjust the accessor methods (like .getType() or .getLexeme()) if yours are named differently.
            // Using printf for a clean, aligned output table
            System.out.printf("Token: %-12s | Lexeme: '%s'%n", typeExtractor(token), lexemeExtractor(token));

        } while (!isEOF(token));
    }

    // Helper methods just in case your Token class uses different getter names
    private static String typeExtractor(Token token) {
        // Replace this with token.getType().name() or similar depending on your Shared.Token implementation
        return token.getType() != null ? token.getType().name() : "UNKNOWN_TYPE";
    }

    private static String lexemeExtractor(Token token) {
        // Replace this with token.getLexeme() depending on your Shared.Token implementation
        return token.getType() != null ? token.getLexeme() : "";
    }

    private static boolean isEOF(Token token) {
        return token.getType() == TokenType.EOF;
    }
}