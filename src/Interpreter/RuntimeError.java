package Interpreter;

public class RuntimeError extends RuntimeException {

    private int line   = -1;
    private int column = -1;

    public RuntimeError(String message) {
        super(message);
    }

    public RuntimeError(String message, int line, int column) {
        super(message);
        this.line   = line;
        this.column = column;
    }

    public int getLine()   { return line; }
    public int getColumn() { return column; }

    @Override
    public String toString() {
        if (line >= 0) {
            return "RuntimeError at line " + line
                    + ", column " + column + ": " + getMessage();
        }
        return "RuntimeError: " + getMessage();
    }
}