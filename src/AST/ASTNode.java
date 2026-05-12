package AST;

public abstract class ASTNode {
    private final int line;
    private final int column;

    public ASTNode(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine()   { return line; }
    public int getColumn() { return column; }

}