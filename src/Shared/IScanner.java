package shared;

/**
 * Interface that the Parser will rely on.
 * Teammate B implements this; Teammate C calls it.
 */
public interface IScanner {
    /**
     * Retrieves the next token from the source code.
     * @return The next Token, or an EOF Token if the end is reached.
     */
    Token getNextToken();
}