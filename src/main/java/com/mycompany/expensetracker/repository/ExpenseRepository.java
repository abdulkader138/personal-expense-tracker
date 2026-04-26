package com.mycompany.expensetracker.repository;

import java.util.List;

import com.mycompany.expensetracker.model.Expense;

public interface ExpenseRepository {

	void save(Expense expense);

	List<Expense> findAll();

	Expense findById(String id);

	void update(Expense expense);

	void delete(String id);
}
