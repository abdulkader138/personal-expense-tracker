package com.mycompany.pet.configurations;

import com.mycompany.pet.database.DatabaseConnection;

public interface DBConfig {

	public void testAndStartDatabaseConnection();

	public DatabaseConnection getDatabaseConnection();

	public void startApplication();
}

