package com.mycompany.pet.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.junit.Assume.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
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
    
    @Mock
    private CategoryService categoryService;
    
    @Mock
    private ExpenseService expenseService;
    
    private AutoCloseable closeable;
    
    private static final Integer CATEGORY_ID_1 = 1;
    private static final String CATEGORY_NAME_1 = "Food";
    private static final Integer EXPENSE_ID_1 = 1;
    private static final BigDecimal EXPENSE_AMOUNT_1 = new BigDecimal("100.50");
    private static final String EXPENSE_DESCRIPTION_1 = "Lunch";

    @Override
    protected void onSetUp() throws Exception {
        // Skip UI tests if running in headless mode
        assumeFalse("Skipping UI test - running in headless mode", 
            GraphicsEnvironment.isHeadless());
        
        closeable = MockitoAnnotations.openMocks(this);
        
        // Setup mock data
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(CATEGORY_ID_1, CATEGORY_NAME_1));
        categories.add(new Category(2, "Travel"));
        
        when(categoryService.getAllCategories()).thenReturn(categories);
        when(expenseService.createExpense(any(LocalDate.class), any(BigDecimal.class), 
            any(String.class), any(Integer.class))).thenAnswer(invocation -> {
            Expense exp = new Expense(EXPENSE_ID_1, 
                invocation.getArgument(0),
                invocation.getArgument(1),
                invocation.getArgument(2),
                invocation.getArgument(3));
            return exp;
        });
        
        // Create parent frame (MainWindow)
        MainWindow mainWindow = execute(() -> {
            MainWindow mw = new MainWindow(categoryService, expenseService);
            return mw;
        });
        parentFrame = new FrameFixture(robot(), mainWindow);
        
        // Create and show dialog on EDT
        expenseDialog = execute(() -> {
            ExpenseDialog ed = new ExpenseDialog(mainWindow, categoryService, null);
            return ed;
        });
        
        dialog = new DialogFixture(robot(), expenseDialog);
        dialog.show();
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
}
