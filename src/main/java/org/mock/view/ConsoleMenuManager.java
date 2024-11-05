package org.mock.view;

import java.util.Scanner;

public class ConsoleMenuManager {
    private final SystemManager systemManager;
    private final Scanner scanner;
    private final String folderPath =  "D:/CodeJava/Project_1/ProcessingFolder";

    public ConsoleMenuManager() {
        this.systemManager = new SystemManager(folderPath);
        this.scanner = new Scanner(System.in);
    }

    public void displayMenu() {
        String choice;

        do {
            System.out.println("=== Menu System Manager ===");
            System.out.println("1. Load all data (Products, Customers, Orders)");
            System.out.println("2. Manage Products");
            System.out.println("   2.1. Add New Products");
            System.out.println("   2.2. Update Products");
            System.out.println("   2.3. Delete Products");
            System.out.println("3. Manage Customers");
            System.out.println("   3.1. Add New Customers");
            System.out.println("   3.2. Update Customers");
            System.out.println("   3.3. Delete Customers");
            System.out.println("4. Manage Orders");
            System.out.println("   4.1. Add New Orders");
            System.out.println("   4.2. Update Orders");
            System.out.println("   4.3. Delete Orders");
            System.out.println("5. Search Functions");
            System.out.println("   5.1. Search Products");
            System.out.println("   5.2. Search Orders by Product ID");
            System.out.println("0. Exit");
            System.out.print("Please enter your choice: ");

            choice = scanner.nextLine();
            processChoice(choice);

        } while (!choice.equals("0"));

        System.out.println("Exiting the System Manager.");
    }

    private void processChoice(String choice) {
        switch (choice) {
            case "1":
                systemManager.processFunction("1");
                break;
            case "2.1":
                systemManager.processFunction("2.1");
                break;
            case "2.2":
                systemManager.processFunction("2.2");
                break;
            case "2.3":
                systemManager.processFunction("2.3");
                break;
            case "3.1":
                systemManager.processFunction("3.1");
                break;
            case "3.2":
                systemManager.processFunction("3.2");
                break;
            case "3.3":
                systemManager.processFunction("3.3");
                break;
            case "4.1":
                systemManager.processFunction("4.1");
                break;
            case "4.2":
                systemManager.processFunction("4.2");
                break;
            case "4.3":
                systemManager.processFunction("4.3");
                break;
            case "5.1":
                systemManager.processFunction("5.1");
                break;
            case "5.2":
                systemManager.processFunction("5.2");
                break;
            case "0":
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
}
