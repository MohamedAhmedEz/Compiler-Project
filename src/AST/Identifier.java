package AST;

public class Identifier extends Expression {
    private final String name;

    public Identifier(String name, int line, int column) {
        super(line, column);
        this.name = name;
    }

    public String getName() { return name; }


}