package io.telekom.orchest.config.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.telekom.orchest.adapter.kafka.config.OrchestKafkaProperties;
import io.telekom.orchest.api.core.properties.EngineScale;
import io.telekom.orchest.config.engine.EngineProperties;
import java.util.List;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link EngineKafkaConfigurations} verifying topic creation, partition counts, and
 * replication factors across all engine scales.
 */
@ExtendWith(MockitoExtension.class)
class EngineKafkaConfigurationsTest {

  @Mock private OrchestKafkaProperties moduleProperties;

  private final EngineProperties engineProperties = new EngineProperties();
  private final OrchestKafkaProperties orchestKafkaProperties = new OrchestKafkaProperties();

  private EngineKafkaConfigurations configuration;

  @BeforeEach
  void setUp() {
    configuration = new EngineKafkaConfigurations(orchestKafkaProperties, engineProperties);
  }

  @ParameterizedTest
  @EnumSource(EngineScale.class)
  @DisplayName("clientWorkerTopics defines 12 topics for every engine scale")
  void getAllEngineTopics_hasTwelveTopicsPerScale(EngineScale scale) {
    engineProperties.setEngineScale(scale);
    List<NewTopic> topics = configuration.getAllEngineTopics();
    assertEquals(13, topics.size());
  }

  @ParameterizedTest
  @EnumSource(EngineScale.class)
  @DisplayName("topic replication matches engine scale replication count")
  void getAllEngineTopics_replicationMatchesScale(EngineScale scale) {
    engineProperties.setEngineScale(scale);
    List<NewTopic> topics = configuration.getAllEngineTopics();
    short expectedReplicas = (short) scale.getReplicationCount();
    for (NewTopic topic : topics) {
      assertEquals(expectedReplicas, topic.replicationFactor());
    }
  }

  @Test
  @DisplayName("topic names are non-blank")
  void getAllEngineTopics_topicNamesPresent() {
    engineProperties.setEngineScale(EngineScale.LOCAL);
    List<NewTopic> topics = configuration.getAllEngineTopics();
    for (NewTopic topic : topics) {
      assertFalse(topic.name() == null || topic.name().isBlank());
    }
  }

  @Test
  @DisplayName("incident topics use default partition count for the scale")
  void getAllEngineTopics_incidentTopicsUseDefaultPartitionCount() {
    engineProperties.setEngineScale(EngineScale.LOCAL);
    List<NewTopic> topics = configuration.getAllEngineTopics();
    List<NewTopic> incidentTopics =
        topics.stream().filter(t -> t.name().toUpperCase().contains("INCIDENT")).toList();
    assertEquals(2, incidentTopics.size());
    int expectedPartitions = EngineScale.LOCAL.getDefaultPartitionCount();
    for (NewTopic topic : incidentTopics) {
      assertEquals(expectedPartitions, topic.numPartitions());
    }
  }
}
