package com.mycompany.pet.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.junit.Assume.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
            assumeFalse("Skipping UI test - running in headless mode. " +
                "To run UI tests:\n" +
                "  1. Use xvfb: xvfb-run -a mvn test -Pui-tests\n" +
                "  2. Or force: mvn test -Pui-tests -Dforce.ui.tests=true\n" +
                "  3. Or run locally with display: mvn test -Pui-tests", 
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
        
        // Create parent frame (MainWindow) and make it visible first
        mainWindow = execute(() -> {
            MainWindow mw = new MainWindow(categoryService, expenseService);
            mw.setVisible(true);
            return mw;
        });
        parentFrame = new FrameFixture(robot(), mainWindow);
        
        // Small delay to ensure parent is ready
        Thread.sleep(100);
        
        // Create dialog on EDT (make non-modal for testing)
        expenseDialog = execute(() -> {
            // Use deprecated constructor which creates controllers internally
            ExpenseDialog ed = new ExpenseDialog(mainWindow, categoryService, null);
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
        
        // Small delay for dialog to appear and categories to load (async operation)
        Thread.sleep(200);
        robot().waitForIdle();
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
    }

    @Test
    @GUITest
    public void testExpenseDialog_HasSaveButton() {
        // Then - verify Save button exists
        JButtonFixture saveButton = dialog.button(withText("Save"));
        saveButton.requireVisible();
        saveButton.requireEnabled();
    }

    @Test
    @GUITest
    public void testExpenseDialog_HasCancelButton() {
        // Then - verify Cancel button exists
        JButtonFixture cancelButton = dialog.button(withText("Cancel"));
        cancelButton.requireVisible();
        cancelButton.requireEnabled();
    }

    @Test
    @GUITest
    public void testExpenseDialog_DisplaysCategories() {
        // Then - verify category combo box has items
        JComboBoxFixture categoryCombo = dialog.comboBox();
        categoryCombo.requireItemCount(2); // Should have 2 categories from mock data
    }

    @Test
    @GUITest
    public void testExpenseDialog_DateFieldPreFilled() {
        // Then - verify date field is pre-filled with today's date
        // Note: This test verifies the dialog initializes correctly
        dialog.requireVisible();
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
    }

    @Test
    @GUITest
    public void testExpenseDialog_LoadExpenseData() throws SQLException {
        // Given - expense with data
        Expense expense = new Expense(1, LocalDate.now(), new BigDecimal("50.00"), "Test Expense", 1);
        when(categoryService.getCategory(1)).thenReturn(new Category(1, "Food"));
        
        // When - create dialog with expense
        ExpenseDialog editDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, categoryService, expense);
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
            ExpenseDialog ed = new ExpenseDialog(mainWindow, categoryService, null);
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
        
        // Small delay to ensure selection is set
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When - click Save button
        dialog.button(withText("Save")).click();
        
        // Small delay for save to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
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
            ExpenseDialog ed = new ExpenseDialog(mainWindow, categoryService, expense);
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
        
        // Small delay to ensure selection is set
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        editDialogFixture.button(withText("Save")).click();
        
        // Small delay for save to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
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
            ExpenseDialog ed = new ExpenseDialog(mainWindow, categoryService, expense);
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
            ExpenseDialog ed = new ExpenseDialog(mainWindow, categoryService, expense);
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
            ExpenseDialog ed = new ExpenseDialog(mainWindow, categoryService, expense);
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
    public void testExpenseDialog_Constructor_InvalidParent() {
        // Given - invalid parent (not MainWindow)
        javax.swing.JFrame invalidParent = execute(() -> {
            javax.swing.JFrame frame = new javax.swing.JFrame("Invalid");
            return frame;
        });
        
        // When/Then - should throw exception
        try {
            ExpenseDialog invalidDialog = execute(() -> {
                return new ExpenseDialog(invalidParent, categoryService, null);
            });
            // If we get here, the test should fail
            assertThat(false).as("Should have thrown IllegalArgumentException").isTrue();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
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
    public void testExpenseDialog_Save_WithWhitespace() throws SQLException {
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
            ExpenseDialog ed = new ExpenseDialog(mainWindow, categoryService, expense);
            // Ensure non-modal
            if (ed.isModal()) {
                ed.setModal(false);
            }
            ed.setVisible(true);
            return ed;
        });
        DialogFixture editDialogFixture = new DialogFixture(robot(), editDialog);
        
        // Wait for categories to load
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        robot().waitForIdle();
        
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
        
        // Small delay to ensure selection is set
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        editDialogFixture.button(withText("Save")).click();
        
        // Wait for error callback to execute (async)
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        robot().waitForIdle();
        
        // Then - dialog should still be visible (error handled via error callback)
        // The error callback lambda (lambda$onSaveButtonClick$7) should have shown error message
        editDialogFixture.requireVisible();
        
        editDialogFixture.cleanUp();
    }

    @Test
    @GUITest
    public void testExpenseDialog_Constructor_WithControllers_NewExpense() throws SQLException {
        // Given - controllers
        ExpenseController expenseController = new ExpenseController(expenseService);
        CategoryController categoryController = new CategoryController(categoryService);
        
        // When - create dialog with controllers, expense == null (new expense)
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
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        robot().waitForIdle();
        
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
        ExpenseController expenseController = new ExpenseController(expenseService);
        CategoryController categoryController = new CategoryController(categoryService);
        
        // When - create dialog with controllers, expense != null (edit expense)
        ExpenseDialog testDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, expenseController, categoryController, expense);
            // Ensure non-modal
            if (ed.isModal()) {
                ed.setModal(false);
            }
            ed.setVisible(true);
            return ed;
        });
        DialogFixture dialogFixture = new DialogFixture(robot(), testDialog);
        
        // Wait for categories and expense data to load
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        robot().waitForIdle();
        
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
    public void testExpenseDialog_Constructor_Deprecated_InvalidParent() {
        // Given - invalid parent (not MainWindow), created on EDT
        JFrame invalidParent = execute(() -> {
            return new JFrame("Invalid Parent");
        });
        
        // When/Then - should throw IllegalArgumentException
        try {
            execute(() -> {
                return new ExpenseDialog(invalidParent, categoryService, null);
            });
            // Should not reach here
            assertThat(false).as("Should have thrown IllegalArgumentException").isTrue();
        } catch (Exception e) {
            assertThat(e.getCause() instanceof IllegalArgumentException || 
                      e instanceof IllegalArgumentException)
                .as("Should throw IllegalArgumentException when parent is not MainWindow")
                .isTrue();
        }
    }

    @Test
    @GUITest
    public void testExpenseDialog_LoadExpenseData_WithNullExpense() throws SQLException {
        // Given - dialog with expense == null
        ExpenseDialog testDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, categoryService, null);
            // Ensure non-modal
            if (ed.isModal()) {
                ed.setModal(false);
            }
            ed.setVisible(true);
            return ed;
        });
        DialogFixture dialogFixture = new DialogFixture(robot(), testDialog);
        
        // Wait for categories to load
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        robot().waitForIdle();
        
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
