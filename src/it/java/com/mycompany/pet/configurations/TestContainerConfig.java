package com.mycompany.pet.configurations;

import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.junit.ClassRule;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.mycompany.pet.database.DatabaseConnection;

public class TestContainerConfig implements DBConfig {

	public static final String DB = "expense_tracker_test";

	public static final String PASSWORD = "";

	public static final String USER = "";

	private static final int MAX_RETRIES = 10;

	private static final long RETRY_DELAY_SECONDS = 2;

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongodb = new GenericContainer(DockerImageName.parse("mongo:4.4"))
			.withExposedPorts(27017).withEnv("MONGO_INITDB_DATABASE", DB)
			.waitingFor(Wait.forListeningPort());

	public static final String HOST = mongodb.getHost();

	public static final Integer PORT = mongodb.isRunning() ? mongodb.getMappedPort(27017) : 27017;

	private DatabaseConnection databaseConnection;

	@Override
	public void testAndStartDatabaseConnection() {
		int attempt = 0;

		mongodb.start();
		while (attempt < MAX_RETRIES) {
			try {
				DatabaseConnection dbConnection = getDatabaseConnection();

				if (dbConnection != null) {
					dbConnection.getDatabase();
					break;
				}
			} catch (Exception i) {
				attempt++;
				if (attempt < MAX_RETRIES) {
					await().atMost(RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
				}
			}
		}
	}

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
	}
}

