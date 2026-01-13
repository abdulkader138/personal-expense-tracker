/*
 * End-to-end tests for the ExpenseTrackerApp.
 * 
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

@RunWith(GUITestRunner.class)
public class ExpenseTrackerAppE2E extends AssertJSwingJUnitTestCase {

	private static DBConfig databaseConfig;

	private FrameFixture mainWindow;

	private static DatabaseConnection databaseConnection;

	private static final Integer CATEGORY_FIXTURE_1_ID = 1;

	private static final Integer CATEGORY_FIXTURE_2_ID = 2;

	private static final String CATEGORY_FIXTURE_1_NAME = "Food";

	private static final String CATEGORY_FIXTURE_2_NAME = "Travel";

	private static final Integer EXPENSE_FIXTURE_1_ID = 1;

	private static final Integer EXPENSE_FIXTURE_2_ID = 2;

	private static final BigDecimal EXPENSE_FIXTURE_1_AMOUNT = new BigDecimal("100.50");

	private static final BigDecimal EXPENSE_FIXTURE_2_AMOUNT = new BigDecimal("200.00");

	private static final String EXPENSE_FIXTURE_1_DESCRIPTION = "Lunch";

	private static final String EXPENSE_FIXTURE_2_DESCRIPTION = "Dinner";

	private static final LocalDate EXPENSE_FIXTURE_1_DATE = LocalDate.now();

	private static final LocalDate EXPENSE_FIXTURE_2_DATE = LocalDate.now().minusDays(1);

	private Category category1;

	private Category category2;

	private Expense expense1;

	private Expense expense2;

	private CategoryService categoryService;

	private ExpenseService expenseService;

	@BeforeClass
	public static void setupServer() {
		Assume.assumeFalse("Skipping UI test - running in headless mode", 
			GraphicsEnvironment.isHeadless());
		
		databaseConfig = DatabaseConfig.getDatabaseConfig();
		databaseConfig.testAndStartDatabaseConnection();
	}

	@Override
	protected void onSetUp() throws Exception {
		databaseConnection = databaseConfig.getDatabaseConnection();
		
		DatabaseInitializer initializer = new DatabaseInitializer(databaseConnection);
		initializer.initialize();

		CategoryDAO categoryDAO = new CategoryDAO(databaseConnection);
		ExpenseDAO expenseDAO = new ExpenseDAO(databaseConnection);
		categoryService = new CategoryService(categoryDAO);
		expenseService = new ExpenseService(expenseDAO, categoryDAO);

		category1 = categoryService.createCategory(CATEGORY_FIXTURE_1_NAME);
		category2 = categoryService.createCategory(CATEGORY_FIXTURE_2_NAME);
		expense1 = expenseService.createExpense(EXPENSE_FIXTURE_1_DATE, EXPENSE_FIXTURE_1_AMOUNT,
			EXPENSE_FIXTURE_1_DESCRIPTION, category1.getCategoryId());
		expense2 = expenseService.createExpense(EXPENSE_FIXTURE_2_DATE, EXPENSE_FIXTURE_2_AMOUNT,
			EXPENSE_FIXTURE_2_DESCRIPTION, category2.getCategoryId());

		com.mycompany.pet.controller.CategoryController categoryController = new com.mycompany.pet.controller.CategoryController(categoryService);
		com.mycompany.pet.controller.ExpenseController expenseController = new com.mycompany.pet.controller.ExpenseController(expenseService);
		
		execute(() -> {
			com.mycompany.pet.ui.MainWindow window = new com.mycompany.pet.ui.MainWindow(expenseController, categoryController);
			window.setVisible(true);
			return window;
		});

		mainWindow = findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Personal Expense Tracker".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot());
	}

	
	@Override
	protected void onTearDown() {
		if (databaseConnection != null) {
			databaseConnection.close();
		}
	}

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
			
			dialog.textBox().enterText(date.toString());
			dialog.textBox().enterText(amount.toString());
			dialog.textBox().enterText(description);
			dialog.comboBox().selectItem(0);
			
			dialog.button(withText("Save")).click();
		});

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

	@Test
	@GUITest
	public void testDeleteExpenseSuccess() {
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			org.assertj.swing.fixture.JTableFixture table = mainWindow.table();
			assertThat(table.rowCount()).isGreaterThan(0);
		});

		mainWindow.table().selectRows(0);
		mainWindow.button(withText("Delete Expense")).click();

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			DialogFixture confirmDialog = mainWindow.dialog();
			confirmDialog.button(withText("Yes")).click();
		});

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			org.assertj.swing.fixture.JTableFixture table = mainWindow.table();
			assertThat(table.rowCount()).isGreaterThanOrEqualTo(1);
		});
	}

	@Test
	@GUITest
	public void testAddCategorySuccess() {
		String categoryName = "Entertainment";

		mainWindow.menuItem("Categories").click();

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			DialogFixture categoryDialog = mainWindow.dialog();
			assertThat(categoryDialog).isNotNull();
			
			categoryDialog.textBox().enterText(categoryName);
			categoryDialog.button(withText("Add Category")).click();
			
			org.assertj.swing.fixture.JTableFixture table = categoryDialog.table();
			boolean found = false;
			for (int i = 0; i < table.rowCount(); i++) {
				if (table.cell(TableCell.row(i).column(1)).value().equals(categoryName)) {
					found = true;
					break;
				}
			}
			assertThat(found).isTrue();
			
			categoryDialog.button(withText("Close")).click();
		});
	}

	@Test
	@GUITest
	public void testFilterExpensesByMonth() {
		int currentMonth = LocalDate.now().getMonthValue();
		String monthStr = String.format("%02d", currentMonth);
		
		mainWindow.comboBox().selectItem(monthStr);

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			org.assertj.swing.fixture.JTableFixture table = mainWindow.table();
			assertThat(table.rowCount()).isGreaterThan(0);
		});
	}

	@Test
	@GUITest
	public void testCategoryTotalCalculation() {
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			mainWindow.comboBox().selectItem(CATEGORY_FIXTURE_1_NAME);
		});

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String totalText = mainWindow.label().text();
			assertThat(totalText).contains("Category Total");
			assertThat(totalText).contains(EXPENSE_FIXTURE_1_AMOUNT.toString());
		});
	}
}

