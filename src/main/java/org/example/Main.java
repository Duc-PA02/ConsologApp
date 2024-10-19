package org.example;
import org.example.view.SystemManager;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        SystemManager systemManager = new SystemManager();
        systemManager.startMenu();
    }
}