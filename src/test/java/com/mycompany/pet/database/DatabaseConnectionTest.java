package com.mycompany.pet.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * Unit tests for DatabaseConnection using mocks.
 */
public class DatabaseConnectionTest {
    private DatabaseConnection dbConnection;
    private MockedStatic<MongoClients> mockedMongoClients;

    @Before
    public void setUp() {
        // Skip tests on Java 8 as mockito-inline requires Java 11+
        String javaVersion = System.getProperty("java.version");
        boolean isJava8 = javaVersion != null && (javaVersion.startsWith("1.8") || javaVersion.startsWith("8."));
        Assume.assumeFalse("Static mocks require Java 11+, skipping on Java 8", isJava8);
        
        try {
            // Mock MongoClients static methods
            // This requires mockito-inline which is only available on Java 11+
            mockedMongoClients = Mockito.mockStatic(MongoClients.class);
        } catch (Exception e) {
            // If mockito-inline is not available, skip the test
            Assume.assumeNoException("mockito-inline not available, skipping test", e);
        }
    }

    @After
    public void tearDown() {
        if (dbConnection != null) {
            dbConnection.close();
        }
        if (mockedMongoClients != null) {
            mockedMongoClients.close();
        }
    }

    @Test
    public void testDefaultConstructor() {
        // Given & When
        dbConnection = new DatabaseConnection();

        // Then
        assertNotNull(dbConnection);
        // Verify default values are set by calling getDatabase() which uses them
        // The default constructor calls this(DEFAULT_CONNECTION_STRING, DEFAULT_DATABASE_NAME)
        // So we verify by checking that getDatabase() uses the defaults
        MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        
        // Use the actual default values
        String defaultConnectionString = "mongodb://localhost:27017";
        String defaultDatabaseName = "expense_tracker";
        
        mockedMongoClients.when(() -> MongoClients.create(defaultConnectionString))
                .thenReturn(mockClient);
        when(mockClient.getDatabase(defaultDatabaseName)).thenReturn(mockDatabase);
        
        MongoDatabase result = dbConnection.getDatabase();
        assertNotNull(result);
        assertEquals(mockDatabase, result);
        // Verify the default connection string and database name were used
        mockedMongoClients.verify(() -> MongoClients.create(defaultConnectionString), times(1));
        verify(mockClient, times(1)).getDatabase(defaultDatabaseName);
    }

    @Test
    public void testParameterizedConstructor() {
        // Given
        String connectionString = "mongodb://test:27017";
        String databaseName = "test_db";

        // When
        dbConnection = new DatabaseConnection(connectionString, databaseName);

        // Then
        assertNotNull(dbConnection);
    }

    @Test
    public void testGetDatabase_CreatesClientOnFirstCall() {
        // Given
        String connectionString = "mongodb://test:27017";
        String databaseName = "test_db";
        dbConnection = new DatabaseConnection(connectionString, databaseName);
        
        MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        
        mockedMongoClients.when(() -> MongoClients.create(connectionString))
                .thenReturn(mockClient);
        when(mockClient.getDatabase(databaseName)).thenReturn(mockDatabase);

        // When
        MongoDatabase result = dbConnection.getDatabase();

        // Then
        assertNotNull(result);
        assertEquals(mockDatabase, result);
        mockedMongoClients.verify(() -> MongoClients.create(connectionString), times(1));
        verify(mockClient, times(1)).getDatabase(databaseName);
    }

    @Test
    public void testGetDatabase_ReusesClientOnSubsequentCalls() {
        // Given
        String connectionString = "mongodb://test:27017";
        String databaseName = "test_db";
        dbConnection = new DatabaseConnection(connectionString, databaseName);
        
        MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        
        mockedMongoClients.when(() -> MongoClients.create(connectionString))
                .thenReturn(mockClient);
        when(mockClient.getDatabase(databaseName)).thenReturn(mockDatabase);

        // When - call multiple times
        MongoDatabase result1 = dbConnection.getDatabase();
        MongoDatabase result2 = dbConnection.getDatabase();
        MongoDatabase result3 = dbConnection.getDatabase();

        // Then - client should only be created once
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(mockDatabase, result1);
        assertEquals(mockDatabase, result2);
        assertEquals(mockDatabase, result3);
        // MongoClients.create should only be called once (lazy initialization)
        mockedMongoClients.verify(() -> MongoClients.create(connectionString), times(1));
        verify(mockClient, times(3)).getDatabase(databaseName);
    }

    @Test
    public void testClose_ClosesClient() {
        // Given
        String connectionString = "mongodb://test:27017";
        String databaseName = "test_db";
        dbConnection = new DatabaseConnection(connectionString, databaseName);
        
        MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        
        mockedMongoClients.when(() -> MongoClients.create(connectionString))
                .thenReturn(mockClient);
        when(mockClient.getDatabase(databaseName)).thenReturn(mockDatabase);
        
        // Initialize the connection
        dbConnection.getDatabase();

        // When
        dbConnection.close();

        // Then - verify close() was called and mongoClient was set to null
        verify(mockClient, times(1)).close();
        // Verify that after close(), mongoClient is null by calling getDatabase() again
        // which should create a new client (proves mongoClient was set to null)
        MongoClient mockClient2 = mock(MongoClient.class);
        mockedMongoClients.when(() -> MongoClients.create(connectionString))
                .thenReturn(mockClient2);
        when(mockClient2.getDatabase(databaseName)).thenReturn(mockDatabase);
        
        dbConnection.getDatabase();
        // Verify a new client was created (proves mongoClient was set to null in close())
        mockedMongoClients.verify(() -> MongoClients.create(connectionString), times(2));
    }

    @Test
    public void testClose_NoOpWhenClientNotCreated() {
        // Given - mongoClient is null (hasn't been created yet)
        dbConnection = new DatabaseConnection();
        
        // No mock setup needed - mongoClient should be null
        // When - close without getting database first
        // This tests the false branch of "if (mongoClient != null)" on line 47
        dbConnection.close();

        // Then - should not throw exception
        // Verify that close() completed without error
        // The if block should be skipped because mongoClient == null
        // Since we can't directly verify the null check, we verify by ensuring
        // that calling getDatabase() after close() still works (proves mongoClient was null)
        MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        String defaultConnectionString = "mongodb://localhost:27017";
        String defaultDatabaseName = "expense_tracker";
        
        mockedMongoClients.when(() -> MongoClients.create(defaultConnectionString))
                .thenReturn(mockClient);
        when(mockClient.getDatabase(defaultDatabaseName)).thenReturn(mockDatabase);
        
        // After close() with null client, getDatabase() should still work (creates new client)
        MongoDatabase result = dbConnection.getDatabase();
        assertNotNull(result);
        // Verify that getDatabase() created a new client (proves mongoClient was null during close())
        mockedMongoClients.verify(() -> MongoClients.create(defaultConnectionString), times(1));
        verify(mockClient, never()).close(); // close() should never have been called
    }

    @Test
    public void testClose_AllowsReconnectionAfterClose() {
        // Given
        String connectionString = "mongodb://test:27017";
        String databaseName = "test_db";
        dbConnection = new DatabaseConnection(connectionString, databaseName);
        
        MongoClient mockClient1 = mock(MongoClient.class);
        MongoClient mockClient2 = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        
        mockedMongoClients.when(() -> MongoClients.create(connectionString))
                .thenReturn(mockClient1, mockClient2);
        when(mockClient1.getDatabase(databaseName)).thenReturn(mockDatabase);
        when(mockClient2.getDatabase(databaseName)).thenReturn(mockDatabase);

        // When - get database, close, then get again
        MongoDatabase result1 = dbConnection.getDatabase();
        dbConnection.close();
        MongoDatabase result2 = dbConnection.getDatabase();

        // Then - should create new client after close
        assertNotNull(result1);
        assertNotNull(result2);
        mockedMongoClients.verify(() -> MongoClients.create(connectionString), times(2));
    }

    @Test
    public void testSetConnectionString() {
        // Given
        dbConnection = new DatabaseConnection();
        String newConnectionString = "mongodb://newhost:27017";

        // When
        dbConnection.setConnectionString(newConnectionString);

        // Then - verify it's set by checking behavior
        MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        
        mockedMongoClients.when(() -> MongoClients.create(newConnectionString))
                .thenReturn(mockClient);
        when(mockClient.getDatabase(anyString())).thenReturn(mockDatabase);

        dbConnection.getDatabase();
        
        // Verify the new connection string was used
        mockedMongoClients.verify(() -> MongoClients.create(newConnectionString), times(1));
    }

    @Test
    public void testSetDatabaseName() {
        // Given
        dbConnection = new DatabaseConnection();
        String newDatabaseName = "new_database";
        
        MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        
        mockedMongoClients.when(() -> MongoClients.create(anyString()))
                .thenReturn(mockClient);
        when(mockClient.getDatabase(newDatabaseName)).thenReturn(mockDatabase);

        // When
        dbConnection.setDatabaseName(newDatabaseName);
        MongoDatabase result = dbConnection.getDatabase();

        // Then
        assertNotNull(result);
        verify(mockClient, times(1)).getDatabase(newDatabaseName);
    }
}

