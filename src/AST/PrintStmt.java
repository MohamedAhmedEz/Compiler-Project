package AST;

public class PrintStmt extends Statement {
    private final Expression expression;

    public PrintStmt(Expression expression, int line, int column) {
        super(line, column);
        this.expression = expression;
    }

    public Expression getExpression() { return expression; }


}