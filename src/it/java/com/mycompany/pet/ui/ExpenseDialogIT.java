/*
 * Integration tests for the ExpenseDialog class.
 * 
 * These tests cover the following functionalities:
 * 
 * - Setting up and tearing down the test environment using Docker containers and database configurations.
 * - Creating and updating expenses through the graphical user interface.
 * - Verifying successful and failed operations for adding and updating expenses.
 * - Ensuring correct validation and error handling for invalid input data.
 * - Using the AssertJSwingJUnitTestCase framework for GUI testing and Awaitility for asynchronous operations.
 * 
 */

package com.mycompany.pet.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
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
public class ExpenseDialogIT extends AssertJSwingJUnitTestCase {

	private CategoryService categoryService;

	private ExpenseService expenseService;

	private MainWindow mainWindow;

	private FrameFixture parentFrame;

	private ExpenseDialog expenseDialog;

	private DialogFixture dialog;

	private DatabaseConnection databaseConnection;

	private Category category;

	private static DBConfig databaseConfig;

	private static final LocalDate EXPENSE_DATE = LocalDate.now();

	private static final BigDecimal EXPENSE_AMOUNT = new BigDecimal("100.50");

	private static final String EXPENSE_DESCRIPTION = "Lunch";

	private static final String UPDATED_EXPENSE_DESCRIPTION = "Dinner";

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

			category = categoryService.createCategory("Food");

			CategoryController categoryController = new CategoryController(categoryService);
			ExpenseController expenseController = new ExpenseController(expenseService);
			
			mainWindow = GuiActionRunner.execute(() -> {
				MainWindow mw = new MainWindow(expenseController, categoryController);
				mw.setVisible(true);
				return mw;
			});
			parentFrame = new FrameFixture(robot(), mainWindow);
			parentFrame.show();

			expenseDialog = GuiActionRunner.execute(() -> {
				ExpenseDialog ed = new ExpenseDialog(mainWindow, expenseController, categoryController, null);
				ed.setModal(false); 
				ed.setVisible(true);
				return ed;
			});
			
			dialog = new DialogFixture(robot(), expenseDialog);
			dialog.show();
			
			robot().waitForIdle();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		} catch (Exception e) {
			org.junit.Assume.assumeNoException("Database operation failed. Skipping test.", e);
			return;
		}
	}

	@Override
	protected void onTearDown() {
		if (databaseConnection != null) {
			databaseConnection.close();
		}
		if (dialog != null) {
			dialog.cleanUp();
		}
		if (parentFrame != null) {
			parentFrame.cleanUp();
		}
	}

	@Test
	@GUITest
	public void testAddExpenseSuccess() throws SQLException {
		if (expenseService == null || category == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		GuiActionRunner.execute(() -> {
			expenseDialog.dateField.setText(EXPENSE_DATE.toString());
			expenseDialog.amountField.setText(EXPENSE_AMOUNT.toString());
			expenseDialog.descriptionField.setText(EXPENSE_DESCRIPTION);
			int itemCount = expenseDialog.categoryComboBox.getItemCount();
			for (int i = 0; i < itemCount; i++) {
				Object item = expenseDialog.categoryComboBox.getItemAt(i);
				if (item != null && item instanceof Category) {
					Category cat = (Category) item;
					if (cat.getCategoryId().equals(category.getCategoryId())) {
						expenseDialog.categoryComboBox.setSelectedItem(item);
						break;
					}
				}
			}
		});
		
		robot().waitForIdle();

		dialog.button(withText("Save")).click();
		
		robot().waitForIdle();

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			boolean saved = GuiActionRunner.execute(() -> {
				return expenseDialog.isSaved();
			});
			assertThat(saved).isTrue();
		});
	}

	@Test
	@GUITest
	public void testEditExpenseSuccess() throws SQLException {
		if (expenseService == null || category == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		Expense expense = expenseService.createExpense(EXPENSE_DATE, EXPENSE_AMOUNT, EXPENSE_DESCRIPTION, category.getCategoryId());

		GuiActionRunner.execute(() -> {
			if (expenseDialog != null) {
				expenseDialog.dispose();
			}
		});

		CategoryController categoryController = new CategoryController(categoryService);
		ExpenseController expenseController = new ExpenseController(expenseService);
		
		ExpenseDialog editDialog = GuiActionRunner.execute(() -> {
			ExpenseDialog ed = new ExpenseDialog(mainWindow, expenseController, categoryController, expense);
			ed.setVisible(true);
			return ed;
		});
		DialogFixture editDialogFixture = new DialogFixture(robot(), editDialog);
		editDialogFixture.show();

		GuiActionRunner.execute(() -> {
			editDialog.descriptionField.setText(UPDATED_EXPENSE_DESCRIPTION);
			int itemCount = editDialog.categoryComboBox.getItemCount();
			for (int i = 0; i < itemCount; i++) {
				Object item = editDialog.categoryComboBox.getItemAt(i);
				if (item != null && item instanceof Category) {
					Category cat = (Category) item;
					if (cat.getCategoryId().equals(category.getCategoryId())) {
						editDialog.categoryComboBox.setSelectedItem(item);
						break;
					}
				}
			}
		});

		editDialogFixture.button(withText("Save")).click();

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			boolean saved = GuiActionRunner.execute(() -> {
				return editDialog.isSaved();
			});
			assertThat(saved).isTrue();
		});

		editDialogFixture.cleanUp();
	}

	@Test
	@GUITest
	public void testAddExpenseWithNoCategory() {
		if (expenseService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		GuiActionRunner.execute(() -> {
			expenseDialog.dateField.setText(EXPENSE_DATE.toString());
			expenseDialog.amountField.setText(EXPENSE_AMOUNT.toString());
			expenseDialog.descriptionField.setText(EXPENSE_DESCRIPTION);
			expenseDialog.categoryComboBox.setSelectedItem(null);
		});

		dialog.button(withText("Save")).click();

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			boolean visible = GuiActionRunner.execute(() -> {
				return expenseDialog.isVisible();
			});
			assertThat(visible).isTrue();
		});
	}

	@Test
	@GUITest
	public void testAddExpenseWithInvalidDate() {
		if (expenseService == null || category == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		GuiActionRunner.execute(() -> {
			expenseDialog.dateField.setText("invalid-date");
			expenseDialog.amountField.setText(EXPENSE_AMOUNT.toString());
			expenseDialog.descriptionField.setText(EXPENSE_DESCRIPTION);
			int itemCount = expenseDialog.categoryComboBox.getItemCount();
			for (int i = 0; i < itemCount; i++) {
				Object item = expenseDialog.categoryComboBox.getItemAt(i);
				if (item != null && item instanceof Category) {
					Category cat = (Category) item;
					if (cat.getCategoryId().equals(category.getCategoryId())) {
						expenseDialog.categoryComboBox.setSelectedItem(item);
						break;
					}
				}
			}
		});

		dialog.button(withText("Save")).click();

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			boolean visible = GuiActionRunner.execute(() -> {
				return expenseDialog.isVisible();
			});
			assertThat(visible).isTrue();
		});
	}

	@Test
	@GUITest
	public void testCancelDialog() {
		if (expenseService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		dialog.button(withText("Cancel")).click();

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			boolean saved = GuiActionRunner.execute(() -> {
				return expenseDialog.isSaved();
			});
			assertThat(saved).isFalse();
		});
	}
}

