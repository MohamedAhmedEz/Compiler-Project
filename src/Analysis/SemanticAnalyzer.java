package Analysis;

import AST.*;
import java.util.HashSet;
import java.util.Set;

public class SemanticAnalyzer {

    private final Set<String> initializedVars = new HashSet<>();

    /**
     * Entry point for analysis.
     * Throws a RuntimeException if an uninitialized variable is used.
     */
    public void analyze(Program program) {
        for (Statement stmt : program.getStatements()) {
            analyzeNode(stmt);
        }
    }

    private void analyzeNode(ASTNode node) {
        switch (node) {
            case ReadStmt r -> {
                // 'read x' makes 'x' initialized
                initializedVars.add(r.getVariableName());
            }
            case AssignStmt a -> {
                // IMPORTANT: Check the expression first
                // e.g., in 'x := x + 1', the 'x' on the right must already exist
                analyzeNode(a.getExpression());
                initializedVars.add(a.getVariableName());
            }
            case PrintStmt p -> {
                analyzeNode(p.getExpression());
            }
            case BinaryExpr b -> {
                analyzeNode(b.getLeft());
                analyzeNode(b.getRight());
            }
            case UnaryExpr u -> {
                analyzeNode(u.getOperand());
            }
            case Identifier id -> {
                // The actual check: Is the variable in our set?
                if (!initializedVars.contains(id.getName())) {
                    throw new RuntimeException(
                            String.format("Semantic Error: Variable '%s' used before initialization at line %d, col %d",
                                    id.getName(), id.getLine(), id.getColumn())
                    );
                }
            }
            case IntLiteral i -> {
                // Numbers don't need initialization
            }
            default -> {}
        }
    }
}