package com.mycompany.pet.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Expense {
    private Integer expenseId;
    
    private LocalDate date;
    
    private BigDecimal amount;
    
    private String description;
    
    private Integer categoryId;

    public Expense() {
    }

    public Expense(Integer expenseId, LocalDate date, BigDecimal amount, String description, Integer categoryId) {
        this.expenseId = expenseId;
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.categoryId = categoryId;
    }

    public Expense(LocalDate date, BigDecimal amount, String description, Integer categoryId) {
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.categoryId = categoryId;
    }

    public Integer getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Integer expenseId) {
        this.expenseId = expenseId;
    }


public LocalDate getDate() {
        return date;
    }    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(expenseId, date, amount, description, categoryId);
    }

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



