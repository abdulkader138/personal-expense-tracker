package com.mycompany.expensetracker.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.mycompany.expensetracker.model.Expense;
import com.mycompany.expensetracker.repository.ExpenseRepository;

@RunWith(Parameterized.class)
public class ExpenseServiceValidationParameterizedTest {

	private enum Operation {
		ADD {
			@Override
			void execute(ExpenseService service, Expense expense) {
				service.addExpense(expense);
			}

			@Override
			void verifyWrite(ExpenseRepository repository, Expense expense) {
				verify(repository).save(expense);
			}
		},
		UPDATE {
			@Override
			void execute(ExpenseService service, Expense expense) {
				service.updateExpense(expense);
			}

			@Override
			void verifyWrite(ExpenseRepository repository, Expense expense) {
				verify(repository).update(expense);
			}
		};

		abstract void execute(ExpenseService service, Expense expense);

		abstract void verifyWrite(ExpenseRepository repository, Expense expense);
	}

	@Parameterized.Parameters(name = "{index}: {0} {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{Operation.ADD, null, true, "Expense cannot be null"},
				{Operation.ADD, new Expense("1", "Lunch", 0.0, null), false, null},
				{Operation.ADD, new Expense("1", "   ", 10.0, null), true, "Description cannot be empty"},
				{Operation.ADD, new Expense("1", "", 10.0, null), true, "Description cannot be empty"},
				{Operation.ADD, new Expense("1", null, 10.0, null), true, "Description cannot be empty"},
				{Operation.UPDATE, null, true, "Expense cannot be null"},
				{Operation.UPDATE, new Expense("1", "Lunch", 0.0, null), false, null},
				{Operation.UPDATE, new Expense("1", "   ", 10.0, null), true, "Description cannot be empty"},
				{Operation.UPDATE, new Expense("1", "", 10.0, null), true, "Description cannot be empty"},
				{Operation.UPDATE, new Expense("1", null, 10.0, null), true, "Description cannot be empty"},
		});
	}

	private final Operation operation;
	private final Expense expense;
	private final boolean shouldThrow;
	private final String expectedMessage;

	private ExpenseRepository repository;
	private ExpenseService service;

	public ExpenseServiceValidationParameterizedTest(Operation operation, Expense expense, boolean shouldThrow, String expectedMessage) {
		this.operation = operation;
		this.expense = expense;
		this.shouldThrow = shouldThrow;
		this.expectedMessage = expectedMessage;
	}

	@Before
	public void setUp() {
		repository = mock(ExpenseRepository.class);
		service = new ExpenseService(repository);
	}

	@Test
	public void validatesExpenseBeforeWriting() {
		if (shouldThrow) {
			assertThatThrownBy(() -> operation.execute(service, expense))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage(expectedMessage);
			verifyNoInteractions(repository);
			return;
		}

		operation.execute(service, expense);
		operation.verifyWrite(repository, expense);
	}
}
