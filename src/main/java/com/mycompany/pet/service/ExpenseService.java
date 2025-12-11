package com.mycompany.pet.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.dao.ExpenseDAO;
import com.mycompany.pet.model.Expense;

/**
 * Service layer for expense management.
 */
public class ExpenseService {

    private final ExpenseDAO expenseDAO;
    private final CategoryDAO categoryDAO;

    // 🔒 Lock to guarantee thread-safety for create/update/delete
    private final Object expenseLock = new Object();

    public ExpenseService(ExpenseDAO expenseDAO, CategoryDAO categoryDAO) {
        this.expenseDAO = expenseDAO;
        this.categoryDAO = categoryDAO;
    }

    public Expense createExpense(LocalDate date, BigDecimal amount, String description, Integer categoryId) throws SQLException {
        validateExpense(date, amount, description, categoryId);
        synchronized (expenseLock) {     // 🔒 Make creation atomic
            Expense expense = new Expense(date, amount, description, categoryId);
            return expenseDAO.create(expense);
        }
    }

    public Expense getExpense(Integer expenseId) throws SQLException {
        if (expenseId == null) {
            throw new IllegalArgumentException("Expense ID cannot be null");
        }
        return expenseDAO.findById(expenseId);
    }

    public List<Expense> getAllExpenses() throws SQLException {
        return expenseDAO.findAll();
    }

    public List<Expense> getExpensesByMonth(int year, int month) throws SQLException {
        return expenseDAO.findByMonth(year, month);
    }

    public List<Expense> getExpensesByCategory(Integer categoryId) throws SQLException {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        return expenseDAO.findByCategory(categoryId);
    }

    public Expense updateExpense(Integer expenseId, LocalDate date, BigDecimal amount, String description, Integer categoryId) throws SQLException {
        if (expenseId == null) {
            throw new IllegalArgumentException("Expense ID cannot be null");
        }
        validateExpense(date, amount, description, categoryId);

        synchronized (expenseLock) {     // 🔒 Make update atomic
            Expense expense = expenseDAO.findById(expenseId);
            if (expense == null) {
                throw new IllegalArgumentException("Expense not found");
            }
            expense.setDate(date);
            expense.setAmount(amount);
            expense.setDescription(description);
            expense.setCategoryId(categoryId);
            return expenseDAO.update(expense);
        }
    }

    public boolean deleteExpense(Integer expenseId) throws SQLException {
        if (expenseId == null) {
            throw new IllegalArgumentException("Expense ID cannot be null");
        }

        synchronized (expenseLock) {     // 🔒 Thread-safe delete
            return expenseDAO.delete(expenseId);
        }
    }

    public BigDecimal getTotalByCategory(Integer categoryId) throws SQLException {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        return expenseDAO.getTotalByCategory(categoryId);
    }

    public BigDecimal getMonthlyTotal(int year, int month) throws SQLException {
        return expenseDAO.getMonthlyTotal(year, month);
    }

    private void validateExpense(LocalDate date, BigDecimal amount, String description, Integer categoryId) throws SQLException {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        if (categoryDAO.findById(categoryId) == null) {
            throw new IllegalArgumentException("Category not found");
        }
    }
}
