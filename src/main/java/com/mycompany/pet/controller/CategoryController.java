package com.mycompany.pet.controller;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

import com.mycompany.pet.model.Category;
import com.mycompany.pet.service.CategoryService;

/**
 * Controller for Category operations.
 * Separates UI concerns from business logic.
 * 
 * This controller handles:
 * - Category CRUD operations
 * - Error handling and user feedback
 * - Thread-safe database operations
 */
public class CategoryController {
    private final CategoryService categoryService;
    
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    /**
     * Load all categories.
     * 
     * @param onSuccess Callback with list of categories
     * @param onError Callback with error message
     */
    public void loadCategories(Consumer<List<Category>> onSuccess, Consumer<String> onError) {
        executeAsync(() -> {
            try {
                List<Category> categories = categoryService.getAllCategories();
                invokeOnSwingThread(() -> onSuccess.accept(categories));
            } catch (IllegalArgumentException e) {
                invokeOnSwingThread(() -> onError.accept(e.getMessage()));
            } catch (SQLException | RuntimeException e) {
                handleError("Error loading categories", e.getMessage(), onError);
            }
        });
    }
    
    /**
     * Create a new category.
     * 
     * @param name Category name
     * @param onSuccess Callback with created category
     * @param onError Callback with error message
     */
    public void createCategory(String name, Consumer<Category> onSuccess, Consumer<String> onError) {
        if (!validateCategoryName(name, onError)) {
            return;
        }
        
        executeAsync(() -> {
            try {
                Category category = categoryService.createCategory(name.trim());
                invokeOnSwingThread(() -> onSuccess.accept(category));
            } catch (IllegalArgumentException e) {
                invokeOnSwingThread(() -> onError.accept(e.getMessage()));
            } catch (SQLException | RuntimeException e) {
                handleError("Error adding category", e.getMessage(), onError);
            }
        });
    }
    
    /**
     * Update an existing category.
     * 
     * @param categoryId Category ID
     * @param name New category name
     * @param onSuccess Callback with updated category
     * @param onError Callback with error message
     */
    public void updateCategory(Integer categoryId, String name, 
                               Consumer<Category> onSuccess, Consumer<String> onError) {
        if (!validateCategoryName(name, onError)) {
            return;
        }
        
        executeAsync(() -> {
            try {
                Category category = categoryService.updateCategory(categoryId, name.trim());
                invokeOnSwingThread(() -> onSuccess.accept(category));
            } catch (IllegalArgumentException e) {
                invokeOnSwingThread(() -> onError.accept(e.getMessage()));
            } catch (SQLException | RuntimeException e) {
                handleError("Error updating category", e.getMessage(), onError);
            }
        });
    }
    
    /**
     * Delete a category.
     * 
     * @param categoryId Category ID
     * @param onSuccess Callback when deletion succeeds
     * @param onError Callback with error message
     */
    public void deleteCategory(Integer categoryId, Runnable onSuccess, Consumer<String> onError) {
        executeAsync(() -> {
            try {
                categoryService.deleteCategory(categoryId);
                invokeOnSwingThread(onSuccess);
            } catch (IllegalArgumentException e) {
                invokeOnSwingThread(() -> onError.accept(e.getMessage()));
            } catch (SQLException | RuntimeException e) {
                handleError("Error deleting category", e.getMessage(), onError);
            }
        });
    }
    
    /**
     * Get a category by ID.
     * 
     * @param categoryId Category ID
     * @return Category or null if not found
     * @throws SQLException if database error occurs
     */
    public Category getCategory(Integer categoryId) throws SQLException {
        return categoryService.getCategory(categoryId);
    }
    
    /**
     * Validates category name and invokes error callback if invalid.
     * 
     * @param name Category name to validate
     * @param onError Error callback
     * @return true if valid, false otherwise
     */
    private boolean validateCategoryName(String name, Consumer<String> onError) {
        if (name == null || name.trim().isEmpty()) {
            invokeOnSwingThread(() -> onError.accept("Category name cannot be empty."));
            return false;
        }
        return true;
    }
    
    /**
     * Executes an operation asynchronously in a daemon thread.
     * 
     * @param operation The operation to execute
     */
    private void executeAsync(Runnable operation) {
        Thread thread = new Thread(operation);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Handles errors by formatting message and invoking error callback on Swing thread.
     * 
     * @param errorPrefix Prefix for error message
     * @param errorMessage Error message
     * @param onError Error callback
     */
    private void handleError(String errorPrefix, String errorMessage, Consumer<String> onError) {
        String errorMsg = errorPrefix + ": " + errorMessage;
        invokeOnSwingThread(() -> onError.accept(errorMsg));
    }
    
    /**
     * Invokes a runnable on the Swing event dispatch thread.
     * 
     * @param runnable The runnable to execute
     */
    private void invokeOnSwingThread(Runnable runnable) {
        javax.swing.SwingUtilities.invokeLater(runnable);
    }
}

