package com.mycompany.pet.ui;

import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.dao.ExpenseDAO;
import com.mycompany.pet.database.DatabaseConnection;
import com.mycompany.pet.database.DatabaseInitializer;
import com.mycompany.pet.service.CategoryService;
import com.mycompany.pet.service.ExpenseService;

/**
 * Main application entry point for the Expense Tracker.
 */
public class ExpenseTrackerApp {
    public static void main(String[] args) {
        // Check if running in headless mode
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("ERROR: This application requires a graphical display.");
            System.err.println("Please run this application in an environment with X11 display support.");
            System.err.println("\nFor WSL, you can:");
            System.err.println("1. Install an X server (e.g., VcXsrv, Xming) on Windows");
            System.err.println("2. Set DISPLAY variable: export DISPLAY=:0.0");
            System.err.println("3. Or run from Eclipse IDE which handles the display automatically");
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize database connection (MongoDB)
                DatabaseConnection dbConnection = new DatabaseConnection();
                DatabaseInitializer initializer = new DatabaseInitializer(dbConnection);
                initializer.initialize();

                // Initialize DAOs and Services
                CategoryDAO categoryDAO = new CategoryDAO(dbConnection);
                ExpenseDAO expenseDAO = new ExpenseDAO(dbConnection);
                CategoryService categoryService = new CategoryService(categoryDAO);
                ExpenseService expenseService = new ExpenseService(expenseDAO, categoryDAO);

                // Create and show main window
                MainWindow mainWindow = new MainWindow(categoryService, expenseService);
                mainWindow.setVisible(true);
            } catch (Exception e) {
                String errorMsg = "Failed to initialize MongoDB database: " + e.getMessage();
                System.err.println(errorMsg);
                System.err.println("\nPlease ensure:");
                System.err.println("1. MongoDB is running (default: mongodb://localhost:27017)");
                System.err.println("2. The 'expense_tracker' database is accessible");
                
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

