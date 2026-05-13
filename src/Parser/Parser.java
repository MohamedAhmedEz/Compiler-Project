package Parser;

import AST.*;
import Shared.IScanner;
import Shared.Token;
import Shared.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser implements IParser {

    private final IScanner scanner;
    private Token currentToken;
    // NEW: Store errors globally for the Server to read
    private final List<ParseError> errors = new ArrayList<>();

    public Parser(IScanner scanner) {
        this.scanner = scanner;
        advance();
    }

    // NEW: Getter so the Server can check if the parsing was flawless
    public List<ParseError> getErrors() {
        return errors;
    }

    // ── Helpers ───────────────────────────────────────────────
    private void advance() {
        do {
            currentToken = scanner.getNextToken();
        } while (currentToken.getType() == TokenType.ERROR);
    }
    private boolean check(TokenType type) { return currentToken.getType() == type; }

    private Token expect(TokenType type, String message) {
        if (check(type)) {
            Token tok = currentToken;
            advance();
            return tok;
        }
        throw error(message + " (found '" + currentToken.getLexeme() + "')");
    }

    private Token expect(TokenType type) { return expect(type, "Expected " + type); }

    private ParseError error(String message) {
        return new ParseError(message, currentToken.getLine(), currentToken.getColumn());
    }

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
    public Program parse() { return parseProgram(); }

    /** program → statement* */
    public Program parseProgram() {
        List<Statement> statements = new ArrayList<>();

        while (!check(TokenType.EOF)) {
            try {
                statements.add(parseStatement());
            } catch (ParseError e) {
                errors.add(e); // Store the error
                synchronize(); // Recover and keep going!
            }
        }

        // CHANGED: We no longer throw an exception here!
        // We return the Program with whatever statements we successfully parsed.
        return new Program(statements);
    }

    // ... (Keep the rest of your parse methods exactly the same: parseStatement, parseAssignStmt, etc.)

    private Statement parseStatement() {
        if (check(TokenType.PRINT))      return parsePrintStmt();
        if (check(TokenType.READ))       return parseReadStmt();
        if (check(TokenType.IDENTIFIER)) return parseAssignStmt();
        throw error("Expected statement (assignment, print, or read)");
    }

    private AssignStmt parseAssignStmt() {
        Token id = expect(TokenType.IDENTIFIER);
        expect(TokenType.ASSIGN, "Expected ':=' in assignment");
        Expression expr = parseExpr();
        expect(TokenType.SEMI, "Expected ';' after assignment");
        return new AssignStmt(id.getLexeme(), expr, id.getLine(), id.getColumn());
    }

    private PrintStmt parsePrintStmt() {
        Token printTok = expect(TokenType.PRINT);
        Expression expr = parseExpr();
        expect(TokenType.SEMI, "Expected ';' after print statement");
        return new PrintStmt(expr, printTok.getLine(), printTok.getColumn());
    }

    private ReadStmt parseReadStmt() {
        Token readTok = expect(TokenType.READ);
        Token id = expect(TokenType.IDENTIFIER, "Expected variable name after 'read'");
        expect(TokenType.SEMI, "Expected ';' after read statement");
        return new ReadStmt(id.getLexeme(), readTok.getLine(), readTok.getColumn());
    }

    private Expression parseExpr() { return parseAddition(); }

    private Expression parseAddition() {
        Expression left = parseMultiplication();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            Token op = currentToken;
            advance();
            Expression right = parseMultiplication();
            left = new BinaryExpr(left, op.getLexeme(), right, op.getLine(), op.getColumn());
        }
        return left;
    }

    private Expression parseMultiplication() {
        Expression left = parseUnary();
        while (check(TokenType.MUL) || check(TokenType.DIV) || check(TokenType.MOD)) {
            Token op = currentToken;
            advance();
            Expression right = parseUnary();
            left = new BinaryExpr(left, op.getLexeme(), right, op.getLine(), op.getColumn());
        }
        return left;
    }

    private Expression parseUnary() {
        if (check(TokenType.MINUS)) {
            Token op = currentToken;
            advance();
            Expression operand = parseUnary();
            return new UnaryExpr(op.getLexeme(), operand, op.getLine(), op.getColumn());
        }
        return parsePower();
    }

    private Expression parsePower() {
        Expression base = parsePrimary();
        if (check(TokenType.POW)) {
            Token op = currentToken;
            advance();
            Expression exponent = parseUnary();
            return new BinaryExpr(base, op.getLexeme(), exponent, op.getLine(), op.getColumn());
        }
        return base;
    }

    private Expression parsePrimary() {
        if (check(TokenType.INTEGER)) {
            Token num = currentToken;
            advance();
            return new IntLiteral(Integer.parseInt(num.getLexeme()), num.getLine(), num.getColumn());
        }
        if (check(TokenType.IDENTIFIER)) {
            Token id = currentToken;
            advance();
            return new Identifier(id.getLexeme(), id.getLine(), id.getColumn());
        }
        if (check(TokenType.LPAREN)) {
            advance();
            Expression expr = parseExpr();
            expect(TokenType.RPAREN, "Expected ')' after expression");
            return expr;
        }
        throw error("Expected integer, identifier, or '('");
    }
}