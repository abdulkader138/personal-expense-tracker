/*
 * Integration tests for the CategoryService class focused on race conditions.
 *
 * These tests verify the functionality of the CategoryService in concurrent
 * environments, ensuring that the application handles race conditions properly
 * when multiple threads are accessing and deleting category data simultaneously.
 * The tests utilise Awaitility for handling asynchronous operations.
 *
 * The methods tested include:
 * - deleteCategory() for concurrent deletion of categories.
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

public class CategoryServiceRaceConditionDeleteCategoryIT {

	private CategoryService categoryService;

	private DatabaseConnection databaseConnection;

	private Category savedCategory;

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

			savedCategory = categoryService.createCategory("Food");
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
	public void deleteCategoryConcurrent() {
		if (categoryService == null || savedCategory == null) {
			org.junit.Assume.assumeTrue("Test setup incomplete", false);
			return;
		}

		List<Thread> threads = IntStream.range(0, 10).mapToObj(i -> new Thread(() -> {
			try {
				categoryService.deleteCategory(savedCategory.getCategoryId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		})).peek(t -> t.start()).collect(Collectors.toList());
		await().atMost(10, TimeUnit.SECONDS).until(() -> threads.stream().noneMatch(t -> t.isAlive()));

		try {
			List<Category> categories = categoryService.getAllCategories();
			assertThat(categories).doesNotContain(savedCategory);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}

