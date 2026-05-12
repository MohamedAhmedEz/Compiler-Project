package Parser;

import AST.ASTPrinter;
import AST.ASTVisualizer;
import AST.Program;
import Lexer.Scanner;
import Shared.IScanner;

public class ParserTester {

    public void runTest() {

        System.out.println("========================================");
        System.out.println("       TinyCalc Parser Tests");
        System.out.println("========================================\n");

        // ── Valid programs ──────────────────────────────────
        testValid("Test 1: Basic assignment and print",
                "x := 10;\n" +
                        "y := x + 5;\n" +
                        "print y;");

        testValid("Test 2: Read and complex expression",
                "read n;\n" +
                        "result := (n + 3) * 2;\n" +
                        "print result;");

        testValid("Test 3: Nested parentheses",
                "a := 4;\n" +
                        "b := a * (2 + 3);\n" +
                        "print b;");

        testValid("Test 4: Exponentiation (right-assoc) and modulo",
                "x := 2 ^ 3 ^ 2;\n" +
                        "y := x % 5;\n" +
                        "print y;");

        testValid("Test 5: Unary minus",
                "x := -5;\n" +
                        "y := x + -3;\n" +
                        "print y;");

        testValid("Test 6: All operators",
                "a := 10;\n" +
                        "b := -a + 2 * 3 ^ 2 % 4;\n" +
                        "print b;");

        testValid("Test 7: Comments ignored",
                "// Calculate something\n" +
                        "x := 42; // inline comment\n" +
                        "print x;");

        // ── Invalid programs ───────────────────────────────
        testInvalid("Test 8: Missing :=",
                "x + 5;");

        testInvalid("Test 9: Missing semicolon",
                "x := 10\n" +
                        "print x;");

        testInvalid("Test 10: Missing expression",
                "x := ;");

        testInvalid("Test 11: Unmatched parenthesis",
                "x := (5 + 3;\n" +
                        "print x;");
    }

    // ── Helpers ─────────────────────────────────────────────

    private void testValid(String name, String source) {
        System.out.println("── " + name + " ──");
        System.out.println("Source:");
        System.out.println(source);
        System.out.println();

        try {
            IScanner scanner = new Scanner(source);
            Parser parser = new Parser(scanner);
            Program program = parser.parse();

            /*ASTPrinter printer = new ASTPrinter();
            printer.print(program);
            System.out.println("AST:");
            System.out.println(printer.getResult());*/

            ASTVisualizer visualizer = new ASTVisualizer();
            String dotOutput = visualizer.toDotFormat(program);

            System.out.println("AST Graphviz (DOT) Output:");
            System.out.println("========================================\n");
            System.out.println(dotOutput);
            System.out.println("========================================\n");

        } catch (ParseError e) {
            System.out.println("UNEXPECTED ERROR: " + e);
        }
        System.out.println();
    }

    private void testInvalid(String name, String source) {
        System.out.println("── " + name + " ──");
        System.out.println("Source:");
        System.out.println(source);
        System.out.println();

        try {
            IScanner scanner = new Scanner(source);
            Parser parser = new Parser(scanner);
            Program program = parser.parse();

            /*ASTPrinter printer = new ASTPrinter();
            printer.print(program);
            System.out.println("AST (partial - error expected):");
            System.out.println(printer.getResult());*/

            ASTVisualizer visualizer = new ASTVisualizer();
            String dotOutput = visualizer.toDotFormat(program);

            System.out.println("AST Graphviz (DOT) Output:");
            System.out.println("========================================\n");
            System.out.println(dotOutput);
            System.out.println("========================================\n");

        } catch (ParseError e) {
            System.out.println("Caught error (expected): " + e);
        }
        System.out.println();
    }
}