package com.mycompany.pet.database;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

/**
 * Initializes MongoDB collections and indexes.
 */
public class DatabaseInitializer {
    private DatabaseConnection dbConnection;

    public DatabaseInitializer(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * Ensures that the required collections and indexes exist.
     */
    public void initialize() {
        MongoDatabase database = dbConnection.getDatabase();

        MongoCollection<Document> categories = database.getCollection("categories");
        MongoCollection<Document> expenses = database.getCollection("expenses");

        // Unique index on category name
        categories.createIndex(Indexes.ascending("name"), new IndexOptions().unique(true));

        // Helpful indexes for common queries
        expenses.createIndex(Indexes.ascending("date"));
        expenses.createIndex(Indexes.ascending("categoryId"));
    }
}

