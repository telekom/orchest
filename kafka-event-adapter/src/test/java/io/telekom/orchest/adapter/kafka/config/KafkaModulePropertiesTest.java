package io.telekom.orchest.adapter.kafka.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests default values of OrchestKafkaProperties configuration. */
class KafkaModulePropertiesTest {

  @Test
  @DisplayName("defaults for replica count and partitions")
  void defaults() {
    OrchestKafkaProperties p = new OrchestKafkaProperties();
    assertEquals(3, p.getReplicaCount());
    assertEquals(3, p.getDefaultPartitions());
  }
}
