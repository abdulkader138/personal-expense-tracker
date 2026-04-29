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
import com.mycompany.expensetracker.service.CategoryService;
import com.mycompany.expensetracker.service.ExpenseService;

@RunWith(Parameterized.class)
public class ExpenseTrackerControllerCategoryValidationParameterizedTest {

	private enum Operation {
		ADD_CATEGORY {
			@Override
			void execute(ExpenseTrackerController controller) {
				controller.addCategory();
			}

			@Override
			void verifyServiceNotCalled(CategoryService categoryService) {
				verify(categoryService, never()).addCategory(any(Category.class));
			}
		},
		UPDATE_CATEGORY {
			@Override
			void execute(ExpenseTrackerController controller) {
				controller.updateCategory();
			}

			@Override
			void verifyServiceNotCalled(CategoryService categoryService) {
				verify(categoryService, never()).updateCategory(any(Category.class));
			}
		};

		abstract void execute(ExpenseTrackerController controller);

		abstract void verifyServiceNotCalled(CategoryService categoryService);
	}

	@Parameterized.Parameters(name = "{index}: {0} {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{Operation.ADD_CATEGORY, "   ", "Category name cannot be empty"},
				{Operation.ADD_CATEGORY, "", "Category name cannot be empty"},
				{Operation.ADD_CATEGORY, null, "Category name cannot be empty"},
				{Operation.UPDATE_CATEGORY, "   ", "Category name cannot be empty"},
				{Operation.UPDATE_CATEGORY, "", "Category name cannot be empty"},
				{Operation.UPDATE_CATEGORY, null, "Category name cannot be empty"},
		});
	}

	private final Operation operation;
	private final String categoryNameText;
	private final String expectedError;

	private ExpenseService expenseService;
	private CategoryService categoryService;
	private ExpenseTrackerView view;
	private ExpenseTrackerController controller;

	public ExpenseTrackerControllerCategoryValidationParameterizedTest(
			Operation operation,
			String categoryNameText,
			String expectedError) {
		this.operation = operation;
		this.categoryNameText = categoryNameText;
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
	public void showsValidationErrorWithoutCallingCategoryService() {
		when(view.getCategoryNameText()).thenReturn(categoryNameText);
		if (operation == Operation.UPDATE_CATEGORY) {
			when(view.getSelectedCategoryInList()).thenReturn(new Category("1", "Food"));
		}

		operation.execute(controller);

		operation.verifyServiceNotCalled(categoryService);
		verify(view).showError(org.mockito.ArgumentMatchers.contains(expectedError));
	}
}
