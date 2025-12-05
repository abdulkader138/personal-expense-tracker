package com.mycompany.pet.service;

import java.sql.SQLException;
import java.util.List;

import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.model.Category;

/**
 * Service layer for category management.
 */
public class CategoryService {
    private CategoryDAO categoryDAO;

    public CategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    public Category createCategory(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        Category category = new Category(name.trim());
        return categoryDAO.create(category);
    }

    public Category getCategory(Integer categoryId) throws SQLException {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        return categoryDAO.findById(categoryId);
    }

    public List<Category> getAllCategories() throws SQLException {
        return categoryDAO.findAll();
    }

    public Category updateCategory(Integer categoryId, String name) throws SQLException {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        Category category = categoryDAO.findById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("Category not found");
        }
        category.setName(name.trim());
        return categoryDAO.update(category);
    }

    public boolean deleteCategory(Integer categoryId) throws SQLException {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        return categoryDAO.delete(categoryId);
    }
}

