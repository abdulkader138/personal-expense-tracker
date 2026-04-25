package com.mycompany.expensetracker.view.swing;

import java.util.List;

import com.mycompany.expensetracker.controller.CategoryService;
import com.mycompany.expensetracker.controller.ExpenseService;
import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.model.Expense;

public class ExpenseTrackerController {

	private final ExpenseService expenseService;
	private final CategoryService categoryService;
	private final ExpenseTrackerView view;

	public ExpenseTrackerController(ExpenseService expenseService, CategoryService categoryService,
			ExpenseTrackerView view) {
		this.expenseService = expenseService;
		this.categoryService = categoryService;
		this.view = view;
	}

	public void allExpenses() {
		view.showExpenses(expenseService.getAllExpenses());
	}
}
