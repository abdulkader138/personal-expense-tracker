package com.mycompany.pet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to exclude methods from JaCoCo coverage reports.
 * 
 * JaCoCo automatically excludes methods annotated with any annotation
 * whose name ends with "Generated" from coverage reports.
 * 
 * This is used for methods containing System.exit() calls that cannot
 * be properly tracked by JaCoCo due to SecurityException propagation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ExcludeFromJacocoGeneratedReport {
    /**
     * Optional description of why this is excluded.
     */
    String value() default "";
}

