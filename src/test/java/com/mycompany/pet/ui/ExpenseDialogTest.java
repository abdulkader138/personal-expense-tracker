package com.mycompany.pet.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.awaitility.Awaitility.await;
import static org.junit.Assume.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.awt.GraphicsEnvironment;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mycompany.pet.controller.CategoryController;
import com.mycompany.pet.controller.ExpenseController;
import com.mycompany.pet.model.Category;
import com.mycompany.pet.model.Expense;
import com.mycompany.pet.service.CategoryService;
import com.mycompany.pet.service.ExpenseService;

/**
 * UI tests for ExpenseDialog using AssertJ Swing.
 * 
 * These tests ensure that the ExpenseDialog correctly displays UI components
 * and interacts with the service layer.
 */
@RunWith(GUITestRunner.class)
public class ExpenseDialogTest extends AssertJSwingJUnitTestCase {
    private DialogFixture dialog;
    private FrameFixture parentFrame;
    private ExpenseDialog expenseDialog;
    private MainWindow mainWindow;
    private CategoryController categoryController;
    private ExpenseController expenseController;
    
    @Mock
    private CategoryService categoryService;
    
    @Mock
    private ExpenseService expenseService;
    
    private AutoCloseable closeable;
    
    private static final Integer CATEGORY_ID_1 = 1;
    private static final String CATEGORY_NAME_1 = "Food";
    private static final Integer EXPENSE_ID_1 = 1;

    @BeforeClass
    public static void checkHeadless() {
        // Check for headless mode BEFORE any test runs
        // This runs before setUp() which initializes the robot
        // Allow forcing UI tests to run with -Dforce.ui.tests=true (e.g., with xvfb)
        String forceUITestsProp = System.getProperty("force.ui.tests");
        boolean forceUITests = "true".equalsIgnoreCase(forceUITestsProp);
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        
        if (!forceUITests && isHeadless) {
            assumeFalse("""
                Skipping UI test - running in headless mode. To run UI tests:
                  1. Use xvfb: xvfb-run -a mvn test -Pui-tests
                  2. Or force: mvn test -Pui-tests -Dforce.ui.tests=true
                  3. Or run locally with display: mvn test -Pui-tests
                """, 
                true);
        }
    }

    @Override
    protected void onSetUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        
        // Setup mock data
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(CATEGORY_ID_1, CATEGORY_NAME_1));
        categories.add(new Category(2, "Travel"));
        
        when(categoryService.getAllCategories()).thenReturn(categories);
        when(expenseService.createExpense(any(LocalDate.class), any(BigDecimal.class), 
            any(String.class), any(Integer.class))).thenAnswer(invocation -> {
            return new Expense(EXPENSE_ID_1, 
                invocation.getArgument(0),
                invocation.getArgument(1),
                invocation.getArgument(2),
                invocation.getArgument(3));
        });
        
        // Create controllers from services
        this.categoryController = new CategoryController(categoryService);
        this.expenseController = new ExpenseController(expenseService);
        
        // Create parent frame (MainWindow) and make it visible first
        mainWindow = execute(() -> {
            MainWindow mw = new MainWindow(expenseController, categoryController);
            mw.setVisible(true);
            return mw;
        });
        parentFrame = new FrameFixture(robot(), mainWindow);
        
        // Small delay to ensure parent is ready
        robot().waitForIdle();
        waitForAsyncOperation();
        
        // Create dialog on EDT (make non-modal for testing)
        expenseDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, expenseController, categoryController, null);
            // Make non-modal for testing
            ed.setModal(false);
            return ed;
        });
        
        // Create fixture BEFORE showing dialog
        dialog = new DialogFixture(robot(), expenseDialog);
        
        // Show dialog on EDT
        execute(() -> {
            expenseDialog.setVisible(true);
        });
        
        // Wait for dialog to appear and categories to load (async operation)
        waitForAsyncOperation();
    }

    /**
     * Waits for async operations to complete using Awaitility.
     */
    private void waitForAsyncOperation() {
        robot().waitForIdle();
        await().atMost(2, TimeUnit.SECONDS).pollInterval(50, TimeUnit.MILLISECONDS)
            .until(() -> {
                robot().waitForIdle();
                return true;
            });
    }

    @Override
    protected void onTearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
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
    public void testExpenseDialog_DisplaysCorrectly() {
        // Then - verify dialog is visible
        dialog.requireVisible();
        assertThat(dialog.target().getTitle()).isEqualTo("Add Expense");
    }

    @Test
    @GUITest
    public void testExpenseDialog_HasCategoryComboBox() {
        // Then - verify category combo box exists
        JComboBoxFixture categoryCombo = dialog.comboBox();
        categoryCombo.requireVisible();
        assertThat(categoryCombo.target()).isNotNull();
        assertThat(categoryCombo.target().isVisible()).isTrue();
    }

    @Test
    @GUITest
    public void testExpenseDialog_HasSaveButton() {
        // Then - verify Save button exists
        JButtonFixture saveButton = dialog.button(withText("Save"));
        saveButton.requireVisible();
        saveButton.requireEnabled();
        assertThat(saveButton.target()).isNotNull();
        assertThat(saveButton.target().isVisible()).isTrue();
        assertThat(saveButton.target().isEnabled()).isTrue();
    }

    @Test
    @GUITest
    public void testExpenseDialog_HasCancelButton() {
        // Then - verify Cancel button exists
        JButtonFixture cancelButton = dialog.button(withText("Cancel"));
        cancelButton.requireVisible();
        cancelButton.requireEnabled();
        assertThat(cancelButton.target()).isNotNull();
        assertThat(cancelButton.target().isVisible()).isTrue();
        assertThat(cancelButton.target().isEnabled()).isTrue();
    }

    @Test
    @GUITest
    public void testExpenseDialog_DisplaysCategories() {
        // Then - verify category combo box has items
        JComboBoxFixture categoryCombo = dialog.comboBox();
        categoryCombo.requireItemCount(2); // Should have 2 categories from mock data
        assertThat(categoryCombo.target().getItemCount()).isEqualTo(2);
    }

    @Test
    @GUITest
    public void testExpenseDialog_DateFieldPreFilled() {
        // Then - verify date field is pre-filled with today's date
        // Note: This test verifies the dialog initializes correctly
        dialog.requireVisible();
        assertThat(dialog.target().isVisible()).isTrue();
        // Verify date field exists and is not empty
        String dateText = execute(() -> expenseDialog.dateField.getText());
        assertThat(dateText)
            .isNotNull()
            .isNotEmpty()
            .isEqualTo(LocalDate.now().toString());
    }

    @Test
    @GUITest
    public void testExpenseDialog_Save_WithNoCategory() {
        // Given - no category selected
        execute(() -> {
            expenseDialog.categoryComboBox.setSelectedItem(null);
            expenseDialog.dateField.setText("2024-01-01");
            expenseDialog.amountField.setText("100.00");
            expenseDialog.descriptionField.setText("Test");
        });
        
        // When - click Save button
        dialog.button(withText("Save")).click();
        
        // Then - dialog should still be visible (validation error shown)
        dialog.requireVisible();
        assertThat(dialog.target().isVisible()).isTrue();
    }

    @Test
    @GUITest
    public void testExpenseDialog_Save_WithError() throws SQLException {
        // Given - service throws exception
        when(expenseService.createExpense(any(), any(), any(), any()))
            .thenThrow(new SQLException("Database error"));
        
        execute(() -> {
            if (expenseDialog.categoryComboBox.getItemCount() > 0) {
                expenseDialog.categoryComboBox.setSelectedIndex(0);
            }
            expenseDialog.dateField.setText("2024-01-01");
            expenseDialog.amountField.setText("100.00");
            expenseDialog.descriptionField.setText("Test");
        });
        
        // When - click Save button
        dialog.button(withText("Save")).click();
        
        // Then - dialog should still be visible (error handled)
        dialog.requireVisible();
        assertThat(dialog.target().isVisible()).isTrue();
    }

    @Test
    @GUITest
    public void testExpenseDialog_LoadExpenseData() throws SQLException {
        // Given - expense with data
        Expense expense = new Expense(1, LocalDate.now(), new BigDecimal("50.00"), "Test Expense", 1);
        when(categoryService.getCategory(1)).thenReturn(new Category(1, "Food"));
        
        // When - create dialog with expense
        ExpenseDialog editDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, expenseController, categoryController, expense);
            // Ensure non-modal
            if (ed.isModal()) {
                ed.setModal(false);
            }
            ed.setVisible(true);
            return ed;
        });
        DialogFixture editDialogFixture = new DialogFixture(robot(), editDialog);
        
        // Then - dialog should be visible
        editDialogFixture.requireVisible();
        assertThat(editDialogFixture.target().isVisible()).isTrue();
        assertThat(editDialogFixture.target().getTitle()).isEqualTo("Edit Expense");
        editDialogFixture.cleanUp();
    }

    @Test
    @GUITest
    public void testExpenseDialog_LoadCategories_HandlesSQLException() throws SQLException {
        // Given - service throws exception
        when(categoryService.getAllCategories())
            .thenThrow(new SQLException("Database error"));
        
        // When - create dialog (categories loaded in constructor)
        ExpenseDialog errorDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, expenseController, categoryController, null);
            // Ensure non-modal
            if (ed.isModal()) {
                ed.setModal(false);
            }
            ed.setVisible(true);
            return ed;
        });
        DialogFixture errorDialogFixture = new DialogFixture(robot(), errorDialog);
        
        // Then - dialog should still be visible (error handled)
        errorDialogFixture.requireVisible();
        assertThat(errorDialogFixture.target().isVisible()).isTrue();
        errorDialogFixture.cleanUp();
    }

    @Test
    @GUITest
    public void testExpenseDialog_Save_WithValidData() throws SQLException {
        // Given - valid data with category selected
        execute(() -> {
            // Wait for categories to be loaded
            int itemCount = expenseDialog.categoryComboBox.getItemCount();
            // Select first non-null category (skip null placeholder if any)
            for (int i = 0; i < itemCount; i++) {
                Object item = expenseDialog.categoryComboBox.getItemAt(i);
                if (item != null && item instanceof Category) {
                    expenseDialog.categoryComboBox.setSelectedItem(item);
                    break;
                }
            }
            expenseDialog.dateField.setText("2024-01-01");
            expenseDialog.amountField.setText("100.00");
            expenseDialog.descriptionField.setText("Test Expense");
        });
        
        // Wait for selection to be set
        robot().waitForIdle();
        
        // When - click Save button
        dialog.button(withText("Save")).click();
        
        // Wait for save to complete
        waitForAsyncOperation();
        
        // Then - dialog should be saved (saved = true)
        boolean saved = execute(() -> expenseDialog.isSaved());
        assertThat(saved).isTrue();
    }

    @Test
    @GUITest
    public void testExpenseDialog_Save_WithInvalidDate() throws SQLException {
        // Given - invalid date
        execute(() -> {
            if (expenseDialog.categoryComboBox.getItemCount() > 0) {
                expenseDialog.categoryComboBox.setSelectedIndex(0);
            }
            expenseDialog.dateField.setText("invalid-date");
            expenseDialog.amountField.setText("100.00");
            expenseDialog.descriptionField.setText("Test");
        });
        
        // When - click Save button
        dialog.button(withText("Save")).click();
        
        // Then - dialog should still be visible (validation error)
        dialog.requireVisible();
        assertThat(dialog.target().isVisible()).isTrue();
    }

    @Test
    @GUITest
    public void testExpenseDialog_Save_WithInvalidAmount() throws SQLException {
        // Given - invalid amount
        execute(() -> {
            if (expenseDialog.categoryComboBox.getItemCount() > 0) {
                expenseDialog.categoryComboBox.setSelectedIndex(0);
            }
            expenseDialog.dateField.setText("2024-01-01");
            expenseDialog.amountField.setText("invalid");
            expenseDialog.descriptionField.setText("Test");
        });
        
        // When - click Save button
        dialog.button(withText("Save")).click();
        
        // Then - dialog should still be visible (validation error)
        dialog.requireVisible();
        assertThat(dialog.target().isVisible()).isTrue();
    }

    @Test
    @GUITest
    public void testExpenseDialog_Save_EditMode() throws SQLException {
        // Given - expense to edit
        Expense expense = new Expense(1, LocalDate.now(), new BigDecimal("50.00"), "Test Expense", 1);
        when(categoryService.getCategory(1)).thenReturn(new Category(1, "Food"));
        when(expenseService.updateExpense(any(Integer.class), any(LocalDate.class), 
            any(BigDecimal.class), any(String.class), any(Integer.class))).thenReturn(expense);
        
        // When - create edit dialog
        ExpenseDialog editDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, expenseController, categoryController, expense);
            // Ensure non-modal
            if (ed.isModal()) {
                ed.setModal(false);
            }
            ed.setVisible(true);
            return ed;
        });
        DialogFixture editDialogFixture = new DialogFixture(robot(), editDialog);
        
        // Then - verify it's in edit mode
        assertThat(editDialogFixture.target().getTitle()).isEqualTo("Edit Expense");
        
        // When - save with valid category selected
        execute(() -> {
            // Select first non-null category
            int itemCount = editDialog.categoryComboBox.getItemCount();
            for (int i = 0; i < itemCount; i++) {
                Object item = editDialog.categoryComboBox.getItemAt(i);
                if (item != null && item instanceof Category) {
                    editDialog.categoryComboBox.setSelectedItem(item);
                    break;
                }
            }
            editDialog.dateField.setText("2024-02-01");
            editDialog.amountField.setText("75.00");
            editDialog.descriptionField.setText("Updated Expense");
        });
        
        // Wait for selection to be set
        robot().waitForIdle();
        await().atMost(500, TimeUnit.MILLISECONDS).pollInterval(50, TimeUnit.MILLISECONDS)
            .until(() -> {
                robot().waitForIdle();
                return true;
            });
        
        editDialogFixture.button(withText("Save")).click();
        
        // Wait for save to complete
        robot().waitForIdle();
        await().atMost(500, TimeUnit.MILLISECONDS).pollInterval(50, TimeUnit.MILLISECONDS)
            .until(() -> {
                robot().waitForIdle();
                return true;
            });
        
        // Then - should be saved
        boolean saved = execute(() -> editDialog.isSaved());
        assertThat(saved).isTrue();
        
        editDialogFixture.cleanUp();
    }

    @Test
    @GUITest
    public void testExpenseDialog_LoadExpenseData_WithCategory() throws SQLException {
        // Given - expense with category
        Expense expense = new Expense(1, LocalDate.of(2024, 1, 15), 
            new BigDecimal("50.00"), "Test Expense", 1);
        Category category = new Category(1, "Food");
        when(categoryService.getCategory(1)).thenReturn(category);
        
        // When - create edit dialog
        ExpenseDialog editDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, expenseController, categoryController, expense);
            // Ensure non-modal
            if (ed.isModal()) {
                ed.setModal(false);
            }
            ed.setVisible(true);
            return ed;
        });
        DialogFixture editDialogFixture = new DialogFixture(robot(), editDialog);
        
        // Then - fields should be populated
        execute(() -> {
            assertThat(editDialog.dateField.getText()).isEqualTo("2024-01-15");
            assertThat(editDialog.amountField.getText()).isEqualTo("50.00");
            assertThat(editDialog.descriptionField.getText()).isEqualTo("Test Expense");
        });
        
        editDialogFixture.cleanUp();
    }

    @Test
    @GUITest
    public void testExpenseDialog_LoadExpenseData_CategoryNotFound() throws SQLException {
        // Given - expense with category that doesn't exist
        Expense expense = new Expense(1, LocalDate.now(), new BigDecimal("50.00"), "Test", 999);
        when(categoryService.getCategory(999)).thenReturn(null);
        
        // When - create edit dialog
        ExpenseDialog editDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, expenseController, categoryController, expense);
            // Ensure non-modal
            if (ed.isModal()) {
                ed.setModal(false);
            }
            ed.setVisible(true);
            return ed;
        });
        DialogFixture editDialogFixture = new DialogFixture(robot(), editDialog);
        
        // Then - dialog should still be visible (category not set but no error)
        editDialogFixture.requireVisible();
        assertThat(editDialogFixture.target().isVisible()).isTrue();
        editDialogFixture.cleanUp();
    }

    @Test
    @GUITest
    public void testExpenseDialog_LoadExpenseData_SQLException() throws SQLException {
        // Given - service throws exception
        Expense expense = new Expense(1, LocalDate.now(), new BigDecimal("50.00"), "Test", 1);
        when(categoryService.getCategory(1)).thenThrow(new SQLException("Database error"));
        
        // When - create edit dialog
        ExpenseDialog editDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, expenseController, categoryController, expense);
            // Ensure non-modal
            if (ed.isModal()) {
                ed.setModal(false);
            }
            ed.setVisible(true);
            return ed;
        });
        DialogFixture editDialogFixture = new DialogFixture(robot(), editDialog);
        
        // Then - dialog should still be visible (error handled)
        editDialogFixture.requireVisible();
        assertThat(editDialog.isVisible()).isTrue();
        editDialogFixture.cleanUp();
    }

    @Test
    @GUITest
    public void testExpenseDialog_Cancel() {
        // When - click Cancel button
        dialog.button(withText("Cancel")).click();
        
        // Then - dialog should be disposed (saved = false)
        boolean saved = execute(() -> expenseDialog.isSaved());
        assertThat(saved).isFalse();
    }


    @Test
    @GUITest
    public void testExpenseDialog_IsSaved_InitialState() {
        // Then - initially not saved
        boolean saved = execute(() -> expenseDialog.isSaved());
        assertThat(saved).isFalse();
    }

    @Test
    @GUITest
    public void testExpenseDialog_DateField_PreFilledForNewExpense() {
        // Given - new expense dialog (expense == null)
        // Then - date field should be pre-filled with today's date
        String dateText = execute(() -> expenseDialog.dateField.getText());
        assertThat(dateText).isEqualTo(LocalDate.now().toString());
    }

    @Test
    @GUITest
    public void testExpenseDialog_Save_WithWhitespace() {
        // Given - data with whitespace
        execute(() -> {
            if (expenseDialog.categoryComboBox.getItemCount() > 0) {
                expenseDialog.categoryComboBox.setSelectedIndex(0);
            }
            expenseDialog.dateField.setText("  2024-01-01  ");
            expenseDialog.amountField.setText("  100.00  ");
            expenseDialog.descriptionField.setText("  Test Expense  ");
        });
        
        // When - click Save button
        dialog.button(withText("Save")).click();
        
        // Then - should handle whitespace (trimmed)
        boolean saved = execute(() -> expenseDialog.isSaved());
        assertThat(saved).isTrue();
    }

    @Test
    @GUITest
    public void testExpenseDialog_Save_EditMode_WithError() throws SQLException {
        // Given - expense to edit, but update fails
        Expense expense = new Expense(1, LocalDate.now(), new BigDecimal("50.00"), "Test Expense", 1);
        when(categoryService.getCategory(1)).thenReturn(new Category(1, "Food"));
        when(expenseService.updateExpense(any(Integer.class), any(LocalDate.class), 
            any(BigDecimal.class), any(String.class), any(Integer.class)))
            .thenThrow(new SQLException("Database error"));
        
        // When - create edit dialog
        ExpenseDialog editDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, expenseController, categoryController, expense);
            // Ensure non-modal
            if (ed.isModal()) {
                ed.setModal(false);
            }
            ed.setVisible(true);
            return ed;
        });
        DialogFixture editDialogFixture = new DialogFixture(robot(), editDialog);
        
        // Wait for categories to load
        waitForAsyncOperation();
        
        // When - select category and save
        execute(() -> {
            // Select first non-null category
            int itemCount = editDialog.categoryComboBox.getItemCount();
            for (int i = 0; i < itemCount; i++) {
                Object item = editDialog.categoryComboBox.getItemAt(i);
                if (item != null && item instanceof Category) {
                    editDialog.categoryComboBox.setSelectedItem(item);
                    break;
                }
            }
            editDialog.dateField.setText("2024-02-01");
            editDialog.amountField.setText("75.00");
            editDialog.descriptionField.setText("Updated Expense");
        });
        
        // Wait for selection to be set
        robot().waitForIdle();
        await().atMost(500, TimeUnit.MILLISECONDS).pollInterval(50, TimeUnit.MILLISECONDS)
            .until(() -> {
                robot().waitForIdle();
                return true;
            });
        
        editDialogFixture.button(withText("Save")).click();
        
        // Wait for error callback to execute (async)
        waitForAsyncOperation();
        
        // Then - dialog should still be visible (error handled via error callback)
        // The error callback lambda (lambda$onSaveButtonClick$7) should have shown error message
        editDialogFixture.requireVisible();
        
        editDialogFixture.cleanUp();
    }

    @Test
    @GUITest
    public void testExpenseDialog_Constructor_WithControllers_NewExpense() {
        // Given - controllers
        ExpenseController testExpenseController = new ExpenseController(expenseService);
        CategoryController testCategoryController = new CategoryController(categoryService);
        
        // When - create dialog with controllers, expense == null (new expense)
        ExpenseDialog testDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, testExpenseController, testCategoryController, null);
            // Ensure non-modal
            if (ed.isModal()) {
                ed.setModal(false);
            }
            ed.setVisible(true);
            return ed;
        });
        DialogFixture dialogFixture = new DialogFixture(robot(), testDialog);
        
        // Wait for categories to load
        waitForAsyncOperation();
        
        // Then - should be in "Add Expense" mode
        assertThat(dialogFixture.target().getTitle()).isEqualTo("Add Expense");
        
        // Then - date field should be pre-filled
        String dateText = execute(() -> testDialog.dateField.getText());
        assertThat(dateText).isEqualTo(LocalDate.now().toString());
        
        dialogFixture.cleanUp();
    }

    @Test
    @GUITest
    public void testExpenseDialog_Constructor_WithControllers_EditExpense() throws SQLException {
        // Given - expense to edit and controllers
        Expense expense = new Expense(1, LocalDate.of(2024, 1, 15), 
            new BigDecimal("50.00"), "Test Expense", 1);
        when(categoryService.getCategory(1)).thenReturn(new Category(1, "Food"));
        ExpenseController testExpenseController = new ExpenseController(expenseService);
        CategoryController testCategoryController = new CategoryController(categoryService);
        
        // When - create dialog with controllers, expense != null (edit expense)
        ExpenseDialog testDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, testExpenseController, testCategoryController, expense);
            // Ensure non-modal
            if (ed.isModal()) {
                ed.setModal(false);
            }
            ed.setVisible(true);
            return ed;
        });
        DialogFixture dialogFixture = new DialogFixture(robot(), testDialog);
        
        // Wait for categories and expense data to load
        waitForAsyncOperation();
        
        // Then - should be in "Edit Expense" mode
        assertThat(dialogFixture.target().getTitle()).isEqualTo("Edit Expense");
        
        // Then - fields should be populated
        execute(() -> {
            assertThat(testDialog.dateField.getText()).isEqualTo("2024-01-15");
            assertThat(testDialog.amountField.getText()).isEqualTo("50.00");
            assertThat(testDialog.descriptionField.getText()).isEqualTo("Test Expense");
        });
        
        dialogFixture.cleanUp();
    }


    @Test
    @GUITest
    public void testExpenseDialog_LoadExpenseData_WithNullExpense() {
        // Given - dialog with expense == null
        ExpenseDialog testDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, expenseController, categoryController, null);
            // Ensure non-modal
            if (ed.isModal()) {
                ed.setModal(false);
            }
            ed.setVisible(true);
            return ed;
        });
        DialogFixture dialogFixture = new DialogFixture(robot(), testDialog);
        
        // Wait for categories to load
        waitForAsyncOperation();
        
        // When - call loadExpenseData directly (should return early)
        execute(() -> {
            testDialog.loadExpenseData(); // Should return early since expense is null
        });
        
        // Then - no exception should be thrown
        // Fields should remain as initialized (date pre-filled, others empty)
        String dateText = execute(() -> testDialog.dateField.getText());
        assertThat(dateText).isEqualTo(LocalDate.now().toString());
        
        dialogFixture.cleanUp();
    }


}
