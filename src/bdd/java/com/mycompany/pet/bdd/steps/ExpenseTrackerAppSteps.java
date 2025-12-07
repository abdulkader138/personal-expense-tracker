/*
 * BDD steps for the ExpenseTrackerApp.
 * 
 * These steps define the behavior of the application during Cucumber BDD tests.
 */

package com.mycompany.pet.bdd.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.core.matcher.JButtonMatcher.withText;

import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;

import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.dao.ExpenseDAO;
import com.mycompany.pet.database.DatabaseConnection;
import com.mycompany.pet.database.DatabaseInitializer;
import com.mycompany.pet.service.CategoryService;
import com.mycompany.pet.service.ExpenseService;
import com.mycompany.pet.ui.MainWindow;

import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * The Class ExpenseTrackerAppSteps.
 */
public class ExpenseTrackerAppSteps extends ConfigSteps {

	/** The robot with current awt hierarchy. */
	private Robot robotWithCurrentAwtHierarchy = BasicRobot.robotWithCurrentAwtHierarchy();

	/** The main window. */
	private FrameFixture mainWindow;

	/** The category service. */
	private CategoryService categoryService;

	/** The expense service. */
	private ExpenseService expenseService;

	/**
	 * Tear down.
	 */
	@After
	public void tearDown() {
		if (mainWindow != null) {
			mainWindow.cleanUp();
		}
	}

	/**
	 * The main window is shown.
	 */
	@When("The Main Window is shown")
	public void the_Main_Window_is_shown() {
		DatabaseConnection dbConnection = databaseConfig.getDatabaseConnection();
		DatabaseInitializer initializer = new DatabaseInitializer(dbConnection);
		initializer.initialize();

		CategoryDAO categoryDAO = new CategoryDAO(dbConnection);
		ExpenseDAO expenseDAO = new ExpenseDAO(dbConnection);
		categoryService = new CategoryService(categoryDAO);
		expenseService = new ExpenseService(expenseDAO, categoryDAO);

		// Create and show main window
		MainWindow window = new MainWindow(categoryService, expenseService);
		window.setVisible(true);

		mainWindow = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Personal Expense Tracker".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robotWithCurrentAwtHierarchy);
	}

	/**
	 * The expense table contains an element with the following values.
	 *
	 * @param values the values
	 */
	@Then("The expense table contains an element with the following values")
	public void the_expense_table_contains_an_element_with_the_following_values(List<List<String>> values) {
		values.forEach(v -> {
			org.assertj.swing.fixture.JTableFixture table = mainWindow.table();
			boolean found = false;
			for (int i = 0; i < table.rowCount(); i++) {
				String description = table.cell(TableCell.row(i).column(3)).value();
				if (description.contains(v.get(2))) { // Description is in column 3
					found = true;
					break;
				}
			}
			assertThat(found).isTrue();
		});
	}

	/**
	 * The user clicks the {string} button.
	 *
	 * @param buttonText the button text
	 */
	@When("The user clicks the {string} button")
	public void the_user_clicks_the_button(String buttonText) {
		mainWindow.button(withText(buttonText)).click();
	}

	/**
	 * The user enters the following values in the expense dialog.
	 *
	 * @param values the values
	 */
	@Then("The user enters the following values in the expense dialog")
	public void the_user_enters_the_following_values_in_the_expense_dialog(List<Map<String, String>> values) {
		DialogFixture dialog = mainWindow.dialog();
		Map<String, String> row = values.get(0);
		
		// Date field (first text box)
		if (row.containsKey("Date")) {
			dialog.textBox().enterText(row.get("Date"));
		}
		// Amount field (second text box)
		if (row.containsKey("Amount")) {
			dialog.textBox().enterText(row.get("Amount"));
		}
		// Description field (third text box)
		if (row.containsKey("Description")) {
			dialog.textBox().enterText(row.get("Description"));
		}
		// Category combo box
		if (row.containsKey("Category")) {
			dialog.comboBox().selectItem(row.get("Category"));
		}
	}

	/**
	 * The user clicks the dialog {string} button.
	 *
	 * @param buttonText the button text
	 */
	@When("The user clicks the dialog {string} button")
	public void the_user_clicks_the_dialog_button(String buttonText) {
		DialogFixture dialog = mainWindow.dialog();
		dialog.button(withText(buttonText)).click();
	}

	/**
	 * The user selects expense from the table.
	 *
	 * @param values the values
	 */
	@Then("The user selects expense from the table")
	public void the_user_selects_expense_from_the_table(List<String> values) {
		org.assertj.swing.fixture.JTableFixture table = mainWindow.table();
		String searchText = values.get(0);
		
		// Find row containing the search text
		for (int i = 0; i < table.rowCount(); i++) {
			String description = table.cell(TableCell.row(i).column(3)).value();
			if (description.contains(searchText)) {
				table.selectRows(i);
				break;
			}
		}
	}

	/**
	 * The user confirms deletion.
	 */
	@When("The user confirms deletion")
	public void the_user_confirms_deletion() {
		DialogFixture confirmDialog = mainWindow.dialog();
		confirmDialog.button(withText("Yes")).click();
	}

	/**
	 * The expense table does not contain an element with the following values.
	 *
	 * @param values the values
	 */
	@Then("The expense table does not contain an element with the following values")
	public void the_expense_table_does_not_contain_an_element_with_the_following_values(List<List<String>> values) {
		values.forEach(v -> {
			org.assertj.swing.fixture.JTableFixture table = mainWindow.table();
			boolean found = false;
			for (int i = 0; i < table.rowCount(); i++) {
				String description = table.cell(TableCell.row(i).column(3)).value();
				if (description.contains(v.get(2))) {
					found = true;
					break;
				}
			}
			assertThat(found).isFalse();
		});
	}
}

