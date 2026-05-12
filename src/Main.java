import Parser.ParserTester;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("What would you like to do?");
        System.out.println("1. Run AST Console Tests");
        System.out.println("2. Start React API Server");
        System.out.print("Enter choice (1 or 2): ");

        // FIX: Use the fully qualified name for the Java input scanner
        java.util.Scanner input = new java.util.Scanner(System.in);

        int choice = input.nextInt();

        if (choice == 1) {
            System.out.println("Starting tests...\n");
            new ParserTester().runTest();
        } else if (choice == 2) {
            System.out.println("Starting server...\n");
            CompilerServer.main(args); // Calls the main method of your server
        } else {
            System.out.println("Invalid choice.");
        }
    }
}