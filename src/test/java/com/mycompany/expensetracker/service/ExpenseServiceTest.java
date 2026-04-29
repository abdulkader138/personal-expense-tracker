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
	public void testAddExpenseWithZeroAmountDelegatesToRepository() {
		Expense expense = new Expense("1", "Lunch", 0.0, null);
		service.addExpense(expense);
		verify(repository).save(expense);
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
