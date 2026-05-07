import AST.ASTPrinter;
import AST.Program;
import Interpreter.Interpreter;
import Interpreter.InterpreterTester;
import Interpreter.RuntimeError;
import Lexer.Scanner;
import Parser.ParseError;
import Parser.Parser;
import Parser.ParserTester;
import Shared.IScanner;
import Shared.Token;
import Shared.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        System.out.println("   TinyCalc Script Language Pipeline");
        System.out.println("   Scanner -> Parser -> AST -> Interpreter");
        System.out.println("------------------------------------------------\n");


        runFullPipelineDemo();

        System.out.println("\n------------------------------------------------");
        System.out.println("             Parser Test Suite");
        System.out.println("------------------------------------------------\n");
        new ParserTester().runTest();

        System.out.println("\n------------------------------------------------");
        System.out.println("           Interpreter Test Suite");
        System.out.println("------------------------------------------------\n");
        new InterpreterTester().runTests();
    }

    private static void runFullPipelineDemo() {

        String source =
                "x := 10;\n" +
                        "y := x + 5;\n" +
                        "print y;\n" +
                        "\n" +
                        "// Exponentiation & modulo\n" +
                        "z := 2 ^ 3 ^ 2;\n" +
                        "w := z % 100;\n" +
                        "print w;\n" +
                        "\n" +
                        "a := -5;\n" +
                        "b := a * (2 + 3);\n" +
                        "print b;";

        System.out.println("-- Full Pipeline Demo --");
        System.out.println("Source:");
        System.out.println(source);

        // Phase 1: Scan
        System.out.println("\n-- Phase 1: Scanner --");
        IScanner scanner2 = new Scanner(source);
        Token token;
        List<Token> allTokens = new ArrayList<>();
        do {
            token = scanner2.getNextToken();
            allTokens.add(token);
        } while (token.getType() != TokenType.EOF);
        System.out.println("Total tokens: " + allTokens.size());

        // Phase 2: Parse -> AST
        System.out.println("\n-- Phase 2: Parser -> AST --");
        try {
            IScanner scanForParser = new Scanner(source);
            Parser parser = new Parser(scanForParser);
            Program program = parser.parse();

            ASTPrinter printer = new ASTPrinter();
            program.accept(printer);
            System.out.println(printer.getResult());

            // Phase 3: Interpret
            System.out.println("-- Phase 3: Interpreter --");
            System.out.println("Output:");
            Interpreter interpreter = new Interpreter();
            interpreter.execute(program);

            System.out.println("\n-- Symbol Table After Execution --");
            System.out.println(interpreter.getSymbolTable());

        } catch (ParseError e) {
            System.out.println("Parse error: " + e);
        } catch (RuntimeError e) {
            System.out.println("Runtime error: " + e);
        }
        System.out.println();
    }
}