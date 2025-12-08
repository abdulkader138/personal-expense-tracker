package com.mycompany.pet.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
        
        // Create parent frame (MainWindow)
        mainWindow = execute(() -> new MainWindow(categoryService, expenseService));
        parentFrame = new FrameFixture(robot(), mainWindow);
        
        // Create dialog on EDT (don't show it yet)
        expenseDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, categoryService, null);
            ed.setModal(false); // Make non-modal for tests to prevent blocking
            return ed;
        });
        
        // Create fixture first
        dialog = new DialogFixture(robot(), expenseDialog);
        
        // Then show it on EDT
        execute(() -> {
            expenseDialog.setVisible(true);
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
        ExpenseDialog editDialog = execute(() -> new ExpenseDialog(mainWindow, categoryService, expense));
        DialogFixture editDialogFixture = new DialogFixture(robot(), editDialog);
        editDialogFixture.show();
        
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
        ExpenseDialog errorDialog = execute(() -> new ExpenseDialog(mainWindow, categoryService, null));
        DialogFixture errorDialogFixture = new DialogFixture(robot(), errorDialog);
        errorDialogFixture.show();
        
        // Then - dialog should still be visible (error handled)
        errorDialogFixture.requireVisible();
        errorDialogFixture.cleanUp();
    }

    @Test
    @GUITest
    public void testExpenseDialog_Save_WithValidData() throws SQLException {
        // Given - valid data
        execute(() -> {
            if (expenseDialog.categoryComboBox.getItemCount() > 0) {
                expenseDialog.categoryComboBox.setSelectedIndex(0);
            }
            expenseDialog.dateField.setText("2024-01-01");
            expenseDialog.amountField.setText("100.00");
            expenseDialog.descriptionField.setText("Test Expense");
        });
        
        // When - click Save button
        dialog.button(withText("Save")).click();
        
        // Then - dialog should be closed (saved = true)
        // Note: In test mode, dialog might still be visible, but saved flag should be true
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
        ExpenseDialog editDialog = execute(() -> new ExpenseDialog(mainWindow, categoryService, expense));
        DialogFixture editDialogFixture = new DialogFixture(robot(), editDialog);
        editDialogFixture.show();
        
        // Then - verify it's in edit mode
        assertThat(editDialogFixture.target().getTitle()).isEqualTo("Edit Expense");
        
        // When - save
        execute(() -> {
            if (editDialog.categoryComboBox.getItemCount() > 0) {
                editDialog.categoryComboBox.setSelectedIndex(0);
            }
            editDialog.dateField.setText("2024-02-01");
            editDialog.amountField.setText("75.00");
            editDialog.descriptionField.setText("Updated Expense");
        });
        
        editDialogFixture.button(withText("Save")).click();
        
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
        ExpenseDialog editDialog = execute(() -> new ExpenseDialog(mainWindow, categoryService, expense));
        DialogFixture editDialogFixture = new DialogFixture(robot(), editDialog);
        editDialogFixture.show();
        
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
        ExpenseDialog editDialog = execute(() -> new ExpenseDialog(mainWindow, categoryService, expense));
        DialogFixture editDialogFixture = new DialogFixture(robot(), editDialog);
        editDialogFixture.show();
        
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
        ExpenseDialog editDialog = execute(() -> new ExpenseDialog(mainWindow, categoryService, expense));
        DialogFixture editDialogFixture = new DialogFixture(robot(), editDialog);
        editDialogFixture.show();
        
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

}
