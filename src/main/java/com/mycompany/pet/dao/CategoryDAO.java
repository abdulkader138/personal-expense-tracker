package com.mycompany.pet.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mycompany.pet.database.DatabaseConnection;
import com.mycompany.pet.model.Category;

/**
 * Data Access Object for Category entities.
 */
public class CategoryDAO {
    private static final String FIELD_CATEGORY_ID = "categoryId";
    
    private final MongoCollection<Document> collection;
    private final MongoCollection<Document> expensesCollection;

    public CategoryDAO(DatabaseConnection dbConnection) {
        MongoDatabase database = dbConnection.getDatabase();
        this.collection = database.getCollection("categories");
        this.expensesCollection = database.getCollection("expenses");
    }

    public Category create(Category category) throws SQLException {
        try {
            // Generate a simple incremental ID
            Document last = collection.find()
                    .sort(Sorts.descending(FIELD_CATEGORY_ID))
                    .first();
            int nextId = 1;
            if (last != null && last.get(FIELD_CATEGORY_ID) != null) {
                nextId = last.getInteger(FIELD_CATEGORY_ID) + 1;
            }
            category.setCategoryId(nextId);

            Document doc = new Document("_id", nextId)
                    .append(FIELD_CATEGORY_ID, nextId)
                    .append("name", category.getName());
            collection.insertOne(doc);
            return category;
        } catch (Exception e) {
            throw new SQLException("Error creating category in MongoDB", e);
        }
    }

    public Category findById(Integer categoryId) throws SQLException {
        try {
            Document doc = collection.find(Filters.eq(FIELD_CATEGORY_ID, categoryId)).first();
            return doc != null ? mapDocumentToCategory(doc) : null;
        } catch (Exception e) {
            throw new SQLException("Error finding category in MongoDB", e);
        }
    }

    public List<Category> findAll() throws SQLException {
        List<Category> categories = new ArrayList<>();
        try {
            for (Document doc : collection.find().sort(Sorts.ascending("name"))) {
                categories.add(mapDocumentToCategory(doc));
            }
            return categories;
        } catch (Exception e) {
            throw new SQLException("Error finding all categories in MongoDB", e);
        }
    }

    public Category update(Category category) throws SQLException {
        try {
            Document update = new Document("$set",
                    new Document("name", category.getName()));
            collection.updateOne(Filters.eq(FIELD_CATEGORY_ID, category.getCategoryId()), update);
            return category;
        } catch (Exception e) {
            throw new SQLException("Error updating category in MongoDB", e);
        }
    }

    public boolean delete(Integer categoryId) throws SQLException {
        try {
            // Delete the category itself
            long deletedCount = collection.deleteOne(Filters.eq(FIELD_CATEGORY_ID, categoryId)).getDeletedCount();

            // Cascade delete expenses with this categoryId to mimic the former FK constraint
            expensesCollection.deleteMany(Filters.eq(FIELD_CATEGORY_ID, categoryId));

            return deletedCount > 0;
        } catch (Exception e) {
            throw new SQLException("Error deleting category in MongoDB", e);
        }
    }

    private Category mapDocumentToCategory(Document doc) {
        Category category = new Category();
        category.setCategoryId(doc.getInteger(FIELD_CATEGORY_ID));
        category.setName(doc.getString("name"));
        return category;
    }
}

