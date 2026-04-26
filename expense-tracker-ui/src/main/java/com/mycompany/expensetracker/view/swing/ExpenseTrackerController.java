package com.mycompany.expensetracker.view.swing;

import java.util.List;

import com.mycompany.expensetracker.service.CategoryService;
import com.mycompany.expensetracker.service.ExpenseService;
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
		view.addAddExpenseListener(e -> newExpense());
		view.addDeleteExpenseListener(e -> {
			Expense selected = view.getSelectedExpense();
			if (selected != null) {
				deleteExpense(selected);
			}
		});
	}

	public void allExpenses() {
		view.showExpenses(expenseService.getAllExpenses());
	}

	public void allCategories() {
		view.showCategories(categoryService.getAllCategories());
	}

	public void newExpense() {
		String description = view.getDescriptionText();
		double amount;
		try {
			amount = Double.parseDouble(view.getAmountText());
		} catch (NumberFormatException e) {
			view.showError("Invalid amount: " + view.getAmountText());
			return;
		}
		Category category = view.getSelectedCategory();
		Expense expense = new Expense(java.util.UUID.randomUUID().toString(), description, amount, category);
		expenseService.addExpense(expense);
		allExpenses();
	}

	public void deleteExpense(Expense expense) {
		expenseService.deleteExpense(expense.getId());
		allExpenses();
	}
}
