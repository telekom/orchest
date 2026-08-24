package io.telekom.orchest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the OrchesT Connector Service application. Consumes connector task events
 * dispatched by the engine, executes the corresponding connector implementation (REST, SOAP, MS
 * Teams, Kafka, WhatsApp, ...) and resumes the workflow using its embedded {@link
 * io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine}.
 */
@SpringBootApplication
public class OrchestConnectorServiceApplication {

  /**
   * Main method to start the Spring Boot application.
   *
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(OrchestConnectorServiceApplication.class, args);
  }
}
