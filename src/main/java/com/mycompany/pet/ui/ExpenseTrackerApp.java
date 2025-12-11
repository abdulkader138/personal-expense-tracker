package com.mycompany.pet.ui;

import java.awt.GraphicsEnvironment;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                // Initialize database connection (MongoDB)
                DatabaseConnection dbConnection = new DatabaseConnection();
                DatabaseInitializer initializer = new DatabaseInitializer(dbConnection);
                initializer.initialize();

                // Initialize DAOs and Services
                CategoryDAO categoryDAO = new CategoryDAO(dbConnection);
                ExpenseDAO expenseDAO = new ExpenseDAO(dbConnection);
                CategoryService categoryService = new CategoryService(categoryDAO);
                ExpenseService expenseService = new ExpenseService(expenseDAO, categoryDAO);

                // Create controllers
                com.mycompany.pet.controller.CategoryController categoryController = 
                    new com.mycompany.pet.controller.CategoryController(categoryService);
                com.mycompany.pet.controller.ExpenseController expenseController = 
                    new com.mycompany.pet.controller.ExpenseController(expenseService);

                // Create and show main window (using controllers)
                MainWindow mainWindow = new MainWindow(expenseController, categoryController);
                mainWindow.loadData(); // Load data after window is created
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

