package Lexer;

import Shared.IScanner;
import Shared.Token;
import Shared.TokenType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Teammate B's implementation of the Scanner (String Mutation Version).
 */
public class Scanner implements IScanner {

    private String source;

    private static final Pattern COMMENT_PATTERN = Pattern.compile("//.*");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[ \t\n\r]+");

    // \G is not needed since lookingAt() matches the start of the current string
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "(?<PRINT>\\bprint\\b)" +
                    "|(?<READ>\\bread\\b)" +
                    "|(?<IDENTIFIER>[a-zA-Z_][a-zA-Z0-9_]*)" +
                    "|(?<INTEGER>[0-9]+)" +
                    "|(?<ASSIGN>:=)" +
                    "|(?<PLUS>\\+)" +
                    "|(?<MINUS>-)" +
                    "|(?<MUL>\\*)" +
                    "|(?<DIV>/)" +
                    "|(?<MOD>%)" +
                    "|(?<POW>\\^)" +
                    "|(?<LPAREN>\\()" +
                    "|(?<RPAREN>\\))" +
                    "|(?<SEMI>;)" +
                    "|(?<UNKNOWN>.)"
    );

    private final Matcher commentMatcher;
    private final Matcher whitespaceMatcher;
    private final Matcher tokenMatcher;

    public Scanner(String source) {
        this.source = source == null ? "" : source;
        this.commentMatcher = COMMENT_PATTERN.matcher(this.source);
        this.whitespaceMatcher = WHITESPACE_PATTERN.matcher(this.source);
        this.tokenMatcher = TOKEN_PATTERN.matcher(this.source);
    }

    private boolean removeComments() {
        commentMatcher.reset(this.source);
        if (commentMatcher.lookingAt()) {
            this.source = this.source.substring(commentMatcher.end());
            return true;
        }
        return false;
    }

    private boolean removeWhitespace() {
        whitespaceMatcher.reset(this.source);
        if (whitespaceMatcher.lookingAt()) {
            this.source = this.source.substring(whitespaceMatcher.end());
            return true;
        }
        return false;
    }

    @Override
    public Token getNextToken() {
        boolean cleaned;
        do {
            cleaned = removeWhitespace() || removeComments();
        } while (cleaned);

        if (this.source.isEmpty()) {
            return new Token(TokenType.EOF, "EOF", 0, 0);
        }

        tokenMatcher.reset(this.source);

        if (tokenMatcher.lookingAt()) {
            for (TokenType type : Token.MATCHABLE_TOKENS) {
                String lexeme = tokenMatcher.group(type.name());

                if (lexeme != null) {
                    this.source = this.source.substring(tokenMatcher.end());
                    return new Token(type, lexeme, 0, 0);
                }
            }

            String unknownLexeme = tokenMatcher.group("UNKNOWN");
            this.source = this.source.substring(tokenMatcher.end());
            return new Token(TokenType.ERROR, unknownLexeme, 0, 0);
        }

        return new Token(TokenType.EOF, "EOF", 0, 0);
    }
}