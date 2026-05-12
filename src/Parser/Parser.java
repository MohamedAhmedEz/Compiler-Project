package Parser;

import AST.*;
import Shared.IScanner;
import Shared.Token;
import Shared.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent parser for the TinyCalc scripting language.
 *
 * Grammar (EBNF):
 *   program        → statement*
 *   statement      → assignment | printStmt | readStmt
 *   assignment     → IDENTIFIER ASSIGN expr SEMI
 *   printStmt      → PRINT expr SEMI
 *   readStmt       → READ IDENTIFIER SEMI
 *   expr           → addition
 *   addition       → multiplication ((PLUS | MINUS) multiplication)*
 *   multiplication → unary ((MUL | DIV | MOD) unary)*
 *   unary          → MINUS unary | power
 *   power          → primary (POW unary)?            // right-associative
 *   primary        → INTEGER | IDENTIFIER | LPAREN expr RPAREN
 */
public class Parser implements IParser {

    private final IScanner scanner;
    private Token currentToken;          // one-token lookahead

    public Parser(IScanner scanner) {
        this.scanner = scanner;
        this.currentToken = scanner.getNextToken();
    }

    // ── Helpers ───────────────────────────────────────────────

    /** Consume the current token and read the next one from the scanner. */
    private void advance() {
        currentToken = scanner.getNextToken();
    }

    /** Return true if the current token has the given type. */
    private boolean check(TokenType type) {
        return currentToken.getType() == type;
    }

    /**
     * If the current token matches, consume it and return it;
     * otherwise return null without consuming anything.
     */
    private Token match(TokenType type) {
        if (check(type)) {
            Token tok = currentToken;
            advance();
            return tok;
        }
        return null;
    }

    /**
     * Consume the current token if it matches; otherwise throw ParseError
     * with a descriptive message including what was found instead.
     */
    private Token expect(TokenType type, String message) {
        if (check(type)) {
            Token tok = currentToken;
            advance();
            return tok;
        }
        throw error(message + " (found '" + currentToken.getLexeme() + "')");
    }

    private Token expect(TokenType type) {
        return expect(type, "Expected " + type);
    }

    /** Build a ParseError anchored at the current token's position. */
    private ParseError error(String message) {
        return new ParseError(message, currentToken.getLine(), currentToken.getColumn());
    }

    /**
     * Synchronise after an error: skip tokens until a statement
     * boundary (SEMI) or EOF is found, so that parsing can continue
     * and potentially report further errors.
     */
    private void synchronize() {
        while (!check(TokenType.EOF)) {
            if (check(TokenType.SEMI)) {
                advance();
                return;
            }
            advance();
        }
    }


    @Override
    public Program parse() {
        return parseProgram();
    }

    /** program → statement* */
    public Program parseProgram() {
        List<Statement> statements = new ArrayList<>();
        List<ParseError> errors = new ArrayList<>();

        while (!check(TokenType.EOF)) {
            try {
                statements.add(parseStatement());
            } catch (ParseError e) {
                errors.add(e);
                synchronize();
            }
        }

        if (!errors.isEmpty()) {
            throw errors.get(0);
        }
        return new Program(statements);
    }

    /** statement → assignment | printStmt | readStmt */
    private Statement parseStatement() {
        if (check(TokenType.PRINT))      return parsePrintStmt();
        if (check(TokenType.READ))       return parseReadStmt();
        if (check(TokenType.IDENTIFIER)) return parseAssignStmt();
        throw error("Expected statement (assignment, print, or read)");
    }

    /** assignment → IDENTIFIER ASSIGN expr SEMI */
    private AssignStmt parseAssignStmt() {
        Token id = expect(TokenType.IDENTIFIER);
        expect(TokenType.ASSIGN, "Expected ':=' in assignment");
        Expression expr = parseExpr();
        expect(TokenType.SEMI, "Expected ';' after assignment");
        return new AssignStmt(id.getLexeme(), expr, id.getLine(), id.getColumn());
    }

    /** printStmt → PRINT expr SEMI */
    private PrintStmt parsePrintStmt() {
        Token printTok = expect(TokenType.PRINT);
        Expression expr = parseExpr();
        expect(TokenType.SEMI, "Expected ';' after print statement");
        return new PrintStmt(expr, printTok.getLine(), printTok.getColumn());
    }

    /** readStmt → READ IDENTIFIER SEMI */
    private ReadStmt parseReadStmt() {
        Token readTok = expect(TokenType.READ);
        Token id = expect(TokenType.IDENTIFIER, "Expected variable name after 'read'");
        expect(TokenType.SEMI, "Expected ';' after read statement");
        return new ReadStmt(id.getLexeme(), readTok.getLine(), readTok.getColumn());
    }


    /** expr → addition */
    private Expression parseExpr() {
        return parseAddition();
    }

    /** addition → multiplication ((PLUS | MINUS) multiplication)* */
    private Expression parseAddition() {
        Expression left = parseMultiplication();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            Token op = currentToken;
            advance();
            Expression right = parseMultiplication();
            left = new BinaryExpr(left, op.getLexeme(), right,
                    op.getLine(), op.getColumn());
        }
        return left;
    }

    /** multiplication → unary ((MUL | DIV | MOD) unary)* */
    private Expression parseMultiplication() {
        Expression left = parseUnary();
        while (check(TokenType.MUL) || check(TokenType.DIV) || check(TokenType.MOD)) {
            Token op = currentToken;
            advance();
            Expression right = parseUnary();
            left = new BinaryExpr(left, op.getLexeme(), right,
                    op.getLine(), op.getColumn());
        }
        return left;
    }

    /** unary → MINUS unary | power */
    private Expression parseUnary() {
        if (check(TokenType.MINUS)) {
            Token op = currentToken;
            advance();
            Expression operand = parseUnary();   // right-recursive
            return new UnaryExpr(op.getLexeme(), operand,
                    op.getLine(), op.getColumn());
        }
        return parsePower();
    }

    /**
     * power → primary (POW unary)?
     * Right-associative:  2 ^ 3 ^ 4  →  2 ^ (3 ^ 4)
     * The exponent calls parseUnary() which will eventually call
     * parsePower() again, producing the right-recursive nesting.
     */
    private Expression parsePower() {
        Expression base = parsePrimary();
        if (check(TokenType.POW)) {
            Token op = currentToken;
            advance();
            Expression exponent = parseUnary();  // enables right-assoc + unary in exponent
            return new BinaryExpr(base, op.getLexeme(), exponent,
                    op.getLine(), op.getColumn());
        }
        return base;
    }

    /** primary → INTEGER | IDENTIFIER | LPAREN expr RPAREN */
    private Expression parsePrimary() {
        // Integer literal
        if (check(TokenType.INTEGER)) {
            Token num = currentToken;
            advance();
            return new IntLiteral(Integer.parseInt(num.getLexeme()),
                    num.getLine(), num.getColumn());
        }
        // Identifier (variable reference)
        if (check(TokenType.IDENTIFIER)) {
            Token id = currentToken;
            advance();
            return new Identifier(id.getLexeme(), id.getLine(), id.getColumn());
        }
        // Parenthesised expression
        if (check(TokenType.LPAREN)) {
            advance();
            Expression expr = parseExpr();
            expect(TokenType.RPAREN, "Expected ')' after expression");
            return expr;
        }
        throw error("Expected integer, identifier, or '('");
    }
}