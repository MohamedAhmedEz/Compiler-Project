package AST;

public class ReadStmt extends Statement {
    private final String variableName;

    public ReadStmt(String variableName, int line, int column) {
        super(line, column);
        this.variableName = variableName;
    }

    public String getVariableName() { return variableName; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}