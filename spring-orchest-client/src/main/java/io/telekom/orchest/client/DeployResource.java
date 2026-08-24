package io.telekom.orchest.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify resources (BPMN/DMN files) to be deployed at application startup. Can be
 * used on configuration classes or methods.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DeployResource {
  /**
   * Path to a single resource file.
   *
   * @return The file path.
   */
  String value() default "";

  /**
   * Array of paths to multiple resource files.
   *
   * @return The file paths.
   */
  String[] paths() default {};
}
