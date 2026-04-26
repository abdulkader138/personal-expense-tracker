package com.mycompany.expensetracker.repository.mongo;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.repository.CategoryRepository;

public class MongoCategoryRepository implements CategoryRepository {

	private static final String COLLECTION = "categories";

	private final MongoCollection<Document> mongoCollection;

	public MongoCategoryRepository(MongoDatabase database) {
		this.mongoCollection = database.getCollection(COLLECTION);
	}

	@Override
	public void save(Category category) {
		mongoCollection.insertOne(toDocument(category));
	}

	@Override
	public List<Category> findAll() {
		List<Category> result = new ArrayList<>();
		for (Document doc : mongoCollection.find()) {
			result.add(fromDocument(doc));
		}
		return result;
	}

	@Override
	public Category findById(String id) {
		Document doc = mongoCollection.find(new Document("id", id)).first();
		if (doc == null) {
			return null;
		}
		return fromDocument(doc);
	}

	@Override
	public void delete(String id) {
		mongoCollection.deleteOne(new Document("id", id));
	}

	private Document toDocument(Category category) {
		return new Document("id", category.getId())
				.append("name", category.getName());
	}

	private Category fromDocument(Document doc) {
		return new Category(doc.getString("id"), doc.getString("name"));
	}
}
