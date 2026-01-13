package com.mycompany.pet.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
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
 * Example test using Hamcrest matchers instead of JUnit assertions.
 * 
 */
public class CategoryServiceHamcrestTest {
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

        // Then - using Hamcrest matchers
        assertThat(result, is(notNullValue()));
        assertThat(result.getCategoryId(), is(equalTo(1)));
        assertThat(result.getName(), is(equalTo(name)));
        assertThat(result, isA(Category.class));
        verify(categoryDAO, times(1)).create(any(Category.class));
    }

    @Test
    public void testGetCategory_Success() throws SQLException {
        // Given
        Integer categoryId = 1;
        Category expectedCategory = new Category(categoryId, "Food");
        when(categoryDAO.findById(categoryId)).thenReturn(expectedCategory);

        // When
        Category result = categoryService.getCategory(categoryId);

        // Then - using Hamcrest matchers
        assertThat(result, is(notNullValue()));
        assertThat(result, is(equalTo(expectedCategory)));
        assertThat(result.getCategoryId(), is(equalTo(categoryId)));
        verify(categoryDAO, times(1)).findById(categoryId);
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

        // Then - using Hamcrest matchers
        assertThat(result, is(notNullValue()));
        assertThat(result, is(not(empty())));
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getName(), is(equalTo("Food")));
        assertThat(result.get(1).getName(), is(equalTo("Travel")));
        verify(categoryDAO, times(1)).findAll();
    }

    @Test
    public void testGetCategory_NotFound() throws SQLException {
        // Given
        Integer categoryId = 999;
        when(categoryDAO.findById(categoryId)).thenReturn(null);

        // When
        Category result = categoryService.getCategory(categoryId);

        // Then - using Hamcrest matchers
        assertThat(result, is(nullValue()));
        verify(categoryDAO, times(1)).findById(categoryId);
    }

    @Test
    public void testDeleteCategory_Success() throws SQLException {
        // Given
        Integer categoryId = 1;
        when(categoryDAO.delete(categoryId)).thenReturn(true);

        // When
        boolean result = categoryService.deleteCategory(categoryId);

        // Then - using Hamcrest matchers
        assertThat(result, is(true));
        assertThat(result, is(equalTo(true)));
        verify(categoryDAO, times(1)).delete(categoryId);
    }
}

