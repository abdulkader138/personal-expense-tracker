package com.mycompany.expensetracker.view.swing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mycompany.expensetracker.service.CategoryService;
import com.mycompany.expensetracker.service.ExpenseService;
import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.model.Expense;

public class ExpenseTrackerControllerTest {

	private ExpenseService expenseService;
	private CategoryService categoryService;
	private ExpenseTrackerView view;
	private ExpenseTrackerController controller;

	@Before
	public void setUp() {
		expenseService = mock(ExpenseService.class);
		categoryService = mock(CategoryService.class);
		view = mock(ExpenseTrackerView.class);
		controller = new ExpenseTrackerController(expenseService, categoryService, view);
	}

	@Test
	public void testAllExpensesShowsExpensesFromService() {
		List<Expense> expenses = Arrays.asList(new Expense("1", "Lunch", 10.0, null));
		when(expenseService.getAllExpenses()).thenReturn(expenses);
		controller.allExpenses();
		verify(view).showExpenses(expenses);
	}

	@Test
	public void testAllCategoriesShowsCategoriesFromService() {
		List<Category> categories = Arrays.asList(new Category("1", "Food"));
		when(categoryService.getAllCategories()).thenReturn(categories);
		controller.allCategories();
		verify(view).showCategories(categories);
	}

	@Test
	public void testNewExpenseCallsServiceAndRefreshesView() {
		Category category = new Category("1", "Food");
		List<Expense> expenses = Arrays.asList(new Expense("1", "Lunch", 10.0, category));
		when(view.getDescriptionText()).thenReturn("Lunch");
		when(view.getAmountText()).thenReturn("10.0");
		when(view.getSelectedCategory()).thenReturn(category);
		when(expenseService.getAllExpenses()).thenReturn(expenses);
		controller.newExpense();
		verify(expenseService).addExpense(any(Expense.class));
		verify(view).showExpenses(expenses);
	}

	@Test
	public void testNewExpenseWithInvalidAmountShowsError() {
		when(view.getDescriptionText()).thenReturn("Lunch");
		when(view.getAmountText()).thenReturn("not-a-number");
		controller.newExpense();
		verify(expenseService, never()).addExpense(any());
		verify(view).showError(contains("not-a-number"));
	}

	@Test
	public void testDeleteExpenseCallsServiceAndRefreshesView() {
		Expense expense = new Expense("1", "Lunch", 10.0, null);
		List<Expense> expenses = Arrays.asList();
		when(expenseService.getAllExpenses()).thenReturn(expenses);
		controller.deleteExpense(expense);
		verify(expenseService).deleteExpense("1");
		verify(view).showExpenses(expenses);
	}
}
