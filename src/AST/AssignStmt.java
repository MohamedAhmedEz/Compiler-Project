package AST;

public class AssignStmt extends Statement {
    private final String variableName;
    private final Expression expression;

    public AssignStmt(String variableName, Expression expression,
                      int line, int column) {
        super(line, column);
        this.variableName = variableName;
        this.expression = expression;
    }

    public String getVariableName()        { return variableName; }
    public Expression getExpression()      { return expression; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}