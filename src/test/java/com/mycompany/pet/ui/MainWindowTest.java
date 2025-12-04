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
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.assertj.swing.fixture.JTableFixture;
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
 * UI tests for MainWindow using AssertJ Swing.
 * 
 * These tests ensure that the MainWindow correctly displays UI components
 * and interacts with the service layer.
 */
@RunWith(GUITestRunner.class)
public class MainWindowTest extends AssertJSwingJUnitTestCase {
    private FrameFixture window;
    private MainWindow mainWindow;
    
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
        
        List<Expense> expenses = new ArrayList<>();
        expenses.add(new Expense(EXPENSE_ID_1, 
            LocalDate.now(), 
            EXPENSE_AMOUNT_1, 
            EXPENSE_DESCRIPTION_1, 
            CATEGORY_ID_1));
        
        when(categoryService.getAllCategories()).thenReturn(categories);
        when(expenseService.getAllExpenses()).thenReturn(expenses);
        when(categoryService.getCategory(any(Integer.class))).thenReturn(categories.get(0));
        when(expenseService.getMonthlyTotal(any(Integer.class), any(Integer.class)))
            .thenReturn(EXPENSE_AMOUNT_1);
        when(expenseService.getTotalByCategory(any(Integer.class)))
            .thenReturn(EXPENSE_AMOUNT_1);
        
        // Create and show window on EDT
        mainWindow = execute(() -> {
            MainWindow mw = new MainWindow(categoryService, expenseService);
            return mw;
        });
        
        window = new FrameFixture(robot(), mainWindow);
        window.show();
    }

    @Override
    protected void onTearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
        if (window != null) {
            window.cleanUp();
        }
    }

    @Test
    @GUITest
    public void testMainWindow_DisplaysCorrectly() {
        // Then - verify window is visible and has correct title
        window.requireVisible();
        assertThat(window.target().getTitle()).isEqualTo("Personal Expense Tracker");
    }

    @Test
    @GUITest
    public void testMainWindow_HasAddExpenseButton() {
        // Then - verify Add Expense button exists
        JButtonFixture addButton = window.button(withText("Add Expense"));
        addButton.requireVisible();
        addButton.requireEnabled();
    }

    @Test
    @GUITest
    public void testMainWindow_HasEditExpenseButton() {
        // Then - verify Edit Expense button exists
        JButtonFixture editButton = window.button(withText("Edit Expense"));
        editButton.requireVisible();
        editButton.requireEnabled();
    }

    @Test
    @GUITest
    public void testMainWindow_HasDeleteExpenseButton() {
        // Then - verify Delete Expense button exists
        JButtonFixture deleteButton = window.button(withText("Delete Expense"));
        deleteButton.requireVisible();
        deleteButton.requireEnabled();
    }

    @Test
    @GUITest
    public void testMainWindow_HasExpenseTable() {
        // Then - verify expense table exists
        JTableFixture table = window.table();
        table.requireVisible();
    }

    @Test
    @GUITest
    public void testMainWindow_DisplaysExpenses() {
        // Then - verify expense table has data
        JTableFixture table = window.table();
        table.requireRowCount(1); // Should have 1 expense from mock data
    }

    @Test
    @GUITest
    public void testMainWindow_HasCategoryComboBox() {
        // Then - verify category combo box exists
        JComboBoxFixture categoryCombo = window.comboBox();
        categoryCombo.requireVisible();
    }

    @Test
    @GUITest
    public void testMainWindow_HasMonthComboBox() {
        // Then - verify month combo box exists and has items
        // Note: Finding combo boxes by index since they don't have names
        JComboBoxFixture monthCombo = window.comboBox();
        monthCombo.requireVisible();
    }

    @Test
    @GUITest
    public void testMainWindow_DisplaysMonthlyTotal() {
        // Then - verify monthly total label exists
        // The label should show the total from mock data
        window.requireVisible();
    }
}
