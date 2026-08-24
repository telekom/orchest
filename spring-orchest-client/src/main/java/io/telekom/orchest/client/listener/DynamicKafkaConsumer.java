package io.telekom.orchest.client.listener;

import static io.telekom.orchest.adapter.kafka.KafkaUtils.SEPARATOR;
import static io.telekom.orchest.adapter.kafka.client.TopicConstant.CLIENT_WORKER_EVENT_TOPIC_PREFIX;

import io.telekom.orchest.adapter.kafka.client.KafkaEncryptionClient;
import io.telekom.orchest.adapter.kafka.model.EncryptedKafkaEvent;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.utils.JsonMapper;
import io.telekom.orchest.client.processor.OrchestJobWorkerAnnotationProcessor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Manager for dynamic Kafka consumers in the client application. Creates and starts listeners for
 * specific worker topics at runtime. Handles decryption and dispatching of messages to the {@link
 * OrchestJobWorkerAnnotationProcessor}.
 */
@Slf4j
@RequiredArgsConstructor
public class DynamicKafkaConsumer implements SmartLifecycle {

  private final ConcurrentKafkaListenerContainerFactory<String, String>
      kafkaListenerContainerFactory;
  private final OrchestJobWorkerAnnotationProcessor orchestJobWorkerAnnotationProcessor;
  private final KafkaEncryptionClient kafkaEncryptionClient;
  private final Map<String, ConcurrentMessageListenerContainer<String, String>> containers =
      new ConcurrentHashMap<>();
  private volatile boolean running = false;

  /**
   * Registers and starts a consumer for a specific worker topic.
   *
   * @param topicName The Kafka topic name.
   * @param concurrency The number of concurrent consumers.
   */
  public void registerWorkerConsumer(String topicName, int concurrency) {
    containers.computeIfAbsent(
        topicName,
        topic -> {
          ConcurrentMessageListenerContainer<String, String> container =
              kafkaListenerContainerFactory.createContainer(topic);
          container.setConcurrency(concurrency);
          container.setBeanName(getThreadName(topic));
          container.setCommonErrorHandler(errorHandler());
          container.setupMessageListener(
              (MessageListener<String, String>)
                  record -> {
                    EncryptedKafkaEvent encryptedKafkaEvent =
                        JsonMapper.readFromJson(record.value(), EncryptedKafkaEvent.class);
                    String decrypted =
                        kafkaEncryptionClient.decrypt(encryptedKafkaEvent.getEvent());
                    WorkerEventRequest workerKafkaEvent =
                        JsonMapper.readFromJson(decrypted, WorkerEventRequest.class);
                    orchestJobWorkerAnnotationProcessor.processEvent(workerKafkaEvent);
                  });

          container.start();
          log.info(
              "Registered consumer for worker topic: {}",
              topic.replace(CLIENT_WORKER_EVENT_TOPIC_PREFIX, ""));
          return container;
        });
  }

  @Override
  public void start() {
    // No-op: Consumers are created dynamically via createConsumer
    running = true;
  }

  @Override
  public void stop() {
    log.info("Shutting down all worker consumers..");
    containers.keySet().forEach(this::stopConsumer);
    containers.clear();
    running = false;
    log.info("All dynamic worker consumers stopped.");
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public int getPhase() {
    return Integer.MAX_VALUE; // Stop late in the shutdown process
  }

  /**
   * Stops a specific consumer for a given topic.
   *
   * @param topic The topic name.
   */
  public void stopConsumer(String topic) {
    try {
      ConcurrentMessageListenerContainer<String, String> container = containers.remove(topic);
      if (container != null) {
        container.stop();
        log.info("Stopped consumer for worker topic: {}", topic);
      }
    } catch (Exception e) {
      log.error("Failed to stop consumer for worker topic: {}", topic, e);
    }
  }

  private String getThreadName(String topicName) {
    return topicName
        .replace(CLIENT_WORKER_EVENT_TOPIC_PREFIX, "")
        .replace(SEPARATOR, "-")
        .toLowerCase();
  }

  /**
   * Creates an error handler with fixed-backoff retry (0ms delay, 2 retries).
   *
   * @return the configured error handler
   */
  public DefaultErrorHandler errorHandler() {
    // 0 ms delay, 2 retries → total 3 attempts
    return new DefaultErrorHandler(
        (record, exception) -> {
          // final failure after retries exhausted
          log.error(
              "Failed to process worker event from topic={}, partition={}, offset={}: {}",
              record.topic(),
              record.partition(),
              record.offset(),
              exception.getMessage(),
              exception);
        },
        new FixedBackOff(0L, 2));
  }

  /**
   * Pauses the consumer for the given topic without stopping it.
   *
   * @param topic the Kafka topic name
   */
  public void pauseConsumer(String topic) {
    ConcurrentMessageListenerContainer<String, String> container = containers.get(topic);
    if (container != null) {
      container.pause();
    }
  }

  /**
   * Resumes a paused consumer, or registers a new one if none exists for the topic.
   *
   * @param topic the Kafka topic name
   * @param concurrency the number of concurrent consumers if newly registered
   */
  public void resumeConsumer(String topic, int concurrency) {
    ConcurrentMessageListenerContainer<String, String> container = containers.get(topic);
    if (container != null) {
      container.resume();
    } else {
      registerWorkerConsumer(topic, concurrency);
    }
  }
}
