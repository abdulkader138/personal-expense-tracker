package com.mycompany.expensetracker.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.MongoDBContainer;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.model.Expense;
import com.mycompany.expensetracker.repository.mongo.MongoExpenseRepository;

public class MongoExpenseRepositoryIT {

	@ClassRule
	public static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

	private MongoExpenseRepository repository;
	private MongoClient client;

	@Before
	public void setUp() {
		client = new MongoClient(new MongoClientURI(mongo.getConnectionString()));
		MongoDatabase database = client.getDatabase("test");
		database.getCollection("expenses").drop();
		repository = new MongoExpenseRepository(database);
	}

	@After
	public void tearDown() {
		client.close();
	}

	@Test
	public void testSaveAndFindAll() {
		Category category = new Category("c1", "Food");
		Expense expense = new Expense("1", "Lunch", 10.0, category);
		repository.save(expense);
		List<Expense> result = repository.findAll();
		assertThat(result).containsExactly(expense);
	}

	@Test
	public void testFindById() {
		Category category = new Category("c1", "Food");
		Expense expense = new Expense("1", "Lunch", 10.0, category);
		repository.save(expense);
		assertThat(repository.findById("1")).isEqualTo(expense);
	}

	@Test
	public void testUpdate() {
		Category category = new Category("c1", "Food");
		Expense expense = new Expense("1", "Lunch", 10.0, category);
		repository.save(expense);
		Expense updated = new Expense("1", "Dinner", 20.0, category);
		repository.update(updated);
		assertThat(repository.findById("1")).isEqualTo(updated);
	}

	@Test
	public void testUpdateWithNullCategory() {
		Category category = new Category("c1", "Food");
		Expense expense = new Expense("1", "Lunch", 10.0, category);
		repository.save(expense);
		Expense updated = new Expense("1", "Dinner", 20.0, null);
		repository.update(updated);
		assertThat(repository.findById("1")).isEqualTo(updated);
	}

	@Test
	public void testDelete() {
		Expense expense = new Expense("1", "Lunch", 10.0, null);
		repository.save(expense);
		repository.delete("1");
		assertThat(repository.findAll()).isEmpty();
	}

	@Test
	public void testFindByIdReturnsNullWhenNotFound() {
		assertThat(repository.findById("nonexistent")).isNull();
	}

	@Test
	public void testSaveAndFindAllWithNullCategory() {
		Expense expense = new Expense("1", "Lunch", 10.0, null);
		repository.save(expense);
		List<Expense> result = repository.findAll();
		assertThat(result).containsExactly(expense);
	}
}
