package com.mycompany.expensetracker.view.swing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

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
	public void testNewExpenseWhenServiceThrowsShowsError() {
		when(view.getDescriptionText()).thenReturn("Lunch");
		when(view.getAmountText()).thenReturn("10.0");
		doThrow(new IllegalArgumentException("boom")).when(expenseService).addExpense(any(Expense.class));
		controller.newExpense();
		verify(view).showError("boom");
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

	@Test
	public void testDeleteListenerDoesNothingWhenNothingIsSelected() {
		ArgumentCaptor<ActionListener> captor = ArgumentCaptor.forClass(ActionListener.class);
		verify(view).addDeleteExpenseListener(captor.capture());
		when(view.getSelectedExpense()).thenReturn(null);
		captor.getValue().actionPerformed(null);
		verify(expenseService, never()).deleteExpense(any());
	}

	@Test
	public void testAddListenerCallsNewExpenseWhenFired() {
		ArgumentCaptor<ActionListener> captor = ArgumentCaptor.forClass(ActionListener.class);
		verify(view).addAddExpenseListener(captor.capture());
		when(view.getDescriptionText()).thenReturn("Lunch");
		when(view.getAmountText()).thenReturn("10.0");
		when(view.getSelectedCategory()).thenReturn(null);
		when(expenseService.getAllExpenses()).thenReturn(Arrays.asList());
		captor.getValue().actionPerformed(null);
		verify(expenseService).addExpense(any(Expense.class));
	}

	@Test
	public void testDeleteListenerCallsDeleteExpenseWhenExpenseIsSelected() {
		ArgumentCaptor<ActionListener> captor = ArgumentCaptor.forClass(ActionListener.class);
		verify(view).addDeleteExpenseListener(captor.capture());
		Expense expense = new Expense("1", "Lunch", 10.0, null);
		when(view.getSelectedExpense()).thenReturn(expense);
		when(expenseService.getAllExpenses()).thenReturn(Arrays.asList());
		captor.getValue().actionPerformed(null);
		verify(expenseService).deleteExpense("1");
	}

	@Test
	public void testUpdateExpenseListenerCallsUpdateExpenseWhenFired() {
		ArgumentCaptor<ActionListener> captor = ArgumentCaptor.forClass(ActionListener.class);
		verify(view).addUpdateExpenseListener(captor.capture());
		Expense selected = new Expense("1", "Lunch", 10.0, null);
		List<Expense> expenses = Arrays.asList(selected);
		when(view.getSelectedExpense()).thenReturn(selected);
		when(view.getDescriptionText()).thenReturn("Dinner");
		when(view.getAmountText()).thenReturn("20.0");
		when(view.getSelectedCategory()).thenReturn(null);
		when(expenseService.getAllExpenses()).thenReturn(expenses);
		captor.getValue().actionPerformed(null);
		verify(expenseService).updateExpense(new Expense("1", "Dinner", 20.0, null));
		verify(view).showExpenses(expenses);
	}

	@Test
	public void testUpdateExpenseCallsServiceAndRefreshesView() {
		Expense selected = new Expense("1", "Lunch", 10.0, null);
		List<Expense> expenses = Arrays.asList(selected);
		when(view.getSelectedExpense()).thenReturn(selected);
		when(view.getDescriptionText()).thenReturn("Dinner");
		when(view.getAmountText()).thenReturn("20.0");
		when(view.getSelectedCategory()).thenReturn(null);
		when(expenseService.getAllExpenses()).thenReturn(expenses);
		controller.updateExpense();
		verify(expenseService).updateExpense(new Expense("1", "Dinner", 20.0, null));
		verify(view).showExpenses(expenses);
	}

	@Test
	public void testUpdateExpenseWhenServiceThrowsShowsError() {
		Expense selected = new Expense("1", "Lunch", 10.0, null);
		when(view.getSelectedExpense()).thenReturn(selected);
		when(view.getDescriptionText()).thenReturn("Dinner");
		when(view.getAmountText()).thenReturn("20.0");
		doThrow(new IllegalArgumentException("boom")).when(expenseService).updateExpense(any(Expense.class));
		controller.updateExpense();
		verify(view).showError("boom");
	}

	@Test
	public void testUpdateExpenseWithNoSelectionShowsError() {
		when(view.getSelectedExpense()).thenReturn(null);
		controller.updateExpense();
		verify(expenseService, never()).updateExpense(any(Expense.class));
		verify(view).showError(contains("Select an expense to update"));
	}

	@Test
	public void testAddCategoryCallsServiceAndRefreshesView() {
		List<Category> categories = Arrays.asList(new Category("1", "Food"));
		when(view.getCategoryNameText()).thenReturn("Travel");
		when(categoryService.getAllCategories()).thenReturn(categories);
		controller.addCategory();
		verify(categoryService).addCategory(any(Category.class));
		verify(view).setCategoryNameText("");
		verify(view).showCategories(categories);
	}

	@Test
	public void testAddCategoryListenerCallsAddCategoryWhenFired() {
		ArgumentCaptor<ActionListener> captor = ArgumentCaptor.forClass(ActionListener.class);
		verify(view).addAddCategoryListener(captor.capture());
		when(view.getCategoryNameText()).thenReturn("Travel");
		when(categoryService.getAllCategories()).thenReturn(Arrays.asList());
		captor.getValue().actionPerformed(null);
		verify(categoryService).addCategory(any(Category.class));
	}

	@Test
	public void testUpdateCategoryCallsServiceAndRefreshesView() {
		Category selected = new Category("1", "Food");
		List<Category> categories = Arrays.asList(selected);
		when(view.getSelectedCategoryInList()).thenReturn(selected);
		when(view.getCategoryNameText()).thenReturn("Groceries");
		when(categoryService.getAllCategories()).thenReturn(categories);
		controller.updateCategory();
		verify(categoryService).updateCategory(new Category("1", "Groceries"));
		verify(view, times(1)).showCategories(categories);
	}

	@Test
	public void testUpdateCategoryListenerCallsUpdateCategoryWhenFired() {
		ArgumentCaptor<ActionListener> captor = ArgumentCaptor.forClass(ActionListener.class);
		verify(view).addUpdateCategoryListener(captor.capture());
		Category selected = new Category("1", "Food");
		List<Category> categories = Arrays.asList(selected);
		when(view.getSelectedCategoryInList()).thenReturn(selected);
		when(view.getCategoryNameText()).thenReturn("Groceries");
		when(categoryService.getAllCategories()).thenReturn(categories);
		captor.getValue().actionPerformed(null);
		verify(categoryService).updateCategory(new Category("1", "Groceries"));
		verify(view).showCategories(categories);
	}

	@Test
	public void testUpdateCategoryWithNoSelectionShowsError() {
		when(view.getSelectedCategoryInList()).thenReturn(null);
		controller.updateCategory();
		verify(categoryService, never()).updateCategory(any(Category.class));
		verify(view).showError(contains("Select a category to update"));
	}

	@Test
	public void testDeleteCategoryCallsServiceAndRefreshesView() {
		Category selected = new Category("1", "Food");
		List<Category> categories = Arrays.asList();
		when(view.getSelectedCategoryInList()).thenReturn(selected);
		when(categoryService.getAllCategories()).thenReturn(categories);
		controller.deleteCategory();
		verify(categoryService).deleteCategory("1");
		verify(view).setCategoryNameText("");
		verify(view).showCategories(categories);
	}

	@Test
	public void testDeleteCategoryListenerCallsDeleteCategoryWhenFired() {
		ArgumentCaptor<ActionListener> captor = ArgumentCaptor.forClass(ActionListener.class);
		verify(view).addDeleteCategoryListener(captor.capture());
		Category selected = new Category("1", "Food");
		List<Category> categories = Arrays.asList();
		when(view.getSelectedCategoryInList()).thenReturn(selected);
		when(categoryService.getAllCategories()).thenReturn(categories);
		captor.getValue().actionPerformed(null);
		verify(categoryService).deleteCategory("1");
		verify(view).showCategories(categories);
	}

	@Test
	public void testDeleteCategoryWithNoSelectionShowsError() {
		when(view.getSelectedCategoryInList()).thenReturn(null);
		controller.deleteCategory();
		verify(categoryService, never()).deleteCategory(anyString());
		verify(view).showError(contains("Select a category to delete"));
	}

	@Test
	public void testCategorySelectionListenerCopiesSelectedNameIntoField() {
		ArgumentCaptor<javax.swing.event.ListSelectionListener> captor =
				ArgumentCaptor.forClass(javax.swing.event.ListSelectionListener.class);
		verify(view).addCategorySelectionListener(captor.capture());
		Category selected = new Category("1", "Food");
		when(view.getSelectedCategoryInList()).thenReturn(selected);
		captor.getValue().valueChanged(null);
		verify(view).setCategoryNameText("Food");
	}

	@Test
	public void testCategorySelectionListenerDoesNothingWhenSelectionIsNull() {
		ArgumentCaptor<javax.swing.event.ListSelectionListener> captor =
				ArgumentCaptor.forClass(javax.swing.event.ListSelectionListener.class);
		verify(view).addCategorySelectionListener(captor.capture());
		when(view.getSelectedCategoryInList()).thenReturn(null);
		captor.getValue().valueChanged(null);
		verify(view, never()).setCategoryNameText(any());
	}

	@Test
	public void testExpenseSelectionListenerCopiesSelectedValuesIntoFields() {
		ArgumentCaptor<javax.swing.event.ListSelectionListener> captor =
				ArgumentCaptor.forClass(javax.swing.event.ListSelectionListener.class);
		verify(view).addExpenseSelectionListener(captor.capture());
		Category category = new Category("1", "Food");
		Expense selected = new Expense("1", "Lunch", 10.0, category);
		when(view.getSelectedExpense()).thenReturn(selected);
		captor.getValue().valueChanged(null);
		verify(view).setDescriptionText("Lunch");
		verify(view).setAmountText("10.0");
		verify(view).setSelectedCategory(category);
	}

	@Test
	public void testExpenseSelectionListenerDoesNothingWhenSelectionIsNull() {
		ArgumentCaptor<javax.swing.event.ListSelectionListener> captor =
				ArgumentCaptor.forClass(javax.swing.event.ListSelectionListener.class);
		verify(view).addExpenseSelectionListener(captor.capture());
		when(view.getSelectedExpense()).thenReturn(null);
		captor.getValue().valueChanged(null);
		verify(view, never()).setDescriptionText(any());
		verify(view, never()).setAmountText(any());
		verify(view, never()).setSelectedCategory(any(Category.class));
	}
}
