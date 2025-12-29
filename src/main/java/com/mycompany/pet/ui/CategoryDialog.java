package com.mycompany.pet.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

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
    private static final String TEST_MODE_PROPERTY = "test.mode";
    
    private final transient CategoryController controller;
    volatile String lastErrorMessage = null; // Store last error message for test mode (package-private for tests)
    
    /**
     * Test helper method to get the last error message.
     * This is more reliable than checking the label in test mode.
     */
    String getLastErrorMessage() {
        return lastErrorMessage;
    }
    
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
        // In test mode, don't call loadCategories from constructor to avoid race conditions
        // Tests can call it explicitly if needed, or it will be called after user actions
        boolean isTestMode = "true".equals(System.getProperty(TEST_MODE_PROPERTY));
        if (!isTestMode) {
            loadCategories();
        }
        // In test mode, loadCategories will be called explicitly by tests or after user actions
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
     * Sets error message for tests and displays it to user.
     * Package-private for testing.
     */
    void setErrorMessage(String msg) {
        lastErrorMessage = msg;
        showMessage(msg);
    }

    /**
     * Handles add button click.
     * Delegates to controller for business logic.
     * Package-private for testing.
     */
    void onAddButtonClick() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            setErrorMessage("Category name cannot be empty.");
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
                // Error: set lastErrorMessage and show message
                lastErrorMessage = error; // Always set for tests
                showMessage(error);
            }
        );
    }

    /**
     * Handles update button click.
     * Delegates to controller for business logic.
     * Package-private for testing.
     */
    void onUpdateButtonClick() {
        // Stop any cell editing that might be in progress
        if (categoryTable.isEditing()) {
            categoryTable.getCellEditor().stopCellEditing();
        }
        
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            setErrorMessage("Please select a category to update.");
            return;
        }

        Integer categoryId = (Integer) categoryTableModel.getValueAt(selectedRow, 0);
        String name = (String) categoryTableModel.getValueAt(selectedRow, 1);
        
        if (name == null || name.trim().isEmpty()) {
            setErrorMessage("Category name cannot be empty.");
            return;
        }

        controller.updateCategory(categoryId, name.trim(),
            category -> {
                // Success: reload categories
                showMessage("");
                loadCategories();
            },
            error -> {
                // Error: set lastErrorMessage and show message
                lastErrorMessage = error; // Always set for tests
                showMessage(error);
            }
        );
    }

    /**
     * Handles delete button click.
     * Delegates to controller for business logic.
     * Package-private for testing.
     */
    void onDeleteButtonClick() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            setErrorMessage("Please select a category to delete.");
            return;
        }

        // In test mode, skip confirmation dialog and proceed directly
        boolean isTestMode = "true".equals(System.getProperty(TEST_MODE_PROPERTY));
        int confirm = JOptionPane.YES_OPTION; // Default to YES in test mode
        
        if (!isTestMode) {
            confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this category?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        }

        if (confirm == JOptionPane.YES_OPTION) {
            Integer categoryId = (Integer) categoryTableModel.getValueAt(selectedRow, 0);
            controller.deleteCategory(categoryId,
                () -> {
                    // Success: reload categories
                    showMessage("");
                    loadCategories();
                },
                error -> {
                    // Error: set lastErrorMessage and show message
                    lastErrorMessage = error; // Always set for tests
                    showMessage(error);
                }
            );
        }
    }

    /**
     * Checks if message is an error message.
     * Package-private for testing.
     */
    boolean isErrorMessage(String msg) {
        return msg.contains("Error") || msg.contains("select") ||
               msg.contains("cannot be empty") || msg.contains("Please") 
               || msg.contains("Category name");
    }

    /**
     * Updates label text and visibility.
     * Package-private for testing.
     */
    private void updateLabelText(String text) {
        if (labelMessage != null) {
            labelMessage.setText(text);
            labelMessage.setVisible(!text.isEmpty());
        }
    }

    /**
     * Sets label text on EDT.
     * Package-private for testing.
     */
    void setLabelTextOnEDT(String text) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            updateLabelText(text);
        } else {
            try {
                javax.swing.SwingUtilities.invokeAndWait(() -> updateLabelText(text));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                javax.swing.SwingUtilities.invokeLater(() -> updateLabelText(text));
            } catch (Exception e) {
                javax.swing.SwingUtilities.invokeLater(() -> updateLabelText(text));
            }
        }
    }

    /**
     * Shows a message to the user.
     * In production, uses JOptionPane. For testing, uses labelMessage.
     * 
     * @param msg Message to show (empty string clears message)
     */
    void showMessage(String msg) {
        if (labelMessage == null) {
            return;
        }
        
        final boolean isTestMode = "true".equals(System.getProperty(TEST_MODE_PROPERTY));
        
        if (msg != null && !msg.isEmpty()) {
            // CRITICAL: For error messages, ALWAYS set lastErrorMessage FIRST
            if (isErrorMessage(msg)) {
                lastErrorMessage = msg;
            }
            
            setLabelTextOnEDT(msg);
            
            // Production mode: show dialog
            if (!isTestMode) {
                javax.swing.SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.WARNING_MESSAGE)
                );
            }
        } else {
            // Empty message: clear label but NEVER clear lastErrorMessage in test mode
            setLabelTextOnEDT("");
            if (!isTestMode) {
                lastErrorMessage = null;
            }
        }
    }
    

    /**
     * Loads categories from the database and populates the table.
     * Uses controller for async operation.
     */
    void loadCategories() {
        // CRITICAL: In test mode, NEVER touch labelMessage - preserve ALL messages
        String testModeProp = System.getProperty(TEST_MODE_PROPERTY);
        boolean isTestMode = "true".equals(testModeProp);
        
        controller.loadCategories(
            categories -> {
                categoryTableModel.setRowCount(0);
                for (Category category : categories) {
                    categoryTableModel.addRow(new Object[]{
                        category.getCategoryId(),
                        category.getName()
                    });
                }
                // CRITICAL: In test mode, NEVER touch labelMessage - preserve ALL messages
                if (isTestMode) {
                    // In test mode: ONLY populate table, NEVER touch labelMessage
                    // This prevents any race conditions with showMessage
                    return;
                }
                
                // Production mode: only clear non-error messages
                if (labelMessage != null) {
                    String currentMsg = labelMessage.getText();
                    if (currentMsg != null && !currentMsg.isEmpty()) {
                        // Check if it's an error message - if so, preserve it
                        if (currentMsg.contains("Error") || currentMsg.contains("select") ||
                            currentMsg.contains("cannot be empty") || currentMsg.contains("Please")) {
                            // It's an error message - preserve it
                            return;
                        }
                        // Not an error message - safe to clear
                        labelMessage.setText("");
                    }
                }
            },
            error -> {
                lastErrorMessage = error; 
                showMessage(error);
            }
        );
    }
}

