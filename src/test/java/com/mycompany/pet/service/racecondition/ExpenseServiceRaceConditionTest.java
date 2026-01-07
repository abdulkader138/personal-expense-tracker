/*
 * Unit tests for the ExpenseService class focused on race conditions.
 *
 * These tests verify the functionality of the ExpenseService in concurrent
 * environments, ensuring that the application handles race conditions properly
 * when multiple threads are accessing and modifying expense data simultaneously.
 * The tests utilise Mockito for mocking dependencies and Awaitility for 
 * handling asynchronous operations.
 *
 * The methods tested include:
 * - createExpense() for concurrent creation of expenses.
 * - deleteExpense() for concurrent deletion of expenses.
 *
 * Each test follows a structured approach with three main phases:
 * 1. Setup: Created environment for the test.
 * 2. Mocks: Configuring the mock objects (Added separate comment just for better readability).
 * 3. Exercise: Calling an instance method.
 * 4. Verify: Verify that the outcome matches the expected behaviour.
 *
 * The setup and teardown methods handle the initialisation and cleanup of mock objects.
 *
 * @see ExpenseService
 * @see ExpenseDAO
 * @see CategoryDAO
 */

package com.mycompany.pet.service.racecondition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.dao.ExpenseDAO;
import com.mycompany.pet.model.Category;
import com.mycompany.pet.model.Expense;
import com.mycompany.pet.service.ExpenseService;

/**
 * The Class ExpenseServiceRaceConditionTest.
 */
public class ExpenseServiceRaceConditionTest {

	/** The expense DAO. */
	@Mock
	private ExpenseDAO expenseDAO;

	/** The category DAO. */
	@Mock
	private CategoryDAO categoryDAO;

	/** The expense service. */
	private ExpenseService expenseService;

	/** The closeable. */
	private AutoCloseable closeable;

	/** The expense id. */
	private Integer expenseId = 1;

	/** The expense date. */
	private LocalDate expenseDate = LocalDate.now();

	/** The expense amount. */
	private BigDecimal expenseAmount = new BigDecimal("100.50");

	/** The expense description. */
	private String expenseDescription = "Lunch";

	/** The category id. */
	private Integer categoryId = 1;

	/** The category. */
	private Category category = new Category(categoryId, "Food");

	/**
	 * Setup.
	 */
	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		expenseService = new ExpenseService(expenseDAO, categoryDAO);
	}

	/**
	 * Release mocks.
	 *
	 * @throws Exception the exception
	 */
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

	/**
	 * Test new expense concurrent.
	 */
	@Test
	public void testNewExpenseConcurrent() throws SQLException {
		// Setup
		List<Expense> expenses = new ArrayList<>();

		// Mocks
		when(categoryDAO.findById(anyInt())).thenAnswer(invocation -> category);
		doAnswer(invocation -> {
			Expense exp = invocation.getArgument(0);
			exp.setExpenseId(expenseId);
			expenses.add(exp);
			return exp;
		}).when(expenseDAO).create(any(Expense.class));

		// Exercise
		List<Thread> threads = IntStream.range(0, 10)
				.mapToObj(i -> new Thread(() -> {
					try {
						expenseService.createExpense(expenseDate, expenseAmount, expenseDescription, categoryId);
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				})).peek(Thread::start).toList();
		await().atMost(10, TimeUnit.SECONDS).until(() -> threads.stream().noneMatch(Thread::isAlive));

		// Verify - all 10 threads should have created expenses
		assertThat(expenses).hasSize(10);
	}

	/**
	 * Test delete expense concurrent.
	 */
	@Test
	public void testDeleteExpenseConcurrent() throws SQLException {
		// Setup
		List<Expense> expenses = new ArrayList<>();
		Expense expense = new Expense(expenseId, expenseDate, expenseAmount, expenseDescription, categoryId);
		expenses.add(expense);

		// Mocks
		when(expenseDAO.findById(anyInt())).thenAnswer(invocation -> expenses.stream().findFirst().orElse(null));

		doAnswer(invocation -> {
			Integer id = invocation.getArgument(0);
			expenses.removeIf(e -> e.getExpenseId().equals(id));
			return true;
		}).when(expenseDAO).delete(anyInt());

		// Exercise
		List<Thread> threads = IntStream.range(0, 10)
				.mapToObj(i -> new Thread(() -> {
					try {
						expenseService.deleteExpense(expenseId);
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				})).peek(Thread::start).toList();
		await().atMost(10, TimeUnit.SECONDS).until(() -> threads.stream().noneMatch(Thread::isAlive));

		// Verify - expense should be deleted (only once, but multiple threads may try)
		// The important thing is that the list is empty after all threads complete
		assertThat(expenses).isEmpty();
	}
}

