package com.mycompany.pet.ui;

import com.mycompany.pet.model.Category;
import com.mycompany.pet.model.Expense;
import com.mycompany.pet.service.CategoryService;
import com.mycompany.pet.service.ExpenseService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Main window for the Expense Tracker application.
 */
public class MainWindow extends JFrame {
    private CategoryService categoryService;
    private ExpenseService expenseService;

    private JTable expenseTable;
    private DefaultTableModel expenseTableModel;
    private JComboBox<Category> categoryComboBox;
    private JComboBox<String> monthComboBox;
    private JComboBox<String> yearComboBox;
    private JLabel monthlyTotalLabel;
    private JLabel categoryTotalLabel;

    public MainWindow(CategoryService categoryService, ExpenseService expenseService) {
        this.categoryService = categoryService;
        this.expenseService = expenseService;
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        setTitle("Personal Expense Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu manageMenu = new JMenu("Manage");
        JMenuItem categoriesItem = new JMenuItem("Categories");
        categoriesItem.addActionListener(e -> showCategoryDialog());
        manageMenu.add(categoriesItem);
        menuBar.add(manageMenu);

        setJMenuBar(menuBar);

        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel for controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = new JButton("Add Expense");
        addButton.addActionListener(e -> showAddExpenseDialog());
        topPanel.add(addButton);

        JButton editButton = new JButton("Edit Expense");
        editButton.addActionListener(e -> showEditExpenseDialog());
        topPanel.add(editButton);

        JButton deleteButton = new JButton("Delete Expense");
        deleteButton.addActionListener(e -> deleteSelectedExpense());
        topPanel.add(deleteButton);

        topPanel.add(new JLabel("Month:"));
        monthComboBox = new JComboBox<>(new String[]{"All", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"});
        monthComboBox.addActionListener(e -> filterExpenses());
        topPanel.add(monthComboBox);

        topPanel.add(new JLabel("Year:"));
        yearComboBox = new JComboBox<>(getYearOptions());
        yearComboBox.addActionListener(e -> filterExpenses());
        topPanel.add(yearComboBox);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel for expense table
        String[] columnNames = {"ID", "Date", "Amount", "Description", "Category"};
        expenseTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        expenseTable = new JTable(expenseTableModel);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel for summary
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        monthlyTotalLabel = new JLabel("Monthly Total: $0.00");
        bottomPanel.add(monthlyTotalLabel);
        bottomPanel.add(new JLabel("  |  "));
        bottomPanel.add(new JLabel("Category:"));
        categoryComboBox = new JComboBox<>();
        categoryComboBox.addItem(null); // "All categories" option
        categoryComboBox.addActionListener(e -> updateCategoryTotal());
        bottomPanel.add(categoryComboBox);
        categoryTotalLabel = new JLabel("Category Total: $0.00");
        bottomPanel.add(categoryTotalLabel);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private String[] getYearOptions() {
        int currentYear = LocalDate.now().getYear();
        String[] years = new String[5];
        for (int i = 0; i < 5; i++) {
            years[i] = String.valueOf(currentYear - 2 + i);
        }
        return years;
    }

    private void loadData() {
        try {
            loadCategories();
            loadExpenses();
            updateSummary();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading data: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCategories() throws SQLException {
        List<Category> categories = categoryService.getAllCategories();
        categoryComboBox.removeAllItems();
        categoryComboBox.addItem(null);
        for (Category category : categories) {
            categoryComboBox.addItem(category);
        }
    }

    private void loadExpenses() throws SQLException {
        expenseTableModel.setRowCount(0);
        List<Expense> expenses = expenseService.getAllExpenses();
        for (Expense expense : expenses) {
            Category category = categoryService.getCategory(expense.getCategoryId());
            String categoryName = category != null ? category.getName() : "Unknown";
            expenseTableModel.addRow(new Object[]{
                expense.getExpenseId(),
                expense.getDate().toString(),
                expense.getAmount().toString(),
                expense.getDescription(),
                categoryName
            });
        }
    }

    private void filterExpenses() {
        try {
            expenseTableModel.setRowCount(0);
            String selectedMonth = (String) monthComboBox.getSelectedItem();
            String selectedYear = (String) yearComboBox.getSelectedItem();

            List<Expense> expenses;
            if ("All".equals(selectedMonth)) {
                expenses = expenseService.getAllExpenses();
            } else {
                int year = Integer.parseInt(selectedYear);
                int month = Integer.parseInt(selectedMonth);
                expenses = expenseService.getExpensesByMonth(year, month);
            }

            for (Expense expense : expenses) {
                Category category = categoryService.getCategory(expense.getCategoryId());
                String categoryName = category != null ? category.getName() : "Unknown";
                expenseTableModel.addRow(new Object[]{
                    expense.getExpenseId(),
                    expense.getDate().toString(),
                    expense.getAmount().toString(),
                    expense.getDescription(),
                    categoryName
                });
            }
            updateSummary();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error filtering expenses: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSummary() {
        try {
            String selectedMonth = (String) monthComboBox.getSelectedItem();
            String selectedYear = (String) yearComboBox.getSelectedItem();

            if ("All".equals(selectedMonth)) {
                monthlyTotalLabel.setText("Monthly Total: N/A");
            } else {
                int year = Integer.parseInt(selectedYear);
                int month = Integer.parseInt(selectedMonth);
                BigDecimal total = expenseService.getMonthlyTotal(year, month);
                monthlyTotalLabel.setText("Monthly Total: $" + total.toString());
            }
            updateCategoryTotal();
        } catch (SQLException e) {
            monthlyTotalLabel.setText("Monthly Total: Error");
        }
    }

    private void updateCategoryTotal() {
        try {
            Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
            if (selectedCategory == null) {
                categoryTotalLabel.setText("Category Total: N/A");
            } else {
                BigDecimal total = expenseService.getTotalByCategory(selectedCategory.getCategoryId());
                categoryTotalLabel.setText("Category Total: $" + total.toString());
            }
        } catch (SQLException e) {
            categoryTotalLabel.setText("Category Total: Error");
        }
    }

    private void showAddExpenseDialog() {
        ExpenseDialog dialog = new ExpenseDialog(this, categoryService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void showEditExpenseDialog() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an expense to edit.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer expenseId = (Integer) expenseTableModel.getValueAt(selectedRow, 0);
        try {
            Expense expense = expenseService.getExpense(expenseId);
            ExpenseDialog dialog = new ExpenseDialog(this, categoryService, expense);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadData();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading expense: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedExpense() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an expense to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this expense?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Integer expenseId = (Integer) expenseTableModel.getValueAt(selectedRow, 0);
            try {
                expenseService.deleteExpense(expenseId);
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error deleting expense: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCategoryDialog() {
        CategoryDialog dialog = new CategoryDialog(this, categoryService);
        dialog.setVisible(true);
        loadData();
    }

    public ExpenseService getExpenseService() {
        return expenseService;
    }
}

