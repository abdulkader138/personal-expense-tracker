package com.mycompany.expensetracker.view.swing;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Pause;
import static org.awaitility.Awaitility.await;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.MongoDBContainer;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mycompany.expensetracker.controller.ExpenseTrackerController;
import com.mycompany.expensetracker.model.Category;
import com.mycompany.expensetracker.repository.mongo.MongoCategoryRepository;
import com.mycompany.expensetracker.repository.mongo.MongoExpenseRepository;
import com.mycompany.expensetracker.service.CategoryService;
import com.mycompany.expensetracker.service.ExpenseService;

@RunWith(GUITestRunner.class)
public class ExpenseTrackerAppE2E extends AssertJSwingJUnitTestCase {

	@ClassRule
	public static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

	private FrameFixture window;
	private MongoClient client;
	private ExpenseTrackerView view;

	@Override
	protected void onSetUp() {
		turnOffCapsLock();
		client = new MongoClient(new MongoClientURI(mongo.getConnectionString()));
		MongoDatabase database = client.getDatabase("expensetracker-e2e");
		database.drop();
		Pause.pause(500);

		MongoCategoryRepository categoryRepo = new MongoCategoryRepository(database);
		MongoExpenseRepository expenseRepo = new MongoExpenseRepository(database);
		CategoryService categoryService = new CategoryService(categoryRepo);
		ExpenseService expenseService = new ExpenseService(expenseRepo);

		categoryService.addCategory(new Category("c1", "Food"));

		view = GuiActionRunner.execute(() -> {
			ExpenseTrackerView v = new ExpenseTrackerView();
			ExpenseTrackerController controller =
				new ExpenseTrackerController(expenseService, categoryService, v);
			controller.allCategories();
			controller.allExpenses();
			return v;
		});

		window = new FrameFixture(robot(), view);
		window.show(new Dimension(800, 600));
		window.focus();
		Pause.pause(500);
	}

	@Override
	protected void onTearDown() throws Exception {
		if (client != null) {
			client.close();
		}
	}

	private void turnOffCapsLock() {
		try {
			Process p = new ProcessBuilder("xset", "q").start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			boolean capsOn = false;
			while ((line = reader.readLine()) != null) {
				if (line.contains("Caps Lock:") && line.contains("on")) {
					capsOn = true;
					break;
				}
			}
			p.waitFor();
			if (capsOn) {
				new ProcessBuilder("xdotool", "key", "Caps_Lock").start().waitFor();
				Pause.pause(150);
			}
		} catch (Exception ignored) {
			// best-effort: silently skip on headless or non-X11 environments
		}
	}

	@Test
	public void testApplicationStartsWithSeededCategoryAndEmptyExpenses() {
		awaitComboContents(1);
		assertThat(comboContents()).contains("Category{id='c1', name='Food'}");
		assertThat(categoryListContents()).hasSize(1);
		assertThat(expenseListContents()).isEmpty();
	}

	@Test
	public void testAddExpenseAppearsInList() {
		GuiActionRunner.execute(() -> {
			view.setDescriptionText("Lunch");
			view.setAmountText("10.0");
			view.setSelectedCategory(new Category("c1", "Food"));
		});
		window.button("btnAddExpense").requireEnabled();
		window.button("btnAddExpense").click();
		awaitExpenseCount(1);
		assertThat(expenseListContents()).hasSize(1);
		assertThat(expenseListContents()[0]).contains("Lunch");
	}

	@Test
	public void testDeleteExpenseRemovesFromList() {
		GuiActionRunner.execute(() -> {
			view.setDescriptionText("Dinner");
			view.setAmountText("20.0");
			view.setSelectedCategory(new Category("c1", "Food"));
		});
		window.button("btnAddExpense").requireEnabled();
		window.button("btnAddExpense").click();
		awaitExpenseCount(1);
		assertThat(expenseListContents()).hasSize(1);
		selectExpense(0);
		window.button("btnDeleteExpense").click();
		awaitExpenseCount(0);
		assertThat(expenseListContents()).isEmpty();
	}

	@Test
	public void testUpdateExpenseChangesPersistedValuesInList() {
		GuiActionRunner.execute(() -> {
			view.setDescriptionText("Lunch");
			view.setAmountText("10.0");
			view.setSelectedCategory(new Category("c1", "Food"));
		});
		window.button("btnAddExpense").requireEnabled();
		window.button("btnAddExpense").click();
		awaitExpenseCount(1);

		selectExpense(0);
		GuiActionRunner.execute(() -> {
			view.setDescriptionText("Dinner");
			view.setAmountText("20.0");
		});
		window.button("btnUpdateExpense").click();

		awaitExpenseCount(1);
		assertThat(expenseListContents()[0]).contains("Dinner", "20.0");
	}

	@Test
	public void testCategoryCrudFlow() {
		GuiActionRunner.execute(() -> view.setCategoryNameText("Travel"));
		window.button("btnAddCategory").click();
		awaitCategoryCount(2);
		assertThat(categoryListContents()).anyMatch(item -> item.contains("Travel"));

		selectCategory(1);
		assertThat(view.getCategoryNameText()).isEqualTo("Travel");

		GuiActionRunner.execute(() -> view.setCategoryNameText("Trips"));
		window.button("btnUpdateCategory").click();
		awaitCategoryCount(2);
		assertThat(categoryListContents()).anyMatch(item -> item.contains("Trips"));
	}

	private void awaitComboContents(int expectedSize) {
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
			assertThat(comboContents()).hasSize(expectedSize)
		);
	}

	private void awaitExpenseCount(int expectedSize) {
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
			assertThat(expenseListContents()).hasSize(expectedSize)
		);
	}

	private void awaitCategoryCount(int expectedSize) {
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
			assertThat(categoryListContents()).hasSize(expectedSize)
		);
	}

	private String[] comboContents() {
		return window.comboBox("comboCategory").contents();
	}

	private String[] expenseListContents() {
		return window.list("listExpenses").contents();
	}

	private String[] categoryListContents() {
		return window.list("listCategories").contents();
	}

	private void selectExpense(int index) {
		window.list("listExpenses").selectItem(index);
	}

	private void selectCategory(int index) {
		GuiActionRunner.execute(() -> window.list("listCategories").target().setSelectedIndex(index));
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
			assertThat(view.getSelectedCategoryInList()).isNotNull()
		);
	}
}
