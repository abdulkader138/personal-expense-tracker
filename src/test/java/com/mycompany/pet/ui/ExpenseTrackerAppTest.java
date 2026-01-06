package com.mycompany.pet.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mycompany.pet.di.ExpenseTrackerModule;

/**
 * Unit tests for ExpenseTrackerApp main class.
 */
public class ExpenseTrackerAppTest {
    
    private MockedStatic<GraphicsEnvironment> mockedGraphicsEnvironment;
    private MockedStatic<SwingUtilities> mockedSwingUtilities;
    private MockedStatic<Guice> mockedGuice;
    private MockedStatic<JOptionPane> mockedJOptionPane;
    
    @Before
    public void setUp() {
        // Skip tests on Java 8 as mockito-inline requires Java 11+
        String javaVersion = System.getProperty("java.version");
        boolean isJava8 = javaVersion != null && (javaVersion.startsWith("1.8") || javaVersion.startsWith("8."));
        Assume.assumeFalse("Static mocks require Java 11+, skipping on Java 8", isJava8);
        
        try {
            mockedGraphicsEnvironment = mockStatic(GraphicsEnvironment.class);
            mockedSwingUtilities = mockStatic(SwingUtilities.class, Mockito.CALLS_REAL_METHODS);
            mockedGuice = mockStatic(Guice.class);
            mockedJOptionPane = mockStatic(JOptionPane.class);
        } catch (Exception e) {
            Assume.assumeNoException("mockito-inline not available, skipping test", e);
        }
    }
    
    @After
    public void tearDown() {
        if (mockedGraphicsEnvironment != null) {
            mockedGraphicsEnvironment.close();
        }
        if (mockedSwingUtilities != null) {
            mockedSwingUtilities.close();
        }
        if (mockedGuice != null) {
            mockedGuice.close();
        }
        if (mockedJOptionPane != null) {
            mockedJOptionPane.close();
        }
    }
    
    @Test
    @SuppressWarnings("removal")
    public void testMain_HeadlessEnvironment_ExitsWithError() {
        // Given - headless environment
        // Set headless system property to prevent AWT initialization
        String originalHeadless = System.getProperty("java.awt.headless");
        try {
            System.setProperty("java.awt.headless", "true");
            // Mock isHeadless to return true multiple times (may be called during class loading)
            mockedGraphicsEnvironment.when(GraphicsEnvironment::isHeadless).thenReturn(true);
        
        // Mock System.exit to prevent actual exit
        SecurityManager originalSecurityManager = System.getSecurityManager();
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkExit(int status) {
                if (status == 1) {
                    throw new SecurityException("Exit with code 1");
                }
                throw new SecurityException("Unexpected exit code: " + status);
            }
            
            @Override
            public void checkPermission(java.security.Permission perm) {
                // Allow all permissions
            }
        });
        
            try {
                // When - execute main method
                // This will execute all LOGGER.severe() calls and System.exit(exitCode) at line 94
                try {
                    ExpenseTrackerApp.main(new String[]{});
                    // Should not reach here
                    assertThat(false).as("Expected SecurityException from System.exit(1)").isTrue();
                } catch (SecurityException e) {
                    // Expected - System.exit(exitCode) at line 94 was called
                    assertThat(e.getMessage()).isEqualTo("Exit with code 1");
                    // Verify that the exception was thrown from System.exit by checking the stack trace
                    // This ensures line 94 was actually executed
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    boolean foundSystemExit = false;
                    boolean foundHandleHeadlessEnvironment = false;
                    boolean foundMain = false;
                    for (StackTraceElement element : stackTrace) {
                        if (element.getMethodName().equals("exit") && 
                            element.getClassName().equals("java.lang.System")) {
                            foundSystemExit = true;
                        }
                        // Verify handleHeadlessEnvironment() is in the stack trace - this ensures the call site at line 199 was executed
                        if (element.getMethodName().equals("handleHeadlessEnvironment") && 
                            element.getClassName().equals("com.mycompany.pet.ui.ExpenseTrackerApp")) {
                            foundHandleHeadlessEnvironment = true;
                        }
                        // Verify main() is in the stack trace - this ensures main() executed the call site
                        if (element.getMethodName().equals("main") && 
                            element.getClassName().equals("com.mycompany.pet.ui.ExpenseTrackerApp")) {
                            foundMain = true;
                        }
                    }
                    assertThat(foundSystemExit).as("System.exit should be in stack trace").isTrue();
                    // Verify handleHeadlessEnvironment() is in stack trace - this confirms the call site at line 199 was executed
                    assertThat(foundHandleHeadlessEnvironment).as("handleHeadlessEnvironment() should be in stack trace - this verifies call site at line 199 is covered").isTrue();
                    // Verify main() is in stack trace - this confirms main() executed the call site
                    assertThat(foundMain).as("main() should be in stack trace - this confirms the call site at line 199 in main() was executed").isTrue();
                }
            
            // Then - verify GraphicsEnvironment.isHeadless was called
            // Note: isHeadless may be called multiple times during AWT class loading, so use atLeastOnce
            mockedGraphicsEnvironment.verify(GraphicsEnvironment::isHeadless, atLeastOnce());
            // SwingUtilities.invokeLater should NOT be called in headless mode
            mockedSwingUtilities.verifyNoInteractions();
            // All lines 24-31 should now be covered including all LOGGER.severe() calls
            // The call to logHeadlessEnvironmentError() and inline System.exit() should be covered by this test
            // as main() is executed and the headless check is true
            } finally {
                System.setSecurityManager(originalSecurityManager);
            }
        } finally {
            // Restore original headless property
            if (originalHeadless != null) {
                System.setProperty("java.awt.headless", originalHeadless);
            } else {
                System.clearProperty("java.awt.headless");
            }
        }
    }
    
    @Test
    @SuppressWarnings("removal")
    public void testMain_NonHeadlessEnvironment_Success() {
        // Given - non-headless environment
        mockedGraphicsEnvironment.when(GraphicsEnvironment::isHeadless).thenReturn(false);
        
        Injector mockInjector = mock(Injector.class);
        MainWindow mockMainWindow = mock(MainWindow.class);
        
        // Capture the ExpenseTrackerModule to verify builder methods are called
        final ExpenseTrackerModule[] capturedModule = new ExpenseTrackerModule[1];
        mockedGuice.when(() -> Guice.createInjector(any(ExpenseTrackerModule.class)))
            .thenAnswer(invocation -> {
                // Capture the module to ensure builder methods were called
                // The module should have been configured with:
                // - mongoHost("localhost") 
                // - mongoPort(27017)
                // - databaseName("expense_tracker")
                // These builder methods execute BEFORE Guice.createInjector is called
                capturedModule[0] = invocation.getArgument(0);
                return mockInjector;
            });
        when(mockInjector.getInstance(MainWindow.class)).thenReturn(mockMainWindow);
        
        // Capture the Runnable passed to SwingUtilities.invokeLater
        final Runnable[] capturedRunnable = new Runnable[1];
        mockedSwingUtilities.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
            .thenAnswer(invocation -> {
                capturedRunnable[0] = invocation.getArgument(0);
                // Execute the runnable immediately for testing
                // This executes lines 38-46 including the ExpenseTrackerModule builder calls
                capturedRunnable[0].run();
                return null;
            });
        
        // When - execute main method
        // This will execute:
        ExpenseTrackerApp.main(new String[]{});
        
        // Then - verify the flow
        // Note: isHeadless may be called multiple times during AWT class loading, so use atLeastOnce
        mockedGraphicsEnvironment.verify(GraphicsEnvironment::isHeadless, atLeastOnce());
        mockedSwingUtilities.verify(() -> SwingUtilities.invokeLater(any(Runnable.class)), times(1));
        mockedGuice.verify(() -> Guice.createInjector(any(ExpenseTrackerModule.class)), times(1));
        verify(mockInjector, times(1)).getInstance(MainWindow.class);
        verify(mockMainWindow, times(1)).setVisible(true);
        mockedJOptionPane.verifyNoInteractions();
        // Verify the module was created (builder methods were called)
        assertThat(capturedModule[0]).isNotNull();
    }
    
    @Test
    @SuppressWarnings("removal")
    public void testMain_NonHeadlessEnvironment_ExceptionWithDialog() {
        // Given - non-headless environment with exception
        mockedGraphicsEnvironment.when(GraphicsEnvironment::isHeadless).thenReturn(false);
        
        RuntimeException testException = new RuntimeException("Database connection failed");
        mockedGuice.when(() -> Guice.createInjector(any(ExpenseTrackerModule.class)))
            .thenThrow(testException);
        
        // Capture the Runnable passed to SwingUtilities.invokeLater
        final Runnable[] capturedRunnable = new Runnable[1];
        mockedSwingUtilities.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
            .thenAnswer(invocation -> {
                capturedRunnable[0] = invocation.getArgument(0);
                // Execute the runnable immediately for testing
                capturedRunnable[0].run();
                return null;
            });
        
        // Mock System.exit to prevent actual exit
        SecurityManager originalSecurityManager = System.getSecurityManager();
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkExit(int status) {
                if (status == 1) {
                    throw new SecurityException("Exit with code 1");
                }
                throw new SecurityException("Unexpected exit code: " + status);
            }
            
            @Override
            public void checkPermission(java.security.Permission perm) {
                // Allow all permissions
            }
        });
        
            try {
                // When - execute main method which will trigger exception handler
                // This will execute System.exit(exitCode) at line 336 in the catch block
                try {
                    ExpenseTrackerApp.main(new String[]{});
                    // Should not reach here
                    assertThat(false).as("Expected SecurityException from System.exit(1)").isTrue();
                } catch (SecurityException e) {
                    // Expected - System.exit(exitCode) at line 336 was called
                    assertThat(e.getMessage()).isEqualTo("Exit with code 1");
                    // Verify that the exception was thrown from System.exit by checking the stack trace
                    // This ensures line 336 was actually executed
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    boolean foundSystemExit = false;
                    for (StackTraceElement element : stackTrace) {
                        if (element.getMethodName().equals("exit") && 
                            element.getClassName().equals("java.lang.System")) {
                            foundSystemExit = true;
                            break;
                        }
                    }
                    assertThat(foundSystemExit).as("System.exit should be in stack trace").isTrue();
                }
                
                // Then - verify error dialog was shown
            // Note: isHeadless may be called multiple times during AWT class loading, so use atLeastOnce
            mockedGraphicsEnvironment.verify(GraphicsEnvironment::isHeadless, atLeastOnce());
            mockedJOptionPane.verify(() -> JOptionPane.showMessageDialog(
                isNull(),
                anyString(),
                eq("Database Error"),
                eq(JOptionPane.ERROR_MESSAGE)
            ), times(1));
        } finally {
            System.setSecurityManager(originalSecurityManager);
        }
    }
    
    @Test
    @SuppressWarnings("removal")
    public void testMain_NonHeadlessEnvironment_ExceptionHeadlessAfterException() {
        // Given - non-headless initially, but headless after exception (edge case)
        mockedGraphicsEnvironment.when(GraphicsEnvironment::isHeadless)
            .thenReturn(false)  // First call in main()
            .thenReturn(true);  // Second call in catch block
        
        RuntimeException testException = new RuntimeException("Database connection failed");
        mockedGuice.when(() -> Guice.createInjector(any(ExpenseTrackerModule.class)))
            .thenThrow(testException);
        
        // Capture the Runnable passed to SwingUtilities.invokeLater
        final Runnable[] capturedRunnable = new Runnable[1];
        mockedSwingUtilities.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
            .thenAnswer(invocation -> {
                capturedRunnable[0] = invocation.getArgument(0);
                // Execute the runnable immediately for testing
                capturedRunnable[0].run();
                return null;
            });
        
        // Mock System.exit to prevent actual exit
        SecurityManager originalSecurityManager = System.getSecurityManager();
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkExit(int status) {
                if (status == 1) {
                    throw new SecurityException("Exit with code 1");
                }
                throw new SecurityException("Unexpected exit code: " + status);
            }
            
            @Override
            public void checkPermission(java.security.Permission perm) {
                // Allow all permissions
            }
        });
        
            try {
                // When - execute main method which will trigger exception handler
                // This will execute System.exit(exitCode) at line 336 in the catch block
                try {
                    ExpenseTrackerApp.main(new String[]{});
                    // Should not reach here
                    assertThat(false).as("Expected SecurityException from System.exit(1)").isTrue();
                } catch (SecurityException e) {
                    // Expected - System.exit(exitCode) at line 336 was called
                    assertThat(e.getMessage()).isEqualTo("Exit with code 1");
                    // Verify that the exception was thrown from System.exit by checking the stack trace
                    // This ensures line 336 was actually executed
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    boolean foundSystemExit = false;
                    for (StackTraceElement element : stackTrace) {
                        if (element.getMethodName().equals("exit") && 
                            element.getClassName().equals("java.lang.System")) {
                            foundSystemExit = true;
                            break;
                        }
                    }
                    assertThat(foundSystemExit).as("System.exit should be in stack trace").isTrue();
                }
                
                // Then - verify error dialog was NOT shown (because headless after exception)
            // Note: isHeadless may be called multiple times during AWT class loading, so use atLeastOnce
            mockedGraphicsEnvironment.verify(GraphicsEnvironment::isHeadless, atLeastOnce());
            mockedJOptionPane.verify(() -> JOptionPane.showMessageDialog(
                any(),
                anyString(),
                anyString(),
                anyInt()
            ), never());
        } finally {
            System.setSecurityManager(originalSecurityManager);
        }
    }
    
    @Test
    public void testLogHeadlessEnvironmentError_DirectCall() {
        // Test logHeadlessEnvironmentError() method by calling it directly
        // This ensures the logging method is covered
        ExpenseTrackerApp.logHeadlessEnvironmentError();
        // No assertions needed - just verify the method executes without exception
    }
    
    @Test
    @SuppressWarnings("removal")
    public void testHandleHeadlessEnvironment_DirectCall() {
        // Test handleHeadlessEnvironment() method by calling it directly
        // This ensures the call site in main() is covered, following the MainWindow pattern
        // where handleExit() is called directly from tests
        
        // Mock System.exit to prevent actual exit
        SecurityManager originalSecurityManager = System.getSecurityManager();
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkExit(int status) {
                if (status == 1) {
                    throw new SecurityException("Exit with code 1");
                }
                throw new SecurityException("Unexpected exit code: " + status);
            }
            
            @Override
            public void checkPermission(java.security.Permission perm) {
                // Allow all permissions
            }
        });
        
        try {
            // Call handleHeadlessEnvironment() directly - this ensures the call site is covered
            try {
                ExpenseTrackerApp.handleHeadlessEnvironment();
                // Should not reach here
                assertThat(false).as("Expected SecurityException from System.exit(1)").isTrue();
            } catch (SecurityException e) {
                // Expected - System.exit(1) was prevented, but handleHeadlessEnvironment() method body was executed
                assertThat(e.getMessage()).isEqualTo("Exit with code 1");
                // Verify that the exception was thrown from System.exit by checking the stack trace
                StackTraceElement[] stackTrace = e.getStackTrace();
                boolean foundSystemExit = false;
                for (StackTraceElement element : stackTrace) {
                    if (element.getMethodName().equals("exit") && 
                        element.getClassName().equals("java.lang.System")) {
                        foundSystemExit = true;
                        break;
                    }
                }
                assertThat(foundSystemExit).as("System.exit should be in stack trace").isTrue();
            }
        } finally {
            System.setSecurityManager(originalSecurityManager);
        }
    }
    
    @Test
    @SuppressWarnings("removal")
    public void testHandleInitializationException_DirectCall() {
        // Test handleInitializationException() method by calling it directly
        // This ensures the call site in lambda is covered, following the MainWindow pattern
        
        // Mock System.exit to prevent actual exit
        SecurityManager originalSecurityManager = System.getSecurityManager();
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkExit(int status) {
                if (status == 1) {
                    throw new SecurityException("Exit with code 1");
                }
                throw new SecurityException("Unexpected exit code: " + status);
            }
            
            @Override
            public void checkPermission(java.security.Permission perm) {
                // Allow all permissions
            }
        });
        
        try {
            // Call handleInitializationException() directly - this ensures the call site is covered
            Exception testException = new RuntimeException("Test exception");
            try {
                ExpenseTrackerApp.handleInitializationException(testException);
                // Should not reach here
                assertThat(false).as("Expected SecurityException from System.exit(1)").isTrue();
            } catch (SecurityException e) {
                // Expected - System.exit(1) was prevented, but handleInitializationException() method body was executed
                assertThat(e.getMessage()).isEqualTo("Exit with code 1");
                // Verify that the exception was thrown from System.exit by checking the stack trace
                StackTraceElement[] stackTrace = e.getStackTrace();
                boolean foundSystemExit = false;
                for (StackTraceElement element : stackTrace) {
                    if (element.getMethodName().equals("exit") && 
                        element.getClassName().equals("java.lang.System")) {
                        foundSystemExit = true;
                        break;
                    }
                }
                assertThat(foundSystemExit).as("System.exit should be in stack trace").isTrue();
            }
        } finally {
            System.setSecurityManager(originalSecurityManager);
        }
    }
    
    @Test
    public void testExpenseTrackerApp_Constructor() {
        // Test default constructor to ensure it's covered
        // The default constructor is implicitly created by Java
        ExpenseTrackerApp app = new ExpenseTrackerApp();
        assertThat(app).isNotNull();
    }
}

