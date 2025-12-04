package com.mycompany.pet.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.dao.ExpenseDAO;
import com.mycompany.pet.model.Category;
import com.mycompany.pet.model.Expense;

/**
 * Unit tests for ExpenseService.
 */
public class ExpenseServiceTest {
    @Mock
    private ExpenseDAO expenseDAO;

    @Mock
    private CategoryDAO categoryDAO;

    private ExpenseService expenseService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        expenseService = new ExpenseService(expenseDAO, categoryDAO);
    }

    @Test
    public void testCreateExpense_Success() throws SQLException {
        // Given
        LocalDate date = LocalDate.now();
        BigDecimal amount = new BigDecimal("100.50");
        String description = "Lunch";
        Integer categoryId = 1;
        Category category = new Category(categoryId, "Food");
        
        when(categoryDAO.findById(categoryId)).thenReturn(category);
        when(expenseDAO.create(any(Expense.class))).thenAnswer(invocation -> {
            Expense exp = invocation.getArgument(0);
            exp.setExpenseId(1);
            return exp;
        });

        // When
        Expense result = expenseService.createExpense(date, amount, description, categoryId);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getExpenseId());
        assertEquals(date, result.getDate());
        assertEquals(amount, result.getAmount());
        assertEquals(description, result.getDescription());
        assertEquals(categoryId, result.getCategoryId());
        verify(expenseDAO, times(1)).create(any(Expense.class));
        verify(categoryDAO, times(1)).findById(categoryId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExpense_NullDate() throws SQLException {
        expenseService.createExpense(null, new BigDecimal("100"), "Description", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExpense_NullAmount() throws SQLException {
        expenseService.createExpense(LocalDate.now(), null, "Description", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExpense_ZeroAmount() throws SQLException {
        expenseService.createExpense(LocalDate.now(), BigDecimal.ZERO, "Description", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExpense_NegativeAmount() throws SQLException {
        expenseService.createExpense(LocalDate.now(), new BigDecimal("-10"), "Description", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExpense_EmptyDescription() throws SQLException {
        expenseService.createExpense(LocalDate.now(), new BigDecimal("100"), "", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExpense_NullCategoryId() throws SQLException {
        expenseService.createExpense(LocalDate.now(), new BigDecimal("100"), "Description", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExpense_CategoryNotFound() throws SQLException {
        when(categoryDAO.findById(1)).thenReturn(null);
        expenseService.createExpense(LocalDate.now(), new BigDecimal("100"), "Description", 1);
    }

    @Test
    public void testGetExpense_Success() throws SQLException {
        // Given
        Integer expenseId = 1;
        Expense expectedExpense = new Expense(expenseId, LocalDate.now(), new BigDecimal("100"), "Lunch", 1);
        when(expenseDAO.findById(expenseId)).thenReturn(expectedExpense);

        // When
        Expense result = expenseService.getExpense(expenseId);

        // Then
        assertNotNull(result);
        assertEquals(expectedExpense, result);
        verify(expenseDAO, times(1)).findById(expenseId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetExpense_NullId() throws SQLException {
        expenseService.getExpense(null);
    }

    @Test
    public void testGetAllExpenses_Success() throws SQLException {
        // Given
        List<Expense> expectedExpenses = Arrays.asList(
            new Expense(1, LocalDate.now(), new BigDecimal("100"), "Lunch", 1),
            new Expense(2, LocalDate.now(), new BigDecimal("50"), "Coffee", 1)
        );
        when(expenseDAO.findAll()).thenReturn(expectedExpenses);

        // When
        List<Expense> result = expenseService.getAllExpenses();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(expenseDAO, times(1)).findAll();
    }

    @Test
    public void testGetExpensesByMonth_Success() throws SQLException {
        // Given
        int year = 2024;
        int month = 1;
        List<Expense> expectedExpenses = Arrays.asList(
            new Expense(1, LocalDate.of(year, month, 15), new BigDecimal("100"), "Lunch", 1)
        );
        when(expenseDAO.findByMonth(year, month)).thenReturn(expectedExpenses);

        // When
        List<Expense> result = expenseService.getExpensesByMonth(year, month);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(expenseDAO, times(1)).findByMonth(year, month);
    }

    @Test
    public void testUpdateExpense_Success() throws SQLException {
        // Given
        Integer expenseId = 1;
        LocalDate newDate = LocalDate.now();
        BigDecimal newAmount = new BigDecimal("150.75");
        String newDescription = "Updated Lunch";
        Integer categoryId = 1;
        Category category = new Category(categoryId, "Food");
        Expense existingExpense = new Expense(expenseId, LocalDate.now().minusDays(1), new BigDecimal("100"), "Lunch", categoryId);
        
        when(expenseDAO.findById(expenseId)).thenReturn(existingExpense);
        when(categoryDAO.findById(categoryId)).thenReturn(category);
        when(expenseDAO.update(any(Expense.class))).thenReturn(existingExpense);

        // When
        Expense result = expenseService.updateExpense(expenseId, newDate, newAmount, newDescription, categoryId);

        // Then
        assertNotNull(result);
        assertEquals(newDate, result.getDate());
        assertEquals(newAmount, result.getAmount());
        assertEquals(newDescription, result.getDescription());
        verify(expenseDAO, times(1)).findById(expenseId);
        verify(expenseDAO, times(1)).update(any(Expense.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateExpense_NullId() throws SQLException {
        expenseService.updateExpense(null, LocalDate.now(), new BigDecimal("100"), "Description", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateExpense_NotFound() throws SQLException {
        when(expenseDAO.findById(1)).thenReturn(null);
        expenseService.updateExpense(1, LocalDate.now(), new BigDecimal("100"), "Description", 1);
    }

    @Test
    public void testDeleteExpense_Success() throws SQLException {
        // Given
        Integer expenseId = 1;
        when(expenseDAO.delete(expenseId)).thenReturn(true);

        // When
        boolean result = expenseService.deleteExpense(expenseId);

        // Then
        assertTrue(result);
        verify(expenseDAO, times(1)).delete(expenseId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteExpense_NullId() throws SQLException {
        expenseService.deleteExpense(null);
    }

    @Test
    public void testGetTotalByCategory_Success() throws SQLException {
        // Given
        Integer categoryId = 1;
        BigDecimal expectedTotal = new BigDecimal("250.50");
        when(expenseDAO.getTotalByCategory(categoryId)).thenReturn(expectedTotal);

        // When
        BigDecimal result = expenseService.getTotalByCategory(categoryId);

        // Then
        assertNotNull(result);
        assertEquals(expectedTotal, result);
        verify(expenseDAO, times(1)).getTotalByCategory(categoryId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTotalByCategory_NullId() throws SQLException {
        expenseService.getTotalByCategory(null);
    }

    @Test
    public void testGetMonthlyTotal_Success() throws SQLException {
        // Given
        int year = 2024;
        int month = 1;
        BigDecimal expectedTotal = new BigDecimal("500.00");
        when(expenseDAO.getMonthlyTotal(year, month)).thenReturn(expectedTotal);

        // When
        BigDecimal result = expenseService.getMonthlyTotal(year, month);

        // Then
        assertNotNull(result);
        assertEquals(expectedTotal, result);
        verify(expenseDAO, times(1)).getMonthlyTotal(year, month);
    }
}

