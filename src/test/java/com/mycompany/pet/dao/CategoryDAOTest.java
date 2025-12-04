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

import java.sql.SQLException;
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
import com.mycompany.pet.model.Category;

/**
 * Unit tests for CategoryDAO using MongoDB mocks.
 */
public class CategoryDAOTest {
    @Mock
    private DatabaseConnection dbConnection;

    @Mock
    private MongoDatabase database;

    @Mock
    private MongoCollection<Document> categoriesCollection;

    @Mock
    private MongoCollection<Document> expensesCollection;

    @Mock
    private FindIterable<Document> findIterable;

    @Mock
    private MongoCursor<Document> mongoCursor;

    private CategoryDAO categoryDAO;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(dbConnection.getDatabase()).thenReturn(database);
        when(database.getCollection("categories")).thenReturn(categoriesCollection);
        when(database.getCollection("expenses")).thenReturn(expensesCollection);
        categoryDAO = new CategoryDAO(dbConnection);
    }

    @Test
    public void testCreate_Success() throws SQLException {
        // Given
        Category category = new Category("Food");
        when(categoriesCollection.find()).thenReturn(findIterable);
        when(findIterable.sort(any())).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null); // No existing categories

        // When
        Category result = categoryDAO.create(category);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getCategoryId());
        assertEquals("Food", result.getName());
        verify(categoriesCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    public void testCreate_WithExistingCategories() throws SQLException {
        // Given
        Category category = new Category("Travel");
        Document existingDoc = new Document("categoryId", 5);
        when(categoriesCollection.find()).thenReturn(findIterable);
        when(findIterable.sort(any())).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);

        // When
        Category result = categoryDAO.create(category);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(6), result.getCategoryId());
        verify(categoriesCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    public void testFindById_Success() throws SQLException {
        // Given
        Integer categoryId = 1;
        Document doc = new Document("categoryId", 1).append("name", "Food");
        when(categoriesCollection.find(Filters.eq("categoryId", categoryId))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(doc);

        // When
        Category result = categoryDAO.findById(categoryId);

        // Then
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getCategoryId());
        assertEquals("Food", result.getName());
    }

    @Test
    public void testFindById_NotFound() throws SQLException {
        // Given
        Integer categoryId = 999;
        when(categoriesCollection.find(Filters.eq("categoryId", categoryId))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);

        // When
        Category result = categoryDAO.findById(categoryId);

        // Then
        assertNull(result);
    }

    @Test
    public void testFindAll_Success() throws SQLException {
        // Given
        Document doc1 = new Document("categoryId", 1).append("name", "Food");
        Document doc2 = new Document("categoryId", 2).append("name", "Travel");
        MongoCursor<Document> cursor = mock(MongoCursor.class);
        
        when(categoriesCollection.find()).thenReturn(findIterable);
        when(findIterable.sort(any())).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(doc1, doc2);

        // When
        List<Category> result = categoryDAO.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Food", result.get(0).getName());
        assertEquals("Travel", result.get(1).getName());
    }

    @Test
    public void testUpdate_Success() throws SQLException {
        // Given
        Category category = new Category(1, "Updated Food");
        when(categoriesCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(null);

        // When
        Category result = categoryDAO.update(category);

        // Then
        assertNotNull(result);
        assertEquals("Updated Food", result.getName());
        verify(categoriesCollection, times(1)).updateOne(any(Bson.class), any(Bson.class));
    }

    @Test
    public void testDelete_Success() throws SQLException {
        // Given
        Integer categoryId = 1;
        com.mongodb.client.result.DeleteResult deleteResult = mock(com.mongodb.client.result.DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        when(categoriesCollection.deleteOne(Filters.eq("categoryId", categoryId))).thenReturn(deleteResult);
        when(expensesCollection.deleteMany(Filters.eq("categoryId", categoryId))).thenReturn(null);

        // When
        boolean result = categoryDAO.delete(categoryId);

        // Then
        assertTrue(result);
        verify(categoriesCollection, times(1)).deleteOne(Filters.eq("categoryId", categoryId));
        verify(expensesCollection, times(1)).deleteMany(Filters.eq("categoryId", categoryId));
    }

    @Test
    public void testDelete_NotFound() throws SQLException {
        // Given
        Integer categoryId = 999;
        com.mongodb.client.result.DeleteResult deleteResult = mock(com.mongodb.client.result.DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(0L);
        when(categoriesCollection.deleteOne(Filters.eq("categoryId", categoryId))).thenReturn(deleteResult);

        // When
        boolean result = categoryDAO.delete(categoryId);

        // Then
        assertFalse(result);
    }
}

