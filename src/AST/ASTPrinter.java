package AST;

public class ASTPrinter implements ASTVisitor<Void> {

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


    @Override
    public Void visit(Program node) {
        emit("Program");
        indentLevel++;
        for (Statement stmt : node.getStatements()) {
            stmt.accept(this);
        }
        indentLevel--;
        return null;
    }


    @Override
    public Void visit(AssignStmt node) {
        emit("AssignStmt [" + node.getLine() + ":" + node.getColumn() + "]");
        indentLevel++;
        emit("variable: " + node.getVariableName());
        emit("value:");
        indentLevel++;
        node.getExpression().accept(this);
        indentLevel--;
        indentLevel--;
        return null;
    }

    @Override
    public Void visit(PrintStmt node) {
        emit("PrintStmt [" + node.getLine() + ":" + node.getColumn() + "]");
        indentLevel++;
        node.getExpression().accept(this);
        indentLevel--;
        return null;
    }

    @Override
    public Void visit(ReadStmt node) {
        emit("ReadStmt [" + node.getLine() + ":" + node.getColumn() + "]");
        indentLevel++;
        emit("variable: " + node.getVariableName());
        indentLevel--;
        return null;
    }


    @Override
    public Void visit(IntLiteral node) {
        emit("IntLiteral(" + node.getValue() + ")");
        return null;
    }

    @Override
    public Void visit(Identifier node) {
        emit("Identifier(" + node.getName() + ")");
        return null;
    }

    @Override
    public Void visit(UnaryExpr node) {
        emit("UnaryExpr(" + node.getOperator() + ")");
        indentLevel++;
        emit("operand:");
        indentLevel++;
        node.getOperand().accept(this);
        indentLevel--;
        indentLevel--;
        return null;
    }

    @Override
    public Void visit(BinaryExpr node) {
        emit("BinaryExpr(" + node.getOperator() + ")");
        indentLevel++;
        emit("left:");
        indentLevel++;
        node.getLeft().accept(this);
        indentLevel--;
        emit("right:");
        indentLevel++;
        node.getRight().accept(this);
        indentLevel--;
        indentLevel--;
        return null;
    }
}