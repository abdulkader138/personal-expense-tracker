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
        new Thread(() -> {
            try {
                List<Category> categories = categoryService.getAllCategories();
                javax.swing.SwingUtilities.invokeLater(() -> onSuccess.accept(categories));
            } catch (SQLException e) {
                String errorMsg = "Error loading categories: " + e.getMessage();
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(errorMsg));
            }
        }).start();
    }
    
    /**
     * Create a new category.
     * 
     * @param name Category name
     * @param onSuccess Callback with created category
     * @param onError Callback with error message
     */
    public void createCategory(String name, Consumer<Category> onSuccess, Consumer<String> onError) {
        if (name == null || name.trim().isEmpty()) {
            javax.swing.SwingUtilities.invokeLater(() -> 
                onError.accept("Category name cannot be empty."));
            return;
        }
        
        new Thread(() -> {
            try {
                Category category = categoryService.createCategory(name.trim());
                javax.swing.SwingUtilities.invokeLater(() -> onSuccess.accept(category));
            } catch (SQLException e) {
                String errorMsg = "Error adding category: " + e.getMessage();
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(errorMsg));
            }
        }).start();
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
        if (name == null || name.trim().isEmpty()) {
            javax.swing.SwingUtilities.invokeLater(() -> 
                onError.accept("Category name cannot be empty."));
            return;
        }
        
        new Thread(() -> {
            try {
                Category category = categoryService.updateCategory(categoryId, name.trim());
                javax.swing.SwingUtilities.invokeLater(() -> onSuccess.accept(category));
            } catch (SQLException e) {
                String errorMsg = "Error updating category: " + e.getMessage();
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(errorMsg));
            } catch (IllegalArgumentException e) {
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
            }
        }).start();
    }
    
    /**
     * Delete a category.
     * 
     * @param categoryId Category ID
     * @param onSuccess Callback when deletion succeeds
     * @param onError Callback with error message
     */
    public void deleteCategory(Integer categoryId, Runnable onSuccess, Consumer<String> onError) {
        new Thread(() -> {
            try {
                categoryService.deleteCategory(categoryId);
                javax.swing.SwingUtilities.invokeLater(onSuccess);
            } catch (SQLException e) {
                String errorMsg = "Error deleting category: " + e.getMessage();
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(errorMsg));
            } catch (IllegalArgumentException e) {
                javax.swing.SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
            }
        }).start();
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
}

