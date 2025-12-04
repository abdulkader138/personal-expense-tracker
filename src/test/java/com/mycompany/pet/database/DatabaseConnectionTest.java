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
        // Mock MongoClients static methods
        mockedMongoClients = Mockito.mockStatic(MongoClients.class);
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
        // Verify default values are set (we can't directly access private fields,
        // but we can test behavior)
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

        // Then
        verify(mockClient, times(1)).close();
    }

    @Test
    public void testClose_NoOpWhenClientNotCreated() {
        // Given
        dbConnection = new DatabaseConnection();
        
        MongoClient mockClient = mock(MongoClient.class);
        mockedMongoClients.when(() -> MongoClients.create(anyString()))
                .thenReturn(mockClient);

        // When - close without getting database first
        dbConnection.close();

        // Then - should not throw exception and should not call close on client
        verify(mockClient, never()).close();
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

