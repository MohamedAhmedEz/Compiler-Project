package Lexer;

import Shared.IScanner;
import Shared.Token;
import Shared.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scanner implements IScanner {

    private String source;
    private int    line   = 1;
    private int    column = 1;

    // NEW: Internal list to hold all lexical errors
    private final List<LexicalError> errors = new ArrayList<>();

    private static final Pattern COMMENT_PATTERN    = Pattern.compile("//[^\n]*");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[ \t\r\n]+");
    private static final Pattern TOKEN_PATTERN      = Pattern.compile(
            "(?<PRINT>\\bprint\\b)"         +
                    "|(?<READ>\\bread\\b)"           +
                    "|(?<IDENTIFIER>[a-zA-Z_][a-zA-Z0-9_]*)" +
                    "|(?<INTEGER>[0-9]+)"            +
                    "|(?<ASSIGN>:=)"                 +
                    "|(?<PLUS>\\+)"                  +
                    "|(?<MINUS>-)"                   +
                    "|(?<MUL>\\*)"                   +
                    "|(?<DIV>/)"                     +
                    "|(?<MOD>%)"                     +
                    "|(?<POW>\\^)"                   +
                    "|(?<LPAREN>\\()"                +
                    "|(?<RPAREN>\\))"               +
                    "|(?<SEMI>;)"                    +
                    "|(?<UNKNOWN>.)"
    );

    private final Matcher commentMatcher;
    private final Matcher whitespaceMatcher;
    private final Matcher tokenMatcher;

    public Scanner(String source) {
        this.source           = (source == null) ? "" : source;
        this.commentMatcher   = COMMENT_PATTERN.matcher(this.source);
        this.whitespaceMatcher= WHITESPACE_PATTERN.matcher(this.source);
        this.tokenMatcher     = TOKEN_PATTERN.matcher(this.source);
    }

    // NEW: Getter so the Server can retrieve the errors
    public List<LexicalError> getErrors() {
        return errors;
    }

    private boolean removeComments() {
        commentMatcher.reset(source);
        if (commentMatcher.lookingAt()) {
            advance(commentMatcher.end());
            return true;
        }
        return false;
    }

    private boolean removeWhitespace() {
        whitespaceMatcher.reset(source);
        if (whitespaceMatcher.lookingAt()) {
            advance(whitespaceMatcher.end());
            return true;
        }
        return false;
    }

    private void advance(int count) {
        String consumed = source.substring(0, count);
        for (char c : consumed.toCharArray()) {
            if (c == '\n') { line++; column = 1; }
            else           { column++; }
        }
        source = source.substring(count);
    }

    @Override
    public Token getNextToken() {
        boolean cleaned;
        do {
            cleaned = removeWhitespace() || removeComments();
        } while (cleaned);

        if (source.isEmpty()) {
            return new Token(TokenType.EOF, "EOF", line, column);
        }

        int tokenLine   = line;
        int tokenColumn = column;

        tokenMatcher.reset(source);
        if (tokenMatcher.lookingAt()) {
            for (TokenType type : Token.MATCHABLE_TOKENS) {
                String lexeme = tokenMatcher.group(type.name());
                if (lexeme != null) {
                    advance(tokenMatcher.end());
                    return new Token(type, lexeme, tokenLine, tokenColumn);
                }
            }

            // CHANGED: We found an unknown character!
            // Log it internally to our new LexicalError list.
            String unknown = tokenMatcher.group("UNKNOWN");
            errors.add(new LexicalError("Unrecognized char '" + unknown + "'", tokenLine, tokenColumn));

            advance(tokenMatcher.end());

            // We still return the ERROR token so the React UI can display it in the grid,
            // but the Server no longer needs to inspect it.
            return new Token(TokenType.ERROR, unknown, tokenLine, tokenColumn);
        }

        return new Token(TokenType.EOF, "EOF", line, column);
    }
}