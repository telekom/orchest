package io.telekom.orchest.orchestrest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;

/** Smoke test verifying the Spring application context loads successfully. */
@SpringBootTest
@EnabledIfSystemProperty(named = "run.integration.tests", matches = "true")
class OrchestRestApplicationTests {

  @Test
  void contextLoads() {}
}
