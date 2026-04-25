package com.mycompany.expensetracker.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnit4TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.model.Expense;

@RunWith(GUITestRunner.class)
public class ExpenseTrackerViewTest extends AssertJSwingJUnit4TestCase {

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
}
