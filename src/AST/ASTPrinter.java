package AST;

public class ASTPrinter {

    private final StringBuilder sb = new StringBuilder();
    private int indentLevel = 0;

    private void emit(String text) {
        for (int i = 0; i < indentLevel; i++) {
            sb.append("  ");
        }
        sb.append(text).append('\n');
    }

    public String getResult() {
        return sb.toString().trim();
    }

    public void print(ASTNode node) {
        switch (node) {
            case Program p -> {
                emit("Program");
                indentLevel++;
                for (Statement stmt : p.getStatements()) {
                    print(stmt); // Recursive call
                }
                indentLevel--;
            }
            case AssignStmt a -> {
                emit("AssignStmt [" + a.getLine() + ":" + a.getColumn() + "]");
                indentLevel++;
                emit("variable: " + a.getVariableName());
                emit("value:");
                indentLevel++;
                print(a.getExpression());
                indentLevel--;
                indentLevel--;
            }
            case PrintStmt p -> {
                emit("PrintStmt [" + p.getLine() + ":" + p.getColumn() + "]");
                indentLevel++;
                print(p.getExpression());
                indentLevel--;
            }
            case ReadStmt r -> {
                emit("ReadStmt [" + r.getLine() + ":" + r.getColumn() + "]");
                indentLevel++;
                emit("variable: " + r.getVariableName());
                indentLevel--;
            }
            case IntLiteral i -> {
                emit("IntLiteral(" + i.getValue() + ")");
            }
            case Identifier id -> {
                emit("Identifier(" + id.getName() + ")");
            }
            case UnaryExpr u -> {
                emit("UnaryExpr(" + u.getOperator() + ")");
                indentLevel++;
                emit("operand:");
                indentLevel++;
                print(u.getOperand());
                indentLevel--;
                indentLevel--;
            }
            case BinaryExpr b -> {
                emit("BinaryExpr(" + b.getOperator() + ")");
                indentLevel++;
                emit("left:");
                indentLevel++;
                print(b.getLeft());
                indentLevel--;
                emit("right:");
                indentLevel++;
                print(b.getRight());
                indentLevel--;
                indentLevel--;
            }
            default -> throw new IllegalStateException("Unknown AST Node: " + node.getClass().getName());
        }
    }
}