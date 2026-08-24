package io.telekom.orchest.adapter.kafka.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Configuration model for a Kafka consumer loaded from consumer.json. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerConsumerConfig {

  /** Kafka consumer beanName. */
  private String id;

  /** Kafka topic to consume from. */
  private String topic;

  /** Consumer group ID. */
  private String groupId;

  /** Number of concurrent consumer threads. */
  @Builder.Default private int concurrency = 3;
}
