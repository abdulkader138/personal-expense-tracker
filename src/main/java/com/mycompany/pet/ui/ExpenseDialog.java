package com.mycompany.pet.ui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mycompany.pet.controller.CategoryController;
import com.mycompany.pet.controller.ExpenseController;
import com.mycompany.pet.model.Category;
import com.mycompany.pet.model.Expense;

/**
 * Dialog for adding/editing expenses.
 * 
 * This dialog uses ExpenseController and CategoryController to separate UI concerns from business logic.
 * All database operations are handled asynchronously by the controllers.
 */
public class ExpenseDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(ExpenseDialog.class);
    
    private final transient ExpenseController expenseController;
    private final transient CategoryController categoryController;
    private final transient Expense expense; // null for new expense, non-null for edit
    private boolean saved = false;

    // UI Components (package-private for testing)
    JTextField dateField;
    JTextField amountField;
    JTextField descriptionField;
    JComboBox<Category> categoryComboBox;
    
    /**
     * Creates a new ExpenseDialog.
     * 
     * @param parent Parent frame
     * @param expenseController Expense controller for business logic
     * @param categoryController Category controller for loading categories
     * @param expense Expense to edit (null for new expense)
     */
    public ExpenseDialog(JFrame parent, ExpenseController expenseController, 
                        CategoryController categoryController, Expense expense) {
        super(parent, expense == null ? "Add Expense" : "Edit Expense", true); // Always modal
        this.expenseController = expenseController;
        this.categoryController = categoryController;
        this.expense = expense;
        initializeUI();
        loadCategories();
        if (expense != null) {
            loadExpenseData();
        }
    }
    

    private void initializeUI() {
        setSize(450, 300);
        setLocationRelativeTo(getParent());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Date field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        dateField = new JTextField(20);
        if (expense == null) {
            dateField.setText(LocalDate.now().toString());
        }
        panel.add(dateField, gbc);

        // Amount field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        amountField = new JTextField(20);
        panel.add(amountField, gbc);

        // Description field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        descriptionField = new JTextField(20);
        panel.add(descriptionField, gbc);

        // Category combo box
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        categoryComboBox = new JComboBox<>();
        panel.add(categoryComboBox, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> onSaveButtonClick());
        buttonPanel.add(saveButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        add(panel);
        pack();
    }

    /**
     * Loads categories into the combo box.
     * Uses controller for async operation.
     */
    private void loadCategories() {
        categoryController.loadCategories(
            categories -> {
                // Success: populate combo box
                categoryComboBox.removeAllItems();
                for (Category category : categories) {
                    categoryComboBox.addItem(category);
                }
            },
            error -> {
                // Error: log but don't block UI
                // User will see empty combo box and can retry
                LOGGER.warn("Error loading categories");
            }
        );
    }

    /**
     * Loads expense data into the form fields.
     */
    void loadExpenseData() {
        if (expense == null) {
            return;
        }
        
        dateField.setText(expense.getDate().toString());
        amountField.setText(expense.getAmount().toString());
        descriptionField.setText(expense.getDescription());
        
        try {
            Category category = categoryController.getCategory(expense.getCategoryId());
            if (category != null) {
                categoryComboBox.setSelectedItem(category);
            }
        } catch (SQLException e) {
            // Ignore - category will remain unselected
        }
    }

    /**
     * Handles save button click.
     * Delegates to controller for business logic.
     */
    private void onSaveButtonClick() {
        try {
            LocalDate date = LocalDate.parse(dateField.getText().trim());
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            String description = descriptionField.getText().trim();
            Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
            
            if (selectedCategory == null) {
                JOptionPane.showMessageDialog(this,
                    "Please select a category.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (expense == null) {
                // Create new expense
                expenseController.createExpense(date, amount, description, selectedCategory.getCategoryId(),
                    createdExpense -> {
                        saved = true;
                        dispose();
                    },
                    error -> 
                        JOptionPane.showMessageDialog(this,
                            error,
                            "Error",
                            JOptionPane.ERROR_MESSAGE)
                );
            } else {
                // Update existing expense
                expenseController.updateExpense(expense.getExpenseId(), date, amount, description, 
                    selectedCategory.getCategoryId(),
                    updatedExpense -> {
                        saved = true;
                        dispose();
                    },
                    error -> 
                        JOptionPane.showMessageDialog(this,
                            error,
                            "Error",
                            JOptionPane.ERROR_MESSAGE)
                );
            }
        } catch (Exception e) {
            // Validation error (parse exception, etc.)
            JOptionPane.showMessageDialog(this,
                "Invalid input: " + e.getMessage(),
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Returns whether the expense was saved.
     * 
     * @return true if expense was saved, false otherwise
     */
    public boolean isSaved() {
        return saved;
    }
}
