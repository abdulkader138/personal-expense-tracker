package com.mycompany.expensetracker.repository.mongo;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.model.Expense;
import com.mycompany.expensetracker.repository.ExpenseRepository;

public class MongoExpenseRepository implements ExpenseRepository {

	private static final String COLLECTION = "expenses";

	private final MongoCollection<Document> mongoCollection;

	public MongoExpenseRepository(MongoDatabase database) {
		this.mongoCollection = database.getCollection(COLLECTION);
	}

	@Override
	public void save(Expense expense) {
		mongoCollection.insertOne(toDocument(expense));
	}

	@Override
	public List<Expense> findAll() {
		List<Expense> result = new ArrayList<>();
		for (Document doc : mongoCollection.find()) {
			result.add(fromDocument(doc));
		}
		return result;
	}

	@Override
	public Expense findById(String id) {
		Document doc = mongoCollection.find(new Document("id", id)).first();
		if (doc == null) {
			return null;
		}
		return fromDocument(doc);
	}

	@Override
	public void update(Expense expense) {
		mongoCollection.replaceOne(
				new Document("id", expense.getId()),
				toDocument(expense));
	}

	@Override
	public void delete(String id) {
		mongoCollection.deleteOne(new Document("id", id));
	}

	private Document toDocument(Expense expense) {
		Document doc = new Document("id", expense.getId())
				.append("description", expense.getDescription())
				.append("amount", expense.getAmount());
		if (expense.getCategory() != null) {
			doc.append("categoryId", expense.getCategory().getId())
					.append("categoryName", expense.getCategory().getName());
		}
		return doc;
	}

	private Expense fromDocument(Document doc) {
		Category category = null;
		String categoryId = doc.getString("categoryId");
		if (categoryId != null) {
			category = new Category(categoryId, doc.getString("categoryName"));
		}
		return new Expense(
				doc.getString("id"),
				doc.getString("description"),
				doc.getDouble("amount"),
				category);
	}
}
