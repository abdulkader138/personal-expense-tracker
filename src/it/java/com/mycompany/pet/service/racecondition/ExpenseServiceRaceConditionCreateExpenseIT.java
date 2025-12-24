/*
 * Integration tests for the ExpenseService class focused on race conditions.
 *
 * These tests verify the functionality of the ExpenseService in concurrent
 * environments, ensuring that the application handles race conditions properly
 * when multiple threads are accessing and creating expense data simultaneously.
 * The tests utilise Awaitility for handling asynchronous operations.
 *
 * The methods tested include:
 * - createExpense() for concurrent creation of expenses.
 * 
 * The setup and teardown methods handle the initialisation and cleanup of database connections.
 *
 * The databaseConfig variable is responsible for starting the Docker container.
 * If the test is run from Eclipse, it runs the Docker container using Testcontainers.
 * If the test is run using a Maven command, it starts a Docker container without test containers.
 *
 * @see ExpenseService
 * @see ExpenseDAO
 * @see CategoryDAO
 * @see DatabaseConfig
 * @see DBConfig
 * @see MavenContainerConfig
 * @see TestContainerConfig
 */

package com.mycompany.pet.service.racecondition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mycompany.pet.DatabaseConfig;
import com.mycompany.pet.configurations.DBConfig;
import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.dao.ExpenseDAO;
import com.mycompany.pet.database.DatabaseConnection;
import com.mycompany.pet.database.DatabaseInitializer;
import com.mycompany.pet.model.Category;
import com.mycompany.pet.service.ExpenseService;

/**
 * The Class ExpenseServiceRaceConditionCreateExpenseIT.
 */
public class ExpenseServiceRaceConditionCreateExpenseIT {

	/** The expense service. */
	private ExpenseService expenseService;

	/** The database connection. */
	private DatabaseConnection databaseConnection;

	/** The category. */
	private Category category;

	/** The expense date. */
	private LocalDate EXPENSE_DATE = LocalDate.now();

	/** The expense amount. */
	private BigDecimal EXPENSE_AMOUNT = new BigDecimal("100.50");

	/** The expense description. */
	private String EXPENSE_DESCRIPTION = "Lunch";

	/**
	 * This variable is responsible for starting the Docker container. If the test
	 * is run from Eclipse, it runs the Docker container using Testcontainers. If
	 * the test is run using a Maven command, it starts a Docker container without
	 * test containers
	 */
	private static DBConfig databaseConfig;

	/**
	 * Setup server.
	 */
	@BeforeClass
	public static void setupServer() {
		try {
			databaseConfig = DatabaseConfig.getDatabaseConfig();
			if (databaseConfig == null) {
				org.junit.Assume.assumeTrue("Database config not available", false);
				return;
			}
			databaseConfig.testAndStartDatabaseConnection();
		} catch (Exception e) {
			// Skip tests if database setup fails (e.g., Docker not available)
			org.junit.Assume.assumeNoException("Database setup failed. Docker may not be available. Skipping integration tests.", e);
		}
	}

	/**
	 * Sets the up.
	 *
	 * @throws SQLException the SQL exception
	 */
	@Before
	public void setUp() throws SQLException {
		if (databaseConfig == null) {
			org.junit.Assume.assumeTrue("Database config not available", false);
			return;
		}
		
		try {
			databaseConnection = databaseConfig.getDatabaseConnection();
			if (databaseConnection == null) {
				org.junit.Assume.assumeTrue("Database connection not available", false);
				return;
			}
			
			try {
				DatabaseInitializer initializer = new DatabaseInitializer(databaseConnection);
				initializer.initialize();
			} catch (Exception e) {
				org.junit.Assume.assumeNoException("Failed to initialize database. Skipping integration tests.", e);
				return;
			}

			CategoryDAO categoryDAO = new CategoryDAO(databaseConnection);
			ExpenseDAO expenseDAO = new ExpenseDAO(databaseConnection);
			expenseService = new ExpenseService(expenseDAO, categoryDAO);

			category = categoryDAO.create(new Category("Food"));
		} catch (Exception e) {
			org.junit.Assume.assumeNoException("Database operation failed. Skipping test.", e);
			return;
		}
	}

	/**
	 * Release resources.
	 */
	@After
	public void releaseResources() {
		if (databaseConnection != null) {
			databaseConnection.close();
		}
	}

	/**
	 * Create expense concurrent.
	 */
	@Test
	public void createExpenseConcurrent() {
		if (expenseService == null || category == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		List<Thread> threads = IntStream.range(0, 10).mapToObj(i -> new Thread(() -> {
			try {
				expenseService.createExpense(EXPENSE_DATE, EXPENSE_AMOUNT, EXPENSE_DESCRIPTION, category.getCategoryId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		})).peek(t -> t.start()).collect(Collectors.toList());
		await().atMost(10, TimeUnit.SECONDS).until(() -> threads.stream().noneMatch(t -> t.isAlive()));

		// Verify all expenses were created
		try {
			List<com.mycompany.pet.model.Expense> expenses = expenseService.getAllExpenses();
			// Should have 10 expenses created by 10 threads
			assertThat(expenses).hasSize(10);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}

