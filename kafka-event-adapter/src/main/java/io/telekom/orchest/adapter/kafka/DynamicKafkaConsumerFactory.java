package io.telekom.orchest.adapter.kafka;

import io.telekom.orchest.adapter.kafka.client.IEncryptionClient;
import io.telekom.orchest.adapter.kafka.model.EncryptedKafkaEvent;
import io.telekom.orchest.api.core.utils.JsonMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.SmartLifecycle;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;

/**
 * Factory for creating and managing dynamic Kafka consumers. Allows registering consumers for
 * topics at runtime, supporting encrypted messages. Implements SmartLifecycle to manage the
 * lifecycle of the consumers.
 *
 * @param <T> The type of the event payload.
 */
@Slf4j
@RequiredArgsConstructor
public class DynamicKafkaConsumerFactory<T> implements SmartLifecycle {

  private final ConcurrentKafkaListenerContainerFactory<String, String>
      kafkaListenerContainerFactory;
  private final Consumer<T> consumer;
  private final IEncryptionClient encryptionClient;
  private final Class<T> type;
  private final Map<String, ConcurrentMessageListenerContainer<String, String>> containers =
      new ConcurrentHashMap<>();
  private volatile boolean running = false;

  /**
   * Registers and starts a new Kafka consumer for the specified topic. Uses the groupId as both the
   * consumer group ID and bean name.
   *
   * @param topicName The name of the topic to consume from.
   * @param groupId The consumer group ID (also used as bean name).
   * @param concurrency The number of concurrent consumers.
   */
  public void registerWorkerConsumer(String topicName, String groupId, int concurrency) {
    registerWorkerConsumer(topicName, groupId, groupId, concurrency);
  }

  /**
   * Registers and starts a new Kafka consumer for the specified topic with an explicit group ID.
   *
   * @param topicName The name of the topic to consume from.
   * @param groupId The consumer group ID (optional; when null, uses factory default).
   * @param beanName The bean name for the listener container.
   * @param concurrency The number of concurrent consumers.
   */
  public void registerWorkerConsumer(
      String topicName, String groupId, String beanName, int concurrency) {
    if (containers.containsKey(topicName)) {
      log.debug("Consumer for topic '{}' with group '{}' already exists.", topicName, groupId);
      return;
    }
    ConcurrentMessageListenerContainer<String, String> container =
        kafkaListenerContainerFactory.createContainer(topicName);
    if (groupId != null) {
      container.getContainerProperties().setGroupId(groupId);
    }
    container.setConcurrency(concurrency);
    container.setBeanName(beanName);
    container.setupMessageListener(
        (MessageListener<String, String>)
            consumerRecord -> {
              Optional<String> encrypted = getHeaderValue(consumerRecord, "encrypted");
              if (encrypted.isPresent()) {
                EncryptedKafkaEvent encryptedKafkaEvent =
                    JsonMapper.readFromJson(consumerRecord.value(), EncryptedKafkaEvent.class);
                String decrypted = encryptionClient.decrypt(encryptedKafkaEvent.getEvent());
                consumer.accept(JsonMapper.readFromJson(decrypted, type));
              } else {
                consumer.accept(JsonMapper.readFromJson(consumerRecord.value(), type));
              }
            });

    container.start();
    containers.put(topicName, container);
    log.info("Registered consumer on topic: {} with consumerGroup: {}", topicName, groupId);
  }

  /**
   * Stops and removes the consumer(s) for the specified topic.
   *
   * @param topicName The name of the topic.
   */
  public void stopConsumer(String topicName) {
    containers
        .entrySet()
        .removeIf(
            entry -> {
              if (entry.getKey().equals(topicName)) {
                try {
                  entry.getValue().stop();
                  log.info("Stopped consumer for worker topic: {}", topicName);
                } catch (Exception e) {
                  log.error("Failed to stop consumer for topic: {}", topicName, e);
                }
                return true;
              }
              return false;
            });
  }

  /**
   * Checks whether a consumer is currently registered for the given topic.
   *
   * @param topicName the topic name to check
   * @return true if a consumer is active for the topic
   */
  public boolean isConsumerRunning(String topicName) {
    return containers.containsKey(topicName);
  }

  /**
   * Extracts a header value from a Kafka consumer record.
   *
   * @param record the consumer record to read from
   * @param key the header key to look up
   * @return the header value if present, empty otherwise
   */
  public Optional<String> getHeaderValue(ConsumerRecord<String, String> record, String key) {
    var header = record.headers().lastHeader(key);
    return header != null
        ? Optional.of(new String(header.value(), StandardCharsets.UTF_8))
        : Optional.empty();
  }

  @Override
  public void start() {
    running = true;
  }

  @Override
  public void stop() {
    log.info("Shutting down all dynamic Kafka consumers..");
    containers.forEach(
        (topic, container) -> {
          try {
            container.stop();
            log.info("Stopped consumer for engine topic: {}", topic);
          } catch (Exception e) {
            log.error("Failed to stop consumer for engine topic: {}", topic, e);
          }
        });
    containers.clear();
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public int getPhase() {
    return Integer.MAX_VALUE; // Stop late in the shutdown process
  }
}
