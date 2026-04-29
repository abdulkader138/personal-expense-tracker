package com.mycompany.expensetracker.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.MongoDBContainer;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
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
		database.getCollection("categories").drop();
		database.getCollection("expenses").drop();

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
				Thread.sleep(150);
			}
		} catch (Exception ignored) {
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
		window.textBox("txtDescription").setText("Lunch");
		window.textBox("txtAmount").setText("10.0");
		window.button("btnAddExpense").requireEnabled();
		window.button("btnAddExpense").click();
		awaitExpenseCount(1);
		assertThat(expenseListContents()).hasSize(1);
		assertThat(expenseListContents()[0]).contains("Lunch");
	}

	@Test
	public void testDeleteExpenseRemovesFromList() {
		window.textBox("txtDescription").setText("Dinner");
		window.textBox("txtAmount").setText("20.0");
		window.button("btnAddExpense").requireEnabled();
		window.button("btnAddExpense").click();
		awaitExpenseCount(1);
		assertThat(expenseListContents()).hasSize(1);
		selectExpense(0);
		window.button("btnDeleteExpense").click();
		awaitExpenseCount(0);
		assertThat(expenseListContents()).hasSize(0);
	}

	@Test
	public void testUpdateExpenseChangesPersistedValuesInList() {
		window.textBox("txtDescription").setText("Lunch");
		window.textBox("txtAmount").setText("10.0");
		window.button("btnAddExpense").requireEnabled();
		window.button("btnAddExpense").click();
		awaitExpenseCount(1);

		selectExpense(0);
		window.textBox("txtDescription").setText("Dinner");
		window.textBox("txtAmount").setText("20.0");
		window.button("btnUpdateExpense").click();

		awaitExpenseCount(1);
		assertThat(expenseListContents()[0]).contains("Dinner");
		assertThat(expenseListContents()[0]).contains("20.0");
	}

	@Test
	public void testCategoryCrudFlow() {
		window.textBox("txtCategoryName").setText("Travel");
		window.button("btnAddCategory").click();
		awaitCategoryCount(2);
		assertThat(categoryListContents()).anyMatch(item -> item.contains("Travel"));

		selectCategory(1);
		assertThat(view.getCategoryNameText()).isEqualTo("Travel");

		window.textBox("txtCategoryName").setText("Trips");
		window.button("btnUpdateCategory").click();
		awaitCategoryCount(2);
		assertThat(categoryListContents()).anyMatch(item -> item.contains("Trips"));

		selectCategory(1);
		window.button("btnDeleteCategory").click();
		awaitCategoryCount(1);
		assertThat(categoryListContents()).noneMatch(item -> item.contains("Trips"));
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
		GuiActionRunner.execute(() -> view.listExpenses.setSelectedIndex(index));
	}

	private void selectCategory(int index) {
		GuiActionRunner.execute(() -> view.listCategories.setSelectedIndex(index));
	}
}
