package com.mycompany.pet.ui;

import com.mycompany.pet.controller.CategoryController;
import com.mycompany.pet.controller.ExpenseController;
import com.mycompany.pet.model.Category;
import com.mycompany.pet.model.Expense;
import com.mycompany.pet.service.CategoryService;
import com.mycompany.pet.service.ExpenseService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Main window for the Expense Tracker application.
 * 
 * This window uses ExpenseController and CategoryController to separate UI concerns from business logic.
 * All database operations are handled asynchronously by the controllers.
 */
public class MainWindow extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String ERROR_TITLE = "Error";
    
    // Controllers (preferred) - package-private for testing
    final ExpenseController expenseController;
    final CategoryController categoryController;
    
    // Services (for backward compatibility and deprecated methods)
    private transient CategoryService categoryService;
    private transient ExpenseService expenseService;

    // UI Components (package-private for testing)
    JTable expenseTable;
    DefaultTableModel expenseTableModel;
    JComboBox<Category> categoryComboBox;
    JComboBox<String> monthComboBox;
    JComboBox<String> yearComboBox;
    JLabel monthlyTotalLabel; // Package-private for testing
    JLabel categoryTotalLabel; // Package-private for testing
    boolean isInitializing = true; // Flag to prevent action listeners during initialization

    /**
     * Creates a new MainWindow with controllers.
     * 
     * @param expenseController Expense controller
     * @param categoryController Category controller
     */
    public MainWindow(ExpenseController expenseController, CategoryController categoryController) {
        this.expenseController = expenseController;
        this.categoryController = categoryController;
        initializeUI();
    }
    
    /**
     * Creates a MainWindow with services (for backward compatibility).
     * This constructor creates controllers internally.
     * 
     * @param categoryService Category service
     * @param expenseService Expense service
     * @deprecated Use MainWindow(ExpenseController, CategoryController) instead
     */
    @Deprecated
    public MainWindow(CategoryService categoryService, ExpenseService expenseService) {
        this.categoryService = categoryService;
        this.expenseService = expenseService;
        // Create controllers from services
        this.categoryController = new CategoryController(categoryService);
        this.expenseController = new ExpenseController(expenseService);
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Personal Expense Tracker");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100, 100, 1000, 700);

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
        monthComboBox.setSelectedItem("All");
        monthComboBox.addActionListener(e -> {
            if (!isInitializing && expenseController != null && expenseTableModel != null) {
                filterExpenses();
            }
        });
        topPanel.add(monthComboBox);

        topPanel.add(new JLabel("Year:"));
        String[] yearOptions = getYearOptions();
        yearComboBox = new JComboBox<>(yearOptions);
        yearComboBox.setSelectedItem(yearOptions[2]); // Current year
        yearComboBox.addActionListener(e -> {
            if (!isInitializing && expenseController != null && expenseTableModel != null) {
                filterExpenses();
            }
        });
        topPanel.add(yearComboBox);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel for expense table
        String[] columnNames = {"ID", "Date", "Amount", "Description", "Category"};
        expenseTableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            
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
        categoryComboBox.addActionListener(e -> {
            if (!isInitializing && expenseController != null) {
                updateCategoryTotal();
            }
        });
        bottomPanel.add(categoryComboBox);
        categoryTotalLabel = new JLabel("Category Total: $0.00");
        bottomPanel.add(categoryTotalLabel);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        isInitializing = false;
    }

    private String[] getYearOptions() {
        int currentYear = LocalDate.now().getYear();
        String[] years = new String[5];
        for (int i = 0; i < 5; i++) {
            years[i] = String.valueOf(currentYear - 2 + i);
        }
        return years;
    }

    /**
     * Loads all data (categories, expenses, and summary).
     * Uses controllers for async operations.
     */
    public void loadData() {
        loadCategories();
        loadExpenses();
        updateSummary();
    }

    /**
     * Loads categories into the combo box.
     * Uses controller for async operation.
     */
    void loadCategories() {
        categoryController.loadCategories(
            categories -> {
                // Success: populate combo box
                categoryComboBox.removeAllItems();
                categoryComboBox.addItem(null);
                for (Category category : categories) {
                    categoryComboBox.addItem(category);
                }
            },
            error -> {
                // Error: show message only if window is visible
                if (isVisible() && isShowing()) {
                    JOptionPane.showMessageDialog(this,
                        error,
                        ERROR_TITLE,
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        );
    }

    /**
     * Loads expenses into the table.
     * Uses controller for async operation.
     */
    void loadExpenses() {
        expenseController.loadExpenses(
            expenses -> {
                // Success: populate table
                expenseTableModel.setRowCount(0);
                for (Expense expense : expenses) {
                    Category category = null;
                    try {
                        category = categoryController.getCategory(expense.getCategoryId());
                    } catch (SQLException e) {
                        // Ignore - will show "Unknown"
                    }
                    String categoryName = category != null ? category.getName() : "Unknown";
                    expenseTableModel.addRow(new Object[]{
                        expense.getExpenseId(),
                        expense.getDate().toString(),
                        expense.getAmount().toString(),
                        expense.getDescription(),
                        categoryName
                    });
                }
            },
            error -> {
                // Error: show message only if window is visible
                if (isVisible() && isShowing()) {
                    JOptionPane.showMessageDialog(this,
                        error,
                        ERROR_TITLE,
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        );
    }

    /**
     * Filters expenses by month and year.
     * Uses controller for async operation.
     */
    public void filterExpenses() {
        String selectedMonth = (String) monthComboBox.getSelectedItem();
        String selectedYear = (String) yearComboBox.getSelectedItem();

        if (selectedMonth == null || selectedYear == null) {
            return;
        }

        if ("All".equals(selectedMonth)) {
            loadExpenses();
        } else {
            try {
                int year = Integer.parseInt(selectedYear);
                int month = Integer.parseInt(selectedMonth);
                expenseController.loadExpensesByMonth(year, month,
                    expenses -> {
                        // Success: populate table
                        expenseTableModel.setRowCount(0);
                        for (Expense expense : expenses) {
                            Category category = null;
                            try {
                                category = categoryController.getCategory(expense.getCategoryId());
                            } catch (SQLException e) {
                                // Ignore - will show "Unknown"
                            }
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
                    },
                    error -> {
                        // Error: show message
                        if (isVisible() && isShowing()) {
                            JOptionPane.showMessageDialog(this,
                                error,
                                ERROR_TITLE,
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                );
            } catch (NumberFormatException e) {
                // Invalid month/year - ignore
            }
        }
    }

    /**
     * Updates the monthly total summary.
     * Uses controller for async operation.
     */
    public void updateSummary() {
        String selectedMonth = (String) monthComboBox.getSelectedItem();
        String selectedYear = (String) yearComboBox.getSelectedItem();

        if (selectedMonth == null || selectedYear == null) {
            monthlyTotalLabel.setText("Monthly Total: N/A");
            return;
        }

        if ("All".equals(selectedMonth)) {
            monthlyTotalLabel.setText("Monthly Total: N/A");
        } else {
            try {
                int year = Integer.parseInt(selectedYear);
                int month = Integer.parseInt(selectedMonth);
                expenseController.getMonthlyTotal(year, month,
                    total -> {
                        monthlyTotalLabel.setText("Monthly Total: $" + total.toString());
                        updateCategoryTotal();
                    },
                    error -> monthlyTotalLabel.setText("Monthly Total: Error")
                );
            } catch (NumberFormatException e) {
                monthlyTotalLabel.setText("Monthly Total: Error");
            }
        }
    }

    /**
     * Updates the category total summary.
     * Uses controller for async operation.
     */
    public void updateCategoryTotal() {
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        if (selectedCategory == null) {
            categoryTotalLabel.setText("Category Total: N/A");
        } else {
            expenseController.getTotalByCategory(selectedCategory.getCategoryId(),
                total -> categoryTotalLabel.setText("Category Total: $" + total.toString()),
                error -> categoryTotalLabel.setText("Category Total: Error")
            );
        }
    }

    /**
     * Shows the add expense dialog.
     */
    public void showAddExpenseDialog() {
        ExpenseDialog dialog = new ExpenseDialog(this, expenseController, categoryController, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    /**
     * Shows the edit expense dialog.
     */
    public void showEditExpenseDialog() {
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
            Expense expense = expenseController.getExpense(expenseId);
            ExpenseDialog dialog = new ExpenseDialog(this, expenseController, categoryController, expense);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadData();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading expense: " + e.getMessage(),
                ERROR_TITLE,
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Deletes the selected expense.
     * Uses controller for async operation.
     */
    public void deleteSelectedExpense() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an expense to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // In test mode, bypass confirmation dialog
        boolean isTestMode = "true".equals(System.getProperty("test.mode"));
        int confirm = isTestMode ? JOptionPane.YES_OPTION : JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this expense?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Integer expenseId = (Integer) expenseTableModel.getValueAt(selectedRow, 0);
            expenseController.deleteExpense(expenseId,
                this::loadData,
                error -> JOptionPane.showMessageDialog(this,
                    error,
                    ERROR_TITLE,
                    JOptionPane.ERROR_MESSAGE)
            );
        }
    }

    /**
     * Shows the category management dialog.
     */
    public void showCategoryDialog() {
        CategoryDialog dialog = new CategoryDialog(this, categoryController);
        dialog.setVisible(true);
        loadData();
    }

    /**
     * Gets the expense service (for backward compatibility).
     * 
     * @return Expense service or null if using controller-only constructor
     * @deprecated Use ExpenseController instead
     */
    @Deprecated
    public ExpenseService getExpenseService() {
        return expenseService;
    }
}
