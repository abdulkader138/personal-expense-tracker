package com.mycompany.pet.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mycompany.pet.controller.CategoryController;
import com.mycompany.pet.controller.ExpenseController;
import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.dao.ExpenseDAO;
import com.mycompany.pet.database.DatabaseConnection;
import com.mycompany.pet.database.DatabaseInitializer;
import com.mycompany.pet.service.CategoryService;
import com.mycompany.pet.service.ExpenseService;
import com.mycompany.pet.ui.MainWindow;

public class ExpenseTrackerModule extends AbstractModule {

    private String mongoHost = "localhost";
    private int mongoPort = 27017;
    private String databaseName = "expense_tracker";

    public ExpenseTrackerModule mongoHost(String mongoHost) {
        this.mongoHost = mongoHost;
        return this;
    }

    public ExpenseTrackerModule mongoPort(int mongoPort) {
        this.mongoPort = mongoPort;
        return this;
    }

    public ExpenseTrackerModule databaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    DatabaseConnection provideDatabaseConnection() {
        return new DatabaseConnection("mongodb://" + mongoHost + ":" + mongoPort, databaseName);
    }

    @Provides
    @Singleton
    MongoClient provideMongoClient() {
        return MongoClients.create("mongodb://" + mongoHost + ":" + mongoPort);
    }

    @Provides
    @Singleton
    DatabaseInitializer provideDatabaseInitializer(DatabaseConnection dbConnection) {
        DatabaseInitializer initializer = new DatabaseInitializer(dbConnection);
        initializer.initialize();
        return initializer;
    }

    @Provides
    CategoryDAO provideCategoryDAO(DatabaseConnection dbConnection) {
        return new CategoryDAO(dbConnection);
    }

    @Provides
    ExpenseDAO provideExpenseDAO(DatabaseConnection dbConnection) {
        return new ExpenseDAO(dbConnection);
    }

    @Provides
    CategoryService provideCategoryService(CategoryDAO categoryDAO) {
        return new CategoryService(categoryDAO);
    }

    @Provides
    ExpenseService provideExpenseService(ExpenseDAO expenseDAO, CategoryDAO categoryDAO) {
        return new ExpenseService(expenseDAO, categoryDAO);
    }

    @Provides
    CategoryController provideCategoryController(CategoryService categoryService) {
        return new CategoryController(categoryService);
    }

    @Provides
    ExpenseController provideExpenseController(ExpenseService expenseService) {
        return new ExpenseController(expenseService);
    }

    @Provides
    MainWindow provideMainWindow(ExpenseController expenseController, 
                                  CategoryController categoryController) {
        MainWindow mainWindow = new MainWindow(expenseController, categoryController);
        mainWindow.loadData(); 
        return mainWindow;
    }
}

