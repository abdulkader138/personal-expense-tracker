package com.mycompany.pet.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

/**
 * Unit tests for Expense model class.
 */
public class ExpenseTest {
    
    private static final Integer EXPENSE_ID = 1;
    private static final LocalDate EXPENSE_DATE = LocalDate.of(2024, 1, 15);
    private static final BigDecimal EXPENSE_AMOUNT = new BigDecimal("100.50");
    private static final String EXPENSE_DESCRIPTION = "Lunch";
    private static final Integer CATEGORY_ID = 1;
    
    @Test
    public void testDefaultConstructor() {
        Expense expense = new Expense();
        assertThat(expense.getExpenseId()).isNull();
        assertThat(expense.getDate()).isNull();
        assertThat(expense.getAmount()).isNull();
        assertThat(expense.getDescription()).isNull();
        assertThat(expense.getCategoryId()).isNull();
    }
    
    @Test
    public void testConstructorWithAllFields() {
        Expense expense = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        assertThat(expense.getExpenseId()).isEqualTo(EXPENSE_ID);
        assertThat(expense.getDate()).isEqualTo(EXPENSE_DATE);
        assertThat(expense.getAmount()).isEqualTo(EXPENSE_AMOUNT);
        assertThat(expense.getDescription()).isEqualTo(EXPENSE_DESCRIPTION);
        assertThat(expense.getCategoryId()).isEqualTo(CATEGORY_ID);
    }
    
    @Test
    public void testConstructorWithoutId() {
        Expense expense = new Expense(EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        assertThat(expense.getExpenseId()).isNull();
        assertThat(expense.getDate()).isEqualTo(EXPENSE_DATE);
        assertThat(expense.getAmount()).isEqualTo(EXPENSE_AMOUNT);
        assertThat(expense.getDescription()).isEqualTo(EXPENSE_DESCRIPTION);
        assertThat(expense.getCategoryId()).isEqualTo(CATEGORY_ID);
    }
    
    @Test
    public void testSetExpenseId() {
        Expense expense = new Expense();
        expense.setExpenseId(EXPENSE_ID);
        assertThat(expense.getExpenseId()).isEqualTo(EXPENSE_ID);
    }
    
    @Test
    public void testSetDate() {
        Expense expense = new Expense();
        expense.setDate(EXPENSE_DATE);
        assertThat(expense.getDate()).isEqualTo(EXPENSE_DATE);
    }
    
    @Test
    public void testSetAmount() {
        Expense expense = new Expense();
        expense.setAmount(EXPENSE_AMOUNT);
        assertThat(expense.getAmount()).isEqualTo(EXPENSE_AMOUNT);
    }
    
    @Test
    public void testSetDescription() {
        Expense expense = new Expense();
        expense.setDescription(EXPENSE_DESCRIPTION);
        assertThat(expense.getDescription()).isEqualTo(EXPENSE_DESCRIPTION);
    }
    
    @Test
    public void testSetCategoryId() {
        Expense expense = new Expense();
        expense.setCategoryId(CATEGORY_ID);
        assertThat(expense.getCategoryId()).isEqualTo(CATEGORY_ID);
    }
    
    @Test
    public void testEquals_SameInstance() {
        Expense expense = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        assertThat(expense.equals(expense)).isTrue();
    }
    
    @Test
    public void testEquals_Null() {
        Expense expense = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        assertThat(expense.equals(null)).isFalse();
    }
    
    @Test
    public void testEquals_DifferentClass() {
        Expense expense = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        assertThat(expense.equals("not an expense")).isFalse();
    }
    
    @Test
    public void testEquals_SameValues() {
        Expense expense1 = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        Expense expense2 = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        assertThat(expense1.equals(expense2)).isTrue();
    }
    
    @Test
    public void testEquals_DifferentExpenseId() {
        Expense expense1 = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        Expense expense2 = new Expense(2, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        assertThat(expense1.equals(expense2)).isFalse();
    }
    
    @Test
    public void testEquals_DifferentDate() {
        Expense expense1 = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        Expense expense2 = new Expense(EXPENSE_ID, LocalDate.of(2024, 1, 16), EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        assertThat(expense1.equals(expense2)).isFalse();
    }
    
    @Test
    public void testEquals_DifferentAmount() {
        Expense expense1 = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        Expense expense2 = new Expense(EXPENSE_ID, EXPENSE_DATE, new BigDecimal("200.00"), 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        assertThat(expense1.equals(expense2)).isFalse();
    }
    
    @Test
    public void testEquals_DifferentDescription() {
        Expense expense1 = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        Expense expense2 = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            "Dinner", CATEGORY_ID);
        assertThat(expense1.equals(expense2)).isFalse();
    }
    
    @Test
    public void testEquals_DifferentCategoryId() {
        Expense expense1 = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        Expense expense2 = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, 2);
        assertThat(expense1.equals(expense2)).isFalse();
    }
    
    @Test
    public void testEquals_WithNullValues() {
        Expense expense1 = new Expense(null, null, null, null, null);
        Expense expense2 = new Expense(null, null, null, null, null);
        assertThat(expense1.equals(expense2)).isTrue();
    }
    
    @Test
    public void testEquals_OneNullExpenseId() {
        Expense expense1 = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        Expense expense2 = new Expense(null, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        assertThat(expense1.equals(expense2)).isFalse();
    }
    
    @Test
    public void testHashCode_SameValues() {
        Expense expense1 = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        Expense expense2 = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        assertThat(expense1).hasSameHashCodeAs(expense2);
    }
    
    @Test
    public void testHashCode_NullValues() {
        Expense expense1 = new Expense(null, null, null, null, null);
        Expense expense2 = new Expense(null, null, null, null, null);
        assertThat(expense1).hasSameHashCodeAs(expense2);
    }
    
    @Test
    public void testToString() {
        Expense expense = new Expense(EXPENSE_ID, EXPENSE_DATE, EXPENSE_AMOUNT, 
            EXPENSE_DESCRIPTION, CATEGORY_ID);
        String toString = expense.toString();
        assertThat(toString)
            .contains("Expense{")
            .contains("expenseId=" + EXPENSE_ID)
            .contains("date=" + EXPENSE_DATE)
            .contains("amount=" + EXPENSE_AMOUNT)
            .contains("description='" + EXPENSE_DESCRIPTION + "'")
            .contains("categoryId=" + CATEGORY_ID);
    }
    
    @Test
    public void testToString_WithNullValues() {
        Expense expense = new Expense(null, null, null, null, null);
        String toString = expense.toString();
        assertThat(toString)
            .contains("Expense{")
            .contains("expenseId=null")
            .contains("date=null")
            .contains("amount=null")
            .contains("description='null'")
            .contains("categoryId=null");
    }
}

