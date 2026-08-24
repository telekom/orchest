package io.telekom.orchest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Spring Boot application entry point for the housekeeping batch job. Runs data purging tasks and
 * exits with the appropriate exit code.
 */
@SpringBootApplication
public class HousekeepingApplication {

  /**
   * Launches the housekeeping application, executes all registered runners, then exits.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    ConfigurableApplicationContext context =
        SpringApplication.run(HousekeepingApplication.class, args);

    int exitCode = SpringApplication.exit(context);
    System.exit(exitCode);
  }
}
