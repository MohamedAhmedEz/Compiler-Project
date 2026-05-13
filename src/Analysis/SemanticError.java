package Analysis;

public class SemanticError {
    private final String message;
    private final int line;
    private final int column;

    public SemanticError(String message, int line, int column) {
        this.message = message;
        this.line = line;
        this.column = column;
    }

    public String getMessage() {
        return message;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return String.format("Semantic Error: %s at line %d, col %d", message, line, column);
    }
}