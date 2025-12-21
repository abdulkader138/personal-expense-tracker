package com.mycompany.pet.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mycompany.pet.model.Expense;
import com.mycompany.pet.service.ExpenseService;

/**
 * Tests for ExpenseController.
 */
public class ExpenseControllerTest {
    @Mock
    private ExpenseService expenseService;
    
    private ExpenseController controller;
    private AutoCloseable closeable;
    
    private static final Integer EXPENSE_ID_1 = 1;
    private static final Integer CATEGORY_ID_1 = 1;
    private static final LocalDate DATE_1 = LocalDate.of(2024, 1, 15);
    private static final BigDecimal AMOUNT_1 = new BigDecimal("100.00");
    private static final String DESCRIPTION_1 = "Test Expense";
    
    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        controller = new ExpenseController(expenseService);
    }
    
    @org.junit.After
    public void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }
    
    @Test
    public void testGetExpense() throws SQLException {
        // Given
        Expense expectedExpense = new Expense(EXPENSE_ID_1, DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1);
        when(expenseService.getExpense(EXPENSE_ID_1)).thenReturn(expectedExpense);
        
        // When
        Expense result = controller.getExpense(EXPENSE_ID_1);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getExpenseId()).isEqualTo(EXPENSE_ID_1);
        verify(expenseService).getExpense(EXPENSE_ID_1);
    }
    
    @Test
    public void testGetExpense_NotFound() throws SQLException {
        // Given
        when(expenseService.getExpense(EXPENSE_ID_1)).thenReturn(null);
        
        // When
        Expense result = controller.getExpense(EXPENSE_ID_1);
        
        // Then
        assertThat(result).isNull();
        verify(expenseService).getExpense(EXPENSE_ID_1);
    }
    
    @Test
    public void testCreateExpense_Success() throws InterruptedException, SQLException {
        // Given
        Expense expectedExpense = new Expense(EXPENSE_ID_1, DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1);
        when(expenseService.createExpense(any(LocalDate.class), any(BigDecimal.class), 
            anyString(), anyInt())).thenReturn(expectedExpense);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Expense> result = new AtomicReference<>();
        
        // When
        controller.createExpense(DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1,
            expense -> {
                result.set(expense);
                latch.countDown();
            },
            error -> {
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNotNull();
        assertThat(result.get().getExpenseId()).isEqualTo(EXPENSE_ID_1);
        verify(expenseService, timeout(2000)).createExpense(DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1);
    }
    
    @Test
    public void testCreateExpense_SQLException() throws InterruptedException, SQLException {
        // Given
        when(expenseService.createExpense(any(LocalDate.class), any(BigDecimal.class), 
            anyString(), anyInt())).thenThrow(new SQLException("Database error"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.createExpense(DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1,
            expense -> {
                latch.countDown();
            },
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(errorResult.get()).isNotNull().contains("Error saving expense");
        verify(expenseService, timeout(2000)).createExpense(DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1);
    }
    
    @Test
    public void testCreateExpense_IllegalArgumentException() throws InterruptedException, SQLException {
        // Given
        when(expenseService.createExpense(any(LocalDate.class), any(BigDecimal.class), 
            anyString(), anyInt())).thenThrow(new IllegalArgumentException("Invalid expense"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.createExpense(DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1,
            expense -> {
                latch.countDown();
            },
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(errorResult.get()).isEqualTo("Invalid expense");
        verify(expenseService, timeout(2000)).createExpense(DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1);
    }
    
    @Test
    public void testUpdateExpense_Success() throws InterruptedException, SQLException {
        // Given
        Expense expectedExpense = new Expense(EXPENSE_ID_1, DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1);
        when(expenseService.updateExpense(anyInt(), any(LocalDate.class), any(BigDecimal.class), 
            anyString(), anyInt())).thenReturn(expectedExpense);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Expense> result = new AtomicReference<>();
        
        // When
        controller.updateExpense(EXPENSE_ID_1, DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1,
            expense -> {
                result.set(expense);
                latch.countDown();
            },
            error -> {
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNotNull();
        assertThat(result.get().getExpenseId()).isEqualTo(EXPENSE_ID_1);
        verify(expenseService, timeout(2000)).updateExpense(EXPENSE_ID_1, DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1);
    }
    
    @Test
    public void testUpdateExpense_SQLException() throws InterruptedException, SQLException {
        // Given
        when(expenseService.updateExpense(anyInt(), any(LocalDate.class), any(BigDecimal.class), 
            anyString(), anyInt())).thenThrow(new SQLException("Database error"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.updateExpense(EXPENSE_ID_1, DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1,
            expense -> {
                latch.countDown();
            },
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(errorResult.get()).isNotNull().contains("Error updating expense");
        verify(expenseService, timeout(2000)).updateExpense(EXPENSE_ID_1, DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1);
    }
    
    @Test
    public void testUpdateExpense_IllegalArgumentException() throws InterruptedException, SQLException {
        // Given
        when(expenseService.updateExpense(anyInt(), any(LocalDate.class), any(BigDecimal.class), 
            anyString(), anyInt())).thenThrow(new IllegalArgumentException("Expense not found"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.updateExpense(EXPENSE_ID_1, DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1,
            expense -> {
                latch.countDown();
            },
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(errorResult.get()).isEqualTo("Expense not found");
        verify(expenseService, timeout(2000)).updateExpense(EXPENSE_ID_1, DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1);
    }
    
    @Test
    public void testDeleteExpense_Success() throws InterruptedException, SQLException {
        // Given
        when(expenseService.deleteExpense(EXPENSE_ID_1)).thenReturn(true);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> successCalled = new AtomicReference<>(false);
        
        // When
        controller.deleteExpense(EXPENSE_ID_1,
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
        verify(expenseService, timeout(2000)).deleteExpense(EXPENSE_ID_1);
    }
    
    @Test
    public void testDeleteExpense_SQLException() throws InterruptedException, SQLException {
        // Given
        when(expenseService.deleteExpense(EXPENSE_ID_1)).thenThrow(new SQLException("Database error"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.deleteExpense(EXPENSE_ID_1,
            () -> {
                latch.countDown();
            },
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(errorResult.get()).isNotNull().contains("Error deleting expense");
        verify(expenseService, timeout(2000)).deleteExpense(EXPENSE_ID_1);
    }
    
    @Test
    public void testDeleteExpense_IllegalArgumentException() throws InterruptedException, SQLException {
        // Given
        when(expenseService.deleteExpense(EXPENSE_ID_1)).thenThrow(new IllegalArgumentException("Expense not found"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.deleteExpense(EXPENSE_ID_1,
            () -> {
                latch.countDown();
            },
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(errorResult.get()).isEqualTo("Expense not found");
        verify(expenseService, timeout(2000)).deleteExpense(EXPENSE_ID_1);
    }
    
    @Test
    public void testLoadExpensesByMonth_Success() throws InterruptedException, SQLException {
        // Given
        List<Expense> expectedExpenses = new ArrayList<>();
        expectedExpenses.add(new Expense(EXPENSE_ID_1, DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1));
        when(expenseService.getExpensesByMonth(2024, 1)).thenReturn(expectedExpenses);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Expense>> result = new AtomicReference<>();
        
        // When
        controller.loadExpensesByMonth(2024, 1,
            expenses -> {
                result.set(expenses);
                latch.countDown();
            },
            error -> {
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNotNull();
        assertThat(result.get().size()).isEqualTo(1);
        verify(expenseService, timeout(2000)).getExpensesByMonth(2024, 1);
    }
    
    @Test
    public void testLoadExpensesByMonth_SQLException() throws InterruptedException, SQLException {
        // Given
        when(expenseService.getExpensesByMonth(2024, 1)).thenThrow(new SQLException("Database error"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.loadExpensesByMonth(2024, 1,
            expenses -> {
                latch.countDown();
            },
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then - wait for async operation and process EDT events
        // The callback is executed via SwingUtilities.invokeLater, so we need to wait
        // and potentially process EDT events
        boolean completed = false;
        for (int i = 0; i < 50; i++) { // Try for up to 5 seconds (50 * 100ms)
            if (latch.await(100, TimeUnit.MILLISECONDS)) {
                completed = true;
                break;
            }
            // Process any pending EDT events
            try {
                javax.swing.SwingUtilities.invokeAndWait(() -> {
                    // Empty - just to process EDT queue
                });
            } catch (Exception e) {
                // EDT might not be available, continue waiting
            }
        }
        
        assertThat(completed).as("Latch should have been counted down").isTrue();
        assertThat(errorResult.get()).isNotNull().contains("Error loading expenses");
        verify(expenseService, timeout(5000)).getExpensesByMonth(2024, 1);
    }
    
    @Test
    public void testGetMonthlyTotal_Success() throws InterruptedException, SQLException {
        // Given
        BigDecimal expectedTotal = new BigDecimal("500.00");
        when(expenseService.getMonthlyTotal(2024, 1)).thenReturn(expectedTotal);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<BigDecimal> result = new AtomicReference<>();
        
        // When
        controller.getMonthlyTotal(2024, 1,
            total -> {
                result.set(total);
                latch.countDown();
            },
            error -> {
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNotNull();
        assertThat(result.get()).isEqualByComparingTo(expectedTotal);
        verify(expenseService, timeout(2000)).getMonthlyTotal(2024, 1);
    }
    
    @Test
    public void testGetMonthlyTotal_SQLException() throws InterruptedException, SQLException {
        // Given
        when(expenseService.getMonthlyTotal(2024, 1)).thenThrow(new SQLException("Database error"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.getMonthlyTotal(2024, 1,
            total -> {
                latch.countDown();
            },
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(errorResult.get()).isNotNull().contains("Error calculating monthly total");
        verify(expenseService, timeout(2000)).getMonthlyTotal(2024, 1);
    }
    
    @Test
    public void testLoadExpenses_Success() throws InterruptedException, SQLException {
        // Given
        List<Expense> expectedExpenses = new ArrayList<>();
        expectedExpenses.add(new Expense(EXPENSE_ID_1, DATE_1, AMOUNT_1, DESCRIPTION_1, CATEGORY_ID_1));
        when(expenseService.getAllExpenses()).thenReturn(expectedExpenses);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Expense>> result = new AtomicReference<>();
        
        // When
        controller.loadExpenses(
            expenses -> {
                result.set(expenses);
                latch.countDown();
            },
            error -> {
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNotNull();
        assertThat(result.get().size()).isEqualTo(1);
        verify(expenseService, timeout(2000)).getAllExpenses();
    }
    
    @Test
    public void testLoadExpenses_SQLException() throws InterruptedException, SQLException {
        // Given
        when(expenseService.getAllExpenses()).thenThrow(new SQLException("Database error"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.loadExpenses(
            expenses -> {
                latch.countDown();
            },
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(errorResult.get()).isNotNull().contains("Error loading expenses");
        verify(expenseService, timeout(2000)).getAllExpenses();
    }
    
    @Test
    public void testGetTotalByCategory_Success() throws InterruptedException, SQLException {
        // Given
        BigDecimal expectedTotal = new BigDecimal("300.00");
        when(expenseService.getTotalByCategory(CATEGORY_ID_1)).thenReturn(expectedTotal);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<BigDecimal> result = new AtomicReference<>();
        
        // When
        controller.getTotalByCategory(CATEGORY_ID_1,
            total -> {
                result.set(total);
                latch.countDown();
            },
            error -> {
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNotNull();
        assertThat(result.get()).isEqualByComparingTo(expectedTotal);
        verify(expenseService, timeout(2000)).getTotalByCategory(CATEGORY_ID_1);
    }
    
    @Test
    public void testGetTotalByCategory_SQLException() throws InterruptedException, SQLException {
        // Given
        when(expenseService.getTotalByCategory(CATEGORY_ID_1)).thenThrow(new SQLException("Database error"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResult = new AtomicReference<>();
        
        // When
        controller.getTotalByCategory(CATEGORY_ID_1,
            total -> {
                latch.countDown();
            },
            error -> {
                errorResult.set(error);
                latch.countDown();
            }
        );
        
        // Then
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(errorResult.get()).isNotNull().contains("Error calculating category total");
        verify(expenseService, timeout(2000)).getTotalByCategory(CATEGORY_ID_1);
    }
}

