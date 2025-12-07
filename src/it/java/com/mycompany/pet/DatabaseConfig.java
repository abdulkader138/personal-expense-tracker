package com.mycompany.pet;

import com.mycompany.pet.configurations.DBConfig;
import com.mycompany.pet.configurations.MavenContainerConfig;
import com.mycompany.pet.configurations.TestContainerConfig;

/**
 * The DatabaseConfig class is responsible for selecting the appropriate
 * database configuration based on the "mongodb.server" system property. It
 * offers a method to obtain the relevant DBConfig implementation.
 * 
 * 1. If the property is not set and the test is initiated via Eclipse, it
 * defaults to using TestContainer for the database and starts the MongoDB
 * Docker test container.
 * 
 * 2. If the property is set to "maven", it uses the container for the database
 * and starts the MongoDB Docker container without test containers as defined
 * in the pom.xml with the "integration-test-profile" profile.
 */
public class DatabaseConfig {
	/** The database configuration instance. */
	public static DBConfig databaseConfig;

	/**
	 * This method checks the system property "mongodb.server" to determine which
	 * DBConfig implementation to use.
	 * 
	 * 1. If the property is not set and the test is initiated via Eclipse, it
	 * defaults to using TestContainer for the database and starts the MongoDB
	 * Docker test container.
	 * 
	 * 2. If the property is set to "maven", it uses the container for the database
	 * and starts the MongoDB Docker container without test containers as defined
	 * in the pom.xml with the "integration-test-profile" profile.
	 *
	 * 
	 * @return the database configuration
	 */
	
	public static DBConfig getDatabaseConfig() {
		String runningServerFrom = System.getProperty("mongodb.server");
		if (runningServerFrom == null) {
			databaseConfig = new TestContainerConfig();
		} else if (runningServerFrom.equals("maven")) {
			databaseConfig = new MavenContainerConfig();
		}
		return databaseConfig;
	}
}

