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

public class ExpenseDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(ExpenseDialog.class);
    
    private final transient ExpenseController expenseController;
    private final transient CategoryController categoryController;
    private final transient Expense expense; 
    private boolean saved = false;

    JTextField dateField;
    JTextField amountField;
    JTextField descriptionField;
    JComboBox<Category> categoryComboBox;
    
    public ExpenseDialog(JFrame parent, ExpenseController expenseController, 
                        CategoryController categoryController, Expense expense) {
        super(parent, expense == null ? "Add Expense" : "Edit Expense", true);
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

    private void loadCategories() {
        categoryController.loadCategories(
            categories -> {
                categoryComboBox.removeAllItems();
                for (Category category : categories) {
                    categoryComboBox.addItem(category);
                }
            },
            error -> LOGGER.warn("Error loading categories")
        );
    }

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
        }
    }

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
                expenseController.createExpense(date, amount, description, selectedCategory.getCategoryId(),
                    createdExpense -> {
                        saved = true;
                        javax.swing.SwingUtilities.invokeLater(this::dispose);
                    },
                    error -> 
                        JOptionPane.showMessageDialog(this,
                            error,
                            "Error",
                            JOptionPane.ERROR_MESSAGE)
                );
            } else {
                expenseController.updateExpense(expense.getExpenseId(), date, amount, description, 
                    selectedCategory.getCategoryId(),
                    updatedExpense -> {
                        saved = true;
                        javax.swing.SwingUtilities.invokeLater(this::dispose);
                    },
                    error -> 
                        JOptionPane.showMessageDialog(this,
                            error,
                            "Error",
                            JOptionPane.ERROR_MESSAGE)
                );
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Invalid input: " + e.getMessage(),
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
