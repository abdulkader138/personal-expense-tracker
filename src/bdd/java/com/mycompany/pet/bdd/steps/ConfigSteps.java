/*
 * Configuration steps for the ExpenseTrackerApp BDD tests.
 * 
 * This class provides the necessary configuration constants used in the BDD test steps.
 * 
 */

package com.mycompany.pet.bdd.steps;

import org.junit.BeforeClass;

import com.mycompany.pet.DatabaseConfig;
import com.mycompany.pet.configurations.DBConfig;

public class ConfigSteps {

	static DBConfig databaseConfig;

	@BeforeClass
	public static void setup() {
		databaseConfig = DatabaseConfig.getDatabaseConfig();
		databaseConfig.testAndStartDatabaseConnection();
	}
}

