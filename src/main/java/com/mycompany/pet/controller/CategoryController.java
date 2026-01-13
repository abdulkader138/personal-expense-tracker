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
    
    public Category getCategory(Integer categoryId) throws SQLException {
        return categoryService.getCategory(categoryId);
    }
    
    private boolean validateCategoryName(String name, Consumer<String> onError) {
        if (name == null || name.trim().isEmpty()) {
            invokeOnSwingThread(() -> onError.accept("Category name cannot be empty."));
            return false;
        }
        return true;
    }
    
    private void executeAsync(Runnable operation) {
        Thread thread = new Thread(operation);
        thread.setDaemon(true);
        thread.start();
    }
    
    private void handleError(String errorPrefix, String errorMessage, Consumer<String> onError) {
        String errorMsg = errorPrefix + ": " + errorMessage;
        invokeOnSwingThread(() -> onError.accept(errorMsg));
    }
    
    private void invokeOnSwingThread(Runnable runnable) {
        javax.swing.SwingUtilities.invokeLater(runnable);
    }
}

