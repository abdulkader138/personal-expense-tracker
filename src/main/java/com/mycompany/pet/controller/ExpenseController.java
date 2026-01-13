package com.mycompany.pet.controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import com.mycompany.pet.model.Expense;
import com.mycompany.pet.service.ExpenseService;

/**
 * Controller for Expense operations.
 * Separates UI concerns from business logic.
 * 
 * This controller handles:
 * - Expense CRUD operations
 * - Error handling and user feedback
 * - Thread-safe database operations
 */
public class ExpenseController {
    private final ExpenseService expenseService;
    
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }
    
    public void loadExpenses(Consumer<List<Expense>> onSuccess, Consumer<String> onError) {
        Thread thread = new Thread(() -> {
            try {
                List<Expense> expenses = expenseService.getAllExpenses();
                javax.swing.SwingUtilities.invokeLater(() -> onSuccess.accept(expenses));
            } catch (SQLException e) {
                String errorMsg = "Error loading expenses: " + e.getMessage();
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(errorMsg));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    
    public void loadExpensesByMonth(int year, int month, 
                                    Consumer<List<Expense>> onSuccess, Consumer<String> onError) {
        Thread thread = new Thread(() -> {
            try {
                List<Expense> expenses = expenseService.getExpensesByMonth(year, month);
                javax.swing.SwingUtilities.invokeLater(() -> onSuccess.accept(expenses));
            } catch (SQLException e) {
                String errorMsg = "Error loading expenses: " + e.getMessage();
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(errorMsg));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    
    public void createExpense(LocalDate date, BigDecimal amount, String description, Integer categoryId,
                              Consumer<Expense> onSuccess, Consumer<String> onError) {
        Thread thread = new Thread(() -> {
            try {
                Expense expense = expenseService.createExpense(date, amount, description, categoryId);
                javax.swing.SwingUtilities.invokeLater(() -> onSuccess.accept(expense));
            } catch (SQLException e) {
                String errorMsg = "Error saving expense: " + e.getMessage();
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(errorMsg));
            } catch (IllegalArgumentException e) {
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    
    public void updateExpense(Integer expenseId, LocalDate date, BigDecimal amount, 
                              String description, Integer categoryId,
                              Consumer<Expense> onSuccess, Consumer<String> onError) {
        Thread thread = new Thread(() -> {
            try {
                Expense expense = expenseService.updateExpense(expenseId, date, amount, description, categoryId);
                javax.swing.SwingUtilities.invokeLater(() -> onSuccess.accept(expense));
            } catch (SQLException e) {
                String errorMsg = "Error updating expense: " + e.getMessage();
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(errorMsg));
            } catch (IllegalArgumentException e) {
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    
    public void deleteExpense(Integer expenseId, Runnable onSuccess, Consumer<String> onError) {
        Thread thread = new Thread(() -> {
            try {
                expenseService.deleteExpense(expenseId);
                javax.swing.SwingUtilities.invokeLater(onSuccess);
            } catch (SQLException e) {
                String errorMsg = "Error deleting expense: " + e.getMessage();
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(errorMsg));
            } catch (IllegalArgumentException e) {
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    
    public Expense getExpense(Integer expenseId) throws SQLException {
        return expenseService.getExpense(expenseId);
    }
    
    public void getMonthlyTotal(int year, int month, 
                                Consumer<BigDecimal> onSuccess, Consumer<String> onError) {
        Thread thread = new Thread(() -> {
            try {
                BigDecimal total = expenseService.getMonthlyTotal(year, month);
                javax.swing.SwingUtilities.invokeLater(() -> onSuccess.accept(total));
            } catch (SQLException e) {
                String errorMsg = "Error calculating monthly total: " + e.getMessage();
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(errorMsg));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    
    public void getTotalByCategory(Integer categoryId, 
                                   Consumer<BigDecimal> onSuccess, Consumer<String> onError) {
        Thread thread = new Thread(() -> {
            try {
                BigDecimal total = expenseService.getTotalByCategory(categoryId);
                javax.swing.SwingUtilities.invokeLater(() -> onSuccess.accept(total));
            } catch (SQLException e) {
                String errorMsg = "Error calculating category total: " + e.getMessage();
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(errorMsg));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}

