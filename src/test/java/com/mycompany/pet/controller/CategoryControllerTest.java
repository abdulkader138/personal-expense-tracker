package com.mycompany.pet.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import com.mycompany.pet.model.Category;
import com.mycompany.pet.service.CategoryService;

/**
 * Tests for CategoryController.
 */
public class CategoryControllerTest {
    private CategoryService categoryService;
    
    private CategoryController controller;
    private AutoCloseable closeable;
    
    private static final Integer CATEGORY_ID_1 = 1;
    private static final String CATEGORY_NAME_1 = "Food";
    
    @Before
    public void setUp() {
        categoryService = org.mockito.Mockito.mock(CategoryService.class);
        controller = new CategoryController(categoryService);
    }
    
    @org.junit.After
    public void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }
    
    @Test
    public void testCreateCategory_Success() throws InterruptedException, SQLException {
        // Given
        Category expectedCategory = new Category(CATEGORY_ID_1, CATEGORY_NAME_1);
        when(categoryService.createCategory(CATEGORY_NAME_1)).thenReturn(expectedCategory);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Category> result = new AtomicReference<>();
        
        // When
        controller.createCategory(CATEGORY_NAME_1,
            category -> {
                result.set(category);
                latch.countDown();
            },
            error -> {
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNotNull();
        assertThat(result.get().getCategoryId()).isEqualTo(CATEGORY_ID_1);
        verify(categoryService, timeout(2000)).createCategory(CATEGORY_NAME_1);
    }
    
    @Test
    public void testCreateCategory_WithInvalidNames() throws InterruptedException {
        String[] invalidNames = { null, "", "   " };
        
        for (String invalidName : invalidNames) {
            // Given
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<String> errorResult = new AtomicReference<>();
            
            // When
            controller.createCategory(invalidName,
                category -> latch.countDown(),
                error -> {
                    errorResult.set(error);
                    latch.countDown();
                }
            );
            
            // Then
            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(errorResult.get()).isNotNull().isEqualTo("Category name cannot be empty.");
            // Service should not be called when name is invalid
        }
    }
    
    @Test
    public void testCreateCategory_SQLException() throws InterruptedException, SQLException {
        // Given
        when(categoryService.createCategory(CATEGORY_NAME_1)).thenThrow(new SQLException("Database error"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.createCategory(CATEGORY_NAME_1,
            category -> {
                latch.countDown();
            },
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(errorResult.get()).isNotNull().contains("Error adding category");
        verify(categoryService, timeout(2000)).createCategory(CATEGORY_NAME_1);
    }
    
    @Test
    public void testUpdateCategory_Success() throws InterruptedException, SQLException {
        // Given
        Category expectedCategory = new Category(CATEGORY_ID_1, CATEGORY_NAME_1);
        when(categoryService.updateCategory(CATEGORY_ID_1, CATEGORY_NAME_1)).thenReturn(expectedCategory);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Category> result = new AtomicReference<>();
        
        // When
        controller.updateCategory(CATEGORY_ID_1, CATEGORY_NAME_1,
            category -> {
                result.set(category);
                latch.countDown();
            },
            error -> {
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNotNull();
        assertThat(result.get().getCategoryId()).isEqualTo(CATEGORY_ID_1);
        verify(categoryService, timeout(2000)).updateCategory(CATEGORY_ID_1, CATEGORY_NAME_1);
    }
    
    @Test
    public void testUpdateCategory_WithInvalidNames() throws InterruptedException {
        String[] invalidNames = { null, "", "   " };
        
        for (String invalidName : invalidNames) {
            // Given
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<String> errorResult = new AtomicReference<>();
            
            // When
            controller.updateCategory(CATEGORY_ID_1, invalidName,
                category -> latch.countDown(),
                error -> {
                    errorResult.set(error);
                    latch.countDown();
                }
            );
            
            // Then
            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(errorResult.get()).isNotNull().isEqualTo("Category name cannot be empty.");
            // Service should not be called when name is invalid
        }
    }
    
    @Test
    public void testUpdateCategory_SQLException() throws InterruptedException, SQLException {
        // Given
        when(categoryService.updateCategory(CATEGORY_ID_1, CATEGORY_NAME_1))
            .thenThrow(new SQLException("Database error"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.updateCategory(CATEGORY_ID_1, CATEGORY_NAME_1,
            category -> {
                latch.countDown();
            },
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(errorResult.get()).isNotNull().contains("Error updating category");
        verify(categoryService, timeout(2000)).updateCategory(CATEGORY_ID_1, CATEGORY_NAME_1);
    }
    
    @Test
    public void testUpdateCategory_IllegalArgumentException() throws InterruptedException, SQLException {
        // Given
        when(categoryService.updateCategory(CATEGORY_ID_1, CATEGORY_NAME_1))
            .thenThrow(new IllegalArgumentException("Category not found"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.updateCategory(CATEGORY_ID_1, CATEGORY_NAME_1,
            category -> {
                latch.countDown();
            },
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(errorResult.get()).isEqualTo("Category not found");
        verify(categoryService, timeout(2000)).updateCategory(CATEGORY_ID_1, CATEGORY_NAME_1);
    }
    
    @Test
    public void testDeleteCategory_Success() throws InterruptedException, SQLException {
        // Given
        when(categoryService.deleteCategory(CATEGORY_ID_1)).thenReturn(true);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> successCalled = new AtomicReference<>(false);
        
        // When
        controller.deleteCategory(CATEGORY_ID_1,
            () -> {
                successCalled.set(true);
                latch.countDown();
            },
            error -> {
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(successCalled.get()).isTrue();
        verify(categoryService, timeout(2000)).deleteCategory(CATEGORY_ID_1);
    }
    
    @Test
    public void testDeleteCategory_SQLException() throws InterruptedException, SQLException {
        // Given
        when(categoryService.deleteCategory(CATEGORY_ID_1)).thenThrow(new SQLException("Database error"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.deleteCategory(CATEGORY_ID_1,
            latch::countDown,
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(errorResult.get()).isNotNull().contains("Error deleting category");
        verify(categoryService, timeout(2000)).deleteCategory(CATEGORY_ID_1);
    }
    
    @Test
    public void testDeleteCategory_IllegalArgumentException() throws InterruptedException, SQLException {
        // Given
        when(categoryService.deleteCategory(CATEGORY_ID_1))
            .thenThrow(new IllegalArgumentException("Category not found"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.deleteCategory(CATEGORY_ID_1,
            latch::countDown,
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(errorResult.get()).isEqualTo("Category not found");
        verify(categoryService, timeout(2000)).deleteCategory(CATEGORY_ID_1);
    }
    
    @Test
    public void testLoadCategories_Success() throws InterruptedException, SQLException {
        // Given
        List<Category> expectedCategories = new ArrayList<>();
        expectedCategories.add(new Category(CATEGORY_ID_1, CATEGORY_NAME_1));
        when(categoryService.getAllCategories()).thenReturn(expectedCategories);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Category>> result = new AtomicReference<>();
        
        // When
        controller.loadCategories(
            categories -> {
                result.set(categories);
                latch.countDown();
            },
            error -> {
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNotNull();
        assertThat(result.get()).hasSize(1);
        verify(categoryService, timeout(2000)).getAllCategories();
    }
    
    @Test
    public void testLoadCategories_SQLException() throws InterruptedException, SQLException {
        // Given
        when(categoryService.getAllCategories()).thenThrow(new SQLException("Database error"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.loadCategories(
            categories -> {
                latch.countDown();
            },
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(errorResult.get()).isNotNull().contains("Error loading categories");
        verify(categoryService, timeout(2000)).getAllCategories();
    }
    
    @Test
    public void testGetCategory() throws SQLException {
        // Given
        Category expectedCategory = new Category(CATEGORY_ID_1, CATEGORY_NAME_1);
        when(categoryService.getCategory(CATEGORY_ID_1)).thenReturn(expectedCategory);
        
        // When
        Category result = controller.getCategory(CATEGORY_ID_1);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCategoryId()).isEqualTo(CATEGORY_ID_1);
        verify(categoryService).getCategory(CATEGORY_ID_1);
    }
    
    @Test
    public void testGetCategory_NotFound() throws SQLException {
        // Given
        when(categoryService.getCategory(CATEGORY_ID_1)).thenReturn(null);
        
        // When
        Category result = controller.getCategory(CATEGORY_ID_1);
        
        // Then
        assertThat(result).isNull();
        verify(categoryService).getCategory(CATEGORY_ID_1);
    }
    
    @Test(expected = SQLException.class)
    public void testGetCategory_SQLException() throws SQLException {
        // Given
        when(categoryService.getCategory(CATEGORY_ID_1)).thenThrow(new SQLException("Database error"));
        
        // When
        controller.getCategory(CATEGORY_ID_1);
        
        // Then - exception should be thrown
    }
}

