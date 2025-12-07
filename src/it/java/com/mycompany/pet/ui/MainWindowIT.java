/*
 * Integration tests for the MainWindow class.
 * 
 * These tests cover the following functionalities:
 * 
 * - Setting up and tearing down the test environment using Docker containers and database configurations.
 * - Creating, updating, fetching, and deleting expenses through the graphical user interface.
 * - Verifying successful and failed operations for adding, updating, fetching, and deleting expenses.
 * - Ensuring correct validation and error handling for invalid input data.
 * - Using the AssertJSwingJUnitTestCase framework for GUI testing and Awaitility for asynchronous operations.
 * 
 * The databaseConfig variable is responsible for starting the Docker container.
 * If the test is run from Eclipse, it runs the Docker container using Testcontainers.
 * If the test is run using a Maven command, it starts a Docker container without test containers.
 * 
 * @see CategoryService
 * @see ExpenseService
 * @see CategoryDAO
 * @see ExpenseDAO
 * @see DatabaseConfig
 * @see DBConfig
 * @see MavenContainerConfig
 * @see TestContainerConfig
 */

package com.mycompany.pet.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.awaitility.Awaitility.await;

import java.awt.GraphicsEnvironment;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTableFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycompany.pet.DatabaseConfig;
import com.mycompany.pet.configurations.DBConfig;
import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.dao.ExpenseDAO;
import com.mycompany.pet.database.DatabaseConnection;
import com.mycompany.pet.database.DatabaseInitializer;
import com.mycompany.pet.model.Category;
import com.mycompany.pet.model.Expense;
import com.mycompany.pet.service.CategoryService;
import com.mycompany.pet.service.ExpenseService;

/**
 * The Class MainWindowIT.
 */
@RunWith(GUITestRunner.class)
public class MainWindowIT extends AssertJSwingJUnitTestCase {

	/** The category service. */
	private CategoryService categoryService;

	/** The expense service. */
	private ExpenseService expenseService;

	/** The main window. */
	private MainWindow mainWindow;

	/** The window. */
	private FrameFixture window;

	/** The database connection. */
	private DatabaseConnection databaseConnection;

	/**
	 * This variable is responsible for starting the Docker container. If the test
	 * is run from Eclipse, it runs the Docker container using Testcontainers. If
	 * the test is run using a Maven command, it starts a Docker container without
	 * test containers
	 */
	private static DBConfig databaseConfig;

	/** The category id 1. */
	private static final Integer CATEGORY_ID_1 = 1;

	/** The category name 1. */
	private static final String CATEGORY_NAME_1 = "Food";

	/** The category id 2. */
	private static final Integer CATEGORY_ID_2 = 2;

	/** The category name 2. */
	private static final String CATEGORY_NAME_2 = "Travel";

	/** The expense id 1. */
	private static final Integer EXPENSE_ID_1 = 1;

	/** The expense amount 1. */
	private static final BigDecimal EXPENSE_AMOUNT_1 = new BigDecimal("100.50");

	/** The expense description 1. */
	private static final String EXPENSE_DESCRIPTION_1 = "Lunch";

	/** The expense date 1. */
	private static final LocalDate EXPENSE_DATE_1 = LocalDate.now();

	/** The expense amount 2. */
	private static final BigDecimal EXPENSE_AMOUNT_2 = new BigDecimal("200.00");

	/** The expense description 2. */
	private static final String EXPENSE_DESCRIPTION_2 = "Dinner";

	/** The expense date 2. */
	private static final LocalDate EXPENSE_DATE_2 = LocalDate.now().minusDays(1);

	/** The category 1. */
	private Category category1;

	/** The category 2. */
	private Category category2;

	/**
	 * Setup server.
	 */
	@BeforeClass
	public static void setupServer() {
		// Skip UI tests if running in headless mode
		Assume.assumeFalse("Skipping UI test - running in headless mode", 
			GraphicsEnvironment.isHeadless());
		
		databaseConfig = DatabaseConfig.getDatabaseConfig();
		databaseConfig.testAndStartDatabaseConnection();
	}

	/**
	 * On set up.
	 *
	 * @throws Exception the exception
	 */
	@Override
	protected void onSetUp() throws Exception {
		databaseConnection = databaseConfig.getDatabaseConnection();
		
		// Initialize database
		DatabaseInitializer initializer = new DatabaseInitializer(databaseConnection);
		initializer.initialize();

		// Initialize DAOs and Services
		CategoryDAO categoryDAO = new CategoryDAO(databaseConnection);
		ExpenseDAO expenseDAO = new ExpenseDAO(databaseConnection);
		categoryService = new CategoryService(categoryDAO);
		expenseService = new ExpenseService(expenseDAO, categoryDAO);

		// Create test categories
		category1 = categoryService.createCategory(CATEGORY_NAME_1);
		category2 = categoryService.createCategory(CATEGORY_NAME_2);

		GuiActionRunner.execute(() -> {
			mainWindow = new MainWindow(categoryService, expenseService);
			return mainWindow;
		});

		window = new FrameFixture(robot(), mainWindow);
		window.show();
	}

	/**
	 * On tear down.
	 */
	@Override
	protected void onTearDown() {
		if (databaseConnection != null) {
			databaseConnection.close();
		}
	}

	/**
	 * Test all expenses are displayed.
	 */
	@Test
	@GUITest
	public void testAllExpensesAreDisplayed() throws SQLException {
		Expense expense1 = expenseService.createExpense(EXPENSE_DATE_1, EXPENSE_AMOUNT_1, 
			EXPENSE_DESCRIPTION_1, category1.getCategoryId());
		Expense expense2 = expenseService.createExpense(EXPENSE_DATE_2, EXPENSE_AMOUNT_2, 
			EXPENSE_DESCRIPTION_2, category2.getCategoryId());

		GuiActionRunner.execute(() -> {
			mainWindow.loadData();
		});

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = window.table();
			assertThat(table.rowCount()).isGreaterThanOrEqualTo(2);
			assertThat(table.cell(TableCell.row(0).column(3)).value()).contains(EXPENSE_DESCRIPTION_1);
			assertThat(table.cell(TableCell.row(1).column(3)).value()).contains(EXPENSE_DESCRIPTION_2);
		});
	}

	/**
	 * Test add expense success.
	 */
	@Test
	@GUITest
	public void testAddExpenseSuccess() throws SQLException {
		window.button(withText("Add Expense")).click();

		// Wait for dialog to appear
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			DialogFixture dialog = window.dialog();
			assertThat(dialog).isNotNull();
			
			// Fill in expense details - find text boxes by their order
			// Date field is typically first
			dialog.textBox().enterText(EXPENSE_DATE_1.toString());
			// Amount field
			dialog.textBox().enterText(EXPENSE_AMOUNT_1.toString());
			// Description field
			dialog.textBox().enterText(EXPENSE_DESCRIPTION_1);
			dialog.comboBox().selectItem(0); // Select first category
			
			// Click Save
			dialog.button(withText("Save")).click();
		});

		// Verify expense was added
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = window.table();
			assertThat(table.rowCount()).isGreaterThan(0);
			assertThat(table.cell(TableCell.row(0).column(3)).value()).contains(EXPENSE_DESCRIPTION_1);
		});
	}

	/**
	 * Test delete expense success.
	 */
	@Test
	@GUITest
	public void testDeleteExpenseSuccess() throws SQLException {
		Expense expense = expenseService.createExpense(EXPENSE_DATE_1, EXPENSE_AMOUNT_1, 
			EXPENSE_DESCRIPTION_1, category1.getCategoryId());

		GuiActionRunner.execute(() -> {
			mainWindow.loadData();
		});

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = window.table();
			table.selectRows(0);
			window.button(withText("Delete Expense")).click();
			
			// Confirm deletion in dialog
			DialogFixture confirmDialog = window.dialog();
			confirmDialog.button(withText("Yes")).click();
		});

		// Verify expense was deleted
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = window.table();
			// The expense should be removed from the table
			boolean found = false;
			for (int i = 0; i < table.rowCount(); i++) {
				if (table.cell(TableCell.row(i).column(0)).value().equals(expense.getExpenseId().toString())) {
					found = true;
					break;
				}
			}
			assertThat(found).isFalse();
		});
	}

	/**
	 * Test filter expenses by month.
	 */
	@Test
	@GUITest
	public void testFilterExpensesByMonth() throws SQLException {
		// Create expenses in different months
		expenseService.createExpense(EXPENSE_DATE_1, EXPENSE_AMOUNT_1, 
			EXPENSE_DESCRIPTION_1, category1.getCategoryId());
		expenseService.createExpense(EXPENSE_DATE_2, EXPENSE_AMOUNT_2, 
			EXPENSE_DESCRIPTION_2, category2.getCategoryId());

		GuiActionRunner.execute(() -> {
			try {
				mainWindow.loadData();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		// Select current month
		int currentMonth = LocalDate.now().getMonthValue();
		String monthStr = String.format("%02d", currentMonth);
		window.comboBox().selectItem(monthStr);

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = window.table();
			// Should show expenses from current month
			assertThat(table.rowCount()).isGreaterThan(0);
		});
	}

	/**
	 * Test category total calculation.
	 */
	@Test
	@GUITest
	public void testCategoryTotalCalculation() throws SQLException {
		expenseService.createExpense(EXPENSE_DATE_1, EXPENSE_AMOUNT_1, 
			EXPENSE_DESCRIPTION_1, category1.getCategoryId());
		expenseService.createExpense(EXPENSE_DATE_1, EXPENSE_AMOUNT_2, 
			EXPENSE_DESCRIPTION_2, category1.getCategoryId());

		GuiActionRunner.execute(() -> {
			try {
				mainWindow.loadData();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		// Select category from combo box
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			window.comboBox().selectItem(category1.getName());
		});

		// Verify category total is calculated
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			BigDecimal expectedTotal = EXPENSE_AMOUNT_1.add(EXPENSE_AMOUNT_2);
			String totalText = window.label().text();
			assertThat(totalText).contains(expectedTotal.toString());
		});
	}
}

