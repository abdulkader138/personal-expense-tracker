/*
 * Database steps for the ExpenseTrackerApp BDD tests.
 * 
 * This class provides steps for setting up test data in the database.
 */

package com.mycompany.pet.bdd.steps;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.dao.ExpenseDAO;
import com.mycompany.pet.database.DatabaseConnection;
import com.mycompany.pet.database.DatabaseInitializer;
import com.mycompany.pet.model.Category;
import com.mycompany.pet.model.Expense;
import com.mycompany.pet.service.CategoryService;
import com.mycompany.pet.service.ExpenseService;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;

/**
 * The Class DatabaseSteps.
 */
public class DatabaseSteps extends ConfigSteps {

	/** The category service. */
	private CategoryService categoryService;

	/** The expense service. */
	private ExpenseService expenseService;

	/**
	 * Setup database.
	 */
	@Before
	public void setupDatabase() {
		DatabaseConnection dbConnection = databaseConfig.getDatabaseConnection();
		DatabaseInitializer initializer = new DatabaseInitializer(dbConnection);
		initializer.initialize();

		CategoryDAO categoryDAO = new CategoryDAO(dbConnection);
		ExpenseDAO expenseDAO = new ExpenseDAO(dbConnection);
		categoryService = new CategoryService(categoryDAO);
		expenseService = new ExpenseService(expenseDAO, categoryDAO);
	}

	/**
	 * The database contains the category with the following values.
	 *
	 * @param values the values
	 * @throws SQLException the SQL exception
	 */
	@Given("The database contains the category with the following values")
	public void the_database_contains_the_category_with_the_following_values(List<Map<String, String>> values)
			throws SQLException {
		for (Map<String, String> row : values) {
			String name = row.get("Name");
			categoryService.createCategory(name);
		}
	}

	/**
	 * The database contains the expense with the following values.
	 *
	 * @param values the values
	 * @throws SQLException the SQL exception
	 */
	@Given("The database contains the expense with the following values")
	public void the_database_contains_the_expense_with_the_following_values(List<Map<String, String>> values)
			throws SQLException {
		for (Map<String, String> row : values) {
			LocalDate date = LocalDate.parse(row.get("Date"));
			BigDecimal amount = new BigDecimal(row.get("Amount"));
			String description = row.get("Description");
			String categoryName = row.get("Category");

			// Find or create category
			Category category = null;
			try {
				List<Category> categories = categoryService.getAllCategories();
				for (Category cat : categories) {
					if (cat.getName().equals(categoryName)) {
						category = cat;
						break;
					}
				}
				if (category == null) {
					category = categoryService.createCategory(categoryName);
				}
			} catch (SQLException e) {
				category = categoryService.createCategory(categoryName);
			}

			expenseService.createExpense(date, amount, description, category.getCategoryId());
		}
	}

	/**
	 * The database deletes the expense with the following values.
	 *
	 * @param values the values
	 * @throws SQLException the SQL exception
	 */
	@Given("The database deletes the expense with the following values")
	public void the_database_deletes_the_expense_with_the_following_values(List<Map<String, String>> values)
			throws SQLException {
		for (Map<String, String> row : values) {
			Integer expenseId = Integer.parseInt(row.get("ID"));
			expenseService.deleteExpense(expenseId);
		}
	}
}

