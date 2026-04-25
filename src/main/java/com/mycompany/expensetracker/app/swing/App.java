package com.mycompany.expensetracker.app.swing;

import javax.swing.SwingUtilities;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mycompany.expensetracker.controller.CategoryService;
import com.mycompany.expensetracker.controller.ExpenseService;
import com.mycompany.expensetracker.repository.mongo.MongoCategoryRepository;
import com.mycompany.expensetracker.repository.mongo.MongoExpenseRepository;
import com.mycompany.expensetracker.view.swing.ExpenseTrackerController;
import com.mycompany.expensetracker.view.swing.ExpenseTrackerView;

public class App {

	public static void main(String[] args) {
		String mongoUri = args.length > 0 ? args[0] : "mongodb://localhost:27017";
		MongoClient client = new MongoClient(new MongoClientURI(mongoUri));
		MongoExpenseRepository expenseRepo =
			new MongoExpenseRepository(client.getDatabase("expensetracker"));
		MongoCategoryRepository categoryRepo =
			new MongoCategoryRepository(client.getDatabase("expensetracker"));
		ExpenseService expenseService = new ExpenseService(expenseRepo);
		CategoryService categoryService = new CategoryService(categoryRepo);
		SwingUtilities.invokeLater(() -> {
			ExpenseTrackerView view = new ExpenseTrackerView();
			ExpenseTrackerController controller =
				new ExpenseTrackerController(expenseService, categoryService, view);
			controller.allCategories();
			controller.allExpenses();
			view.setVisible(true);
		});
	}
}
