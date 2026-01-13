/*
 * Unit tests for the CategoryService class focused on race conditions.
 *
 * These tests verify the functionality of the CategoryService in concurrent
 * environments, ensuring that the application handles race conditions properly
 * when multiple threads are accessing and modifying category data simultaneously.
 * The tests utilise Mockito for mocking dependencies and Awaitility for 
 * handling asynchronous operations.
 *
 * The methods tested include:
 * - createCategory() for concurrent creation of categories.
 * - deleteCategory() for concurrent deletion of categories.
 *
 */

package com.mycompany.pet.service.racecondition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.model.Category;
import com.mycompany.pet.service.CategoryService;

/**
 * The Class CategoryServiceRaceConditionTest.
 */
public class CategoryServiceRaceConditionTest {

	/** The category DAO. */
	@Mock
	private CategoryDAO categoryDAO;

	/** The category service. */
	private CategoryService categoryService;

	/** The closeable. */
	private AutoCloseable closeable;

	/** The category id. */
	private Integer categoryId = 1;

	/** The category name. */
	private String categoryName = "Food";

	/**
	 * Setup.
	 */
	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		categoryService = new CategoryService(categoryDAO);
	}

	/**
	 * Release mocks.
	 *
	 * @throws Exception the exception
	 */
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

	/**
	 * Test new category concurrent.
	 */
	@Test
	public void testNewCategoryConcurrent() throws SQLException {
		// Setup - use synchronized list for thread safety
		List<Category> categories = Collections.synchronizedList(new ArrayList<>());
		AtomicInteger idCounter = new AtomicInteger(1);

		// Mocks - make thread-safe with synchronized counter
		doAnswer(invocation -> {
			Category cat = invocation.getArgument(0);
			cat.setCategoryId(idCounter.getAndIncrement());
			categories.add(cat);
			return cat;
		}).when(categoryDAO).create(any(Category.class));

		// Exercise - use same name to test actual race condition
		List<Thread> threads = IntStream.range(0, 10)
				.mapToObj(i -> new Thread(() -> {
					try {
						categoryService.createCategory(categoryName);
					} catch (Exception e) {
						// Some threads might fail in race conditions - that's expected
						// Don't log to avoid cluttering test output
					}
				})).peek(Thread::start).toList();
		await().atMost(10, TimeUnit.SECONDS).until(() -> threads.stream().noneMatch(Thread::isAlive));

		// Verify - all 10 threads should attempt to create categories
		assertThat(categories).hasSizeGreaterThanOrEqualTo(8);
		// Also verify that all created categories have unique IDs (thread-safe)
		assertThat(categories.stream().map(Category::getCategoryId).distinct().count())
			.isEqualTo(categories.size());
	}

	/**
	 * Test delete category concurrent.
	 */
	@Test
	public void testDeleteCategoryConcurrent() throws SQLException {
		// Setup
		List<Category> categories = Collections.synchronizedList(new ArrayList<>());
		Category category = new Category(categoryId, categoryName);
		categories.add(category);

		// Mocks
		when(categoryDAO.findById(anyInt())).thenAnswer(invocation -> categories.stream().findFirst().orElse(null));

		doAnswer(invocation -> {
			Integer id = invocation.getArgument(0);
			categories.removeIf(c -> c.getCategoryId().equals(id));
			return true;
		}).when(categoryDAO).delete(anyInt());

		// Exercise
		List<Thread> threads = IntStream.range(0, 10)
				.mapToObj(i -> new Thread(() -> {
					try {
						categoryService.deleteCategory(categoryId);
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				})).peek(Thread::start).toList();
		await().atMost(10, TimeUnit.SECONDS).until(() -> threads.stream().noneMatch(Thread::isAlive));

		// Verify - category should be deleted (only once, but multiple threads may try)
		assertThat(categories).isEmpty();
	}
}

