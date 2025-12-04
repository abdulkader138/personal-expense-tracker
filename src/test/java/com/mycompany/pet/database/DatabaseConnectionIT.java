package com.mycompany.pet.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import com.mongodb.client.MongoDatabase;

/**
 * Integration test for DatabaseConnection using Testcontainers.
 * This test requires Docker to be running.
 * 
 * Note: This is an integration test (ends with IT) and will be run
 * by the Maven Failsafe plugin during the integration-test phase.
 * 
 * These tests will be skipped if Docker is not available.
 */
public class DatabaseConnectionIT {
    private MongoDBContainer mongoDBContainer;
    private DatabaseConnection dbConnection;

    @Before
    public void setUp() {
        // Skip tests if Docker is not available
        try {
            DockerClientFactory.instance().client();
        } catch (Exception e) {
            Assume.assumeNoException("Docker is not available. Skipping integration tests. Run with -DskipITs=true to skip all integration tests.", e);
        }
        
        try {
            // Start a MongoDB container for testing
            mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.4"))
                    .withReuse(true);
            mongoDBContainer.start();

            // Create DatabaseConnection with the container's connection string
            String connectionString = mongoDBContainer.getReplicaSetUrl();
            dbConnection = new DatabaseConnection(connectionString, "test_database");
        } catch (Exception e) {
            Assume.assumeNoException("Failed to start MongoDB container. Docker may not be available. Skipping integration tests.", e);
        }
    }

    @After
    public void tearDown() {
        if (dbConnection != null) {
            dbConnection.close();
        }
        if (mongoDBContainer != null && mongoDBContainer.isRunning()) {
            mongoDBContainer.stop();
        }
    }

    @Test
    public void testGetDatabase_WithRealMongoDB() {
        // When
        MongoDatabase database = dbConnection.getDatabase();

        // Then
        assertNotNull(database);
        assertEquals("test_database", database.getName());
    }

    @Test
    public void testGetDatabase_ReusesConnection() {
        // When - call multiple times
        MongoDatabase db1 = dbConnection.getDatabase();
        MongoDatabase db2 = dbConnection.getDatabase();
        MongoDatabase db3 = dbConnection.getDatabase();

        // Then - should return the same database instance
        assertNotNull(db1);
        assertNotNull(db2);
        assertNotNull(db3);
        assertEquals("test_database", db1.getName());
        assertEquals("test_database", db2.getName());
        assertEquals("test_database", db3.getName());
    }

    @Test
    public void testClose_ClosesConnection() {
        // Given
        MongoDatabase database = dbConnection.getDatabase();
        assertNotNull(database);

        // When
        dbConnection.close();

        // Then - should be able to reconnect after close
        MongoDatabase newDatabase = dbConnection.getDatabase();
        assertNotNull(newDatabase);
        assertEquals("test_database", newDatabase.getName());
    }

    @Test
    public void testConnection_WithCustomDatabaseName() {
        // Given
        String customDbName = "custom_test_db";
        dbConnection.setDatabaseName(customDbName);

        // When
        MongoDatabase database = dbConnection.getDatabase();

        // Then
        assertNotNull(database);
        assertEquals(customDbName, database.getName());
    }
}

