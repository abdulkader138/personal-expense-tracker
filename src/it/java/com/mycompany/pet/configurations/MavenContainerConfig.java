package com.mycompany.pet.configurations;

import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import com.mycompany.pet.database.DatabaseConnection;

public class MavenContainerConfig implements DBConfig {

	private static final int MAX_RETRIES = 10;

	private static final long RETRY_DELAY_SECONDS = 2;

	private DatabaseConnection databaseConnection;

	@Override
	public void testAndStartDatabaseConnection() {
		int attempt = 0;
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
			String connectionString = "mongodb://localhost:27017";
			String dbName = System.getProperty("mongodb.dbName", "expense_tracker");
			databaseConnection = new DatabaseConnection(connectionString, dbName);
		}
		return databaseConnection;
	}

	@Override
	public void startApplication() {
	}
}

