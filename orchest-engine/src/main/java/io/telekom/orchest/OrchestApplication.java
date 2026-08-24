package io.telekom.orchest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the OrchesT Engine application. This Spring Boot application initializes the
 * workflow engine, API endpoints, and background services necessary for process orchestration.
 */
@EnableScheduling
@SpringBootApplication
public class OrchestApplication {
  /**
   * Main method to start the Spring Boot application.
   *
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(OrchestApplication.class, args);
  }
}
