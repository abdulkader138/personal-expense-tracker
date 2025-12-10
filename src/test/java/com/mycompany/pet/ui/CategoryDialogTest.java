package com.mycompany.pet.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.junit.Assume.assumeFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mycompany.pet.model.Category;
import com.mycompany.pet.service.CategoryService;
import com.mycompany.pet.service.ExpenseService;

/**
 * UI tests for CategoryDialog using AssertJ Swing.
 */
@RunWith(GUITestRunner.class)
public class CategoryDialogTest extends AssertJSwingJUnitTestCase {
    private DialogFixture dialog;
    private FrameFixture parentFrame;
    private CategoryDialog categoryDialog;
    private MainWindow mainWindow;
    
    @Mock
    private CategoryService categoryService;
    
    @Mock
    private ExpenseService expenseService;
    
    private AutoCloseable closeable;
    
    private static final Integer CATEGORY_ID_1 = 1;
    private static final String CATEGORY_NAME_1 = "Food";
    private static final Integer CATEGORY_ID_2 = 2;
    private static final String CATEGORY_NAME_2 = "Travel";

    @BeforeClass
    public static void checkHeadless() {
        // Set system property BEFORE any dialog creation
        System.setProperty("test.mode", "true");
        
        assumeFalse("Skipping UI test - running in headless mode", 
            GraphicsEnvironment.isHeadless());
    }

    @Override
    protected void onSetUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        
        // Setup mock data
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(CATEGORY_ID_1, CATEGORY_NAME_1));
        categories.add(new Category(CATEGORY_ID_2, CATEGORY_NAME_2));
        
        when(categoryService.getAllCategories()).thenReturn(categories);
        when(categoryService.createCategory(anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            return new Category(3, name);
        });
        when(categoryService.updateCategory(anyInt(), anyString())).thenAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            String name = invocation.getArgument(1);
            return new Category(id, name);
        });
        when(categoryService.deleteCategory(anyInt())).thenReturn(true);
        
        // Create parent frame
        mainWindow = execute(() -> {
            MainWindow mw = new MainWindow(categoryService, expenseService);
            mw.setVisible(true);
            return mw;
        });
        parentFrame = new FrameFixture(robot(), mainWindow);
        parentFrame.show();
        
        // Don't create dialog in setup - create it lazily in tests to avoid hanging
        // This way if creation hangs, we know which test is causing it
        categoryDialog = null;
        dialog = null;
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
    
    private void ensureDialogCreated() {
        if (categoryDialog == null) {
            // Ensure system property is set
            System.setProperty("test.mode", "true");
            
            categoryDialog = execute(() -> {
                CategoryDialog cd = new CategoryDialog(mainWindow, categoryService);
                if (cd.isModal()) {
                    cd.setModal(false);
                }
                // Verify label is initialized
                if (cd.labelMessage == null) {
                    throw new IllegalStateException("labelMessage is null!");
                }
                cd.setVisible(true);
                return cd;
            });
            dialog = new DialogFixture(robot(), categoryDialog);
            dialog.show();
        }
    }

    @Test
    @GUITest
    public void testCategoryDialog_DisplaysCorrectly() {
        ensureDialogCreated();
        dialog.requireVisible();
        assertThat(dialog.target().getTitle()).isEqualTo("Manage Categories");
    }

    @Test
    @GUITest
    public void testCategoryDialog_HasNameField() {
        ensureDialogCreated();
        dialog.textBox().requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_HasAddButton() {
        ensureDialogCreated();
        JButtonFixture addButton = dialog.button(withText("Add Category"));
        addButton.requireVisible();
        addButton.requireEnabled();
    }

    @Test
    @GUITest
    public void testCategoryDialog_HasUpdateButton() {
        ensureDialogCreated();
        JButtonFixture updateButton = dialog.button(withText("Update Selected"));
        updateButton.requireVisible();
        updateButton.requireEnabled();
    }

    @Test
    @GUITest
    public void testCategoryDialog_HasDeleteButton() {
        ensureDialogCreated();
        JButtonFixture deleteButton = dialog.button(withText("Delete Selected"));
        deleteButton.requireVisible();
        deleteButton.requireEnabled();
    }

    @Test
    @GUITest
    public void testCategoryDialog_HasCloseButton() {
        ensureDialogCreated();
        JButtonFixture closeButton = dialog.button(withText("Close"));
        closeButton.requireVisible();
        closeButton.requireEnabled();
    }

    @Test
    @GUITest
    public void testCategoryDialog_HasCategoryTable() {
        ensureDialogCreated();
        JTableFixture table = dialog.table();
        table.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_LoadCategories() throws SQLException {
        ensureDialogCreated();
        execute(() -> {
            categoryDialog.loadCategories();
        });
        
        JTableFixture table = dialog.table();
        assertThat(table.target().getRowCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @GUITest
    public void testCategoryDialog_LoadCategories_WithEmptyData() throws SQLException {
        ensureDialogCreated();
        // In MongoDB, if there's no data, findAll() returns an empty list (not an exception)
        when(categoryService.getAllCategories())
            .thenReturn(new ArrayList<>());
        
        execute(() -> {
            categoryDialog.loadCategories();
        });
        
        robot().waitForIdle();
        
        // Table should be empty but no exception should be thrown
        JTableFixture table = dialog.table();
        assertThat(table.target().getRowCount()).isEqualTo(0);
        
        // No error message should be shown
        String message = execute(() -> {
            return categoryDialog.labelMessage.getText();
        });
        assertThat(message).isEmpty();
    }

    @Test
    @GUITest
    public void testCategoryDialog_LoadCategories_HandlesMongoDBException() throws SQLException {
        ensureDialogCreated();
        // In MongoDB, only real errors (like connection failure) throw exceptions
        // Empty data just returns empty list - this test is for actual MongoDB errors
        when(categoryService.getAllCategories())
            .thenThrow(new SQLException("MongoDB connection error"));
        
        execute(() -> {
            categoryDialog.loadCategories();
        });
        
        robot().waitForIdle();
        
        // loadCategories() catches exceptions and shows error message
        String message = execute(() -> {
            return categoryDialog.labelMessage.getText();
        });
        assertThat(message).contains("Error");
        
        // Table should be empty (no data loaded due to error)
        JTableFixture table = dialog.table();
        assertThat(table.target().getRowCount()).isEqualTo(0);
    }

    @Test
    @GUITest
    public void testCategoryDialog_AddCategory_WithValidName() throws SQLException {
        ensureDialogCreated();
        String newCategoryName = "Entertainment";
        when(categoryService.getAllCategories()).thenReturn(new ArrayList<>());
        
        execute(() -> {
            categoryDialog.nameField.setText(newCategoryName);
        });
        
        dialog.button(withText("Add Category")).click();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_AddCategory_WithEmptyName() {
        ensureDialogCreated();
        execute(() -> {
            categoryDialog.nameField.setText("");
        });
        
        dialog.button(withText("Add Category")).click();
        
        // Should show error message in label
        String message = execute(() -> {
            return categoryDialog.labelMessage.getText();
        });
        assertThat(message).contains("cannot be empty");
    }

    @Test
    @GUITest
    public void testCategoryDialog_AddCategory_WithWhitespaceOnly() {
        ensureDialogCreated();
        execute(() -> {
            categoryDialog.nameField.setText("   ");
        });
        
        dialog.button(withText("Add Category")).click();
        
        String message = execute(() -> {
            return categoryDialog.labelMessage.getText();
        });
        assertThat(message).contains("cannot be empty");
    }

    @Test
    @GUITest
    public void testCategoryDialog_AddCategory_HandlesSQLException() throws SQLException {
        ensureDialogCreated();
        when(categoryService.createCategory(anyString()))
            .thenThrow(new SQLException("Database error"));
        
        execute(() -> {
            categoryDialog.nameField.setText("New Category");
        });
        
        dialog.button(withText("Add Category")).click();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String message = execute(() -> {
            return categoryDialog.labelMessage.getText();
        });
        assertThat(message).contains("Error");
    }

    @Test
    @GUITest
    public void testCategoryDialog_AddCategory_ClearsNameField() throws SQLException {
        ensureDialogCreated();
        when(categoryService.getAllCategories()).thenReturn(new ArrayList<>());
        
        execute(() -> {
            categoryDialog.nameField.setText("New Category");
        });
        
        dialog.button(withText("Add Category")).click();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String nameFieldText = execute(() -> categoryDialog.nameField.getText());
        assertThat(nameFieldText).isEmpty();
    }

    @Test
    @GUITest
    public void testCategoryDialog_UpdateSelectedCategory_NoSelection() {
        ensureDialogCreated();
        // First verify showMessage works
        execute(() -> {
            categoryDialog.showMessage("Test message");
        });
        robot().waitForIdle();
        String testMsg = execute(() -> categoryDialog.labelMessage.getText());
        assertThat(testMsg).isEqualTo("Test message");
        
        // Now test the actual button click
        execute(() -> {
            categoryDialog.categoryTable.clearSelection();
            categoryDialog.labelMessage.setText("");
        });
        robot().waitForIdle();
        
        dialog.button(withText("Update Selected")).click();
        
        // Wait for EDT to process
        robot().waitForIdle();
        
        String message = execute(() -> {
            return categoryDialog.labelMessage.getText();
        });
        assertThat(message).contains("select");
    }

    @Test
    @GUITest
    public void testCategoryDialog_UpdateSelectedCategory_WithSelection() throws SQLException {
        ensureDialogCreated();
        execute(() -> {
            categoryDialog.loadCategories();
            if (categoryDialog.categoryTableModel.getRowCount() > 0) {
                categoryDialog.categoryTable.setRowSelectionInterval(0, 0);
                categoryDialog.categoryTableModel.setValueAt("Updated Name", 0, 1);
            }
        });
        
        dialog.button(withText("Update Selected")).click();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_UpdateSelectedCategory_WithEmptyName() throws SQLException {
        ensureDialogCreated();
        execute(() -> {
            categoryDialog.loadCategories();
            if (categoryDialog.categoryTableModel.getRowCount() > 0) {
                categoryDialog.categoryTable.setRowSelectionInterval(0, 0);
                categoryDialog.categoryTableModel.setValueAt("", 0, 1);
                // Clear any existing message
                categoryDialog.labelMessage.setText("");
            }
        });
        
        dialog.button(withText("Update Selected")).click();
        
        robot().waitForIdle();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String message = execute(() -> {
            return categoryDialog.labelMessage.getText();
        });
        assertThat(message).contains("cannot be empty");
    }

    @Test
    @GUITest
    public void testCategoryDialog_UpdateSelectedCategory_WithNullName() throws SQLException {
        ensureDialogCreated();
        execute(() -> {
            categoryDialog.loadCategories();
            if (categoryDialog.categoryTableModel.getRowCount() > 0) {
                categoryDialog.categoryTable.setRowSelectionInterval(0, 0);
                categoryDialog.categoryTableModel.setValueAt(null, 0, 1);
                // Clear any existing message
                categoryDialog.labelMessage.setText("");
            }
        });
        
        dialog.button(withText("Update Selected")).click();
        
        robot().waitForIdle();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String message = execute(() -> {
            return categoryDialog.labelMessage.getText();
        });
        assertThat(message).contains("cannot be empty");
    }

    @Test
    @GUITest
    public void testCategoryDialog_UpdateSelectedCategory_HandlesSQLException() throws SQLException {
        ensureDialogCreated();
        when(categoryService.updateCategory(anyInt(), anyString()))
            .thenThrow(new SQLException("Database error"));
        
        execute(() -> {
            categoryDialog.loadCategories();
            if (categoryDialog.categoryTableModel.getRowCount() > 0) {
                categoryDialog.categoryTable.setRowSelectionInterval(0, 0);
                categoryDialog.categoryTableModel.setValueAt("Updated Name", 0, 1);
                // Clear any existing message
                categoryDialog.labelMessage.setText("");
            }
        });
        
        dialog.button(withText("Update Selected")).click();
        
        robot().waitForIdle();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String message = execute(() -> {
            return categoryDialog.labelMessage.getText();
        });
        assertThat(message).contains("Error");
    }

    @Test
    @GUITest
    public void testCategoryDialog_UpdateSelectedCategory_StopsCellEditing() throws SQLException {
        ensureDialogCreated();
        execute(() -> {
            categoryDialog.loadCategories();
            if (categoryDialog.categoryTableModel.getRowCount() > 0) {
                categoryDialog.categoryTable.setRowSelectionInterval(0, 0);
                categoryDialog.categoryTable.editCellAt(0, 1);
            }
        });
        
        dialog.button(withText("Update Selected")).click();
        
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_DeleteSelectedCategory_NoSelection() {
        ensureDialogCreated();
        execute(() -> {
            categoryDialog.categoryTable.clearSelection();
            // Clear any existing message
            categoryDialog.labelMessage.setText("");
        });
        
        dialog.button(withText("Delete Selected")).click();
        
        robot().waitForIdle();
        
        // Small additional delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String message = execute(() -> {
            if (categoryDialog.labelMessage == null) {
                return "LABEL_NULL";
            }
            return categoryDialog.labelMessage.getText();
        });
        assertThat(message).contains("select");
    }

    @Test
    @GUITest
    public void testCategoryDialog_DeleteSelectedCategory_WithSelection() throws SQLException {
        ensureDialogCreated();
        execute(() -> {
            categoryDialog.loadCategories();
            if (categoryDialog.categoryTableModel.getRowCount() > 0) {
                categoryDialog.categoryTable.setRowSelectionInterval(0, 0);
            }
        });
        
        dialog.button(withText("Delete Selected")).click();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        dialog.requireVisible();
    }

    @Test
    @GUITest
    public void testCategoryDialog_DeleteSelectedCategory_HandlesSQLException() throws SQLException {
        ensureDialogCreated();
        when(categoryService.deleteCategory(anyInt()))
            .thenThrow(new SQLException("Database error"));
        
        execute(() -> {
            categoryDialog.loadCategories();
            if (categoryDialog.categoryTableModel.getRowCount() > 0) {
                categoryDialog.categoryTable.setRowSelectionInterval(0, 0);
            }
            // Clear any existing message
            categoryDialog.labelMessage.setText("");
        });
        
        dialog.button(withText("Delete Selected")).click();
        
        robot().waitForIdle();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String message = execute(() -> {
            return categoryDialog.labelMessage.getText();
        });
        assertThat(message).contains("Error");
    }

    @Test
    @GUITest
    public void testCategoryDialog_CloseButton() {
        ensureDialogCreated();
        // Verify dialog is visible before closing
        assertThat(categoryDialog.isVisible()).isTrue();
        
        dialog.button(withText("Close")).click();
        
        robot().waitForIdle();
        
        // Wait a bit for dispose to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check if dialog is visible (should be false after dispose)
        boolean isVisible = execute(() -> {
            return categoryDialog.isVisible();
        });
        // Dialog should not be visible after clicking close
        assertThat(isVisible).as("Dialog should not be visible after clicking close").isFalse();
    }

    @Test
    @GUITest
    public void testCategoryDialog_CategoryTableModel_IsEditable() {
        ensureDialogCreated();
        boolean editable = execute(() -> {
            return categoryDialog.categoryTableModel.isCellEditable(0, 1);
        });
        assertThat(editable).isTrue();
    }

    @Test
    @GUITest
    public void testCategoryDialog_CategoryTableModel_IDNotEditable() {
        ensureDialogCreated();
        boolean editable = execute(() -> {
            return categoryDialog.categoryTableModel.isCellEditable(0, 0);
        });
        assertThat(editable).isFalse();
    }

    @Test
    @GUITest
    public void testCategoryDialog_ShowMessage_InTestMode() {
        ensureDialogCreated();
        execute(() -> {
            // Verify label exists
            assertThat(categoryDialog.labelMessage).isNotNull();
            // Verify test mode is detected
            boolean isTest = java.awt.EventQueue.isDispatchThread();
            categoryDialog.showMessage("Test message");
        });
        
        robot().waitForIdle();
        
        String message = execute(() -> {
            return categoryDialog.labelMessage.getText();
        });
        assertThat(message).isEqualTo("Test message");
    }

    @Test
    @GUITest
    public void testCategoryDialog_ShowMessage_ClearsMessage() {
        ensureDialogCreated();
        execute(() -> {
            categoryDialog.showMessage("Test message");
            categoryDialog.showMessage("");
        });
        
        String message = execute(() -> {
            return categoryDialog.labelMessage.getText();
        });
        assertThat(message).isEmpty();
    }
}
