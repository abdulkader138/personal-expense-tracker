package com.mycompany.pet.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.model.Category;

/**
 * Unit tests for CategoryService.
 */
public class CategoryServiceTest {
    @Mock
    private CategoryDAO categoryDAO;

    private CategoryService categoryService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        categoryService = new CategoryService(categoryDAO);
    }

    @Test
    public void testCreateCategory_Success() throws SQLException {
        // Given
        String name = "Food";
        when(categoryDAO.create(any(Category.class))).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            cat.setCategoryId(1);
            return cat;
        });

        // When
        Category result = categoryService.createCategory(name);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getCategoryId());
        assertEquals(name, result.getName());
        verify(categoryDAO, times(1)).create(any(Category.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategory_EmptyName() throws SQLException {
        categoryService.createCategory("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategory_NullName() throws SQLException {
        categoryService.createCategory(null);
    }

    @Test
    public void testGetCategory_Success() throws SQLException {
        // Given
        Integer categoryId = 1;
        when(categoryDAO.findById(categoryId)).thenReturn(new Category(categoryId, "Food"));

        // When
        Category result = categoryService.getCategory(categoryId);

        // Then
        assertNotNull(result);
        assertEquals(new Category(categoryId, "Food"), result);
        verify(categoryDAO, times(1)).findById(categoryId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCategory_NullId() throws SQLException {
        categoryService.getCategory(null);
    }

    @Test
    public void testGetAllCategories_Success() throws SQLException {
        // Given
        List<Category> expectedCategories = Arrays.asList(
            new Category(1, "Food"),
            new Category(2, "Travel")
        );
        when(categoryDAO.findAll()).thenReturn(expectedCategories);

        // When
        List<Category> result = categoryService.getAllCategories();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(categoryDAO, times(1)).findAll();
    }

    @Test
    public void testUpdateCategory_Success() throws SQLException {
        // Given
        Integer categoryId = 1;
        String newName = "Updated Food";
        Category existingCategory = new Category(categoryId, "Food");
        when(categoryDAO.findById(categoryId)).thenReturn(existingCategory);
        when(categoryDAO.update(any(Category.class))).thenReturn(existingCategory);

        // When
        Category result = categoryService.updateCategory(categoryId, newName);

        // Then
        assertNotNull(result);
        assertEquals(newName, result.getName());
        verify(categoryDAO, times(1)).findById(categoryId);
        verify(categoryDAO, times(1)).update(any(Category.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateCategory_NullId() throws SQLException {
        categoryService.updateCategory(null, "Name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateCategory_EmptyName() throws SQLException {
        categoryService.updateCategory(1, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateCategory_NotFound() throws SQLException {
        when(categoryDAO.findById(1)).thenReturn(null);
        categoryService.updateCategory(1, "Name");
    }

    @Test
    public void testDeleteCategory_Success() throws SQLException {
        // Given
        Integer categoryId = 1;
        when(categoryDAO.delete(categoryId)).thenReturn(true);

        // When
        boolean result = categoryService.deleteCategory(categoryId);

        // Then
        assertTrue(result);
        verify(categoryDAO, times(1)).delete(categoryId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteCategory_NullId() throws SQLException {
        categoryService.deleteCategory(null);
    }

    @Test
    public void testDeleteCategory_NotFound() throws SQLException {
        // Given - category doesn't exist
        Integer categoryId = 999;
        when(categoryDAO.delete(categoryId)).thenReturn(false);

        // When
        boolean result = categoryService.deleteCategory(categoryId);

        // Then
        assertFalse(result);
        verify(categoryDAO, times(1)).delete(categoryId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategory_WhitespaceOnlyName() throws SQLException {
        categoryService.createCategory("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateCategory_WhitespaceOnlyName() throws SQLException {
        Category existingCategory = new Category(1, "Food");
        when(categoryDAO.findById(1)).thenReturn(existingCategory);
        categoryService.updateCategory(1, "   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateCategory_NullName() throws SQLException {
        Category existingCategory = new Category(1, "Food");
        when(categoryDAO.findById(1)).thenReturn(existingCategory);
        categoryService.updateCategory(1, null);
    }
}

