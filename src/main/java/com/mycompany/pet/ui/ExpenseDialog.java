package com.mycompany.pet.ui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mycompany.pet.model.Category;
import com.mycompany.pet.model.Expense;
import com.mycompany.pet.service.CategoryService;
import com.mycompany.pet.service.ExpenseService;

/**
 * Dialog for adding/editing expenses.
 */
public class ExpenseDialog extends JDialog {
    private CategoryService categoryService;
    private ExpenseService expenseService;
    private Expense expense;
    private boolean saved = false;

    private JTextField dateField;
    private JTextField amountField;
    private JTextField descriptionField;
    private JComboBox<Category> categoryComboBox;

    public ExpenseDialog(JFrame parent, CategoryService categoryService, Expense expense) {
        super(parent, expense == null ? "Add Expense" : "Edit Expense", true);
        this.categoryService = categoryService;
        this.expense = expense;
        
        // Get expense service from parent
        if (parent instanceof MainWindow) {
            this.expenseService = ((MainWindow) parent).getExpenseService();
        } else {
            throw new IllegalArgumentException("Parent must be MainWindow");
        }
        
        initializeUI();
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
        try {
            List<Category> categories = categoryService.getAllCategories();
            for (Category category : categories) {
                categoryComboBox.addItem(category);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading categories: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
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
        saveButton.addActionListener(e -> saveExpense());
        buttonPanel.add(saveButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        add(panel);
        pack();
    }

    private void loadExpenseData() {
        dateField.setText(expense.getDate().toString());
        amountField.setText(expense.getAmount().toString());
        descriptionField.setText(expense.getDescription());
        
        try {
            Category category = categoryService.getCategory(expense.getCategoryId());
            if (category != null) {
                categoryComboBox.setSelectedItem(category);
            }
        } catch (SQLException e) {
            // Ignore
        }
    }

    private void saveExpense() {
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
                expenseService.createExpense(date, amount, description, selectedCategory.getCategoryId());
            } else {
                expenseService.updateExpense(expense.getExpenseId(), date, amount, description, selectedCategory.getCategoryId());
            }
            saved = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error saving expense: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}

