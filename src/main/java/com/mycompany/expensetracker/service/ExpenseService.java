package com.mycompany.expensetracker.service;

import java.util.List;

import com.mycompany.expensetracker.model.Expense;
import com.mycompany.expensetracker.repository.ExpenseRepository;

public class ExpenseService {

	private final ExpenseRepository repository;

	public ExpenseService(ExpenseRepository repository) {
		this.repository = repository;
	}

	public void addExpense(Expense expense) {
		if (expense == null) {
			throw new IllegalArgumentException("Expense cannot be null");
		}
		if (expense.getAmount() < 0) {
			throw new IllegalArgumentException("Amount cannot be negative");
		}
		repository.save(expense);
	}

	public List<Expense> getAllExpenses() {
		return repository.findAll();
	}

	public void deleteExpense(String id) {
		repository.delete(id);
	}
}
