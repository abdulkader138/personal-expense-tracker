package com.mycompany.pet.configurations;

import com.mycompany.pet.database.DatabaseConnection;

/**
 * The DBConfig interface defines the contract for database configurations.
 * Implementing classes are responsible for testing and starting the database
 * connection, as well as providing the DatabaseConnection for MongoDB operations.
 * 
 * 1. If the test is initiated via Eclipse, it defaults to using TestContainer
 * for the database and starts the MongoDB Docker test container.
 * 
 * 2. If the test is initiated via "maven" command, it does not use test containers but
 * container for the database and starts the MongoDB Docker container as defined in the
 * pom.xml with the "integration-test-profile" profile.
 */
public interface DBConfig {

	/**
	 * Test and start database connection. This method is responsible for attempting
	 * to establish a database connection, with potential retry mechanisms.
	 */
	public void testAndStartDatabaseConnection();

	/**
	 * This method provides a DatabaseConnection for MongoDB operations.
	 * 
	 * @return the database connection
	 */
	public DatabaseConnection getDatabaseConnection();

	/**
	 * Starts the application for E2E tests.
	 */
	public void startApplication();
}

