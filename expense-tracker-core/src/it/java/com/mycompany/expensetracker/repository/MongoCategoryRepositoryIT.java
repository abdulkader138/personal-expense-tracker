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
import com.mycompany.expensetracker.repository.mongo.MongoCategoryRepository;

public class MongoCategoryRepositoryIT {

	@ClassRule
	public static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

	private MongoCategoryRepository repository;
	private MongoClient client;

	@Before
	public void setUp() {
		client = new MongoClient(new MongoClientURI(mongo.getConnectionString()));
		MongoDatabase database = client.getDatabase("test");
		database.getCollection("categories").drop();
		repository = new MongoCategoryRepository(database);
	}

	@After
	public void tearDown() {
		client.close();
	}

	@Test
	public void testSaveAndFindAll() {
		Category category = new Category("1", "Food");
		repository.save(category);
		List<Category> result = repository.findAll();
		assertThat(result).containsExactly(category);
	}

	@Test
	public void testFindById() {
		Category category = new Category("1", "Food");
		repository.save(category);
		assertThat(repository.findById("1")).isEqualTo(category);
	}

	@Test
	public void testDelete() {
		Category category = new Category("1", "Food");
		repository.save(category);
		repository.delete("1");
		assertThat(repository.findAll()).isEmpty();
	}

	@Test
	public void testFindByIdReturnsNullWhenNotFound() {
		assertThat(repository.findById("nonexistent")).isNull();
	}
}
