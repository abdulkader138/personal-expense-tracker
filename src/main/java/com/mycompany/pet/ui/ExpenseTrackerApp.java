package com.mycompany.pet.ui;

import java.awt.GraphicsEnvironment;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mycompany.pet.di.ExpenseTrackerModule;

/**
 * Main application entry point for the Expense Tracker.
 * 
 * This application uses Google Guice for Dependency Injection, following the pattern
 * from "Test-Driven Development, Build Automation, Continuous Integration" book.
 */
public class ExpenseTrackerApp {
    private static final Logger LOGGER = Logger.getLogger(ExpenseTrackerApp.class.getName());
    
    public static void main(String[] args) {
        // Check if running in headless mode
        if (GraphicsEnvironment.isHeadless()) {
            LOGGER.severe("ERROR: This application requires a graphical display.");
            LOGGER.severe("Please run this application in an environment with X11 display support.");
            LOGGER.severe("\nFor WSL, you can:");
            LOGGER.severe("1. Install an X server (e.g., VcXsrv, Xming) on Windows");
            LOGGER.severe("2. Set DISPLAY variable: export DISPLAY=:0.0");
            LOGGER.severe("3. Or run from Eclipse IDE which handles the display automatically");
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                // Create Guice injector with ExpenseTrackerModule
                // All components will be automatically wired together by Guice
                Injector injector = Guice.createInjector(
                    new ExpenseTrackerModule()
                        .mongoHost("localhost")
                        .mongoPort(27017)
                        .databaseName("expense_tracker")
                );

                MainWindow mainWindow = injector.getInstance(MainWindow.class);
                mainWindow.setVisible(true);
            } catch (Exception e) {
                String errorMsg = "Failed to initialize MongoDB database: " + e.getMessage();
                LOGGER.log(Level.SEVERE, errorMsg, e);
                LOGGER.severe("\nPlease ensure:");
                LOGGER.severe("1. MongoDB is running (default: mongodb://localhost:27017)");
                LOGGER.severe("2. The 'expense_tracker' database is accessible");
                
                if (!GraphicsEnvironment.isHeadless()) {
                    JOptionPane.showMessageDialog(null,
                        errorMsg + "\n\nCheck console for setup instructions.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                }
                System.exit(1);
            }
        });
    }
}

