package AST;

public class BinaryExpr extends Expression {
    private final Expression left;
    private final String operator;
    private final Expression right;

    public BinaryExpr(Expression left, String operator, Expression right,
                      int line, int column) {
        super(line, column);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression getLeft()     { return left; }
    public String getOperator()     { return operator; }
    public Expression getRight()    { return right; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}