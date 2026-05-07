package AST;

public interface ASTVisitor<T> {
    T visit(Program    node);
    T visit(AssignStmt node);
    T visit(PrintStmt  node);
    T visit(ReadStmt   node);
    T visit(IntLiteral node);
    T visit(Identifier node);
    T visit(UnaryExpr  node);
    T visit(BinaryExpr node);
}