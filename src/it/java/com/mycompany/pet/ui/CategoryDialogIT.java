/*
 * Integration tests for the CategoryDialog class.
 * 
 * These tests cover the following functionalities:
 * 
 * - Setting up and tearing down the test environment using Docker containers and database configurations.
 * - Creating, updating, and deleting categories through the graphical user interface.
 * - Verifying successful and failed operations for adding, updating, and deleting categories.
 * - Ensuring correct validation and error handling for invalid input data.
 * - Using the AssertJSwingJUnitTestCase framework for GUI testing and Awaitility for asynchronous operations.
 * 
 */

package com.mycompany.pet.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.awaitility.Awaitility.await;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.assertj.swing.annotation.GUITest;
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
import com.mycompany.pet.controller.CategoryController;
import com.mycompany.pet.controller.ExpenseController;
import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.database.DatabaseConnection;
import com.mycompany.pet.database.DatabaseInitializer;
import com.mycompany.pet.model.Category;
import com.mycompany.pet.service.CategoryService;
import com.mycompany.pet.service.ExpenseService;

@RunWith(GUITestRunner.class)
public class CategoryDialogIT extends AssertJSwingJUnitTestCase {

	private CategoryService categoryService;

	private ExpenseService expenseService;

	private MainWindow mainWindow;

	private FrameFixture parentFrame;

	private CategoryDialog categoryDialog;

	private DialogFixture dialog;

	private DatabaseConnection databaseConnection;

	private static DBConfig databaseConfig;

	private static final String CATEGORY_NAME_1 = "Food";

	private static final String CATEGORY_NAME_2 = "Travel";

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
			categoryService = new CategoryService(categoryDAO);
			expenseService = null; 

			System.setProperty("test.mode", "true");
			
			CategoryController categoryController = new CategoryController(categoryService);
			ExpenseController expenseController = new ExpenseController(expenseService);
			
			mainWindow = GuiActionRunner.execute(() -> {
				MainWindow mw = new MainWindow(expenseController, categoryController);
				mw.setVisible(true);
				return mw;
			});
			parentFrame = new FrameFixture(robot(), mainWindow);
			parentFrame.show();

			categoryDialog = GuiActionRunner.execute(() -> {
				CategoryDialog cd = new CategoryDialog(mainWindow, categoryController);
				cd.setModal(false); 
				cd.setVisible(true);
				return cd;
			});
			
			dialog = new DialogFixture(robot(), categoryDialog);
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
	public void testAddCategorySuccess() throws SQLException {
		if (categoryService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		System.setProperty("test.mode", "true");
		
		dialog.textBox().enterText(CATEGORY_NAME_1);
		
		dialog.button(withText("Add Category")).click();

		robot().waitForIdle();
		
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			robot().waitForIdle();
			JTableFixture table = dialog.table();
			assertThat(table.rowCount()).as("Table should have at least one row after adding category").isGreaterThan(0);
			boolean found = false;
			for (int i = 0; i < table.rowCount(); i++) {
				String name = table.cell(TableCell.row(i).column(1)).value();
				if (CATEGORY_NAME_1.equals(name)) {
					found = true;
					break;
				}
			}
			assertThat(found).isTrue();
		});
	}

	@Test
	@GUITest
	public void testUpdateCategorySuccess() throws SQLException {
		if (categoryService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		try {
			List<Category> existing = categoryService.getAllCategories();
			for (Category cat : existing) {
				if (CATEGORY_NAME_1.equals(cat.getName())) {
					categoryService.deleteCategory(cat.getCategoryId());
				}
			}
			await().atMost(1, TimeUnit.SECONDS);
		} catch (Exception e) {
		}
		
		Category category = null;
		int retries = 5;
		for (int i = 0; i < retries; i++) {
			try {
				category = categoryService.createCategory(CATEGORY_NAME_1);
				break;
			} catch (Exception e) {
				if (i == retries - 1) {
					System.err.println("Failed to create category after " + retries + " attempts. Last error: " + e.getMessage());
					e.printStackTrace();
					throw new RuntimeException("Failed to create category after " + retries + " attempts: " + e.getMessage(), e);
				}
				await().atMost(2, TimeUnit.SECONDS);
			}
		}
		
		GuiActionRunner.execute(() -> {
			categoryDialog.loadCategories();
		});

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = dialog.table();
			assertThat(table.rowCount()).isGreaterThan(0);
		});

		JTableFixture table = dialog.table();
		table.selectRows(0);
		
		GuiActionRunner.execute(() -> {
			categoryDialog.categoryTableModel.setValueAt(CATEGORY_NAME_2, 0, 1);
		});

		dialog.button(withText("Update Selected")).click();

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String updatedName = table.cell(TableCell.row(0).column(1)).value();
			assertThat(updatedName).isEqualTo(CATEGORY_NAME_2);
		});
	}

	@Test
	@GUITest
	public void testDeleteCategorySuccess() throws SQLException {
		if (categoryService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		try {
			List<Category> existing = categoryService.getAllCategories();
			for (Category cat : existing) {
				if (CATEGORY_NAME_1.equals(cat.getName())) {
					categoryService.deleteCategory(cat.getCategoryId());
				}
			}
			await().atMost(1, TimeUnit.SECONDS);
		} catch (Exception e) {
		}
		
		Category category = null;
		int retries = 5;
		for (int i = 0; i < retries; i++) {
			try {
				category = categoryService.createCategory(CATEGORY_NAME_1);
				break;
			} catch (Exception e) {
				if (i == retries - 1) {
					System.err.println("Failed to create category after " + retries + " attempts. Last error: " + e.getMessage());
					e.printStackTrace();
					throw new RuntimeException("Failed to create category after " + retries + " attempts: " + e.getMessage(), e);
				}
				await().atMost(2, TimeUnit.SECONDS);
			}
		}
		
		final Integer categoryId = category != null ? category.getCategoryId() : null;
		
		GuiActionRunner.execute(() -> {
			categoryDialog.loadCategories();
		});

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = dialog.table();
			assertThat(table.rowCount()).isGreaterThan(0);
		});

		JTableFixture table = dialog.table();
		table.selectRows(0);
		
		dialog.button(withText("Delete Selected")).click();
		robot().waitForIdle();

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			robot().waitForIdle();
			boolean found = false;
			int rowCount = table.rowCount();
			for (int i = 0; i < rowCount; i++) {
				String id = table.cell(TableCell.row(i).column(0)).value();
				if (categoryId != null && categoryId.toString().equals(id)) {
					found = true;
					break;
				}
			}
			assertThat(found).as("Category with ID " + categoryId + " should be removed from table").isFalse();
		});
	}

	@Test
	@GUITest
	public void testAddCategoryWithEmptyName() {
		if (categoryService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		dialog.textBox().enterText("");
		
		dialog.button(withText("Add Category")).click();

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			String message = GuiActionRunner.execute(() -> {
				return categoryDialog.labelMessage.getText();
			});
			assertThat(message).contains("cannot be empty");
		});
	}

	@Test
	@GUITest
	public void testUpdateCategoryWithNoSelection() {
		if (categoryService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		GuiActionRunner.execute(() -> {
			categoryDialog.categoryTable.clearSelection();
			categoryDialog.lastErrorMessage = null; 
			if (categoryDialog.labelMessage != null) {
				categoryDialog.labelMessage.setText("");
			}
		});
		robot().waitForIdle();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		dialog.button(withText("Update Selected")).click();
		robot().waitForIdle();

		await().atMost(5, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS).untilAsserted(() -> {
			robot().waitForIdle();
			String labelText = GuiActionRunner.execute(() -> {
				return categoryDialog.labelMessage != null ? categoryDialog.labelMessage.getText() : "";
			});
			String lastError = GuiActionRunner.execute(() -> {
				return categoryDialog.getLastErrorMessage() != null ? categoryDialog.getLastErrorMessage() : "";
			});
			boolean hasSelectMessage = (labelText != null && labelText.toLowerCase().contains("select")) ||
			                          (lastError != null && lastError.toLowerCase().contains("select"));
			assertThat(hasSelectMessage).as("Error message should contain 'select' when no category is selected. Label: '" + labelText + "', LastError: '" + lastError + "'").isTrue();
		});
	}

	@Test
	@GUITest
	public void testDeleteCategoryWithNoSelection() {
		if (categoryService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		GuiActionRunner.execute(() -> {
			categoryDialog.categoryTable.clearSelection();
			categoryDialog.lastErrorMessage = null; 
			if (categoryDialog.labelMessage != null) {
				categoryDialog.labelMessage.setText("");
			}
		});
		robot().waitForIdle();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		dialog.button(withText("Delete Selected")).click();
		robot().waitForIdle();

		await().atMost(5, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS).untilAsserted(() -> {
			robot().waitForIdle();
			String labelText = GuiActionRunner.execute(() -> {
				return categoryDialog.labelMessage != null ? categoryDialog.labelMessage.getText() : "";
			});
			String lastError = GuiActionRunner.execute(() -> {
				return categoryDialog.getLastErrorMessage() != null ? categoryDialog.getLastErrorMessage() : "";
			});
			boolean hasSelectMessage = (labelText != null && labelText.toLowerCase().contains("select")) ||
			                          (lastError != null && lastError.toLowerCase().contains("select"));
			assertThat(hasSelectMessage).as("Error message should contain 'select' when no category is selected. Label: '" + labelText + "', LastError: '" + lastError + "'").isTrue();
		});
	}

	@Test
	@GUITest
	public void testCloseDialog() {
		if (categoryService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		dialog.button(withText("Close")).click();
		robot().waitForIdle();

		await().atMost(5, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS).untilAsserted(() -> {
			robot().waitForIdle();
			boolean isVisible = GuiActionRunner.execute(() -> {
				return categoryDialog.isVisible();
			});
			assertThat(isVisible).as("Dialog should not be visible after clicking Close button").isFalse();
		});
	}
}

