package Analysis;

import AST.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SemanticAnalyzer {

    private final Set<String> initializedVars = new HashSet<>();
    // CHANGED: Now uses our new SemanticError class!
    private final List<SemanticError> errors = new ArrayList<>();

    public List<SemanticError> getErrors() {
        return errors;
    }

    public void analyze(Program program) {
        if (program == null || program.getStatements() == null) return;
        for (Statement stmt : program.getStatements()) {
            if (stmt != null) analyzeNode(stmt);
        }
    }

    private void analyzeNode(ASTNode node) {
        if (node == null) return;

        switch (node) {
            case ReadStmt r -> initializedVars.add(r.getVariableName());
            case AssignStmt a -> {
                analyzeNode(a.getExpression());
                initializedVars.add(a.getVariableName());
            }
            case PrintStmt p -> analyzeNode(p.getExpression());
            case BinaryExpr b -> {
                analyzeNode(b.getLeft());
                analyzeNode(b.getRight());
            }
            case UnaryExpr u -> analyzeNode(u.getOperand());
            case Identifier id -> {
                if (!initializedVars.contains(id.getName())) {
                    // CHANGED: Create a SemanticError object
                    errors.add(new SemanticError(
                            "Variable '" + id.getName() + "' used before initialization",
                            id.getLine(),
                            id.getColumn()
                    ));
                }
            }
            case IntLiteral i -> {}
            default -> {}
        }
    }
}