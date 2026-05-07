package AST;

import java.util.List;

public class Program extends ASTNode {
    private final List<Statement> statements;

    public Program(List<Statement> statements) {
        super(0, 0);
        this.statements = statements;
    }

    public List<Statement> getStatements() { return statements; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}