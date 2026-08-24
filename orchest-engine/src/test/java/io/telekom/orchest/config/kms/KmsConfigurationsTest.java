package io.telekom.orchest.config.kms;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.telekom.solutions.kmsclient.EnableKMS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

/** Tests for {@link KmsConfigurations} verifying required annotations and instantiation. */
class KmsConfigurationsTest {

  @Test
  @DisplayName("KmsConfigurations is a Spring @Configuration with @EnableKMS")
  void annotations() {
    assertTrue(KmsConfigurations.class.isAnnotationPresent(Configuration.class));
    assertTrue(KmsConfigurations.class.isAnnotationPresent(EnableKMS.class));
  }

  @Test
  @DisplayName("default constructor can be used for instantiation")
  void canInstantiate() {
    assertNotNull(new KmsConfigurations());
  }
}
