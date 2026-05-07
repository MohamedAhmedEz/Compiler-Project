package Interpreter;

import AST.ASTPrinter;
import AST.Program;
import Lexer.Scanner;
import Parser.ParseError;
import Parser.Parser;
import Shared.IScanner;

public class InterpreterTester {

    public void runTests() {
        System.out.println("========================================");
        System.out.println("   TinyCalc Interpreter Tests");
        System.out.println("========================================\n");

        test("Test 1: Basic assignment & print",
                "x := 10;\ny := x + 5;\nprint y;");

        test("Test 2: All arithmetic operators",
                "a := 20;\nb := a - 8;\nc := b * 2;\nd := c / 3;\nprint d;");

        test("Test 3: Parentheses & precedence",
                "a := 4;\nb := a * (2 + 3);\nprint b;");

        test("Test 4: Exponentiation right-assoc",
                "x := 2 ^ 3 ^ 2;\nprint x;");

        test("Test 5: Modulo",
                "x := 17;\ny := x % 5;\nprint y;");

        test("Test 6: Unary minus",
                "x := -5;\ny := x + -3;\nprint y;");

        test("Test 7: Complex expression",
                "a := 10;\nb := -a + 2 * 3 ^ 2 % 4;\nprint b;");

        testError("Test 8: Uninitialised variable",
                "print z;");

        testError("Test 9: Division by zero",
                "x := 5 / 0;\nprint x;");

        System.out.println("-- Test 10: Read statement --");
        System.out.println("Source:");
        System.out.println("read n;\nresult := (n + 3) * 2;\nprint result;");
        System.out.println("\n(Skipping live input in automated test.)");
        System.out.println("  If n = 7, then result = (7+3)*2 = 20\n");
    }

    private void test(String name, String source) {
        System.out.println("-- " + name + " --");
        System.out.println("Source:");
        System.out.println(source);

        try {
            IScanner scanner = new Scanner(source);
            Parser parser = new Parser(scanner);
            Program program = parser.parse();

            Interpreter interpreter = new Interpreter();
            System.out.println("\nOutput:");
            interpreter.execute(program);

        } catch (ParseError e) {
            System.out.println("Parse ERROR: " + e);
        } catch (RuntimeError e) {
            System.out.println("Runtime ERROR: " + e);
        }
        System.out.println();
    }

    private void testError(String name, String source) {
        System.out.println("-- " + name + " --");
        System.out.println("Source:");
        System.out.println(source);

        try {
            IScanner scanner = new Scanner(source);
            Parser parser = new Parser(scanner);
            Program program = parser.parse();

            Interpreter interpreter = new Interpreter();
            interpreter.execute(program);

            System.out.println("ERROR: Expected runtime error but none occurred!");
        } catch (RuntimeError e) {
            System.out.println("\nCaught (expected): " + e);
        } catch (ParseError e) {
            System.out.println("\nParseError (unexpected): " + e);
        }
        System.out.println();
    }
}