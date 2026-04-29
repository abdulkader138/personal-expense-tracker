package com.mycompany.expensetracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mycompany.expensetracker.model.Expense;
import com.mycompany.expensetracker.repository.ExpenseRepository;

public class ExpenseServiceTest {

	private ExpenseRepository repository;
	private ExpenseService service;

	@Before
	public void setUp() {
		repository = mock(ExpenseRepository.class);
		service = new ExpenseService(repository);
	}

	@Test
	public void testAddExpenseDelegatesToRepository() {
		Expense expense = new Expense("1", "Lunch", 10.0, null);
		service.addExpense(expense);
		verify(repository).save(expense);
	}

	@Test
	public void testAddExpenseWithNullThrowsException() {
		assertThatThrownBy(() -> service.addExpense(null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testAddExpenseWithNegativeAmountThrowsException() {
		Expense expense = new Expense("1", "Lunch", -5.0, null);
		assertThatThrownBy(() -> service.addExpense(expense))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testAddExpenseWithNullDescriptionThrowsException() {
		Expense expense = new Expense("1", null, 10.0, null);
		assertThatThrownBy(() -> service.addExpense(expense))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Description cannot be empty");
	}

	@Test
	public void testAddExpenseWithZeroAmountDelegatesToRepository() {
		Expense expense = new Expense("1", "Lunch", 0.0, null);
		service.addExpense(expense);
		verify(repository).save(expense);
	}

	@Test
	public void testAddExpenseWithBlankDescriptionThrowsException() {
		Expense expense = new Expense("1", "   ", 10.0, null);
		assertThatThrownBy(() -> service.addExpense(expense))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Description cannot be empty");
	}

	@Test
	public void testAddExpenseWithEmptyDescriptionThrowsException() {
		Expense expense = new Expense("1", "", 10.0, null);
		assertThatThrownBy(() -> service.addExpense(expense))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Description cannot be empty");
	}

	@Test
	public void testUpdateExpenseDelegatesToRepository() {
		Expense expense = new Expense("1", "Lunch", 10.0, null);
		service.updateExpense(expense);
		verify(repository).update(expense);
	}

	@Test
	public void testUpdateExpenseWithNullThrowsException() {
		assertThatThrownBy(() -> service.updateExpense(null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testUpdateExpenseWithNegativeAmountThrowsException() {
		Expense expense = new Expense("1", "Lunch", -5.0, null);
		assertThatThrownBy(() -> service.updateExpense(expense))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testUpdateExpenseWithNullDescriptionThrowsException() {
		Expense expense = new Expense("1", null, 10.0, null);
		assertThatThrownBy(() -> service.updateExpense(expense))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Description cannot be empty");
	}

	@Test
	public void testUpdateExpenseWithZeroAmountDelegatesToRepository() {
		Expense expense = new Expense("1", "Lunch", 0.0, null);
		service.updateExpense(expense);
		verify(repository).update(expense);
	}

	@Test
	public void testUpdateExpenseWithBlankDescriptionThrowsException() {
		Expense expense = new Expense("1", "   ", 10.0, null);
		assertThatThrownBy(() -> service.updateExpense(expense))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Description cannot be empty");
	}

	@Test
	public void testUpdateExpenseWithEmptyDescriptionThrowsException() {
		Expense expense = new Expense("1", "", 10.0, null);
		assertThatThrownBy(() -> service.updateExpense(expense))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Description cannot be empty");
	}

	@Test
	public void testGetAllExpensesReturnsFromRepository() {
		List<Expense> expenses = Arrays.asList(new Expense("1", "Lunch", 10.0, null));
		when(repository.findAll()).thenReturn(expenses);
		assertThat(service.getAllExpenses()).isEqualTo(expenses);
	}

	@Test
	public void testDeleteExpenseDelegatesToRepository() {
		service.deleteExpense("1");
		verify(repository).delete("1");
	}
}
