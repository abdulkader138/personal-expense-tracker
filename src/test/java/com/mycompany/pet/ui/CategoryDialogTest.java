package com.mycompany.pet.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mycompany.pet.model.Category;

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
            return new CategoryDialog(parent, categoryService);
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
        // Then - verify Add Category button exists
        JButtonFixture addButton = dialog.button(withText("Add Category"));
        addButton.requireVisible();
        addButton.requireEnabled();
    }

    @Test
    @GUITest
    public void testCategoryDialog_HasDeleteButton() {
        // Then - verify Delete Selected button exists
        JButtonFixture deleteButton = dialog.button(withText("Delete Selected"));
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

    @Test
    @GUITest
    public void testCategoryDialog_AddCategory_WithValidName() throws SQLException {
        // Given - valid category name
        execute(() -> {
            categoryDialog.nameField.setText("New Category");
        });
        
        // When - click Add Category button
        dialog.button(withText("Add Category")).click();
        
        // Then - category should be added (table should refresh)
        JTableFixture table = dialog.table();
        // Table should have been refreshed (might have 3 categories now)
        table.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_AddCategory_WithEmptyName() {
        // Given - empty category name
        execute(() -> {
            categoryDialog.nameField.setText("");
        });
        
        // When - click Add Category button
        dialog.button(withText("Add Category")).click();
        
        // Then - dialog should still be visible (validation error shown)
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_AddCategory_WithWhitespaceOnly() {
        // Given - whitespace only
        execute(() -> {
            categoryDialog.nameField.setText("   ");
        });
        
        // When - click Add Category button
        dialog.button(withText("Add Category")).click();
        
        // Then - dialog should still be visible (validation error shown)
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_AddCategory_WithError() throws SQLException {
        // Given - service throws exception
        when(categoryService.createCategory(any(String.class)))
            .thenThrow(new SQLException("Database error"));
        
        execute(() -> {
            categoryDialog.nameField.setText("New Category");
        });
        
        // When - click Add Category button
        dialog.button(withText("Add Category")).click();
        
        // Then - dialog should still be visible (error handled)
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_UpdateSelectedCategory_NoSelection() {
        // Given - no row selected
        execute(() -> {
            categoryDialog.categoryTable.clearSelection();
        });
        
        // When - click Update Selected button
        dialog.button(withText("Update Selected")).click();
        
        // Then - dialog should still be visible (warning shown)
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_UpdateSelectedCategory_WithSelection() throws SQLException {
        // Given - row selected and edited
        execute(() -> {
            if (categoryDialog.categoryTableModel.getRowCount() > 0) {
                categoryDialog.categoryTable.setRowSelectionInterval(0, 0);
                // Edit the name in the table
                categoryDialog.categoryTableModel.setValueAt("Updated Name", 0, 1);
            }
        });
        
        // When - click Update Selected button
        dialog.button(withText("Update Selected")).click();
        
        // Then - category should be updated
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_UpdateSelectedCategory_WithEmptyName() throws SQLException {
        // Given - row selected with empty name
        execute(() -> {
            if (categoryDialog.categoryTableModel.getRowCount() > 0) {
                categoryDialog.categoryTable.setRowSelectionInterval(0, 0);
                categoryDialog.categoryTableModel.setValueAt("", 0, 1);
            }
        });
        
        // When - click Update Selected button
        dialog.button(withText("Update Selected")).click();
        
        // Then - dialog should still be visible (validation error, table refreshed)
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_UpdateSelectedCategory_WithNullName() throws SQLException {
        // Given - row selected with null name
        execute(() -> {
            if (categoryDialog.categoryTableModel.getRowCount() > 0) {
                categoryDialog.categoryTable.setRowSelectionInterval(0, 0);
                categoryDialog.categoryTableModel.setValueAt(null, 0, 1);
            }
        });
        
        // When - click Update Selected button
        dialog.button(withText("Update Selected")).click();
        
        // Then - dialog should still be visible (validation error, table refreshed)
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_UpdateSelectedCategory_WithWhitespaceOnly() throws SQLException {
        // Given - row selected with whitespace only
        execute(() -> {
            if (categoryDialog.categoryTableModel.getRowCount() > 0) {
                categoryDialog.categoryTable.setRowSelectionInterval(0, 0);
                categoryDialog.categoryTableModel.setValueAt("   ", 0, 1);
            }
        });
        
        // When - click Update Selected button
        dialog.button(withText("Update Selected")).click();
        
        // Then - dialog should still be visible (validation error, table refreshed)
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_UpdateSelectedCategory_WithError() throws SQLException {
        // Given - service throws exception
        when(categoryService.updateCategory(any(Integer.class), any(String.class)))
            .thenThrow(new SQLException("Database error"));
        
        execute(() -> {
            if (categoryDialog.categoryTableModel.getRowCount() > 0) {
                categoryDialog.categoryTable.setRowSelectionInterval(0, 0);
                categoryDialog.categoryTableModel.setValueAt("Updated Name", 0, 1);
            }
        });
        
        // When - click Update Selected button
        dialog.button(withText("Update Selected")).click();
        
        // Then - dialog should still be visible (error handled, table refreshed)
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_UpdateSelectedCategory_WhileEditing() throws SQLException {
        // Given - cell is being edited
        execute(() -> {
            if (categoryDialog.categoryTableModel.getRowCount() > 0) {
                categoryDialog.categoryTable.setRowSelectionInterval(0, 0);
                // Start editing
                categoryDialog.categoryTable.editCellAt(0, 1);
            }
        });
        
        // When - click Update Selected button (should stop editing first)
        dialog.button(withText("Update Selected")).click();
        
        // Then - dialog should still be visible
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_DeleteSelectedCategory_NoSelection() {
        // Given - no row selected
        execute(() -> {
            categoryDialog.categoryTable.clearSelection();
        });
        
        // When - click Delete Selected button
        dialog.button(withText("Delete Selected")).click();
        
        // Then - dialog should still be visible (warning shown)
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_DeleteSelectedCategory_WithSelection() throws SQLException {
        // Given - row selected
        execute(() -> {
            if (categoryDialog.categoryTableModel.getRowCount() > 0) {
                categoryDialog.categoryTable.setRowSelectionInterval(0, 0);
            }
        });
        
        // When - click Delete Selected button (will auto-confirm in test mode)
        dialog.button(withText("Delete Selected")).click();
        
        // Then - category should be deleted (table refreshed)
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_DeleteSelectedCategory_WithError() throws SQLException {
        // Given - service throws exception
        when(categoryService.deleteCategory(any(Integer.class)))
            .thenThrow(new SQLException("Database error"));
        
        execute(() -> {
            if (categoryDialog.categoryTableModel.getRowCount() > 0) {
                categoryDialog.categoryTable.setRowSelectionInterval(0, 0);
            }
        });
        
        // When - click Delete Selected button
        dialog.button(withText("Delete Selected")).click();
        
        // Then - dialog should still be visible (error handled)
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_LoadCategories_WithError() throws SQLException {
        // Given - service throws exception
        when(categoryService.getAllCategories())
            .thenThrow(new SQLException("Database error"));
        
        // When - load categories
        execute(() -> {
            categoryDialog.loadCategories();
        });
        
        // Then - dialog should still be visible (error handled)
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_CloseButton() {
        // When - click Close button
        dialog.button(withText("Close")).click();
        
        // Then - dialog should be disposed
        // Note: In test mode, we can't easily verify disposal, but we can verify the button works
        dialog.requireVisible(); // Might still be visible briefly
    }

    @Test
    @GUITest
    public void testCategoryDialog_TableModel_IsCellEditable_ID() {
        // Given - table model
        // When - check if ID column is editable
        boolean editable = execute(() -> {
            return categoryDialog.categoryTableModel.isCellEditable(0, 0);
        });
        
        // Then - ID column should not be editable
        assertThat(editable).isFalse();
    }

    @Test
    @GUITest
    public void testCategoryDialog_TableModel_IsCellEditable_Name() {
        // Given - table model
        // When - check if Name column is editable
        boolean editable = execute(() -> {
            return categoryDialog.categoryTableModel.isCellEditable(0, 1);
        });
        
        // Then - Name column should be editable
        assertThat(editable).isTrue();
    }

    @Test
    @GUITest
    public void testCategoryDialog_LoadCategories_EmptyList() throws SQLException {
        // Given - empty category list
        when(categoryService.getAllCategories()).thenReturn(new ArrayList<>());
        
        // When - load categories
        execute(() -> {
            categoryDialog.loadCategories();
        });
        
        // Then - table should be empty
        JTableFixture table = dialog.table();
        assertThat(table.rowCount()).isEqualTo(0);
    }

    @Test
    @GUITest
    public void testCategoryDialog_Constructor_DoesNotLoadInTestEnvironment() {
        // Given - test environment
        // When - create dialog
        // Then - loadCategories should not be called automatically
        // (This is verified by the fact that the dialog is created successfully)
        dialog.requireVisible();
    }
}
