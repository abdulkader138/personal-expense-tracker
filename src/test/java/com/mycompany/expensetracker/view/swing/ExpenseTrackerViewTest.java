package com.mycompany.expensetracker.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.model.Expense;

@RunWith(GUITestRunner.class)
public class ExpenseTrackerViewTest extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	private ExpenseTrackerView view;

	@Override
	protected void onSetUp() {
		view = GuiActionRunner.execute(() -> new ExpenseTrackerView());
		window = new FrameFixture(robot(), view);
		window.show();
	}

	@Test
	public void testAddButtonIsDisabledByDefault() {
		window.button("btnAddExpense").requireDisabled();
	}

	@Test
	public void testAddButtonEnabledWhenDescriptionAndAmountFilled() {
		window.textBox("txtDescription").enterText("Lunch");
		window.textBox("txtAmount").enterText("10.0");
		window.button("btnAddExpense").requireEnabled();
	}

	@Test
	public void testAddButtonDisabledWhenDescriptionCleared() {
		window.textBox("txtDescription").enterText("Lunch");
		window.textBox("txtAmount").enterText("10.0");
		window.textBox("txtDescription").deleteText();
		window.button("btnAddExpense").requireDisabled();
	}

	@Test
	public void testShowExpensesUpdatesListModel() {
		List<Expense> expenses = Arrays.asList(
			new Expense("1", "Lunch", 10.0, null),
			new Expense("2", "Dinner", 20.0, null)
		);
		GuiActionRunner.execute(() -> view.showExpenses(expenses));
		assertThat(window.list("listExpenses").contents()).hasSize(2);
	}

	@Test
	public void testShowErrorSetsLabelText() {
		GuiActionRunner.execute(() -> view.showError("Test error message"));
		window.label("lblError").requireText("Test error message");
	}

	@Test
	public void testShowCategoriesUpdatesComboBox() {
		List<Category> categories = Arrays.asList(
			new Category("1", "Food"),
			new Category("2", "Transport")
		);
		GuiActionRunner.execute(() -> view.showCategories(categories));
		window.comboBox("comboCategory").requireItemCount(2);
	}
}
