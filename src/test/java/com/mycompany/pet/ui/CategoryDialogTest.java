package com.mycompany.pet.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.junit.Assume.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTableFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mycompany.pet.model.Category;
import com.mycompany.pet.service.CategoryService;

/**
 * UI tests for CategoryDialog using AssertJ Swing.
 * 
 * These tests ensure that the CategoryDialog correctly displays UI components
 * and interacts with the CategoryService.
 */
@RunWith(GUITestRunner.class)
public class CategoryDialogTest extends AssertJSwingJUnitTestCase {
    private DialogFixture dialog;
    private FrameFixture parentFrame;
    private CategoryDialog categoryDialog;
    
    @Mock
    private CategoryService categoryService;
    
    private AutoCloseable closeable;
    
    private static final Integer CATEGORY_ID_1 = 1;
    private static final String CATEGORY_NAME_1 = "Food";
    private static final Integer CATEGORY_ID_2 = 2;
    private static final String CATEGORY_NAME_2 = "Travel";

    @Override
    protected void onSetUp() throws Exception {
        // Skip UI tests if running in headless mode
        assumeFalse("Skipping UI test - running in headless mode", 
            GraphicsEnvironment.isHeadless());
        
        closeable = MockitoAnnotations.openMocks(this);
        
        // Setup mock data
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(CATEGORY_ID_1, CATEGORY_NAME_1));
        categories.add(new Category(CATEGORY_ID_2, CATEGORY_NAME_2));
        
        when(categoryService.getAllCategories()).thenReturn(categories);
        when(categoryService.createCategory(any(String.class))).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            Category cat = new Category(3, name);
            cat.setCategoryId(3);
            return cat;
        });
        
        // Create parent frame
        javax.swing.JFrame parent = execute(() -> {
            javax.swing.JFrame frame = new javax.swing.JFrame("Test Parent");
            frame.setVisible(true);
            return frame;
        });
        parentFrame = new FrameFixture(robot(), parent);
        
        // Create and show dialog on EDT
        categoryDialog = execute(() -> {
            CategoryDialog cd = new CategoryDialog(parent, categoryService);
            return cd;
        });
        
        dialog = new DialogFixture(robot(), categoryDialog);
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
    public void testCategoryDialog_DisplaysCorrectly() {
        // Then - verify dialog is visible
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_HasAddButton() {
        // Then - verify Add button exists
        JButtonFixture addButton = dialog.button(withText("Add"));
        addButton.requireVisible();
        addButton.requireEnabled();
    }

    @Test
    @GUITest
    public void testCategoryDialog_HasDeleteButton() {
        // Then - verify Delete button exists
        JButtonFixture deleteButton = dialog.button(withText("Delete"));
        deleteButton.requireVisible();
        deleteButton.requireEnabled();
    }

    @Test
    @GUITest
    public void testCategoryDialog_HasUpdateButton() {
        // Then - verify Update Selected button exists
        JButtonFixture updateButton = dialog.button(withText("Update Selected"));
        updateButton.requireVisible();
        updateButton.requireEnabled();
    }

    @Test
    @GUITest
    public void testCategoryDialog_HasCategoryTable() {
        // Then - verify category table exists
        JTableFixture table = dialog.table();
        table.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_DisplaysCategories() {
        // Then - verify category table has data
        JTableFixture table = dialog.table();
        table.requireRowCount(2); // Should have 2 categories from mock data
    }

    @Test
    @GUITest
    public void testCategoryDialog_TableShowsCategoryData() {
        // Then - verify table displays category information
        JTableFixture table = dialog.table();
        // Check that table has the expected columns (ID and Name)
        assertThat(table.target().getColumnCount()).isGreaterThanOrEqualTo(2);
    }
}
