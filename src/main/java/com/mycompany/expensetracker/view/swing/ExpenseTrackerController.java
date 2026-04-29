package com.mycompany.expensetracker.view.swing;

import java.util.UUID;

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
		view.addUpdateExpenseListener(e -> updateExpense());
		view.addAddCategoryListener(e -> addCategory());
		view.addUpdateCategoryListener(e -> updateCategory());
		view.addDeleteCategoryListener(e -> deleteCategory());
		view.addExpenseSelectionListener(e -> {
			Expense selected = view.getSelectedExpense();
			if (selected != null) {
				view.setDescriptionText(selected.getDescription());
				view.setAmountText(String.valueOf(selected.getAmount()));
				view.setSelectedCategory(selected.getCategory());
			}
		});
		view.addCategorySelectionListener(e -> {
			Category selected = view.getSelectedCategoryInList();
			if (selected != null) {
				view.setCategoryNameText(selected.getName());
			}
		});
	}

	public void allExpenses() {
		refreshExpenses();
	}

	public void allCategories() {
		refreshCategories();
	}

	public void newExpense() {
		Expense expense = buildExpenseFromView(java.util.UUID.randomUUID().toString());
		if (expense == null) {
			return;
		}
		try {
			expenseService.addExpense(expense);
		} catch (IllegalArgumentException e) {
			view.showError(e.getMessage());
			return;
		}
		refreshExpenses();
	}

	public void deleteExpense(Expense expense) {
		expenseService.deleteExpense(expense.getId());
		refreshExpenses();
	}

	public void updateExpense() {
		Expense selected = view.getSelectedExpense();
		if (selected == null) {
			view.showError("Select an expense to update");
			return;
		}
		Expense updated = buildExpenseFromView(selected.getId());
		if (updated == null) {
			return;
		}
		try {
			expenseService.updateExpense(updated);
		} catch (IllegalArgumentException e) {
			view.showError(e.getMessage());
			return;
		}
		refreshExpenses();
	}

	public void addCategory() {
		String name = view.getCategoryNameText();
		if (name == null || name.trim().isEmpty()) {
			view.showError("Category name cannot be empty");
			return;
		}
		Category category = new Category(UUID.randomUUID().toString(), name.trim());
		categoryService.addCategory(category);
		view.setCategoryNameText("");
		refreshCategories();
	}

	public void updateCategory() {
		Category selected = view.getSelectedCategoryInList();
		if (selected == null) {
			view.showError("Select a category to update");
			return;
		}
		String name = view.getCategoryNameText();
		if (name == null || name.trim().isEmpty()) {
			view.showError("Category name cannot be empty");
			return;
		}
		Category updated = new Category(selected.getId(), name.trim());
		categoryService.updateCategory(updated);
		refreshCategories();
	}

	public void deleteCategory() {
		Category selected = view.getSelectedCategoryInList();
		if (selected == null) {
			view.showError("Select a category to delete");
			return;
		}
		categoryService.deleteCategory(selected.getId());
		view.setCategoryNameText("");
		refreshCategories();
	}

	private Expense buildExpenseFromView(String id) {
		String description = view.getDescriptionText();
		if (description == null || description.trim().isEmpty()) {
			view.showError("Description cannot be empty");
			return null;
		}
		double amount;
		try {
			amount = Double.parseDouble(view.getAmountText());
		} catch (NumberFormatException e) {
			view.showError("Invalid amount: " + view.getAmountText());
			return null;
		}
		return new Expense(id, description.trim(), amount, view.getSelectedCategory());
	}

	private void refreshExpenses() {
		view.showExpenses(expenseService.getAllExpenses());
	}

	private void refreshCategories() {
		view.showCategories(categoryService.getAllCategories());
	}
}
