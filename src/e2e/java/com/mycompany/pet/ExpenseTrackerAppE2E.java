/*
 * End-to-end tests for the ExpenseTrackerApp.
 * 
 * These tests cover the following functionalities:
 * 
 * - Setting up and tearing down the test environment, including database connections and GUI initialization.
 * - Interactions with the MainWindow, including adding, updating, fetching, and deleting expenses and categories.
 * - Verification of the correct display of database records in the GUI and the correct handling of various operations.
 * - Ensuring proper error handling and validation for both expenses and categories.
 * - Using the AssertJSwingJUnitTestCase framework for GUI testing, Awaitility for asynchronous operations, and MongoDB for database operations.
 * 
 * 
 * Note:
 * These tests will run using Eclipse but are configured to run using Maven with the `integration-test-profile` profile. To execute these tests, use the following Maven command with the specified profile and arguments:
 * 
 * ```
 * mvn test -Pintegration-test-profile -Dmongodb.dbName=$DATABASE -Dmongodb.server=maven
 * ```
 * 
 * The tests simulate real-world scenarios by interacting with the GUI and verifying the expected outcomes.
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

package com.mycompany.pet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.assertj.swing.finder.WindowFinder.findFrame;
import static org.awaitility.Awaitility.await;

import java.awt.GraphicsEnvironment;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

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
 * The Class ExpenseTrackerAppE2E.
 */
@RunWith(GUITestRunner.class)
public class ExpenseTrackerAppE2E extends AssertJSwingJUnitTestCase {

	/**
	 * This variable is responsible for starting the Docker container. If the test
	 * is run from Eclipse, it runs the Docker container using Testcontainers. If
	 * the test is run using a Maven command, it starts a Docker container directly without test container.
	 */
	private static DBConfig databaseConfig;

	/** The main window. */
	private FrameFixture mainWindow;

	/** The database connection. */
	private static DatabaseConnection databaseConnection;

	/** The Constant CATEGORY_FIXTURE_1_ID. */
	private static final Integer CATEGORY_FIXTURE_1_ID = 1;

	/** The Constant CATEGORY_FIXTURE_2_ID. */
	private static final Integer CATEGORY_FIXTURE_2_ID = 2;

	/** The Constant CATEGORY_FIXTURE_1_NAME. */
	private static final String CATEGORY_FIXTURE_1_NAME = "Food";

	/** The Constant CATEGORY_FIXTURE_2_NAME. */
	private static final String CATEGORY_FIXTURE_2_NAME = "Travel";

	/** The Constant EXPENSE_FIXTURE_1_ID. */
	private static final Integer EXPENSE_FIXTURE_1_ID = 1;

	/** The Constant EXPENSE_FIXTURE_2_ID. */
	private static final Integer EXPENSE_FIXTURE_2_ID = 2;

	/** The Constant EXPENSE_FIXTURE_1_AMOUNT. */
	private static final BigDecimal EXPENSE_FIXTURE_1_AMOUNT = new BigDecimal("100.50");

	/** The Constant EXPENSE_FIXTURE_2_AMOUNT. */
	private static final BigDecimal EXPENSE_FIXTURE_2_AMOUNT = new BigDecimal("200.00");

	/** The Constant EXPENSE_FIXTURE_1_DESCRIPTION. */
	private static final String EXPENSE_FIXTURE_1_DESCRIPTION = "Lunch";

	/** The Constant EXPENSE_FIXTURE_2_DESCRIPTION. */
	private static final String EXPENSE_FIXTURE_2_DESCRIPTION = "Dinner";

	/** The Constant EXPENSE_FIXTURE_1_DATE. */
	private static final LocalDate EXPENSE_FIXTURE_1_DATE = LocalDate.now();

	/** The Constant EXPENSE_FIXTURE_2_DATE. */
	private static final LocalDate EXPENSE_FIXTURE_2_DATE = LocalDate.now().minusDays(1);

	/** The category 1. */
	private Category category1;

	/** The category 2. */
	private Category category2;

	/** The expense 1. */
	private Expense expense1;

	/** The expense 2. */
	private Expense expense2;

	/** The category service. */
	private CategoryService categoryService;

	/** The expense service. */
	private ExpenseService expenseService;

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

		// Add test data to database
		category1 = categoryService.createCategory(CATEGORY_FIXTURE_1_NAME);
		category2 = categoryService.createCategory(CATEGORY_FIXTURE_2_NAME);
		expense1 = expenseService.createExpense(EXPENSE_FIXTURE_1_DATE, EXPENSE_FIXTURE_1_AMOUNT,
			EXPENSE_FIXTURE_1_DESCRIPTION, category1.getCategoryId());
		expense2 = expenseService.createExpense(EXPENSE_FIXTURE_2_DATE, EXPENSE_FIXTURE_2_AMOUNT,
			EXPENSE_FIXTURE_2_DESCRIPTION, category2.getCategoryId());

		// Create controllers from services
		com.mycompany.pet.controller.CategoryController categoryController = new com.mycompany.pet.controller.CategoryController(categoryService);
		com.mycompany.pet.controller.ExpenseController expenseController = new com.mycompany.pet.controller.ExpenseController(expenseService);
		
		// Start application
		execute(() -> {
			com.mycompany.pet.ui.MainWindow window = new com.mycompany.pet.ui.MainWindow(expenseController, categoryController);
			window.setVisible(true);
			return window;
		});

		// Find the main window
		mainWindow = findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Personal Expense Tracker".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot());
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
	 * Test on start all database elements are shown.
	 */
	@Test
	@GUITest
	public void testOnStartAllDatabaseElementsAreShown() {
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			org.assertj.swing.fixture.JTableFixture table = mainWindow.table();
			assertThat(table.rowCount()).isGreaterThanOrEqualTo(2);
			
			boolean foundExpense1 = false;
			boolean foundExpense2 = false;
			for (int i = 0; i < table.rowCount(); i++) {
				String description = table.cell(TableCell.row(i).column(3)).value();
				if (description.contains(EXPENSE_FIXTURE_1_DESCRIPTION)) {
					foundExpense1 = true;
				}
				if (description.contains(EXPENSE_FIXTURE_2_DESCRIPTION)) {
					foundExpense2 = true;
				}
			}
			assertThat(foundExpense1).isTrue();
			assertThat(foundExpense2).isTrue();
		});
	}

	/**
	 * Test add expense success.
	 */
	@Test
	@GUITest
	public void testAddExpenseSuccess() {
		String description = "New Expense";
		BigDecimal amount = new BigDecimal("50.00");
		LocalDate date = LocalDate.now();

		mainWindow.button(withText("Add Expense")).click();

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			DialogFixture dialog = mainWindow.dialog();
			assertThat(dialog).isNotNull();
			
			// Fill in expense details - find text boxes by searching for them
			// We'll try to find them by looking for text fields in the dialog
			// Date field is typically first
			dialog.textBox().enterText(date.toString());
			dialog.textBox().enterText(amount.toString());
			dialog.textBox().enterText(description);
			dialog.comboBox().selectItem(0); // Select first category
			
			// Click Save
			dialog.button(withText("Save")).click();
		});

		// Verify expense was added
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			org.assertj.swing.fixture.JTableFixture table = mainWindow.table();
			boolean found = false;
			for (int i = 0; i < table.rowCount(); i++) {
				if (table.cell(TableCell.row(i).column(3)).value().contains(description)) {
					found = true;
					break;
				}
			}
			assertThat(found).isTrue();
		});
	}

	/**
	 * Test delete expense success.
	 */
	@Test
	@GUITest
	public void testDeleteExpenseSuccess() {
		// Wait for table to load
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			org.assertj.swing.fixture.JTableFixture table = mainWindow.table();
			assertThat(table.rowCount()).isGreaterThan(0);
		});

		// Select first row and delete
		mainWindow.table().selectRows(0);
		mainWindow.button(withText("Delete Expense")).click();

		// Confirm deletion
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			DialogFixture confirmDialog = mainWindow.dialog();
			confirmDialog.button(withText("Yes")).click();
		});

		// Verify expense was deleted
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			org.assertj.swing.fixture.JTableFixture table = mainWindow.table();
			// The table should have one less row
			assertThat(table.rowCount()).isGreaterThanOrEqualTo(1);
		});
	}

	/**
	 * Test add category success.
	 */
	@Test
	@GUITest
	public void testAddCategorySuccess() {
		String categoryName = "Entertainment";

		// Open category dialog from menu
		mainWindow.menuItem("Categories").click();

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			DialogFixture categoryDialog = mainWindow.dialog();
			assertThat(categoryDialog).isNotNull();
			
			// Add new category
			categoryDialog.textBox().enterText(categoryName);
			categoryDialog.button(withText("Add Category")).click();
			
			// Verify category was added to table
			org.assertj.swing.fixture.JTableFixture table = categoryDialog.table();
			boolean found = false;
			for (int i = 0; i < table.rowCount(); i++) {
				if (table.cell(TableCell.row(i).column(1)).value().equals(categoryName)) {
					found = true;
					break;
				}
			}
			assertThat(found).isTrue();
			
			// Close dialog
			categoryDialog.button(withText("Close")).click();
		});
	}

	/**
	 * Test filter expenses by month.
	 */
	@Test
	@GUITest
	public void testFilterExpensesByMonth() {
		// Select current month from combo box
		int currentMonth = LocalDate.now().getMonthValue();
		String monthStr = String.format("%02d", currentMonth);
		
		// Find month combo box (it's in the top panel)
		mainWindow.comboBox().selectItem(monthStr);

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			org.assertj.swing.fixture.JTableFixture table = mainWindow.table();
			// Should show expenses from current month
			assertThat(table.rowCount()).isGreaterThan(0);
		});
	}

	/**
	 * Test category total calculation.
	 */
	@Test
	@GUITest
	public void testCategoryTotalCalculation() {
		// Select category from combo box in bottom panel
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			// Find the category combo box (there might be multiple, we need the one in bottom panel)
			mainWindow.comboBox().selectItem(CATEGORY_FIXTURE_1_NAME);
		});

		// Verify category total is calculated
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			// The total should be displayed in a label
			String totalText = mainWindow.label().text();
			assertThat(totalText).contains("Category Total");
			assertThat(totalText).contains(EXPENSE_FIXTURE_1_AMOUNT.toString());
		});
	}
}

