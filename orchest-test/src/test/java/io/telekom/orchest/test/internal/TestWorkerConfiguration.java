package io.telekom.orchest.test.internal;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Minimal Spring configuration used by {@code OrchestTestLibraryDemoTest} to register {@link
 * SampleWorkers} without a full {@code @SpringBootApplication}.
 */
@Configuration
@ComponentScan(basePackageClasses = SampleWorkers.class)
public class TestWorkerConfiguration {}
