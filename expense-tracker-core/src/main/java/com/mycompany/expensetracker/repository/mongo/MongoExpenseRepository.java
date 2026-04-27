package com.mycompany.expensetracker.repository.mongo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.model.Expense;
import com.mycompany.expensetracker.repository.ExpenseRepository;

public class MongoExpenseRepository implements ExpenseRepository {

	private final MongoCollection<Document> collection;

	public MongoExpenseRepository(MongoDatabase database) {
		collection = database.getCollection("expenses");
	}

	@Override
	public void save(Expense expense) {
		Document doc = new Document("_id", expense.getId())
				.append("description", expense.getDescription())
				.append("amount", expense.getAmount());
		if (expense.getCategory() != null) {
			doc.append("category", new Document("_id", expense.getCategory().getId())
					.append("name", expense.getCategory().getName()));
		}
		collection.insertOne(doc);
	}

	@Override
	public List<Expense> findAll() {
		return StreamSupport.stream(collection.find().spliterator(), false)
				.map(this::documentToExpense)
				.collect(Collectors.toList());
	}

	@Override
	public Expense findById(String id) {
		Document doc = collection.find(new Document("_id", id)).first();
		if (doc == null) return null;
		return documentToExpense(doc);
	}

	@Override
	public void update(Expense expense) {
		Document setDoc = new Document("description", expense.getDescription())
				.append("amount", expense.getAmount());
		if (expense.getCategory() != null) {
			setDoc.append("category", new Document("_id", expense.getCategory().getId())
					.append("name", expense.getCategory().getName()));
		} else {
			setDoc.append("category", null);
		}
		collection.updateOne(new Document("_id", expense.getId()), new Document("$set", setDoc));
	}

	@Override
	public void delete(String id) {
		collection.deleteOne(new Document("_id", id));
	}

	private Expense documentToExpense(Document doc) {
		Category category = null;
		Document catDoc = doc.get("category", Document.class);
		if (catDoc != null) {
			category = new Category(catDoc.getString("_id"), catDoc.getString("name"));
		}
		return new Expense(doc.getString("_id"), doc.getString("description"),
				doc.getDouble("amount"), category);
	}
}
