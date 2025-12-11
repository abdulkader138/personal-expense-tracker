package com.mycompany.pet.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.mycompany.pet.controller.CategoryController;
import com.mycompany.pet.model.Category;

/**
 * Dialog for managing categories.
 * 
 * This dialog uses CategoryController to separate UI concerns from business logic.
 * All database operations are handled asynchronously by the controller.
 */
public class CategoryDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private final CategoryController controller;
    
    // UI Components (package-private for testing)
    JTable categoryTable;
    DefaultTableModel categoryTableModel;
    JTextField nameField;
    JButton addButton;
    JButton updateButton;
    JButton deleteButton;
    JLabel labelMessage; // For displaying messages to user
    
    /**
     * Creates a new CategoryDialog.
     * 
     * @param parent Parent frame
     * @param controller Category controller for business logic
     */
    public CategoryDialog(JFrame parent, CategoryController controller) {
        super(parent, "Manage Categories", true); // Always modal
        this.controller = controller;
        initializeUI();
        loadCategories();
    }
    
    /**
     * Creates a CategoryDialog with CategoryService (for backward compatibility).
     * This constructor creates a controller internally.
     * 
     * @param parent Parent frame
     * @param categoryService Category service (will be wrapped in controller)
     * @deprecated Use CategoryDialog(JFrame, CategoryController) instead
     */
    @Deprecated
    public CategoryDialog(JFrame parent, com.mycompany.pet.service.CategoryService categoryService) {
        this(parent, new CategoryController(categoryService));
    }

    private void initializeUI() {
        setSize(500, 420);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel for add category
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nameField = new JTextField(15);
        addButton = new JButton("Add Category");
        addButton.addActionListener(e -> onAddButtonClick());
        topPanel.add(new JLabel("Name:"));
        topPanel.add(nameField);
        topPanel.add(addButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel for category table
        String[] columnNames = {"ID", "Name"};
        categoryTableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only name is editable
            }
        };
        categoryTable = new JTable(categoryTableModel);
        categoryTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        categoryTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel for buttons
        JPanel bottomPanel = new JPanel(new FlowLayout());
        updateButton = new JButton("Update Selected");
        updateButton.addActionListener(e -> onUpdateButtonClick());
        bottomPanel.add(updateButton);

        deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> onDeleteButtonClick());
        bottomPanel.add(deleteButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> {
            setVisible(false);
            dispose();
        });
        bottomPanel.add(closeButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Message area for user feedback
        labelMessage = new JLabel("");
        labelMessage.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(labelMessage, BorderLayout.PAGE_END);

        add(mainPanel);
    }

    /**
     * Handles add button click.
     * Delegates to controller for business logic.
     */
    private void onAddButtonClick() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showMessage("Category name cannot be empty.");
            return;
        }
        
        controller.createCategory(name,
            category -> {
                // Success: clear field and reload
                nameField.setText("");
                showMessage("");
                loadCategories();
            },
            error -> {
                // Error: show message
                showMessage(error);
            }
        );
    }

    /**
     * Handles update button click.
     * Delegates to controller for business logic.
     */
    private void onUpdateButtonClick() {
        // Stop any cell editing that might be in progress
        if (categoryTable.isEditing()) {
            categoryTable.getCellEditor().stopCellEditing();
        }
        
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            showMessage("Please select a category to update.");
            return;
        }

        Integer categoryId = (Integer) categoryTableModel.getValueAt(selectedRow, 0);
        String name = (String) categoryTableModel.getValueAt(selectedRow, 1);
        
        if (name == null || name.trim().isEmpty()) {
            showMessage("Category name cannot be empty.");
            return;
        }

        controller.updateCategory(categoryId, name.trim(),
            category -> {
                // Success: reload categories
                showMessage("");
                loadCategories();
            },
            error -> {
                // Error: show message (don't reload to preserve error)
                showMessage(error);
            }
        );
    }

    /**
     * Handles delete button click.
     * Delegates to controller for business logic.
     */
    private void onDeleteButtonClick() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            showMessage("Please select a category to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this category?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Integer categoryId = (Integer) categoryTableModel.getValueAt(selectedRow, 0);
            controller.deleteCategory(categoryId,
                () -> {
                    // Success: reload categories
                    showMessage("");
                    loadCategories();
                },
                error -> {
                    // Error: show message (don't reload to preserve error)
                    showMessage(error);
                }
            );
        }
    }

    /**
     * Shows a message to the user.
     * In production, uses JOptionPane. For testing, uses labelMessage.
     * 
     * @param msg Message to show (empty string clears message)
     */
    void showMessage(String msg) {
        if (msg == null || msg.isEmpty()) {
            if (labelMessage != null) {
                labelMessage.setText("");
            }
            return;
        }
        
        // Check if we're in a test environment by checking if labelMessage is being monitored
        // This is a cleaner approach than stack trace inspection
        boolean useLabel = isLabelMessageMode();
        
        // Always update label if in test mode, regardless of whether we show dialog
        if (useLabel && labelMessage != null) {
            // Test mode: use label for assertions
            labelMessage.setText(msg);
            labelMessage.setVisible(true);
        }
        
        // In production mode, show dialog
        if (!useLabel) {
            JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.WARNING_MESSAGE);
            // Also update label if it exists (for consistency)
            if (labelMessage != null) {
                labelMessage.setText("");
            }
        }
    }
    
    /**
     * Determines if label message mode should be used.
     * This checks if the label is being actively monitored (test scenario).
     * 
     * @return true if label message mode should be used
     */
    private boolean isLabelMessageMode() {
        // Check system property for explicit test mode
        String testMode = System.getProperty("test.mode");
        if ("true".equals(testMode)) {
            return true;
        }
        // In production, always use dialogs
        return false;
    }

    /**
     * Loads categories from the database and populates the table.
     * Uses controller for async operation.
     */
    void loadCategories() {
        controller.loadCategories(
            categories -> {
                // Success: populate table
                categoryTableModel.setRowCount(0);
                for (Category category : categories) {
                    categoryTableModel.addRow(new Object[]{
                        category.getCategoryId(),
                        category.getName()
                    });
                }
                // Clear any non-error messages
                String currentMsg = labelMessage.getText();
                if (currentMsg != null && !currentMsg.isEmpty() && 
                    !currentMsg.contains("Error") && !currentMsg.contains("select") &&
                    !currentMsg.contains("cannot be empty") && !currentMsg.contains("Please")) {
                    labelMessage.setText("");
                }
            },
            error -> {
                // Error: show message
                showMessage(error);
            }
        );
    }
}
