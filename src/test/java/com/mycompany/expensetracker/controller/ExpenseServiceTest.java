package com.mycompany.expensetracker.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
}
