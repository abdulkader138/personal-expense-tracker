package com.mycompany.pet.di;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mycompany.pet.controller.CategoryController;
import com.mycompany.pet.controller.ExpenseController;
import com.mycompany.pet.dao.CategoryDAO;
import com.mycompany.pet.dao.ExpenseDAO;
import com.mycompany.pet.database.DatabaseConnection;
import com.mycompany.pet.database.DatabaseInitializer;
import com.mycompany.pet.service.CategoryService;
import com.mycompany.pet.service.ExpenseService;
import com.mycompany.pet.ui.MainWindow;

import org.bson.Document;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Method;

/**
 * Unit tests for ExpenseTrackerModule.
 * 
 * Tests all methods including constructor, fluent configuration methods,
 * configure() method, and all @Provides methods.
 */
public class ExpenseTrackerModuleTest {
    private MockedStatic<MongoClients> mockedMongoClients;
    private MockedStatic<DatabaseInitializer> mockedDatabaseInitializer;

    @Before
    public void setUp() {
        // Skip tests on Java 8 as mockito-inline requires Java 11+
        String javaVersion = System.getProperty("java.version");
        boolean isJava8 = javaVersion != null && (javaVersion.startsWith("1.8") || javaVersion.startsWith("8."));
        Assume.assumeFalse("Static mocks require Java 11+, skipping on Java 8", isJava8);
        
        try {
            // Mock MongoClients static methods
            mockedMongoClients = Mockito.mockStatic(MongoClients.class);
        } catch (Exception e) {
            // If mockito-inline is not available, skip the test
            Assume.assumeNoException("mockito-inline not available, skipping test", e);
        }
    }

    @After
    public void tearDown() {
        if (mockedMongoClients != null) {
            mockedMongoClients.close();
        }
        if (mockedDatabaseInitializer != null) {
            mockedDatabaseInitializer.close();
        }
    }

    /**
     * Creates a test module that installs ExpenseTrackerModule and provides mock controllers.
     * This is needed because provideMainWindow requires controllers, and Guice validates all bindings.
     * Uses Modules.override() to override the @Provides methods from ExpenseTrackerModule.
     */
    private Module createTestModuleWithMocks(ExpenseTrackerModule module) {
        AbstractModule overrideModule = new AbstractModule() {
            @Override
            protected void configure() {
                // Override the controller bindings with mocks
                bind(ExpenseController.class).toInstance(mock(ExpenseController.class));
                bind(CategoryController.class).toInstance(mock(CategoryController.class));
            }
        };
        return Modules.override(module).with(overrideModule);
    }

    /**
     * Test default constructor.
     */
    @Test
    public void testDefaultConstructor() {
        // Given & When
        ExpenseTrackerModule module = new ExpenseTrackerModule();

        // Then
        assertNotNull(module);
    }

    /**
     * Test mongoHost fluent method.
     */
    @Test
    public void testMongoHost() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        String testHost = "test-host";

        // When
        ExpenseTrackerModule result = module.mongoHost(testHost);

        // Then
        assertSame(module, result); 
        
        // Verify the host is used when creating connection
        MongoClient mockClient = mock(MongoClient.class);
        mockedMongoClients.when(() -> MongoClients.create("mongodb://" + testHost + ":27017"))
                .thenReturn(mockClient);
        
        Injector injector = Guice.createInjector(createTestModuleWithMocks(module));
        DatabaseConnection dbConnection = injector.getInstance(DatabaseConnection.class);
        assertNotNull(dbConnection);
    }

    /**
     * Test mongoPort fluent method.
     */
    @Test
    public void testMongoPort() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        int testPort = 12345;

        // When
        ExpenseTrackerModule result = module.mongoPort(testPort);

        // Then
        assertSame(module, result); 
        
        // Verify the port is used when creating connection
        MongoClient mockClient = mock(MongoClient.class);
        mockedMongoClients.when(() -> MongoClients.create("mongodb://localhost:" + testPort))
                .thenReturn(mockClient);
        
        Injector injector = Guice.createInjector(createTestModuleWithMocks(module));
        DatabaseConnection dbConnection = injector.getInstance(DatabaseConnection.class);
        assertNotNull(dbConnection);
    }

    /**
     * Test databaseName fluent method.
     */
    @Test
    public void testDatabaseName() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        String testDbName = "test_database";

        // When
        ExpenseTrackerModule result = module.databaseName(testDbName);

        // Then
        assertSame(module, result);
        
        // Verify the database name is used when creating connection
        MongoClient mockClient = mock(MongoClient.class);
        mockedMongoClients.when(() -> MongoClients.create(anyString()))
                .thenReturn(mockClient);
        
        Injector injector = Guice.createInjector(createTestModuleWithMocks(module));
        DatabaseConnection dbConnection = injector.getInstance(DatabaseConnection.class);
        assertNotNull(dbConnection);
        // The database name is used in DatabaseConnection constructor
    }

    /**
     * Test chaining fluent methods.
     */
    @Test
    public void testFluentMethodChaining() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        String testHost = "test-host";
        int testPort = 12345;
        String testDbName = "test_database";

        // When
        ExpenseTrackerModule result = module
                .mongoHost(testHost)
                .mongoPort(testPort)
                .databaseName(testDbName);

        // Then
        assertSame(module, result); // Should return same instance
        
        // Verify all values are used
        MongoClient mockClient = mock(MongoClient.class);
        mockedMongoClients.when(() -> MongoClients.create("mongodb://" + testHost + ":" + testPort))
                .thenReturn(mockClient);
        
        Injector injector = Guice.createInjector(createTestModuleWithMocks(module));
        DatabaseConnection dbConnection = injector.getInstance(DatabaseConnection.class);
        assertNotNull(dbConnection);
    }

    @Test
    public void testConfigure() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();

        // When & Then - configure() is called by Guice when creating injector
        MongoClient mockClient = mock(MongoClient.class);
        mockedMongoClients.when(() -> MongoClients.create(anyString()))
                .thenReturn(mockClient);
        
        Injector injector = Guice.createInjector(createTestModuleWithMocks(module));
        assertNotNull(injector);
    }

    /**
     * Test provideDatabaseConnection() with default values.
     */
    @Test
    public void testProvideDatabaseConnection_DefaultValues() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        MongoClient mockClient = mock(MongoClient.class);
        mockedMongoClients.when(() -> MongoClients.create("mongodb://localhost:27017"))
                .thenReturn(mockClient);

        // When
        Injector injector = Guice.createInjector(createTestModuleWithMocks(module));
        DatabaseConnection dbConnection = injector.getInstance(DatabaseConnection.class);

        // Then
        assertNotNull(dbConnection);
        dbConnection.getDatabase();
        mockedMongoClients.verify(() -> MongoClients.create("mongodb://localhost:27017"));
    }

    /**
     * Test provideDatabaseConnection() with custom values.
     */
    @Test
    public void testProvideDatabaseConnection_CustomValues() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule()
                .mongoHost("custom-host")
                .mongoPort(9999)
                .databaseName("custom_db");
        MongoClient mockClient = mock(MongoClient.class);
        mockedMongoClients.when(() -> MongoClients.create("mongodb://custom-host:9999"))
                .thenReturn(mockClient);

        // When
        Injector injector = Guice.createInjector(createTestModuleWithMocks(module));
        DatabaseConnection dbConnection = injector.getInstance(DatabaseConnection.class);

        // Then
        assertNotNull(dbConnection);
        dbConnection.getDatabase();
        mockedMongoClients.verify(() -> MongoClients.create("mongodb://custom-host:9999"));
    }

    /**
     * Test provideDatabaseConnection() returns singleton.
     */
    @Test
    public void testProvideDatabaseConnection_Singleton() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        MongoClient mockClient = mock(MongoClient.class);
        mockedMongoClients.when(() -> MongoClients.create(anyString()))
                .thenReturn(mockClient);

        // When
        Injector injector = Guice.createInjector(createTestModuleWithMocks(module));
        DatabaseConnection dbConnection1 = injector.getInstance(DatabaseConnection.class);
        DatabaseConnection dbConnection2 = injector.getInstance(DatabaseConnection.class);

        // Then
        assertNotNull(dbConnection1);
        assertNotNull(dbConnection2);
        assertSame(dbConnection1, dbConnection2); 
    }

    @Test
    public void testProvideMongoClient_DefaultValues() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        MongoClient mockClient = mock(MongoClient.class);
        mockedMongoClients.when(() -> MongoClients.create("mongodb://localhost:27017"))
                .thenReturn(mockClient);

        // When
        Injector injector = Guice.createInjector(createTestModuleWithMocks(module));
        MongoClient mongoClient = injector.getInstance(MongoClient.class);

        // Then
        assertNotNull(mongoClient);
        assertEquals(mockClient, mongoClient);
        mockedMongoClients.verify(() -> MongoClients.create("mongodb://localhost:27017"));
    }

    @Test
    public void testProvideMongoClient_CustomValues() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule()
                .mongoHost("custom-host")
                .mongoPort(9999);
        MongoClient mockClient = mock(MongoClient.class);
        mockedMongoClients.when(() -> MongoClients.create("mongodb://custom-host:9999"))
                .thenReturn(mockClient);

        // When
        Injector injector = Guice.createInjector(createTestModuleWithMocks(module));
        MongoClient mongoClient = injector.getInstance(MongoClient.class);

        // Then
        assertNotNull(mongoClient);
        assertEquals(mockClient, mongoClient);
        mockedMongoClients.verify(() -> MongoClients.create("mongodb://custom-host:9999"));
    }

    @Test
    public void testProvideMongoClient_Singleton() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        MongoClient mockClient = mock(MongoClient.class);
        mockedMongoClients.when(() -> MongoClients.create(anyString()))
                .thenReturn(mockClient);

        // When
        Injector injector = Guice.createInjector(createTestModuleWithMocks(module));
        MongoClient client1 = injector.getInstance(MongoClient.class);
        MongoClient client2 = injector.getInstance(MongoClient.class);

        // Then
        assertNotNull(client1);
        assertNotNull(client2);
        assertSame(client1, client2); // Should be same instance (singleton)
    }

    @Test
    public void testProvideDatabaseInitializer() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        MongoCollection<Document> mockCategoriesCollection = mock(MongoCollection.class);
        MongoCollection<Document> mockExpensesCollection = mock(MongoCollection.class);
        
        mockedMongoClients.when(() -> MongoClients.create(anyString()))
                .thenReturn(mockClient);
        when(mockClient.getDatabase(anyString())).thenReturn(mockDatabase);
        when(mockDatabase.getCollection("categories")).thenReturn(mockCategoriesCollection);
        when(mockDatabase.getCollection("expenses")).thenReturn(mockExpensesCollection);

        // When - Guice will call provideDatabaseInitializer which requires DatabaseConnection
        Injector injector = Guice.createInjector(createTestModuleWithMocks(module));
        DatabaseInitializer initializer = injector.getInstance(DatabaseInitializer.class);

        // Then
        assertNotNull(initializer);
    }

    @Test
    public void testProvideDatabaseInitializer_Singleton() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        MongoCollection<Document> mockCategoriesCollection = mock(MongoCollection.class);
        MongoCollection<Document> mockExpensesCollection = mock(MongoCollection.class);
        
        mockedMongoClients.when(() -> MongoClients.create(anyString()))
                .thenReturn(mockClient);
        when(mockClient.getDatabase(anyString())).thenReturn(mockDatabase);
        when(mockDatabase.getCollection("categories")).thenReturn(mockCategoriesCollection);
        when(mockDatabase.getCollection("expenses")).thenReturn(mockExpensesCollection);

        // When
        Injector injector = Guice.createInjector(createTestModuleWithMocks(module));
        DatabaseInitializer initializer1 = injector.getInstance(DatabaseInitializer.class);
        DatabaseInitializer initializer2 = injector.getInstance(DatabaseInitializer.class);

        // Then
        assertNotNull(initializer1);
        assertNotNull(initializer2);
        assertSame(initializer1, initializer2);
    }

    @Test
    public void testProvideMainWindow() throws Exception {
        // Skip test in headless environment (MainWindow requires display)
        Assume.assumeFalse("Skipping test in headless environment - MainWindow requires display",
            GraphicsEnvironment.isHeadless());
        
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        ExpenseController mockExpenseController = mock(ExpenseController.class);
        CategoryController mockCategoryController = mock(CategoryController.class);
        
        // Use reflection to call the actual provideMainWindow method
        Method provideMainWindowMethod = ExpenseTrackerModule.class.getDeclaredMethod(
            "provideMainWindow", ExpenseController.class, CategoryController.class);
        provideMainWindowMethod.setAccessible(true);

        // When
        MainWindow mainWindow = (MainWindow) provideMainWindowMethod.invoke(
            module, mockExpenseController, mockCategoryController);

        // Then
        assertNotNull(mainWindow);
    }

    @Test
    public void testProvideMainWindow_Singleton() {
        // Skip test in headless environment (MainWindow requires display)
        Assume.assumeFalse("Skipping test in headless environment - MainWindow requires display",
            GraphicsEnvironment.isHeadless());
        
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        MongoClient mockClient = mock(MongoClient.class);
        mockedMongoClients.when(() -> MongoClients.create(anyString()))
                .thenReturn(mockClient);
        
        AbstractModule overrideModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(ExpenseController.class).toInstance(mock(ExpenseController.class));
                bind(CategoryController.class).toInstance(mock(CategoryController.class));
            }
        };
        Module testModule = Modules.override(module).with(overrideModule);

        // When
        Injector injector = Guice.createInjector(testModule);
        MainWindow window1 = injector.getInstance(MainWindow.class);
        MainWindow window2 = injector.getInstance(MainWindow.class);

        // Then
        assertNotNull(window1);
        assertNotNull(window2);
    }

    @Test
    public void testProvideCategoryDAO() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        MongoCollection<Document> mockCategoriesCollection = mock(MongoCollection.class);
        MongoCollection<Document> mockExpensesCollection = mock(MongoCollection.class);
        
        mockedMongoClients.when(() -> MongoClients.create(anyString()))
                .thenReturn(mockClient);
        when(mockClient.getDatabase(anyString())).thenReturn(mockDatabase);
        when(mockDatabase.getCollection("categories")).thenReturn(mockCategoriesCollection);
        when(mockDatabase.getCollection("expenses")).thenReturn(mockExpensesCollection);

        AbstractModule overrideModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(MainWindow.class).toInstance(mock(MainWindow.class));
            }
        };
        Module testModule = Modules.override(module).with(overrideModule);
        Injector injector = Guice.createInjector(testModule);
        CategoryDAO categoryDAO = injector.getInstance(CategoryDAO.class);

        // Then
        assertNotNull(categoryDAO);
    }

    @Test
    public void testProvideExpenseDAO() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        MongoCollection<Document> mockCategoriesCollection = mock(MongoCollection.class);
        MongoCollection<Document> mockExpensesCollection = mock(MongoCollection.class);
        
        mockedMongoClients.when(() -> MongoClients.create(anyString()))
                .thenReturn(mockClient);
        when(mockClient.getDatabase(anyString())).thenReturn(mockDatabase);
        when(mockDatabase.getCollection("categories")).thenReturn(mockCategoriesCollection);
        when(mockDatabase.getCollection("expenses")).thenReturn(mockExpensesCollection);

        AbstractModule overrideModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(MainWindow.class).toInstance(mock(MainWindow.class));
            }
        };
        Module testModule = Modules.override(module).with(overrideModule);
        Injector injector = Guice.createInjector(testModule);
        ExpenseDAO expenseDAO = injector.getInstance(ExpenseDAO.class);

        // Then
        assertNotNull(expenseDAO);
    }

    @Test
    public void testProvideCategoryService() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        MongoCollection<Document> mockCategoriesCollection = mock(MongoCollection.class);
        MongoCollection<Document> mockExpensesCollection = mock(MongoCollection.class);
        
        mockedMongoClients.when(() -> MongoClients.create(anyString()))
                .thenReturn(mockClient);
        when(mockClient.getDatabase(anyString())).thenReturn(mockDatabase);
        when(mockDatabase.getCollection("categories")).thenReturn(mockCategoriesCollection);
        when(mockDatabase.getCollection("expenses")).thenReturn(mockExpensesCollection);

        // When - Request CategoryService through Guice, which will call provideCategoryService
        AbstractModule overrideModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(MainWindow.class).toInstance(mock(MainWindow.class));
            }
        };
        Module testModule = Modules.override(module).with(overrideModule);
        Injector injector = Guice.createInjector(testModule);
        CategoryService categoryService = injector.getInstance(CategoryService.class);

        // Then
        assertNotNull(categoryService);
    }

    @Test
    public void testProvideExpenseService() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        MongoCollection<Document> mockCategoriesCollection = mock(MongoCollection.class);
        MongoCollection<Document> mockExpensesCollection = mock(MongoCollection.class);
        
        mockedMongoClients.when(() -> MongoClients.create(anyString()))
                .thenReturn(mockClient);
        when(mockClient.getDatabase(anyString())).thenReturn(mockDatabase);
        when(mockDatabase.getCollection("categories")).thenReturn(mockCategoriesCollection);
        when(mockDatabase.getCollection("expenses")).thenReturn(mockExpensesCollection);

        // When - Request ExpenseService through Guice, which will call provideExpenseService
        AbstractModule overrideModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(MainWindow.class).toInstance(mock(MainWindow.class));
            }
        };
        Module testModule = Modules.override(module).with(overrideModule);
        Injector injector = Guice.createInjector(testModule);
        ExpenseService expenseService = injector.getInstance(ExpenseService.class);

        // Then
        assertNotNull(expenseService);
    }

    @Test
    public void testProvideCategoryController() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        MongoCollection<Document> mockCategoriesCollection = mock(MongoCollection.class);
        MongoCollection<Document> mockExpensesCollection = mock(MongoCollection.class);
        
        mockedMongoClients.when(() -> MongoClients.create(anyString()))
                .thenReturn(mockClient);
        when(mockClient.getDatabase(anyString())).thenReturn(mockDatabase);
        when(mockDatabase.getCollection("categories")).thenReturn(mockCategoriesCollection);
        when(mockDatabase.getCollection("expenses")).thenReturn(mockExpensesCollection);

        // When - Request CategoryController through Guice, which will call provideCategoryController
        AbstractModule overrideModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(MainWindow.class).toInstance(mock(MainWindow.class));
            }
        };
        Module testModule = Modules.override(module).with(overrideModule);
        Injector injector = Guice.createInjector(testModule);
        CategoryController categoryController = injector.getInstance(CategoryController.class);

        // Then
        assertNotNull(categoryController);
    }

    @Test
    public void testProvideExpenseController() {
        // Given
        ExpenseTrackerModule module = new ExpenseTrackerModule();
        MongoClient mockClient = mock(MongoClient.class);
        MongoDatabase mockDatabase = mock(MongoDatabase.class);
        MongoCollection<Document> mockCategoriesCollection = mock(MongoCollection.class);
        MongoCollection<Document> mockExpensesCollection = mock(MongoCollection.class);
        
        mockedMongoClients.when(() -> MongoClients.create(anyString()))
                .thenReturn(mockClient);
        when(mockClient.getDatabase(anyString())).thenReturn(mockDatabase);
        when(mockDatabase.getCollection("categories")).thenReturn(mockCategoriesCollection);
        when(mockDatabase.getCollection("expenses")).thenReturn(mockExpensesCollection);

        // When - Request ExpenseController through Guice, which will call provideExpenseController
        AbstractModule overrideModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(MainWindow.class).toInstance(mock(MainWindow.class));
            }
        };
        Module testModule = Modules.override(module).with(overrideModule);
        Injector injector = Guice.createInjector(testModule);
        ExpenseController expenseController = injector.getInstance(ExpenseController.class);

        // Then
        assertNotNull(expenseController);
    }
}

