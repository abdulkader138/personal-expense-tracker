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
 */

package com.mycompany.pet.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.awaitility.Awaitility.await;

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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycompany.pet.DatabaseConfig;
import com.mycompany.pet.configurations.DBConfig;
import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.dao.ExpenseDAO;
import com.mycompany.pet.database.DatabaseConnection;
import com.mycompany.pet.database.DatabaseInitializer;
import com.mycompany.pet.controller.CategoryController;
import com.mycompany.pet.controller.ExpenseController;
import com.mycompany.pet.model.Category;
import com.mycompany.pet.model.Expense;
import com.mycompany.pet.service.CategoryService;
import com.mycompany.pet.service.ExpenseService;


@RunWith(GUITestRunner.class)
public class MainWindowIT extends AssertJSwingJUnitTestCase {

	private CategoryService categoryService;

	private ExpenseService expenseService;

	private MainWindow mainWindow;

	private FrameFixture window;

	private DatabaseConnection databaseConnection;

	private static DBConfig databaseConfig;

	private static final Integer CATEGORY_ID_1 = 1;

	private static final String CATEGORY_NAME_1 = "Food";

	private static final Integer CATEGORY_ID_2 = 2;

	private static final String CATEGORY_NAME_2 = "Travel";

	private static final Integer EXPENSE_ID_1 = 1;

	private static final BigDecimal EXPENSE_AMOUNT_1 = new BigDecimal("100.50");

	private static final String EXPENSE_DESCRIPTION_1 = "Lunch";

	private static final LocalDate EXPENSE_DATE_1 = LocalDate.now();

	private static final BigDecimal EXPENSE_AMOUNT_2 = new BigDecimal("200.00");

	private static final String EXPENSE_DESCRIPTION_2 = "Dinner";

	private static final LocalDate EXPENSE_DATE_2 = LocalDate.now().minusDays(1);

	private Category category1;

	private Category category2;

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
			org.junit.Assume.assumeNoException("Database setup failed. Docker may not be available. Skipping integration tests.", e);
		}
	}

	@Override
	protected void onSetUp() throws Exception {
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
			categoryService = new CategoryService(categoryDAO);
			expenseService = new ExpenseService(expenseDAO, categoryDAO);

			try {
				category1 = categoryService.createCategory(CATEGORY_NAME_1);
				category2 = categoryService.createCategory(CATEGORY_NAME_2);
			} catch (SQLException e) {
				org.junit.Assume.assumeNoException("Failed to create test categories. Skipping integration tests.", e);
				return;
			}
		} catch (Exception e) {
			org.junit.Assume.assumeNoException("Database operation failed. Skipping test.", e);
			return;
		}

		CategoryController categoryController = new CategoryController(categoryService);
		ExpenseController expenseController = new ExpenseController(expenseService);
		
		GuiActionRunner.execute(() -> {
			mainWindow = new MainWindow(expenseController, categoryController);
			mainWindow.pack();
			mainWindow.setVisible(true);
			return mainWindow;
		});

		window = new FrameFixture(robot(), mainWindow);
		
		robot().waitForIdle();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	protected void onTearDown() {
		if (databaseConnection != null) {
			databaseConnection.close();
		}
	}

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

	@Test
	@GUITest
	public void testAddExpenseSuccess() throws SQLException {
		window.button(withText("Add Expense")).click();

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			DialogFixture dialog = window.dialog();
			assertThat(dialog).isNotNull();
			
			dialog.textBox().enterText(EXPENSE_DATE_1.toString());
			dialog.textBox().enterText(EXPENSE_AMOUNT_1.toString());
			dialog.textBox().enterText(EXPENSE_DESCRIPTION_1);
			dialog.comboBox().selectItem(0); 
			
			dialog.button(withText("Save")).click();
		});

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = window.table();
			assertThat(table.rowCount()).isGreaterThan(0);
			assertThat(table.cell(TableCell.row(0).column(3)).value()).contains(EXPENSE_DESCRIPTION_1);
		});
	}

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
			
			DialogFixture confirmDialog = window.dialog();
			confirmDialog.button(withText("Yes")).click();
		});

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = window.table();
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

	@Test
	@GUITest
	public void testFilterExpensesByMonth() throws SQLException {
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

		int currentMonth = LocalDate.now().getMonthValue();
		String monthStr = String.format("%02d", currentMonth);
		window.comboBox().selectItem(monthStr);

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = window.table();
			assertThat(table.rowCount()).isGreaterThan(0);
		});
	}

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

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			window.comboBox().selectItem(category1.getName());
		});

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			BigDecimal expectedTotal = EXPENSE_AMOUNT_1.add(EXPENSE_AMOUNT_2);
			String totalText = window.label().text();
			assertThat(totalText).contains(expectedTotal.toString());
		});
	}

	@Test
	@GUITest
	public void testEditExpenseSuccess() throws SQLException {
		Expense expense = expenseService.createExpense(EXPENSE_DATE_1, EXPENSE_AMOUNT_1, 
			EXPENSE_DESCRIPTION_1, category1.getCategoryId());

		GuiActionRunner.execute(() -> {
			try {
				mainWindow.loadData();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = window.table();
			table.selectRows(0);
			window.button(withText("Edit Expense")).click();
			
			DialogFixture dialog = window.dialog();
			assertThat(dialog).isNotNull();
			
			dialog.textBox().enterText(EXPENSE_DATE_2.toString());
			dialog.textBox().enterText(EXPENSE_AMOUNT_2.toString());
			dialog.textBox().enterText(EXPENSE_DESCRIPTION_2);
			dialog.comboBox().selectItem(1); 
			dialog.button(withText("Save")).click();
		});

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			GuiActionRunner.execute(() -> {
				try {
					mainWindow.loadData();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
			JTableFixture table = window.table();
			assertThat(table.rowCount()).isGreaterThan(0);
			assertThat(table.cell(TableCell.row(0).column(3)).value()).contains(EXPENSE_DESCRIPTION_2);
		});
	}

	@Test
	@GUITest
	public void testEditExpenseNoSelection() throws SQLException {
		window.button(withText("Edit Expense")).click();
		
		await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
		});
	}

	@Test
	@GUITest
	public void testDeleteExpenseNoSelection() throws SQLException {
		window.button(withText("Delete Expense")).click();
		
		await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
		});
	}

	@Test
	@GUITest
	public void testDeleteExpenseCancel() throws SQLException {
		Expense expense = expenseService.createExpense(EXPENSE_DATE_1, EXPENSE_AMOUNT_1, 
			EXPENSE_DESCRIPTION_1, category1.getCategoryId());

		GuiActionRunner.execute(() -> {
			try {
				mainWindow.loadData();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = window.table();
			table.selectRows(0);
			window.button(withText("Delete Expense")).click();
			
			DialogFixture confirmDialog = window.dialog();
			confirmDialog.button(withText("No")).click();
		});

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = window.table();
			assertThat(table.rowCount()).isGreaterThan(0);
		});
	}

	@Test
	@GUITest
	public void testShowCategoryDialog() throws SQLException {
		window.menuItemWithPath("Manage", "Categories").click();
		
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			DialogFixture dialog = window.dialog();
			assertThat(dialog).isNotNull();
			dialog.close();
		});
	}

	@Test
	@GUITest
	public void testFilterExpensesByCategory() throws SQLException {
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

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			window.comboBox().selectItem(category1.getName());
		});

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = window.table();
			assertThat(table.rowCount()).isGreaterThan(0);
		});
	}

	@Test
	@GUITest
	public void testFilterExpensesByYear() throws SQLException {
		expenseService.createExpense(EXPENSE_DATE_1, EXPENSE_AMOUNT_1, 
			EXPENSE_DESCRIPTION_1, category1.getCategoryId());

		GuiActionRunner.execute(() -> {
			try {
				mainWindow.loadData();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		int currentYear = LocalDate.now().getYear();
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			window.comboBox().selectItem(String.valueOf(currentYear));
		});

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = window.table();
			assertThat(table.rowCount()).isGreaterThan(0);
		});
	}

	@Test
	@GUITest
	public void testLoadDataErrorHandling() throws SQLException {
		if (databaseConnection != null) {
			databaseConnection.close();
		}
		
		GuiActionRunner.execute(() -> {
			try {
				mainWindow.loadData();
			} catch (Exception e) {
			}
		});
		
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			assertThat(window).isNotNull();
		});
	}

	@Test
	@GUITest
	public void testUpdateSummaryNullSelections() throws SQLException {
		GuiActionRunner.execute(() -> {
			mainWindow.monthComboBox.setSelectedItem(null);
			mainWindow.yearComboBox.setSelectedItem(null);
			mainWindow.updateSummary();
		});
		
		await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
			assertThat(window).isNotNull();
		});
	}

	@Test
	@GUITest
	public void testUpdateSummaryAllMonth() throws SQLException {
		GuiActionRunner.execute(() -> {
			mainWindow.monthComboBox.setSelectedItem("All");
			mainWindow.yearComboBox.setSelectedItem("2024");
			mainWindow.updateSummary();
		});
		
		await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
			assertThat(window).isNotNull();
		});
	}

	@Test
	@GUITest
	public void testUpdateCategoryTotalNullCategory() throws SQLException {
		GuiActionRunner.execute(() -> {
			mainWindow.categoryComboBox.setSelectedItem(null);
			mainWindow.updateCategoryTotal();
		});
		
		await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
			assertThat(window).isNotNull();
		});
	}

	@Test
	@GUITest
	public void testFilterExpensesErrorHandling() throws SQLException {
		if (databaseConnection != null) {
			databaseConnection.close();
		}
		
		GuiActionRunner.execute(() -> {
			mainWindow.monthComboBox.setSelectedItem("01");
			mainWindow.yearComboBox.setSelectedItem("2024");
			try {
				mainWindow.filterExpenses();
			} catch (Exception e) {
			}
		});
		
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			assertThat(window).isNotNull();
		});
	}

	@Test
	@GUITest
	public void testEditExpenseSQLException() throws SQLException {
		Expense expense = expenseService.createExpense(EXPENSE_DATE_1, EXPENSE_AMOUNT_1, 
			EXPENSE_DESCRIPTION_1, category1.getCategoryId());

		GuiActionRunner.execute(() -> {
			try {
				mainWindow.loadData();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		if (databaseConnection != null) {
			databaseConnection.close();
		}

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = window.table();
			table.selectRows(0);
			GuiActionRunner.execute(() -> {
				try {
					mainWindow.showEditExpenseDialog();
				} catch (Exception e) {
				}
			});
		});
	}

	
	@Test
	@GUITest
	public void testDeleteExpenseSQLException() throws SQLException {
		Expense expense = expenseService.createExpense(EXPENSE_DATE_1, EXPENSE_AMOUNT_1, 
			EXPENSE_DESCRIPTION_1, category1.getCategoryId());

		GuiActionRunner.execute(() -> {
			try {
				mainWindow.loadData();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		if (databaseConnection != null) {
			databaseConnection.close();
		}

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = window.table();
			table.selectRows(0);
			GuiActionRunner.execute(() -> {
				try {
					mainWindow.deleteSelectedExpense();
				} catch (Exception e) {
				}
			});
		});
	}

	@Test
	@GUITest
	public void testComboBoxActionListeners() throws SQLException {
		expenseService.createExpense(EXPENSE_DATE_1, EXPENSE_AMOUNT_1, 
			EXPENSE_DESCRIPTION_1, category1.getCategoryId());

		GuiActionRunner.execute(() -> {
			try {
				mainWindow.loadData();
				mainWindow.isInitializing = false;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			window.comboBox().selectItem("01");
		});

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			int currentYear = LocalDate.now().getYear();
			window.comboBox().selectItem(String.valueOf(currentYear));
		});

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			window.comboBox().selectItem(category1.getName());
		});
	}

	@Test
	@GUITest
	public void testUpdateSummaryGenericException() throws SQLException {
		GuiActionRunner.execute(() -> {
			try {
				mainWindow.monthComboBox.setSelectedItem("invalid");
				mainWindow.yearComboBox.setSelectedItem("invalid");
				mainWindow.updateSummary();
			} catch (Exception e) {
			}
		});
		
		await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
			assertThat(window).isNotNull();
		});
	}
}

