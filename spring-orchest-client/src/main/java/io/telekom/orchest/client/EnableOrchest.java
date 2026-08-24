package io.telekom.orchest.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.ComponentScan;

/**
 * Annotation to enable the OrchesT Client integration in a Spring Boot application. This triggers
 * component scanning for OrchesT components (configuration, processors, listeners).
 *
 * <p>Usage:
 *
 * <pre>{@code
 * @SpringBootApplication
 * @EnableOrchest
 * public class MyApplication { ... }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ComponentScan(basePackages = "io.telekom.orchest")
public @interface EnableOrchest {}
