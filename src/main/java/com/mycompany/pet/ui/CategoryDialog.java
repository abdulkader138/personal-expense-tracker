package com.mycompany.pet.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
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

import com.mycompany.pet.model.Category;
import com.mycompany.pet.service.CategoryService;

/**
 * Dialog for managing categories.
 */
public class CategoryDialog extends JDialog {
    private CategoryService categoryService;
    private JTable categoryTable;
    private DefaultTableModel categoryTableModel;

    public CategoryDialog(JFrame parent, CategoryService categoryService) {
        super(parent, "Manage Categories", true);
        this.categoryService = categoryService;
        initializeUI();
        loadCategories();
    }

    private void initializeUI() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel for add category
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField nameField = new JTextField(15);
        JButton addButton = new JButton("Add Category");
        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Category name cannot be empty.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                categoryService.createCategory(name);
                nameField.setText("");
                loadCategories();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error adding category: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        topPanel.add(new JLabel("Name:"));
        topPanel.add(nameField);
        topPanel.add(addButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel for category table
        String[] columnNames = {"ID", "Name"};
        categoryTableModel = new DefaultTableModel(columnNames, 0) {
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
        JButton editButton = new JButton("Update Selected");
        editButton.addActionListener(e -> updateSelectedCategory());
        bottomPanel.add(editButton);

        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteSelectedCategory());
        bottomPanel.add(deleteButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadCategories() {
        try {
            categoryTableModel.setRowCount(0);
            List<Category> categories = categoryService.getAllCategories();
            for (Category category : categories) {
                categoryTableModel.addRow(new Object[]{
                    category.getCategoryId(),
                    category.getName()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading categories: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedCategory() {
        // Stop any cell editing that might be in progress
        if (categoryTable.isEditing()) {
            categoryTable.getCellEditor().stopCellEditing();
        }
        
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a category to update.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer categoryId = (Integer) categoryTableModel.getValueAt(selectedRow, 0);
        String name = (String) categoryTableModel.getValueAt(selectedRow, 1);
        
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Category name cannot be empty.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            loadCategories();
            return;
        }

        try {
            categoryService.updateCategory(categoryId, name.trim());
            loadCategories();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error updating category: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            loadCategories();
        }
    }

    private void deleteSelectedCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a category to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this category? All associated expenses will also be deleted.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Integer categoryId = (Integer) categoryTableModel.getValueAt(selectedRow, 0);
            try {
                categoryService.deleteCategory(categoryId);
                loadCategories();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error deleting category: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

