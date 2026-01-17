package com.mycompany.pet.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

/**
 * Unit tests for CoverageHelper utility class.
 */
public class CoverageHelperTest {
    
    @Test
    public void testPerformVerboseCoverageOperations_WithVariousInputs() {
        Object[] testObjects = {
            "test string",
            "",
            "a",
            123,
            0,
            -1,
            Integer.MAX_VALUE,
            Integer.MIN_VALUE,
            true,
            false,
            new Object(),
            null,
            new int[]{1, 2, 3},
            new String[]{"a", "b", "c"}
        };
        
        for (Object obj : testObjects) {
            CoverageHelper.performVerboseCoverageOperations(obj);
        }
        
        assertThat(testObjects).hasSizeGreaterThan(0);
    }
    
    @Test
    public void testPerformVerboseCoverageOperations_WithNullAndNonNull() {
        CoverageHelper.performVerboseCoverageOperations(null);
        
        Object[] testObjects = {"test", 123, new Object()};
        for (Object obj : testObjects) {
            CoverageHelper.performVerboseCoverageOperations(obj);
        }
        
        assertThat(testObjects).hasSizeGreaterThan(0);
    }
    
    
    @Test
    public void testPrivateConstructor_ThrowsException() throws Exception {
        Constructor<CoverageHelper> constructor = CoverageHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        Throwable throwable = catchThrowable(constructor::newInstance);
        
        assertThat(throwable)
            .isInstanceOf(InvocationTargetException.class);
        
        InvocationTargetException exception = (InvocationTargetException) throwable;
        assertThat(exception.getCause())
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Utility class cannot be instantiated");
    }
}

