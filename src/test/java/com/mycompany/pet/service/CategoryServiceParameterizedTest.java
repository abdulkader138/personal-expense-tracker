package com.mycompany.pet.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.model.Category;

/**
 * Parameterized test example for CategoryService using JUnit 4.
 * 
 * Parameterized tests allow testing multiple inputs with the same test logic.
 */
@RunWith(Parameterized.class)
public class CategoryServiceParameterizedTest {

    @Mock
    private CategoryDAO categoryDAO;

    private CategoryService categoryService;

    // Parameters for the test
    private final String categoryName;
    private final boolean shouldSucceed;
    private final String expectedName;

    // Constructor receives parameters
    public CategoryServiceParameterizedTest(String categoryName, boolean shouldSucceed, String expectedName) {
        this.categoryName = categoryName;
        this.shouldSucceed = shouldSucceed;
        this.expectedName = expectedName;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        categoryService = new CategoryService(categoryDAO);
    }

    // Define test data
    @Parameters(name = "Category: {0}, Should succeed: {1}, Expected: {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "Food", true, "Food" },
            { "Travel", true, "Travel" },
            { "Bills", true, "Bills" },
            { "Entertainment", true, "Entertainment" },
            { "  Food  ", true, "Food" }, 
            { "Food\n", true, "Food" }, 
        });
    }

    @Test
    public void testCreateCategoryWithVariousNames() throws SQLException {
        // Given
        when(categoryDAO.create(any(Category.class))).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            cat.setCategoryId(1);
            return cat;
        });

        // When
        Category result = categoryService.createCategory(categoryName);

        // Then
        if (shouldSucceed) {
            assertNotNull(result);
            assertEquals(Integer.valueOf(1), result.getCategoryId());
            assertEquals(expectedName, result.getName());
        }
    }
}

