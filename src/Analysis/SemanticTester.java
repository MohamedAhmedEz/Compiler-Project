package Analysis;

import AST.Program;
import Lexer.Scanner;
import Parser.Parser;

public class SemanticTester {

    public static void main(String[] args) {
        System.out.println("=== RUNNING SEMANTIC TESTS ===\n");

        // Test 1: Using a variable that was never declared
        runTest("Test 1: Print Before Assign",
                "print x;");

        // Test 2: Using a variable on the right-hand side of its own assignment
        runTest("Test 2: Right-Hand Side Evaluation",
                "x := x + 1;");

        // Test 3: Math with an uninitialized variable
        runTest("Test 3: Undeclared Variable in Math",
                "read a;\n" +
                        "b := a + c;\n" +
                        "print b;");

        // Test 4: Perfectly valid code (should have 0 errors)
        runTest("Test 4: Valid Code",
                "read a;\n" +
                        "b := a + 10;\n" +
                        "print b;");
    }

    private static void runTest(String testName, String sourceCode) {
        System.out.println("----- " + testName + " -----");
        System.out.println("Source Code:\n" + sourceCode);

        // 1. Lexical Analysis
        Scanner scanner = new Scanner(sourceCode);

        // 2. Syntax Analysis (Parser automatically pulls tokens from the scanner)
        Parser parser = new Parser(scanner);
        Program program = parser.parse();

        // 3. Semantic Analysis
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(program);

        // 4. Report all errors found in the pipeline
        boolean hasErrors = false;

        // Note: I'm using "var" here assuming you are on Java 10+,
        // otherwise replace "var" with the specific Error class name.
        for (var lexErr : scanner.getErrors()) {
            System.out.println("❌ " + lexErr.toString()); // Assuming LexicalError has toString()
            hasErrors = true;
        }
        for (var parseErr : parser.getErrors()) {
            System.out.println("❌ Syntax Error: " + parseErr.getMessage());
            hasErrors = true;
        }
        for (SemanticError semErr : analyzer.getErrors()) {
            System.out.println("❌ " + semErr.toString());
            hasErrors = true;
        }

        if (!hasErrors) {
            System.out.println("✅ Passed cleanly with NO errors!");
        }
        System.out.println("\n");
    }
}