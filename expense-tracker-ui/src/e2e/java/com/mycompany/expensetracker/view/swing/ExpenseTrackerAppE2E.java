package com.mycompany.expensetracker.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.InputStreamReader;


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
	public void testAddExpenseAppearsInList() {
		window.textBox("txtDescription").enterText("Lunch");
		window.textBox("txtAmount").enterText("10.0");
		window.button("btnAddExpense").click();
		assertThat(window.list("listExpenses").contents()).hasSize(1);
		assertThat(window.list("listExpenses").contents()[0]).contains("Lunch");
	}

	@Test
	public void testDeleteExpenseRemovesFromList() {
		window.textBox("txtDescription").enterText("Dinner");
		window.textBox("txtAmount").enterText("20.0");
		window.button("btnAddExpense").click();
		assertThat(window.list("listExpenses").contents()).hasSize(1);
		GuiActionRunner.execute(() -> view.listExpenses.setSelectedIndex(0));
		window.button("btnDeleteExpense").click();
		assertThat(window.list("listExpenses").contents()).hasSize(0);
	}
}
