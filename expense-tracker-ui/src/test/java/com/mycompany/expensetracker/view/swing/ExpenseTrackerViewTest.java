package com.mycompany.expensetracker.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

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
		view = GuiActionRunner.execute(ExpenseTrackerView::new);
		window = new FrameFixture(robot(), view);
		window.show(new java.awt.Dimension(800, 600));
	}

	@Test
	public void testAddButtonIsDisabledByDefault() {
		window.button("btnAddExpense").requireDisabled();
		assertThat(window.button("btnAddExpense").target().isEnabled()).isFalse();
	}

	@Test
	public void testAddButtonEnabledWhenDescriptionAndAmountFilled() {
		window.textBox("txtDescription").enterText("Lunch");
		window.textBox("txtAmount").enterText("10.0");
		window.button("btnAddExpense").requireEnabled();
		assertThat(window.button("btnAddExpense").target().isEnabled()).isTrue();
	}

	@Test
	public void testAddButtonDisabledWhenDescriptionCleared() {
		window.textBox("txtDescription").enterText("Lunch");
		window.textBox("txtAmount").enterText("10.0");
		window.textBox("txtDescription").deleteText();
		window.button("btnAddExpense").requireDisabled();
		assertThat(window.button("btnAddExpense").target().isEnabled()).isFalse();
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
		assertThat(window.label("lblError").target().getText()).isEqualTo("Test error message");
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

	@Test
	public void testGetDescriptionTextReturnsFieldContent() {
		GuiActionRunner.execute(() -> view.txtDescription.setText("Lunch"));
		assertThat(view.getDescriptionText()).isEqualTo("Lunch");
	}

	@Test
	public void testGetAmountTextReturnsFieldContent() {
		GuiActionRunner.execute(() -> view.txtAmount.setText("10.0"));
		assertThat(view.getAmountText()).isEqualTo("10.0");
	}

	@Test
	public void testGetSelectedCategoryReturnsSelectedItem() {
		Category category = new Category("1", "Food");
		GuiActionRunner.execute(() -> view.showCategories(Arrays.asList(category)));
		window.comboBox("comboCategory").selectItem(0);
		assertThat(view.getSelectedCategory()).isEqualTo(category);
	}

	@Test
	public void testGetSelectedExpenseReturnsSelectedItem() {
		Expense expense = new Expense("1", "Lunch", 10.0, null);
		GuiActionRunner.execute(() -> {
			view.showExpenses(Arrays.asList(expense));
			view.listExpenses.setSelectedIndex(0);
		});
		assertThat(view.getSelectedExpense()).isEqualTo(expense);
	}

	@Test
	public void testAddAddExpenseListenerAttachesListener() {
		ActionListener listener = mock(ActionListener.class);
		GuiActionRunner.execute(() -> view.addAddExpenseListener(listener));
		window.textBox("txtDescription").enterText("Lunch");
		window.textBox("txtAmount").enterText("10.0");
		window.button("btnAddExpense").click();
		verify(listener).actionPerformed(any());
	}

	@Test
	public void testAddDeleteExpenseListenerAttachesListener() {
		ActionListener listener = mock(ActionListener.class);
		GuiActionRunner.execute(() -> view.addDeleteExpenseListener(listener));
		window.button("btnDeleteExpense").click();
		verify(listener).actionPerformed(any());
	}

	@Test
	public void testChangedUpdateEnablesButtonWhenBothFieldsFilled() {
		GuiActionRunner.execute(() -> {
			view.txtDescription.setText("Lunch");
			view.txtAmount.setText("10.0");
			DocumentListener[] listeners = ((AbstractDocument) view.txtDescription.getDocument())
					.getListeners(DocumentListener.class);
			for (DocumentListener l : listeners) {
				if (l.getClass().getEnclosingClass() == ExpenseTrackerView.class) {
					l.changedUpdate(null);
				}
			}
		});
		window.button("btnAddExpense").requireEnabled();
		assertThat(window.button("btnAddExpense").target().isEnabled()).isTrue();
	}
}
