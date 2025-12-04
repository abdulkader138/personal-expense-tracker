package com.mycompany.pet.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * Manages MongoDB connections.
 */
public class DatabaseConnection {
    private static final String DEFAULT_CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DEFAULT_DATABASE_NAME = "expense_tracker";

    private String connectionString;
    private String databaseName;
    private MongoClient mongoClient;

    /**
     * Creates a connection using the default local MongoDB instance and database name.
     */
    public DatabaseConnection() {
        this(DEFAULT_CONNECTION_STRING, DEFAULT_DATABASE_NAME);
    }

    /**
     * Creates a connection for the given MongoDB connection string and database name.
     */
    public DatabaseConnection(String connectionString, String databaseName) {
        this.connectionString = connectionString;
        this.databaseName = databaseName;
    }

    /**
     * Lazily creates and returns a MongoDatabase instance.
     */
    public MongoDatabase getDatabase() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(connectionString);
        }
        return mongoClient.getDatabase(databaseName);
    }

    /**
     * Closes the underlying MongoClient, if it has been created.
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}

