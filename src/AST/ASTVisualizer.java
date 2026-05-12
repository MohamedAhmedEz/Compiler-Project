package AST;

import java.util.stream.Collectors;

public class ASTVisualizer {

    private final StringBuilder sb = new StringBuilder();

    public String toDotFormat(Program program) {
        sb.setLength(0); // clear previous run
        sb.append("digraph AST {\n");
        // General Graphviz formatting options
        sb.append("  node [shape=box, style=filled, color=lightgrey, fontname=\"Courier\"]\n");
        sb.append("  edge [fontname=\"Courier\"]\n");

        // The root 'Program' node
        String rootId = getNodeId(program);
        sb.append("  ").append(rootId).append(" [label=\"Program\", shape=folder, fillcolor=lightblue]\n");

        // Process statements
        for (Statement stmt : program.getStatements()) {
            String stmtId = generateNodes(stmt);
            // Connect Program to each Statement
            sb.append("  ").append(rootId).append(" -> ").append(stmtId).append("\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    /** Helper to generate a unique ID based on memory address */
    private String getNodeId(ASTNode node) {
        return "node_" + System.identityHashCode(node);
    }

    /**
     * Traverses the tree recursively (Pattern Matching)
     * Returns the generated node's ID so it can be linked by the caller.
     */
    private String generateNodes(ASTNode node) {
        String myId = getNodeId(node);

        switch (node) {
            case AssignStmt a -> {
                // The root of the assignment is just the := operator
                sb.append("  ").append(myId).append(" [label=\":=\", shape=box, style=filled, fillcolor=\"#fff3cd\", color=\"#ffc107\", fontcolor=black]\n");

                // We create a "fake" visual node for the variable being assigned to so it acts as the Left branch
                String varId = myId + "_var";
                sb.append("  ").append(varId).append(" [label=\"").append(a.getVariableName()).append("\", shape=ellipse, style=filled, fillcolor=\"#cce5ff\", color=\"#004085\", fontcolor=black]\n");

                // The mathematical expression is the Right branch
                String childId = generateNodes(a.getExpression());

                // Connect them without text labels on the arrows
                sb.append("  ").append(myId).append(" -> ").append(varId).append(" [color=\"#6c757d\"]\n");
                sb.append("  ").append(myId).append(" -> ").append(childId).append(" [color=\"#6c757d\"]\n");
            }
            case PrintStmt p -> {
                sb.append("  ").append(myId).append(" [label=\"print\", shape=box, style=filled, fillcolor=\"#fff3cd\", color=\"#ffc107\", fontcolor=black]\n");

                String childId = generateNodes(p.getExpression());
                sb.append("  ").append(myId).append(" -> ").append(childId).append(" [color=\"#6c757d\"]\n");
            }
            case ReadStmt r -> {
                sb.append("  ").append(myId).append(" [label=\"read\", shape=box, style=filled, fillcolor=\"#fff3cd\", color=\"#ffc107\", fontcolor=black]\n");

                String varId = myId + "_var";
                sb.append("  ").append(varId).append(" [label=\"").append(r.getVariableName()).append("\", shape=ellipse, style=filled, fillcolor=\"#cce5ff\", color=\"#004085\", fontcolor=black]\n");
                sb.append("  ").append(myId).append(" -> ").append(varId).append(" [color=\"#6c757d\"]\n");
            }
            case BinaryExpr b -> {
                String op = b.getOperator().replace("\"", "\\\"");
                // Changed to a circle for a clean math look
                sb.append("  ").append(myId).append(" [label=\"").append(op).append("\", shape=circle, style=filled, fillcolor=\"#e2e3e5\", color=\"#383d41\", fontcolor=black]\n");

                String leftId = generateNodes(b.getLeft());
                String rightId = generateNodes(b.getRight());

                // Graphviz automatically draws the first link on the left, so we don't need text labels!
                sb.append("  ").append(myId).append(" -> ").append(leftId).append(" [color=\"#6c757d\"]\n");
                sb.append("  ").append(myId).append(" -> ").append(rightId).append(" [color=\"#6c757d\"]\n");
            }
            case UnaryExpr u -> {
                sb.append("  ").append(myId).append(" [label=\"").append(u.getOperator()).append("\", shape=circle, style=filled, fillcolor=\"#e2e3e5\", color=\"#383d41\", fontcolor=black]\n");

                String operandId = generateNodes(u.getOperand());
                sb.append("  ").append(myId).append(" -> ").append(operandId).append(" [color=\"#6c757d\"]\n");
            }
            case IntLiteral i -> {
                // Just the raw number
                sb.append("  ").append(myId).append(" [label=\"").append(i.getValue()).append("\", shape=ellipse, style=filled, fillcolor=\"#d4edda\", color=\"#28a745\", fontcolor=black]\n");
            }
            case Identifier id -> {
                // Just the raw variable name
                sb.append("  ").append(myId).append(" [label=\"").append(id.getName()).append("\", shape=ellipse, style=filled, fillcolor=\"#cce5ff\", color=\"#004085\", fontcolor=black]\n");
            }
            case null, default -> {
                // Do nothing
            }
        }

        return myId;
    }
}