package com.mycompany.expensetracker.app.swing;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mycompany.expensetracker.repository.mongo.MongoCategoryRepository;
import com.mycompany.expensetracker.repository.mongo.MongoExpenseRepository;
import com.mycompany.expensetracker.service.CategoryService;
import com.mycompany.expensetracker.service.ExpenseService;
import com.mycompany.expensetracker.view.swing.ExpenseTrackerController;
import com.mycompany.expensetracker.view.swing.ExpenseTrackerView;

public class App {

	public static void main(String[] args) {
		String mongoUri = args.length > 0 ? args[0] : "mongodb://localhost:27017";
		CountDownLatch appClosed = new CountDownLatch(1);
		try (MongoClient client = new MongoClient(new MongoClientURI(mongoUri))) {
			MongoExpenseRepository expenseRepo =
				new MongoExpenseRepository(client.getDatabase("expensetracker"));
			MongoCategoryRepository categoryRepo =
				new MongoCategoryRepository(client.getDatabase("expensetracker"));
			ExpenseService expenseService = new ExpenseService(expenseRepo);
			CategoryService categoryService = new CategoryService(categoryRepo);
			SwingUtilities.invokeLater(() -> {
				ExpenseTrackerView view = new ExpenseTrackerView();
				view.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				view.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						appClosed.countDown();
					}
				});
				ExpenseTrackerController controller =
					new ExpenseTrackerController(expenseService, categoryService, view);
				controller.allCategories();
				controller.allExpenses();
				view.setVisible(true);
			});
			appClosed.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		System.exit(0);
	}
}
