import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);


        System.out.println("Choose an option:");
        System.out.println("1. Start Web Server");
        System.out.println("2. Run Console Tester");
        System.out.print("Enter choice: ");

        String choice = input.nextLine().trim();

        System.out.println();

        switch (choice) {
            case "1":
                System.out.println("Starting web server...");
                try {
                    CompilerServer.main(args);
                } catch (Exception e) {
                    System.out.println("Error " + e.getMessage());
                }
                break;

            case "2":
                System.out.println("Running tester...");
                CompilerTester.main(args);
                break;

            default:
                System.out.println("Invalid choice.");
                break;
        }

        input.close();
    }
}