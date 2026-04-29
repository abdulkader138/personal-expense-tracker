package com.mycompany.expensetracker.view.swing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.model.Expense;
import com.mycompany.expensetracker.service.CategoryService;
import com.mycompany.expensetracker.service.ExpenseService;

@RunWith(Parameterized.class)
public class ExpenseTrackerControllerExpenseValidationParameterizedTest {

	private enum Operation {
		NEW_EXPENSE {
			@Override
			void execute(ExpenseTrackerController controller) {
				controller.newExpense();
			}

			@Override
			void verifyServiceNotCalled(ExpenseService expenseService) {
				verify(expenseService, never()).addExpense(any(Expense.class));
			}
		},
		UPDATE_EXPENSE {
			@Override
			void execute(ExpenseTrackerController controller) {
				controller.updateExpense();
			}

			@Override
			void verifyServiceNotCalled(ExpenseService expenseService) {
				verify(expenseService, never()).updateExpense(any(Expense.class));
			}
		};

		abstract void execute(ExpenseTrackerController controller);

		abstract void verifyServiceNotCalled(ExpenseService expenseService);
	}

	@Parameterized.Parameters(name = "{index}: {0} {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{Operation.NEW_EXPENSE, "   ", "10.0", "Description cannot be empty"},
				{Operation.NEW_EXPENSE, "", "10.0", "Description cannot be empty"},
				{Operation.NEW_EXPENSE, null, "10.0", "Description cannot be empty"},
				{Operation.NEW_EXPENSE, "Lunch", "oops", "Invalid amount: oops"},
				{Operation.UPDATE_EXPENSE, "   ", "20.0", "Description cannot be empty"},
				{Operation.UPDATE_EXPENSE, "", "20.0", "Description cannot be empty"},
				{Operation.UPDATE_EXPENSE, null, "20.0", "Description cannot be empty"},
				{Operation.UPDATE_EXPENSE, "Dinner", "oops", "Invalid amount: oops"},
		});
	}

	private final Operation operation;
	private final String descriptionText;
	private final String amountText;
	private final String expectedError;

	private ExpenseService expenseService;
	private CategoryService categoryService;
	private ExpenseTrackerView view;
	private ExpenseTrackerController controller;

	public ExpenseTrackerControllerExpenseValidationParameterizedTest(
			Operation operation,
			String descriptionText,
			String amountText,
			String expectedError) {
		this.operation = operation;
		this.descriptionText = descriptionText;
		this.amountText = amountText;
		this.expectedError = expectedError;
	}

	@Before
	public void setUp() {
		expenseService = mock(ExpenseService.class);
		categoryService = mock(CategoryService.class);
		view = mock(ExpenseTrackerView.class);
		controller = new ExpenseTrackerController(expenseService, categoryService, view);
	}

	@Test
	public void showsValidationErrorWithoutCallingService() {
		Category category = new Category("1", "Food");
		Expense selected = new Expense("1", "Dinner", 20.0, category);

		when(view.getDescriptionText()).thenReturn(descriptionText);
		when(view.getAmountText()).thenReturn(amountText);
		when(view.getSelectedCategory()).thenReturn(category);
		when(view.getSelectedExpense()).thenReturn(selected);

		operation.execute(controller);

		operation.verifyServiceNotCalled(expenseService);
		verify(expenseService, never()).getAllExpenses();
		verify(view, never()).showExpenses(any());
		verify(view).showError(org.mockito.ArgumentMatchers.contains(expectedError));
	}
}
