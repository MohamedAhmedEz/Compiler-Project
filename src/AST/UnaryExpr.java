package AST;

public class UnaryExpr extends Expression {
    private final String operator;
    private final Expression operand;

    public UnaryExpr(String operator, Expression operand, int line, int column) {
        super(line, column);
        this.operator = operator;
        this.operand = operand;
    }

    public String getOperator() { return operator; }
    public Expression getOperand() { return operand; }


}