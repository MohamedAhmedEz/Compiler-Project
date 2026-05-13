import AST.ASTPrinter;
import AST.Program;
import Lexer.Scanner;
import Parser.Parser;
import Analysis.SemanticAnalyzer;
public class CompilerTester {

    public static void main(String[] args) {
        System.out.println("🚀 STARTING FINAL COMPILER GAUNTLET 🚀\n");

        runTest("Test 1: Perfectly Valid Code",
                "read a;\n" +
                        "b := a * 10;\n" +
                        "print b;");

        runTest("Test 2: Lexical Error (@ symbol)",
                "x := 10;\n" +
                        "@\n" +
                        "print x;");

        runTest("Test 3: Syntax Error (Missing Semicolon)",
                "x := 10\n" +
                        "print x;");

        runTest("Test 4: Semantic Error (Undeclared Variable)",
                "x := 10;\n" +
                        "print y;");
    }

    private static void runTest(String testName, String sourceCode) {
        System.out.println("==================================================");
        System.out.println(testName);
        System.out.println("==================================================");
        System.out.println("Code:\n" + sourceCode + "\n");

        // 1. Lexical Analysis
        Scanner scanner = new Scanner(sourceCode);

        // 2. Syntax Analysis
        Parser parser = new Parser(scanner);
        Program program = parser.parse();

        // 3. Semantic Analysis
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(program);

        // 4. Report Errors
        boolean hasErrors = false;

        for (var lexErr : scanner.getErrors()) {
            System.out.println("❌ " + lexErr.toString());
            hasErrors = true;
        }

        for (var parseErr : parser.getErrors()) {
            System.out.println("❌ Syntax Error: " + parseErr.getMessage() +
                    " at line " + parseErr.getLine() + ", col " + parseErr.getColumn());
            hasErrors = true;
        }

        for (var semErr : analyzer.getErrors()) {
            System.out.println("❌ " + semErr.toString());
            hasErrors = true;
        }

        if (!hasErrors) {
            System.out.println("✅ Passed cleanly with NO errors!");
        }

        // 5. PRINT THE AST (Always attempt to print whatever we salvaged)
        if (program != null && !program.getStatements().isEmpty()) {
            System.out.println("\n--- Generated AST ---");
            ASTPrinter printer = new ASTPrinter();
            printer.print(program);
            System.out.println(printer.getResult());
        } else {
            System.out.println("\n--- No AST Generated ---");
        }

        System.out.println("\n\n");
    }
}