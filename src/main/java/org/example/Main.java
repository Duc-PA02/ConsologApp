package org.example;

import org.example.view.ConsoleMenuManager;
import org.example.view.SystemManager;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Error: Missing arguments. Usage: <function_code> <processing_folder_path>");
            return;
        }
        String functionCode = args[0];
        String folderPath = args[1];
        SystemManager systemManager = new SystemManager(folderPath);
        systemManager.processFunction(functionCode);
//        ConsoleMenuManager consoleMenuManager = new ConsoleMenuManager();
//        consoleMenuManager.displayMenu();
    }
}