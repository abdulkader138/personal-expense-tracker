package com.mycompany.pet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.model.Category;

/**
 * Parameterized test example for CategoryService using JUnit 5.
 * 
 * This demonstrates JUnit 5 parameterized tests as covered in 
 * "Test-Driven Development, Build Automation, Continuous Integration" book.
 * JUnit 5 provides more flexible parameterized test options than JUnit 4.
 */
@ExtendWith(MockitoExtension.class)
public class CategoryServiceJUnit5ParameterizedTest {

    @Mock
    private CategoryDAO categoryDAO;

    private CategoryService categoryService;

    @BeforeEach
    public void setUp() {
        categoryService = new CategoryService(categoryDAO);
    }

    // Method source for parameterized test
    static Stream<Arguments> categoryNamesProvider() {
        return Stream.of(
            Arguments.of("Food", "Food"),
            Arguments.of("Travel", "Travel"),
            Arguments.of("Bills", "Bills"),
            Arguments.of("Entertainment", "Entertainment"),
            Arguments.of("  Food  ", "Food"), 
            Arguments.of("Food\n", "Food") 
        );
    }

    @ParameterizedTest(name = "Create category with name: {0}, expected: {1}")
    @MethodSource("categoryNamesProvider")
    public void testCreateCategoryWithVariousNames(String categoryName, String expectedName) throws SQLException {
        // Given
        when(categoryDAO.create(any(Category.class))).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            cat.setCategoryId(1);
            return cat;
        });

        // When
        Category result = categoryService.createCategory(categoryName);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getCategoryId());
        assertEquals(expectedName, result.getName());
    }

    // Example with @ValueSource for simple values
    @ParameterizedTest(name = "Get category with ID: {0}")
    @org.junit.jupiter.params.provider.ValueSource(ints = { 1, 2, 3, 100, 999 })
    public void testGetCategoryWithVariousIds(int categoryId) throws SQLException {
        // Given
        Category expectedCategory = new Category(categoryId, "Category " + categoryId);
        when(categoryDAO.findById(categoryId)).thenReturn(expectedCategory);

        // When
        Category result = categoryService.getCategory(categoryId);

        // Then
        assertNotNull(result);
        assertEquals(categoryId, result.getCategoryId());
        assertEquals("Category " + categoryId, result.getName());
    }
}

