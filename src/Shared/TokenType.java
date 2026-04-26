package Shared;

/**
 * Enum of all valid tokens in the TinyCalc language.
 */
public enum TokenType {
    // Keywords
    PRINT, READ,

    // Identifiers & Literals
    IDENTIFIER, INTEGER,

    // Operators
    ASSIGN, PLUS, MINUS, MUL, DIV, MOD, POW,

    // Punctuation
    LPAREN, RPAREN, SEMI,

    // Special Tokens
    EOF,      // End of File
    ERROR     // Lexical error
}