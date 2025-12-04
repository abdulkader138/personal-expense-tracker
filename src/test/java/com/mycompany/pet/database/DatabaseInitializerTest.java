package com.mycompany.pet.database;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

/**
 * Unit tests for DatabaseInitializer using mocks.
 */
public class DatabaseInitializerTest {
    private DatabaseConnection mockDbConnection;
    private MongoDatabase mockDatabase;
    private MongoCollection<Document> mockCategoriesCollection;
    private MongoCollection<Document> mockExpensesCollection;
    private DatabaseInitializer initializer;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        mockDbConnection = mock(DatabaseConnection.class);
        mockDatabase = mock(MongoDatabase.class);
        mockCategoriesCollection = mock(MongoCollection.class);
        mockExpensesCollection = mock(MongoCollection.class);

        when(mockDbConnection.getDatabase()).thenReturn(mockDatabase);
        when(mockDatabase.getCollection("categories")).thenReturn(mockCategoriesCollection);
        when(mockDatabase.getCollection("expenses")).thenReturn(mockExpensesCollection);

        initializer = new DatabaseInitializer(mockDbConnection);
    }

    @Test
    public void testConstructor() {
        // Given & When
        DatabaseInitializer testInitializer = new DatabaseInitializer(mockDbConnection);

        // Then
        assertNotNull(testInitializer);
    }

    @Test
    public void testInitialize_CreatesIndexes() {
        // When
        initializer.initialize();

        // Then - verify collections are retrieved
        verify(mockDatabase, times(1)).getCollection("categories");
        verify(mockDatabase, times(1)).getCollection("expenses");

        // Verify indexes are created
        // Note: We can't easily verify the exact IndexOptions, but we can verify the method is called
        verify(mockCategoriesCollection, times(1)).createIndex(any(), any(IndexOptions.class));
        verify(mockExpensesCollection, times(2)).createIndex(any());
    }

    @Test
    public void testInitialize_CreatesUniqueIndexOnCategoryName() {
        // When
        initializer.initialize();

        // Then - verify unique index is created on category name
        verify(mockCategoriesCollection, times(1)).createIndex(any(), any(IndexOptions.class));
    }

    @Test
    public void testInitialize_CreatesIndexesOnExpenses() {
        // When
        initializer.initialize();

        // Then - verify indexes are created on expenses collection
        verify(mockExpensesCollection, times(2)).createIndex(any());
    }

    @Test
    public void testInitialize_CanBeCalledMultipleTimes() {
        // When - call initialize multiple times
        initializer.initialize();
        initializer.initialize();
        initializer.initialize();

        // Then - should not throw exception and should create indexes each time
        verify(mockCategoriesCollection, times(3)).createIndex(any(), any(IndexOptions.class));
        verify(mockExpensesCollection, times(6)).createIndex(any()); // 2 indexes × 3 calls
    }
}

