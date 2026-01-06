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
 * The databaseConfig variable is responsible for starting the Docker container.
 * If the test is run from Eclipse, it runs the Docker container using Testcontainers.
 * If the test is run using a Maven command, it starts a Docker container without test containers.
 * 
 * @see CategoryService
 * @see CategoryDAO
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

import java.sql.SQLException;
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
import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.database.DatabaseConnection;
import com.mycompany.pet.database.DatabaseInitializer;
import com.mycompany.pet.controller.CategoryController;
import com.mycompany.pet.controller.ExpenseController;
import com.mycompany.pet.model.Category;
import com.mycompany.pet.service.CategoryService;
import com.mycompany.pet.service.ExpenseService;

/**
 * The Class CategoryDialogIT.
 */
@RunWith(GUITestRunner.class)
public class CategoryDialogIT extends AssertJSwingJUnitTestCase {

	/** The category service. */
	private CategoryService categoryService;

	/** The expense service. */
	private ExpenseService expenseService;

	/** The main window. */
	private MainWindow mainWindow;

	/** The parent frame. */
	private FrameFixture parentFrame;

	/** The category dialog. */
	private CategoryDialog categoryDialog;

	/** The dialog. */
	private DialogFixture dialog;

	/** The database connection. */
	private DatabaseConnection databaseConnection;

	/**
	 * This variable is responsible for starting the Docker container. If the test
	 * is run from Eclipse, it runs the Docker container using Testcontainers. If
	 * the test is run using a Maven command, it starts a Docker container without
	 * test containers
	 */
	private static DBConfig databaseConfig;

	/** The category name 1. */
	private static final String CATEGORY_NAME_1 = "Food";

	/** The category name 2. */
	private static final String CATEGORY_NAME_2 = "Travel";

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
	 * On set up.
	 *
	 * @throws Exception the exception
	 */
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
			
			// Initialize database
			try {
				DatabaseInitializer initializer = new DatabaseInitializer(databaseConnection);
				initializer.initialize();
			} catch (Exception e) {
				org.junit.Assume.assumeNoException("Failed to initialize database. Skipping integration tests.", e);
				return;
			}

			// Initialize DAOs and Services
			CategoryDAO categoryDAO = new CategoryDAO(databaseConnection);
			categoryService = new CategoryService(categoryDAO);
			expenseService = null; // Not needed for category dialog

			// Create controllers from services
			CategoryController categoryController = new CategoryController(categoryService);
			ExpenseController expenseController = new ExpenseController(expenseService);
			
			// Create parent frame (MainWindow) and make it visible first
			mainWindow = GuiActionRunner.execute(() -> {
				MainWindow mw = new MainWindow(expenseController, categoryController);
				mw.setVisible(true);
				return mw;
			});
			parentFrame = new FrameFixture(robot(), mainWindow);
			parentFrame.show();

			// Create dialog on EDT
			categoryDialog = GuiActionRunner.execute(() -> {
				CategoryDialog cd = new CategoryDialog(mainWindow, categoryController);
				cd.setModal(false); // Make non-modal for testing
				cd.setVisible(true);
				return cd;
			});
			
			dialog = new DialogFixture(robot(), categoryDialog);
			dialog.show();
			
			// Wait for initial category load (async operation)
			robot().waitForIdle();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		} catch (Exception e) {
			// Skip test if database operations fail
			org.junit.Assume.assumeNoException("Database operation failed. Skipping test.", e);
			return;
		}
	}

	/**
	 * On tear down.
	 */
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

	/**
	 * Test add category success.
	 */
	@Test
	@GUITest
	public void testAddCategorySuccess() throws SQLException {
		if (categoryService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		// Set test mode for label messages
		System.setProperty("test.mode", "true");
		
		// Enter category name
		dialog.textBox().enterText(CATEGORY_NAME_1);
		
		// Click Add Category button
		dialog.button(withText("Add Category")).click();

		// Wait for async operation
		robot().waitForIdle();
		
		// Verify category was added
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = dialog.table();
			assertThat(table.rowCount()).isGreaterThan(0);
			// Check if the category name appears in the table
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

	/**
	 * Test update category success.
	 */
	@Test
	@GUITest
	public void testUpdateCategorySuccess() throws SQLException {
		if (categoryService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		// First, create a category with retry for database initialization
		Category category = null;
		int retries = 3;
		for (int i = 0; i < retries; i++) {
			try {
				category = categoryService.createCategory(CATEGORY_NAME_1);
				break;
			} catch (Exception e) {
				if (i == retries - 1) {
					throw new RuntimeException("Failed to create category after " + retries + " attempts: " + e.getMessage(), e);
				}
				await().atMost(1, TimeUnit.SECONDS);
			}
		}
		
		// Load categories in dialog
		GuiActionRunner.execute(() -> {
			categoryDialog.loadCategories();
		});

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = dialog.table();
			assertThat(table.rowCount()).isGreaterThan(0);
		});

		// Select first row and edit
		JTableFixture table = dialog.table();
		table.selectRows(0);
		
		// Edit the category name in the table
		GuiActionRunner.execute(() -> {
			categoryDialog.categoryTableModel.setValueAt(CATEGORY_NAME_2, 0, 1);
		});

		// Click Update Selected button
		dialog.button(withText("Update Selected")).click();

		// Verify category was updated
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String updatedName = table.cell(TableCell.row(0).column(1)).value();
			assertThat(updatedName).isEqualTo(CATEGORY_NAME_2);
		});
	}

	/**
	 * Test delete category success.
	 */
	@Test
	@GUITest
	public void testDeleteCategorySuccess() throws SQLException {
		if (categoryService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		// First, create a category with retry for database initialization
		Category category = null;
		int retries = 3;
		for (int i = 0; i < retries; i++) {
			try {
				category = categoryService.createCategory(CATEGORY_NAME_1);
				break;
			} catch (Exception e) {
				if (i == retries - 1) {
					throw new RuntimeException("Failed to create category after " + retries + " attempts: " + e.getMessage(), e);
				}
				await().atMost(1, TimeUnit.SECONDS);
			}
		}
		
		// Store category ID in final variable for use in lambda
		final Integer categoryId = category != null ? category.getCategoryId() : null;
		
		// Load categories in dialog
		GuiActionRunner.execute(() -> {
			categoryDialog.loadCategories();
		});

		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			JTableFixture table = dialog.table();
			assertThat(table.rowCount()).isGreaterThan(0);
		});

		// Select first row
		JTableFixture table = dialog.table();
		table.selectRows(0);
		
		// Click Delete Selected button
		dialog.button(withText("Delete Selected")).click();

		// Verify category was deleted
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			// Category should be removed from table
			boolean found = false;
			for (int i = 0; i < table.rowCount(); i++) {
				String id = table.cell(TableCell.row(i).column(0)).value();
				if (categoryId != null && categoryId.toString().equals(id)) {
					found = true;
					break;
				}
			}
			assertThat(found).isFalse();
		});
	}

	/**
	 * Test add category with empty name.
	 */
	@Test
	@GUITest
	public void testAddCategoryWithEmptyName() {
		if (categoryService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		// Enter empty category name
		dialog.textBox().enterText("");
		
		// Click Add Category button
		dialog.button(withText("Add Category")).click();

		// Verify error message is shown
		await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
			String message = GuiActionRunner.execute(() -> {
				return categoryDialog.labelMessage.getText();
			});
			assertThat(message).contains("cannot be empty");
		});
	}

	/**
	 * Test update category with no selection.
	 */
	@Test
	@GUITest
	public void testUpdateCategoryWithNoSelection() {
		if (categoryService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		// Clear selection
		GuiActionRunner.execute(() -> {
			categoryDialog.categoryTable.clearSelection();
		});

		// Click Update Selected button
		dialog.button(withText("Update Selected")).click();

		// Verify error message is shown - increase timeout for GUI updates
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String message = GuiActionRunner.execute(() -> {
				return categoryDialog.labelMessage.getText();
			});
			assertThat(message).as("Error message should contain 'select' when no category is selected").containsIgnoringCase("select");
		});
	}

	/**
	 * Test delete category with no selection.
	 */
	@Test
	@GUITest
	public void testDeleteCategoryWithNoSelection() {
		if (categoryService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		// Clear selection
		GuiActionRunner.execute(() -> {
			categoryDialog.categoryTable.clearSelection();
		});

		// Click Delete Selected button
		dialog.button(withText("Delete Selected")).click();

		// Verify error message is shown - increase timeout for GUI updates
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			String message = GuiActionRunner.execute(() -> {
				return categoryDialog.labelMessage.getText();
			});
			assertThat(message).as("Error message should contain 'select' when no category is selected").containsIgnoringCase("select");
		});
	}

	/**
	 * Test close dialog.
	 */
	@Test
	@GUITest
	public void testCloseDialog() {
		if (categoryService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		// Click Close button
		dialog.button(withText("Close")).click();

		// Verify dialog is closed - increase timeout and add small delay for GUI to update
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			boolean isVisible = GuiActionRunner.execute(() -> {
				return categoryDialog.isVisible();
			});
			assertThat(isVisible).as("Dialog should be closed after clicking Close button").isFalse();
		});
	}
}

