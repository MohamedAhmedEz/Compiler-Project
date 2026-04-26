package Shared;

/**
 * The data transfer object passed from the Scanner.java to the Parser.
 */
public class Token {
    private final TokenType type;
    private final String lexeme;
    private final int line;
    private final int column;
    public static final TokenType[] MATCHABLE_TOKENS = {
            TokenType.PRINT, TokenType.READ, TokenType.INTEGER, TokenType.IDENTIFIER,
            TokenType.ASSIGN, TokenType.PLUS, TokenType.MINUS, TokenType.MUL,
            TokenType.DIV, TokenType.MOD, TokenType.POW, TokenType.LPAREN,
            TokenType.RPAREN, TokenType.SEMI
    };

    public Token(TokenType type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    public TokenType getType() { return type; }
    public String getLexeme() { return lexeme; }
    public int getLine() { return line; }
    public int getColumn() { return column; }

    public static TokenType[] getMatchableTokenTypes() {
        return MATCHABLE_TOKENS;
    }

    @Override
    public String toString() {
        return String.format("Token[type=%s, lexeme='%s', line=%d, col=%d]",
                type, lexeme, line, column);
    }

}