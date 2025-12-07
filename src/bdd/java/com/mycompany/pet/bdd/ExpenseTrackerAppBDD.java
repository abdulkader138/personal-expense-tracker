/*
 * BDD test runner for the ExpenseTrackerApp.
 * 
 * This class configures and runs the Cucumber BDD tests.
 * 
 * - Annotations:
 *   - `@RunWith(Cucumber.class)`: Specifies that the class should use the Cucumber JUnit runner.
 *   - `@CucumberOptions`: Provides options for running the Cucumber tests:
 *     - `features`: Specifies the path to the feature files.
 *     - `monochrome`: Ensures that the console output is readable by disabling colored output.
 * 
 * - Setup:
 *   - The `setUpOnce` method is annotated with `@BeforeClass` to ensure it runs once before any of the tests.
 *   - `FailOnThreadViolationRepaintManager.install()`: Ensures that all access to Swing components is performed in the Event Dispatch Thread (EDT), which is critical for thread safety in Swing applications.
 * 
 *  * Note:
 * These tests will run using Eclipse but are configured to run using Maven with the `integration-test-profile` profile. To execute these tests, use the following Maven command with the specified profile and arguments:
 * ```
 * mvn test -Pintegration-test-profile -Dmongodb.dbName=$DATABASE -Dmongodb.server=maven
 * 
 * ```
 * This configuration ensures that the Cucumber BDD tests are executed correctly and that the Swing application components are accessed in a thread-safe manner.
 *
 * To add more BDD tests, edit the feature files located in `src/bdd/resources/expense_view.feature` or `src/bdd/resources/category_view.feature`.
 * 
 *
 * @see CategoryService
 * @see ExpenseService
 * @see CategoryDAO
 * @see ExpenseDAO
 * @see DatabaseConfig
 */

package com.mycompany.pet.bdd;

import java.awt.GraphicsEnvironment;

import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.mycompany.pet.DatabaseConfig;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

/**
 * The Class ExpenseTrackerAppBDD.
 */
@RunWith(Cucumber.class)
@CucumberOptions(features = "src/bdd/resources", monochrome = true, strict = true)
public class ExpenseTrackerAppBDD {

	/**
	 * Sets the up once.
	 */
	@BeforeClass
	public static void setUpOnce() {
		// Skip UI tests if running in headless mode
		Assume.assumeFalse("Skipping UI test - running in headless mode", 
			GraphicsEnvironment.isHeadless());
		
		// Checks that all access to Swing components is performed in the EDT.
		FailOnThreadViolationRepaintManager.install();
		DatabaseConfig.getDatabaseConfig().testAndStartDatabaseConnection();
	}
}

