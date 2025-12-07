/*
 * Configuration steps for the ExpenseTrackerApp BDD tests.
 * 
 * This class provides the necessary configuration constants used in the BDD test steps.
 * 
 * The configuration includes:
 * 
 * - Database connection details:
 *   - `host`: The hostname of the MongoDB database server.
 *   - `port`: The port number of the MongoDB database server.
 *   - `database`: The name of the MongoDB database, retrieved from system properties.
 * 
 * These constants are used across various BDD steps to ensure consistent and correct database connections and configurations during the tests.
 */

package com.mycompany.pet.bdd.steps;

import org.junit.BeforeClass;

import com.mycompany.pet.DatabaseConfig;
import com.mycompany.pet.configurations.DBConfig;

/**
 * The Class ConfigSteps.
 */
public class ConfigSteps {

	/** The database configuration instance. */
	static DBConfig databaseConfig;

	@BeforeClass
	public static void setup() {
		databaseConfig = DatabaseConfig.getDatabaseConfig();
		// Check for database connection
		databaseConfig.testAndStartDatabaseConnection();
	}
}

