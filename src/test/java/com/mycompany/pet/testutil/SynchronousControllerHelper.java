package com.mycompany.pet.testutil;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

/**
 * Helper class for testing controllers synchronously.
 * 
 * This class provides utilities to make async controller operations
 * execute synchronously in tests, making assertions easier.
 */
public class SynchronousControllerHelper {
    
    /**
     * Executes a supplier synchronously on the EDT.
     * Useful for testing controller callbacks.
     * 
     * @param supplier Supplier to execute
     * @return Result of supplier
     */
    public static <T> T executeOnEDT(Supplier<T> supplier) {
        if (SwingUtilities.isEventDispatchThread()) {
            return supplier.get();
        }
        
        final Object[] result = new Object[1];
        final Exception[] exception = new Exception[1];
        
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    result[0] = supplier.get();
                } catch (Exception e) {
                    exception[0] = e;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        if (exception[0] != null) {
            throw new RuntimeException(exception[0]);
        }
        
        @SuppressWarnings("unchecked")
        T typedResult = (T) result[0];
        return typedResult;
    }
    
    /**
     * Executes a runnable synchronously on the EDT.
     * 
     * @param runnable Runnable to execute
     */
    public static void executeOnEDT(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
            return;
        }
        
        try {
            SwingUtilities.invokeAndWait(runnable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Creates a synchronous consumer that executes immediately.
     * Useful for testing controller callbacks.
     * 
     * @param action Action to execute
     * @return Consumer that executes synchronously
     */
    public static <T> Consumer<T> synchronousConsumer(Consumer<T> action) {
        return value -> {
            if (SwingUtilities.isEventDispatchThread()) {
                action.accept(value);
            } else {
                try {
                    SwingUtilities.invokeAndWait(() -> action.accept(value));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}

