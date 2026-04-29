package com.mycompany.expensetracker.repository.mongo;

import java.util.List;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.repository.CategoryRepository;

public class MongoCategoryRepository implements CategoryRepository {

	private final MongoCollection<Document> collection;

	public MongoCategoryRepository(MongoDatabase database) {
		collection = database.getCollection("categories");
	}

	@Override
	public void save(Category category) {
		collection.insertOne(new Document("_id", category.getId()).append("name", category.getName()));
	}

	@Override
	public List<Category> findAll() {
		return StreamSupport.stream(collection.find().spliterator(), false)
				.map(doc -> new Category(doc.getString("_id"), doc.getString("name")))
				.toList();
	}

	@Override
	public Category findById(String id) {
		Document doc = collection.find(new Document("_id", id)).first();
		if (doc == null) return null;
		return new Category(doc.getString("_id"), doc.getString("name"));
	}

	@Override
	public void delete(String id) {
		collection.deleteOne(new Document("_id", id));
	}
}
