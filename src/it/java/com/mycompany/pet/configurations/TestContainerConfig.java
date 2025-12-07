package com.mycompany.pet.configurations;

import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.junit.ClassRule;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.mycompany.pet.database.DatabaseConnection;

/**
 * The Class TestContainerConfig implements DBConfig interface for test
 * container configurations. This class is responsible for setting up and
 * testing the database connection using Testcontainers. It includes retry
 * mechanisms to handle database connection attempts.
 */
public class TestContainerConfig implements DBConfig {

	/** The Constant DB. */
	// The value of database for test container
	public static final String DB = "expense_tracker_test";

	/** The Constant PASSWORD. */
	// Not used for MongoDB, but kept for consistency
	public static final String PASSWORD = "";

	/** The Constant USER. */
	// Not used for MongoDB, but kept for consistency
	public static final String USER = "";

	/** The Constant MAX_RETRIES. */
	// The maximum number of retry attempts for establishing a database connection
	private static final int MAX_RETRIES = 10;

	/** The Constant RETRY_DELAY_SECONDS. */
	// The delay between each retry attempt in seconds
	private static final long RETRY_DELAY_SECONDS = 2;

	/** The Constant mongodb. */
	// Define and configure the MongoDB test container using Testcontainers
	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongodb = new GenericContainer(DockerImageName.parse("mongo:4.4"))
			.withExposedPorts(27017).withEnv("MONGO_INITDB_DATABASE", DB)
			.waitingFor(Wait.forListeningPort());

	/** The Constant HOST. */
	// The host address of the test container
	public static final String HOST = mongodb.getHost();

	/** The Constant PORT. */
	// The port number on which the test container is running
	public static final Integer PORT = mongodb.isRunning() ? mongodb.getMappedPort(27017) : 27017;

	/** The database connection. */
	private DatabaseConnection databaseConnection;

	/**
	 * Test and start database connection.
	 */
	@Override
	public void testAndStartDatabaseConnection() {
		int attempt = 0;

		// Start test container if not already running
		mongodb.start();
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
			String connectionString = "mongodb://" + mongodb.getHost() + ":" + mongodb.getMappedPort(27017);
			databaseConnection = new DatabaseConnection(connectionString, DB);
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

