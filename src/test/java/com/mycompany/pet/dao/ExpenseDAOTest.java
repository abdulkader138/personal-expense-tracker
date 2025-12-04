package com.mycompany.pet.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mycompany.pet.database.DatabaseConnection;
import com.mycompany.pet.model.Expense;

/**
 * Unit tests for ExpenseDAO using MongoDB mocks.
 */
public class ExpenseDAOTest {
    @Mock
    private DatabaseConnection dbConnection;

    @Mock
    private MongoDatabase database;

    @Mock
    private MongoCollection<Document> expensesCollection;

    @Mock
    private FindIterable<Document> findIterable;

    @Mock
    private MongoCursor<Document> mongoCursor;

    private ExpenseDAO expenseDAO;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(dbConnection.getDatabase()).thenReturn(database);
        when(database.getCollection("expenses")).thenReturn(expensesCollection);
        expenseDAO = new ExpenseDAO(dbConnection);
    }

    @Test
    public void testCreate_Success() throws SQLException {
        // Given
        Expense expense = new Expense(LocalDate.now(), new BigDecimal("100.50"), "Lunch", 1);
        when(expensesCollection.find()).thenReturn(findIterable);
        when(findIterable.sort(any())).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null); // No existing expenses

        // When
        Expense result = expenseDAO.create(expense);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getExpenseId());
        assertEquals(new BigDecimal("100.50"), result.getAmount());
        verify(expensesCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    public void testCreate_WithExistingExpenses() throws SQLException {
        // Given
        Expense expense = new Expense(LocalDate.now(), new BigDecimal("50.00"), "Coffee", 1);
        Document existingDoc = new Document("expenseId", 10);
        when(expensesCollection.find()).thenReturn(findIterable);
        when(findIterable.sort(any())).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);

        // When
        Expense result = expenseDAO.create(expense);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(11), result.getExpenseId());
        verify(expensesCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    public void testFindById_Success() throws SQLException {
        // Given
        Integer expenseId = 1;
        LocalDate date = LocalDate.of(2024, 1, 15);
        Document doc = new Document("expenseId", 1)
                .append("date", date.toString())
                .append("amount", "100.50")
                .append("description", "Lunch")
                .append("categoryId", 1);
        when(expensesCollection.find(Filters.eq("expenseId", expenseId))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);

        // When
        Expense result = expenseDAO.findById(expenseId);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getExpenseId());
        assertEquals(date, result.getDate());
        assertEquals(new BigDecimal("100.50"), result.getAmount());
        assertEquals("Lunch", result.getDescription());
        assertEquals(Integer.valueOf(1), result.getCategoryId());
    }

    @Test
    public void testFindById_NotFound() throws SQLException {
        // Given
        Integer expenseId = 999;
        when(expensesCollection.find(Filters.eq("expenseId", expenseId))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);

        // When
        Expense result = expenseDAO.findById(expenseId);

        // Then
        assertNull(result);
    }

    @Test
    public void testFindAll_Success() throws SQLException {
        // Given
        LocalDate date1 = LocalDate.of(2024, 1, 15);
        LocalDate date2 = LocalDate.of(2024, 1, 10);
        Document doc1 = new Document("expenseId", 1)
                .append("date", date1.toString())
                .append("amount", "100.50")
                .append("description", "Lunch")
                .append("categoryId", 1);
        Document doc2 = new Document("expenseId", 2)
                .append("date", date2.toString())
                .append("amount", "50.00")
                .append("description", "Coffee")
                .append("categoryId", 1);
        List<Document> documents = new ArrayList<>();
        documents.add(doc1);
        documents.add(doc2);

        MongoCursor<Document> cursor = mock(MongoCursor.class);
        when(expensesCollection.find()).thenReturn(findIterable);
        when(findIterable.sort(any())).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(doc1, doc2);

        // When
        List<Expense> result = expenseDAO.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindByMonth_Success() throws SQLException {
        // Given
        int year = 2024;
        int month = 1;
        LocalDate date1 = LocalDate.of(year, month, 15);
        LocalDate date2 = LocalDate.of(year, month, 20);
        Document doc1 = new Document("expenseId", 1)
                .append("date", date1.toString())
                .append("amount", "100.50")
                .append("description", "Lunch")
                .append("categoryId", 1);
        Document doc2 = new Document("expenseId", 2)
                .append("date", date2.toString())
                .append("amount", "50.00")
                .append("description", "Coffee")
                .append("categoryId", 1);
        List<Document> documents = new ArrayList<>();
        documents.add(doc1);
        documents.add(doc2);

        MongoCursor<Document> cursor = mock(MongoCursor.class);
        when(expensesCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(doc1, doc2);

        // When
        List<Expense> result = expenseDAO.findByMonth(year, month);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindByCategory_Success() throws SQLException {
        // Given
        Integer categoryId = 1;
        LocalDate date = LocalDate.of(2024, 1, 15);
        Document doc = new Document("expenseId", 1)
                .append("date", date.toString())
                .append("amount", "100.50")
                .append("description", "Lunch")
                .append("categoryId", categoryId);
        List<Document> documents = new ArrayList<>();
        documents.add(doc);

        MongoCursor<Document> cursor = mock(MongoCursor.class);
        when(expensesCollection.find(Filters.eq("categoryId", categoryId))).thenReturn(findIterable);
        when(findIterable.sort(any())).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn(doc);

        // When
        List<Expense> result = expenseDAO.findByCategory(categoryId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(categoryId, result.get(0).getCategoryId());
    }

    @Test
    public void testUpdate_Success() throws SQLException {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        Expense expense = new Expense(1, date, new BigDecimal("150.00"), "Updated Lunch", 1);
        when(expensesCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(null);

        // When
        Expense result = expenseDAO.update(expense);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("150.00"), result.getAmount());
        verify(expensesCollection, times(1)).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testDelete_Success() throws SQLException {
        // Given
        Integer expenseId = 1;
        com.mongodb.client.result.DeleteResult deleteResult = mock(com.mongodb.client.result.DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        when(expensesCollection.deleteOne(Filters.eq("expenseId", expenseId))).thenReturn(deleteResult);

        // When
        boolean result = expenseDAO.delete(expenseId);

        // Then
        assertTrue(result);
        verify(expensesCollection, times(1)).deleteOne(Filters.eq("expenseId", expenseId));
    }

    @Test
    public void testDelete_NotFound() throws SQLException {
        // Given
        Integer expenseId = 999;
        com.mongodb.client.result.DeleteResult deleteResult = mock(com.mongodb.client.result.DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(0L);
        when(expensesCollection.deleteOne(Filters.eq("expenseId", expenseId))).thenReturn(deleteResult);

        // When
        boolean result = expenseDAO.delete(expenseId);

        // Then
        assertFalse(result);
    }

    @Test
    public void testGetTotalByCategory_Success() throws SQLException {
        // Given
        Integer categoryId = 1;
        LocalDate date1 = LocalDate.of(2024, 1, 15);
        LocalDate date2 = LocalDate.of(2024, 1, 20);
        Document doc1 = new Document("expenseId", 1)
                .append("date", date1.toString())
                .append("amount", "100.50")
                .append("description", "Lunch")
                .append("categoryId", categoryId);
        Document doc2 = new Document("expenseId", 2)
                .append("date", date2.toString())
                .append("amount", "50.00")
                .append("description", "Coffee")
                .append("categoryId", categoryId);
        List<Document> documents = new ArrayList<>();
        documents.add(doc1);
        documents.add(doc2);

        MongoCursor<Document> cursor = mock(MongoCursor.class);
        when(expensesCollection.find(Filters.eq("categoryId", categoryId))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(doc1, doc2);

        // When
        BigDecimal result = expenseDAO.getTotalByCategory(categoryId);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("150.50"), result);
    }

    @Test
    public void testGetMonthlyTotal_Success() throws SQLException {
        // Given
        int year = 2024;
        int month = 1;
        LocalDate date1 = LocalDate.of(year, month, 15);
        LocalDate date2 = LocalDate.of(year, month, 20);
        Document doc1 = new Document("expenseId", 1)
                .append("date", date1.toString())
                .append("amount", "100.50")
                .append("description", "Lunch")
                .append("categoryId", 1);
        Document doc2 = new Document("expenseId", 2)
                .append("date", date2.toString())
                .append("amount", "50.00")
                .append("description", "Coffee")
                .append("categoryId", 1);
        List<Document> documents = new ArrayList<>();
        documents.add(doc1);
        documents.add(doc2);

        MongoCursor<Document> cursor = mock(MongoCursor.class);
        when(expensesCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(doc1, doc2);

        // When
        BigDecimal result = expenseDAO.getMonthlyTotal(year, month);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("150.50"), result);
    }
}

