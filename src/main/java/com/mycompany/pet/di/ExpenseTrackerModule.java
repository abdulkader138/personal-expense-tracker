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

/**
 * Guice module for Expense Tracker application.
 * 
 * This module configures dependency injection for all components following the pattern
 * from "Test-Driven Development, Build Automation, Continuous Integration" book.
 */
public class ExpenseTrackerModule extends AbstractModule {

    private String mongoHost = "localhost";
    private int mongoPort = 27017;
    private String databaseName = "expense_tracker";

    /**
     * Fluent method to configure MongoDB host.
     */
    public ExpenseTrackerModule mongoHost(String mongoHost) {
        this.mongoHost = mongoHost;
        return this;
    }

    /**
     * Fluent method to configure MongoDB port.
     */
    public ExpenseTrackerModule mongoPort(int mongoPort) {
        this.mongoPort = mongoPort;
        return this;
    }

    /**
     * Fluent method to configure database name.
     */
    public ExpenseTrackerModule databaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    @Override
    protected void configure() {
        // DatabaseConnection is provided via @Provides method below
        // DAOs will automatically get DatabaseConnection injected via constructor
        // Services will automatically get DAOs injected via constructor
        // Controllers will automatically get Services injected via constructor
    }

    /**
     * Provides DatabaseConnection instance.
     */
    @Provides
    @Singleton
    DatabaseConnection provideDatabaseConnection() {
        return new DatabaseConnection("mongodb://" + mongoHost + ":" + mongoPort, databaseName);
    }

    /**
     * Provides MongoClient instance.
     */
    @Provides
    @Singleton
    MongoClient provideMongoClient() {
        return MongoClients.create("mongodb://" + mongoHost + ":" + mongoPort);
    }

    /**
     * Provides DatabaseInitializer and initializes the database.
     */
    @Provides
    @Singleton
    DatabaseInitializer provideDatabaseInitializer(DatabaseConnection dbConnection) {
        DatabaseInitializer initializer = new DatabaseInitializer(dbConnection);
        initializer.initialize();
        return initializer;
    }

    /**
     * Provides MainWindow instance.
     */
    @Provides
    MainWindow provideMainWindow(ExpenseController expenseController, 
                                  CategoryController categoryController) {
        MainWindow mainWindow = new MainWindow(expenseController, categoryController);
        mainWindow.loadData(); // Load data after window is created
        return mainWindow;
    }
}

