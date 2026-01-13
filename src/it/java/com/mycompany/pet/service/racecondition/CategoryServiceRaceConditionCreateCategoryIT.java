/*
 * Integration tests for the CategoryService class focused on race conditions.
 *
 * These tests verify the functionality of the CategoryService in concurrent
 * environments, ensuring that the application handles race conditions properly
 * when multiple threads are accessing and creating category data simultaneously.
 * The tests utilise Awaitility for handling asynchronous operations.
 *
 * The methods tested include:
 * - createCategory() for concurrent creation of categories.
 * 
 * The setup and teardown methods handle the initialisation and cleanup of database connections.
 *
 * The databaseConfig variable is responsible for starting the Docker container.
 * If the test is run from Eclipse, it runs the Docker container using Testcontainers.
 * If the test is run using a Maven command, it starts a Docker container without test containers.
 */

package com.mycompany.pet.service.racecondition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mycompany.pet.DatabaseConfig;
import com.mycompany.pet.configurations.DBConfig;
import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.database.DatabaseConnection;
import com.mycompany.pet.database.DatabaseInitializer;
import com.mycompany.pet.model.Category;
import com.mycompany.pet.service.CategoryService;

public class CategoryServiceRaceConditionCreateCategoryIT {

	private CategoryService categoryService;

	private DatabaseConnection databaseConnection;

	private String CATEGORY_NAME = "Food";

	private static DBConfig databaseConfig;

	@BeforeClass
	public static void setupServer() {
		try {
			databaseConfig = DatabaseConfig.getDatabaseConfig();
			if (databaseConfig == null) {
				org.junit.Assume.assumeTrue("Database config not available", false);
				return;
			}
			databaseConfig.testAndStartDatabaseConnection();
		} catch (Exception e) {
			org.junit.Assume.assumeNoException("Database setup failed. Docker may not be available. Skipping integration tests.", e);
		}
	}

	@Before
	public void setUp() throws SQLException {
		if (databaseConfig == null) {
			org.junit.Assume.assumeTrue("Database config not available", false);
			return;
		}
		
		try {
			databaseConnection = databaseConfig.getDatabaseConnection();
			if (databaseConnection == null) {
				org.junit.Assume.assumeTrue("Database connection not available", false);
				return;
			}
			
			try {
				DatabaseInitializer initializer = new DatabaseInitializer(databaseConnection);
				initializer.initialize();
			} catch (Exception e) {
				org.junit.Assume.assumeNoException("Failed to initialize database. Skipping integration tests.", e);
				return;
			}

			CategoryDAO categoryDAO = new CategoryDAO(databaseConnection);
			categoryService = new CategoryService(categoryDAO);
		} catch (Exception e) {
			org.junit.Assume.assumeNoException("Database operation failed. Skipping test.", e);
			return;
		}
	}

	@After
	public void releaseResources() {
		if (databaseConnection != null) {
			databaseConnection.close();
		}
	}

	@Test
	public void createCategoryConcurrent() {
		if (categoryService == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		try {
			databaseConnection.getDatabase().getCollection("categories").drop();
			await().atMost(2, TimeUnit.SECONDS);
		} catch (Exception e) {
			try {
				List<Category> existing = categoryService.getAllCategories();
				for (Category cat : existing) {
					categoryService.deleteCategory(cat.getCategoryId());
				}
				await().atMost(2, TimeUnit.SECONDS);
			} catch (SQLException ex) {
			}
		}

		java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
		java.util.concurrent.atomic.AtomicInteger failureCount = new java.util.concurrent.atomic.AtomicInteger(0);
		List<Thread> threads = IntStream.range(0, 10).mapToObj(i -> new Thread(() -> {
			try {
				categoryService.createCategory(CATEGORY_NAME + "_" + i);
				successCount.incrementAndGet();
			} catch (Exception e) {
				failureCount.incrementAndGet();
				System.err.println("Thread " + i + " failed to create category: " + e.getMessage());
				e.printStackTrace();
			}
		})).peek(t -> t.start()).collect(Collectors.toList());
		
		await().atMost(15, TimeUnit.SECONDS).until(() -> threads.stream().noneMatch(t -> t.isAlive()));

		await().atMost(5, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS).until(() -> {
			try {
				List<Category> categories = categoryService.getAllCategories();
				int currentSize = categories.size();
				return currentSize >= successCount.get();
			} catch (SQLException e) {
				return false;
			}
		});

		try {
			List<Category> categories = categoryService.getAllCategories();
			assertThat(categories).as("Expected 10 categories but found " + categories.size() + ". Success count: " + successCount.get() + ", Failure count: " + failureCount.get()).hasSize(10);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}

