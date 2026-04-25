package com.mycompany.expensetracker.controller;

import com.mycompany.expensetracker.model.Expense;
import com.mycompany.expensetracker.repository.ExpenseRepository;

public class ExpenseService {

	private final ExpenseRepository repository;

	public ExpenseService(ExpenseRepository repository) {
		this.repository = repository;
	}

	public void addExpense(Expense expense) {
		repository.save(expense);
	}
}
