package AST;

public class IntLiteral extends Expression {
    private final int value;

    public IntLiteral(int value, int line, int column) {
        super(line, column);
        this.value = value;
    }

    public int getValue() { return value; }


}