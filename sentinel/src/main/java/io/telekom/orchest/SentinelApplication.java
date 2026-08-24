package io.telekom.orchest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the Sentinel application. Sentinel is a background worker service handling
 * scheduled tasks, housekeeping, and other maintenance jobs.
 */
@SpringBootApplication
@EnableAsync
public class SentinelApplication {
  /**
   * Application entry point.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(SentinelApplication.class, args);
  }
}
