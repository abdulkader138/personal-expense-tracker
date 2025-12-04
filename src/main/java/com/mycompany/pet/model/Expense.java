package com.mycompany.pet.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents an expense entry
 */
public class Expense {
    /**
     * The unique identifier for this expense.
     */
    private Integer expenseId;
    
    /**
     * The date when the expense occurred.
     */
    private LocalDate date;
    
    /**
     * The monetary amount of the expense.
     */
    private BigDecimal amount;
    
    /**
     * A descriptive text explaining what the expense was for.
     */
    private String description;
    
    /**
     * The identifier of the category this expense belongs to.
     */
    private Integer categoryId;

    /**
     * Default constructor.
     */
    public Expense() {
    }

    /**
     * Parameterised constructor.
     * 
     * @param expenseId the unique identifier for this expense
     * @param date the date when the expense occurred
     * @param amount the monetary amount of the expense
     * @param description a description of what the expense was for
     * @param categoryId the identifier of the category this expense belongs to
     */
    public Expense(Integer expenseId, LocalDate date, BigDecimal amount, String description, Integer categoryId) {
        this.expenseId = expenseId;
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.categoryId = categoryId;
    }

    /**
     * Parameterised constructor.
     * 
     * @param date the date when the expense occurred
     * @param amount the monetary amount of the expense
     * @param description a description of what the expense was for
     * @param categoryId the identifier of the category this expense belongs to
     */
    public Expense(LocalDate date, BigDecimal amount, String description, Integer categoryId) {
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.categoryId = categoryId;
    }

    /**
     * Returns the id of this expense.
     * 
     * @return the expense identifier, or null if not yet persisted
     */
    public Integer getExpenseId() {
        return expenseId;
    }

    /**
     * Sets the unique id of expense.
     * 
     * @param expenseId the expense identifier to set
     */
    public void setExpenseId(Integer expenseId) {
        this.expenseId = expenseId;
    }

    /**
     * Returns the date of this expense.
     * 
     * @return the expense date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Sets the date of expense.
     * 
     * @param date the expense date to set
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Returns the amount of this expense.
     * 
     * @return the expense amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Sets the amount of expense.
     * 
     * @param amount the expense amount to set
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Returns the description of this expense.
     * 
     * @return the expense description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of expense.
     * 
     * @param description the expense description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the category id of this expense.
     * 
     * @return the category identifier
     */
    public Integer getCategoryId() {
        return categoryId;
    }

    /**
     * Sets the category id of expense.
     * 
     * @param categoryId the category identifier to set
     */
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Two expenses are considered equal if they have the same expenseId, date, amount,
     * description, and categoryId.
     * 
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return Objects.equals(expenseId, expense.expenseId) &&
               Objects.equals(date, expense.date) &&
               Objects.equals(amount, expense.amount) &&
               Objects.equals(description, expense.description) &&
               Objects.equals(categoryId, expense.categoryId);
    }

    /**
     * Returns a hash code value for this expense.
     * 
     * @return a hash code value for this expense
     */
    @Override
    public int hashCode() {
        return Objects.hash(expenseId, date, amount, description, categoryId);
    }

    /**
     * Returns a string representation of this expense.
     * 
     * @return a string representation of this expense
     */
    @Override
    public String toString() {
        return "Expense{" +
               "expenseId=" + expenseId +
               ", date=" + date +
               ", amount=" + amount +
               ", description='" + description + '\'' +
               ", categoryId=" + categoryId +
               '}';
    }
}



