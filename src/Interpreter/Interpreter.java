package Interpreter;

import AST.AssignStmt;
import AST.BinaryExpr;
import AST.Identifier;
import AST.IntLiteral;
import AST.PrintStmt;
import AST.Program;
import AST.ReadStmt;
import AST.UnaryExpr;
import AST.ASTVisitor;

public class Interpreter implements ASTVisitor<Integer>, IInterpreter {

    private final SymbolTable symbolTable = new SymbolTable();

    // java.util.Scanner for reading user input from the console
    private final java.util.Scanner input = new java.util.Scanner(System.in);

    @Override
    public void execute(Program program) {
        program.accept(this);
    }

    // ── Program ──────────────────────────────────────────

    @Override
    public Integer visit(Program node) {
        for (var stmt : node.getStatements()) {
            stmt.accept(this);
        }
        return null;
    }

    // ── Statements ───────────────────────────────────────

    @Override
    public Integer visit(AssignStmt node) {
        int value = node.getExpression().accept(this);
        symbolTable.set(node.getVariableName(), value);
        return value;
    }

    @Override
    public Integer visit(PrintStmt node) {
        int value = node.getExpression().accept(this);
        System.out.println(value);
        return value;
    }

    @Override
    public Integer visit(ReadStmt node) {
        System.out.print("  Enter value for " + node.getVariableName() + ": ");
        int value;
        try {
            value = Integer.parseInt(input.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new RuntimeError(
                    "Invalid integer input for variable '"
                            + node.getVariableName() + "'",
                    node.getLine(), node.getColumn());
        }
        symbolTable.set(node.getVariableName(), value);
        return value;
    }

    // ── Expressions ──────────────────────────────────────

    @Override
    public Integer visit(IntLiteral node) {
        return node.getValue();
    }

    @Override
    public Integer visit(Identifier node) {
        return symbolTable.get(node.getName());
    }

    @Override
    public Integer visit(UnaryExpr node) {
        int operand = node.getOperand().accept(this);
        if ("-".equals(node.getOperator())) {
            return -operand;
        }
        throw new RuntimeError(
                "Unknown unary operator: " + node.getOperator(),
                node.getLine(), node.getColumn());
    }

    @Override
    public Integer visit(BinaryExpr node) {
        int left  = node.getLeft().accept(this);
        int right = node.getRight().accept(this);

        switch (node.getOperator()) {
            case "+": return left + right;
            case "-": return left - right;
            case "*": return left * right;
            case "/":
                if (right == 0) {
                    throw new RuntimeError("Division by zero",
                            node.getLine(), node.getColumn());
                }
                return left / right;
            case "%":
                if (right == 0) {
                    throw new RuntimeError("Modulo by zero",
                            node.getLine(), node.getColumn());
                }
                return left % right;
            case "^":
                return (int) Math.pow(left, right);
            default:
                throw new RuntimeError(
                        "Unknown operator: " + node.getOperator(),
                        node.getLine(), node.getColumn());
        }
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}