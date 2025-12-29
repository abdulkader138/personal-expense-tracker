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
    private GraphicsEnvironment mockGraphicsEnvironment;
    
    @Before
    public void setUp() {
        // Skip tests on Java 8 as mockito-inline requires Java 11+
        String javaVersion = System.getProperty("java.version");
        boolean isJava8 = javaVersion != null && (javaVersion.startsWith("1.8") || javaVersion.startsWith("8."));
        Assume.assumeFalse("Static mocks require Java 11+, skipping on Java 8", isJava8);
        
        try {
            mockGraphicsEnvironment = mock(GraphicsEnvironment.class);
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
            // This will execute all LOGGER.severe() calls in lines 25-30
            try {
                ExpenseTrackerApp.main(new String[]{});
                // Should not reach here
                assertThat(false).as("Expected SecurityException from System.exit(1)").isTrue();
            } catch (SecurityException e) {
                // Expected - System.exit(1) was called
                assertThat(e.getMessage()).isEqualTo("Exit with code 1");
            }
            
            // Then - verify GraphicsEnvironment.isHeadless was called
            // Note: isHeadless may be called multiple times during AWT class loading, so use atLeastOnce
            mockedGraphicsEnvironment.verify(GraphicsEnvironment::isHeadless, atLeastOnce());
            // SwingUtilities.invokeLater should NOT be called in headless mode
            mockedSwingUtilities.verifyNoInteractions();
            // All lines 24-31 should now be covered including all LOGGER.severe() calls
        } finally {
            System.setSecurityManager(originalSecurityManager);
        }
    }
    
    @Test
    @SuppressWarnings("removal")
    public void testMain_NonHeadlessEnvironment_Success() {
        // Given - non-headless environment
        mockedGraphicsEnvironment.when(GraphicsEnvironment::isHeadless).thenReturn(false);
        
        Injector mockInjector = mock(Injector.class);
        MainWindow mockMainWindow = mock(MainWindow.class);
        
        mockedGuice.when(() -> Guice.createInjector(any(ExpenseTrackerModule.class)))
            .thenReturn(mockInjector);
        when(mockInjector.getInstance(MainWindow.class)).thenReturn(mockMainWindow);
        
        // Capture the Runnable passed to SwingUtilities.invokeLater
        final Runnable[] capturedRunnable = new Runnable[1];
        mockedSwingUtilities.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
            .thenAnswer(invocation -> {
                capturedRunnable[0] = invocation.getArgument(0);
                // Execute the runnable immediately for testing
                capturedRunnable[0].run();
                return null;
            });
        
        // When
        ExpenseTrackerApp.main(new String[]{});
        
        // Then - verify the flow
        // Note: isHeadless may be called multiple times during AWT class loading, so use atLeastOnce
        mockedGraphicsEnvironment.verify(GraphicsEnvironment::isHeadless, atLeastOnce());
        mockedSwingUtilities.verify(() -> SwingUtilities.invokeLater(any(Runnable.class)), times(1));
        mockedGuice.verify(() -> Guice.createInjector(any(ExpenseTrackerModule.class)), times(1));
        verify(mockInjector, times(1)).getInstance(MainWindow.class);
        verify(mockMainWindow, times(1)).setVisible(true);
        mockedJOptionPane.verifyNoInteractions();
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
            // When
            try {
                ExpenseTrackerApp.main(new String[]{});
                // Should not reach here
                assertThat(false).as("Expected SecurityException from System.exit(1)").isTrue();
            } catch (SecurityException e) {
                // Expected - System.exit(1) was called
                assertThat(e.getMessage()).isEqualTo("Exit with code 1");
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
            // When
            try {
                ExpenseTrackerApp.main(new String[]{});
                // Should not reach here
                assertThat(false).as("Expected SecurityException from System.exit(1)").isTrue();
            } catch (SecurityException e) {
                // Expected - System.exit(1) was called
                assertThat(e.getMessage()).isEqualTo("Exit with code 1");
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
}

