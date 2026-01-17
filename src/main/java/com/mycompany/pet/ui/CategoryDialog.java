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

public class CategoryDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final String TEST_MODE_PROPERTY = "test.mode";
    
    private final transient CategoryController controller;
    volatile String lastErrorMessage = null;
    
    String getLastErrorMessage() {
        return lastErrorMessage;
    }
    
    JTable categoryTable;
    DefaultTableModel categoryTableModel;
    JTextField nameField;
    JButton addButton;
    JButton updateButton;
    JButton deleteButton;
    JLabel labelMessage; 
    
    public CategoryDialog(JFrame parent, CategoryController controller) {
        super(parent, "Manage Categories", true); 
        this.controller = controller;
        initializeUI();
        boolean isTestMode = "true".equals(System.getProperty(TEST_MODE_PROPERTY));
        if (!isTestMode) {
            loadCategories();
        }
    }
    

    private void initializeUI() {
        setSize(500, 420);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nameField = new JTextField(15);
        addButton = new JButton("Add Category");
        addButton.addActionListener(e -> onAddButtonClick());
        topPanel.add(new JLabel("Name:"));
        topPanel.add(nameField);
        topPanel.add(addButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Name"};
        categoryTableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };
        categoryTable = new JTable(categoryTableModel);
        categoryTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        categoryTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        updateButton = new JButton("Update Selected");
        updateButton.addActionListener(e -> onUpdateButtonClick());
        buttonPanel.add(updateButton);

        deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> onDeleteButtonClick());
        buttonPanel.add(deleteButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> {
            setVisible(false);
            dispose();
        });
        buttonPanel.add(closeButton);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        labelMessage = new JLabel("");
        labelMessage.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bottomPanel.add(labelMessage, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    void setErrorMessage(String msg) {
        lastErrorMessage = msg;
        showMessage(msg);
    }

    void onAddButtonClick() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            setErrorMessage("Category name cannot be empty.");
            return;
        }
        
        controller.createCategory(name,
            category -> {
                nameField.setText("");
                showMessage("");
                loadCategories();
            },
            error -> {
                lastErrorMessage = error;
                showMessage(error);
            }
        );
    }

   
    void onUpdateButtonClick() {
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
                showMessage("");
                loadCategories();
            },
            error -> {
                lastErrorMessage = error;
                showMessage(error);
            }
        );
    }

    void onDeleteButtonClick() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            setErrorMessage("Please select a category to delete.");
            return;
        }

        boolean isTestMode = "true".equals(System.getProperty(TEST_MODE_PROPERTY));
        int confirm = JOptionPane.YES_OPTION; 
        
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
                    showMessage("");
                    loadCategories();
                },
                error -> {
                    lastErrorMessage = error; 
                    showMessage(error);
                }
            );
        }
    }

    boolean isErrorMessage(String msg) {
        return msg.contains("Error") || msg.contains("select") ||
               msg.contains("cannot be empty") || msg.contains("Please") 
               || msg.contains("Category name");
    }

    private void updateLabelText(String text) {
        if (labelMessage != null) {
            labelMessage.setText(text);
            labelMessage.setVisible(!text.isEmpty());
        }
    }

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

    void showMessage(String msg) {
        if (labelMessage == null) {
            return;
        }
        
        final boolean isTestMode = "true".equals(System.getProperty(TEST_MODE_PROPERTY));
        
        if (msg != null && !msg.isEmpty()) {
            if (isErrorMessage(msg)) {
                lastErrorMessage = msg;
            }
            
            setLabelTextOnEDT(msg);
            
            if (!isTestMode) {
                javax.swing.SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.WARNING_MESSAGE)
                );
            }
        } else {
            setLabelTextOnEDT("");
            if (!isTestMode) {
                lastErrorMessage = null;
            }
        }
    }
    

    void loadCategories() {
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
                if (isTestMode) {
                    return;
                }
                
                if (labelMessage != null) {
                    String currentMsg = labelMessage.getText();
                    if (currentMsg != null && !currentMsg.isEmpty()) {
                        if (currentMsg.contains("Error") || currentMsg.contains("select") ||
                            currentMsg.contains("cannot be empty") || currentMsg.contains("Please")) {
                            return;
                        }
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

