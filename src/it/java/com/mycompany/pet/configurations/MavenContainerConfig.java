package com.mycompany.pet.configurations;

import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import com.mycompany.pet.database.DatabaseConnection;

/**
 * The Class MavenContainerConfig implements DBConfig interface for database
 * configurations. This class is responsible for setting up and testing the
 * database connection using docker container without test containers. It includes retry mechanisms
 * to handle database connection attempts.
 */
public class MavenContainerConfig implements DBConfig {

	/** The Constant MAX_RETRIES. */
	// The maximum number of retry attempts for establishing a database connection
	private static final int MAX_RETRIES = 10;

	/** The Constant RETRY_DELAY_SECONDS. */
	// The delay between each retry attempt in seconds
	private static final long RETRY_DELAY_SECONDS = 2;

	/** The database connection. */
	private DatabaseConnection databaseConnection;

	/**
	 * Test and start database connection. This method attempts to establish a
	 * database connection with a retry mechanism. If the connection fails, it
	 * retries up to MAX_RETRIES times with a delay of RETRY_DELAY_SECONDS between
	 * each attempt.
	 */
	@Override
	public void testAndStartDatabaseConnection() {
		int attempt = 0;
		while (attempt < MAX_RETRIES) {
			try {
				// Attempt to get the DatabaseConnection
				DatabaseConnection dbConnection = getDatabaseConnection();

				// Check if the connection is successfully created
				if (dbConnection != null) {
					// Try to get database to verify connection
					dbConnection.getDatabase();
					break;
				}
			} catch (Exception i) {
				attempt++;
				if (attempt < MAX_RETRIES) {
					// Wait for the specified retry delay before the next attempt
					await().atMost(RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
				}
			}
		}
	}

	/**
	 * Gets the database connection.
	 *
	 * @return the database connection
	 */
	@Override
	public DatabaseConnection getDatabaseConnection() {
		if (databaseConnection == null) {
			// Use localhost with default port for Maven container setup
			// The docker-maven-plugin should start MongoDB on localhost:27017
			String connectionString = "mongodb://localhost:27017";
			String dbName = System.getProperty("mongodb.dbName", "expense_tracker");
			databaseConnection = new DatabaseConnection(connectionString, dbName);
		}
		return databaseConnection;
	}

	@Override
	public void startApplication() {
		// For MongoDB, we don't need to start the application with special args
		// The application will use the connection string from the database connection
		// This method is kept for consistency with the interface
	}
}

