package Parser;

/**
 * Thrown when the parser encounters a token that does not match
 * the expected grammar production.  Carries line/column information
 * taken from the offending token.
 */
public class ParseError extends RuntimeException {

    private final int line;
    private final int column;

    public ParseError(String message, int line, int column) {
        super(message);
        this.line   = line;
        this.column = column;
    }

    public int getLine()   { return line; }
    public int getColumn() { return column; }

    @Override
    public String toString() {
        return "ParseError at line " + line + ", column " + column + ": " + getMessage();
    }
}