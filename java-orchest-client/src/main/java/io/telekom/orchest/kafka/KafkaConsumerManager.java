package io.telekom.orchest.kafka;

import io.telekom.orchest.config.OrchestClientProperties;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * Enterprise-grade Kafka Consumer Manager for pure Java client. Manages the lifecycle of Kafka
 * consumers with safe multi-threading support. Each consumer runs in its own thread with proper
 * shutdown handling.
 */
@Slf4j
public class KafkaConsumerManager implements AutoCloseable {

  private final OrchestClientProperties properties;
  private final ConcurrentHashMap<String, ConsumerWorker> consumers;
  private final ExecutorService consumerExecutor;
  private final ExecutorService messageProcessingExecutor;
  private final AtomicBoolean running;

  /**
   * Creates a new Kafka consumer manager.
   *
   * @param properties The OrchesT client properties
   */
  public KafkaConsumerManager(OrchestClientProperties properties) {
    this.properties = properties;
    this.consumers = new ConcurrentHashMap<>();
    this.running = new AtomicBoolean(false);

    // Thread pool for consumer polling threads (one per topic)
    this.consumerExecutor =
        Executors.newCachedThreadPool(
            new ThreadFactory() {
              private int counter = 0;

              @Override
              public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("orchest-consumer-" + (++counter));
                thread.setDaemon(false);
                thread.setUncaughtExceptionHandler(
                    (t, e) ->
                        log.error("Uncaught exception in consumer thread {}", t.getName(), e));
                return thread;
              }
            });

    // Thread pool for message processing (configurable concurrency)
    this.messageProcessingExecutor =
        new ThreadPoolExecutor(
            properties.getWorkerThreadCount(),
            properties.getWorkerThreadCount() * 2,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadFactory() {
              private int counter = 0;

              @Override
              public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("orchest-message-processor-" + (++counter));
                thread.setDaemon(false);
                thread.setUncaughtExceptionHandler(
                    (t, e) ->
                        log.error(
                            "Uncaught exception in message processor thread {}", t.getName(), e));
                return thread;
              }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // Back-pressure mechanism
            );

    this.running.set(true);
    log.info("Kafka Consumer Manager initialized successfully");
  }

  /**
   * Registers a consumer for a specific topic with a message handler.
   *
   * @param topic The Kafka topic to consume
   * @param messageHandler Handler function for processing messages (key, value)
   */
  public void registerConsumer(String topic, BiConsumer<String, String> messageHandler) {
    if (!running.get()) {
      throw new IllegalStateException("Consumer manager is not running");
    }

    if (consumers.containsKey(topic)) {
      log.warn("Consumer for topic '{}' already exists", topic);
      return;
    }

    ConsumerWorker worker = new ConsumerWorker(topic, messageHandler);
    consumers.put(topic, worker);
    consumerExecutor.submit(worker);

    log.info("Registered and started consumer for topic: {}", topic);
  }

  /**
   * Stops a consumer for a specific topic.
   *
   * @param topic The Kafka topic
   */
  public void stopConsumer(String topic) {
    ConsumerWorker worker = consumers.remove(topic);
    if (worker != null) {
      worker.shutdown();
      log.info("Stopped consumer for topic: {}", topic);
    }
  }

  /**
   * Creates a Kafka consumer with appropriate configuration.
   *
   * @param topic The topic this consumer will subscribe to (used for stable member identity)
   * @return Configured Kafka consumer
   */
  private Consumer<String, String> createConsumer(String topic) {
    Properties props = new Properties();

    String groupId =
        "ORCHEST_JAVA_CLIENT_"
            + StringUtils.upperCase(properties.getProcessIds().getFirst()).replace("-", "_");
    String clientId = "ORCHEST_JAVA_CLIENT" + properties.getKafkaSuffix();

    // Basic configuration
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

    // Stable member identity so broker can assign member id (fixes MemberIdRequiredException on
    // Kafka 3.x)
    String groupInstanceId = properties.getKafkaConsumer().getGroupInstanceId();
    if (StringUtils.isNotBlank(groupInstanceId)) {
      props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, groupInstanceId);
    } else {
      props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, clientId + "-" + topic);
    }

    // Consumer-specific configuration from properties
    props.putAll(properties.getKafkaConsumer().toProperties());

    // Isolation level for transactional reads
    props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

    // Metrics configuration
    props.put(ConsumerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, "30000");
    props.put(ConsumerConfig.METRICS_NUM_SAMPLES_CONFIG, "2");

    log.debug("Kafka Consumer configuration: {}", sanitizeConfig(props));

    return new KafkaConsumer<>(props);
  }

  /** Checks if the consumer manager is running. */
  public boolean isRunning() {
    return running.get();
  }

  /** Closes all consumers and shuts down thread pools. */
  @Override
  public void close() {
    if (running.compareAndSet(true, false)) {
      log.info("Closing Kafka Consumer Manager...");

      // Stop all consumers
      consumers.values().forEach(ConsumerWorker::shutdown);
      consumers.clear();

      // Shutdown executors
      shutdownExecutor(consumerExecutor, "Consumer Executor");
      shutdownExecutor(messageProcessingExecutor, "Message Processing Executor");

      log.info("Kafka Consumer Manager closed successfully");
    }
  }

  /** Gracefully shuts down an executor service. */
  private void shutdownExecutor(ExecutorService executor, String name) {
    executor.shutdown();
    try {
      if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
        log.warn("{} did not terminate gracefully, forcing shutdown", name);
        List<Runnable> droppedTasks = executor.shutdownNow();
        log.warn("{} dropped {} tasks", name, droppedTasks.size());
      }
    } catch (InterruptedException e) {
      log.error("Interrupted while waiting for {} to terminate", name, e);
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  /** Sanitizes configuration for logging. */
  private String sanitizeConfig(Properties props) {
    Properties sanitized = new Properties();
    sanitized.putAll(props);
    sanitized
        .keySet()
        .forEach(
            key -> {
              String property = String.valueOf(key);
              if (property.contains("ssl") || property.contains("sasl")) {
                sanitized.remove(key);
              }
            });
    return sanitized.toString();
  }

  /**
   * Worker class that runs a Kafka consumer in its own thread. Handles polling and message
   * processing with proper error handling and shutdown.
   */
  private class ConsumerWorker implements Runnable {
    private final String topic;
    private final BiConsumer<String, String> messageHandler;
    private final Consumer<String, String> consumer;
    private final AtomicBoolean running;
    private final Duration pollTimeout;

    public ConsumerWorker(String topic, BiConsumer<String, String> messageHandler) {
      this.topic = topic;
      this.messageHandler = messageHandler;
      this.consumer = createConsumer(topic);
      this.running = new AtomicBoolean(true);
      this.pollTimeout = Duration.ofMillis(properties.getPollTimeoutMs());
    }

    @Override
    public void run() {
      try {
        consumer.subscribe(Collections.singletonList(topic));
        log.info("Consumer started for topic: {}", topic);

        while (running.get()) {
          try {
            ConsumerRecords<String, String> records = consumer.poll(pollTimeout);

            if (!records.isEmpty()) {
              log.debug("Polled {} records from topic '{}'", records.count(), topic);
              processRecords(records);
            }
          } catch (WakeupException e) {
            // Expected exception for shutdown
            if (running.get()) {
              log.error("Unexpected wakeup exception", e);
            }
          } catch (Exception e) {
            log.error("Error processing records from topic '{}'", topic, e);
            // Continue processing after error
            try {
              Thread.sleep(1000); // Brief pause before retry
            } catch (InterruptedException ie) {
              Thread.currentThread().interrupt();
              break;
            }
          }
        }
      } finally {
        try {
          consumer.close(Duration.ofSeconds(10));
          log.info("Consumer closed for topic: {}", topic);
        } catch (Exception e) {
          log.error("Error closing consumer for topic '{}'", topic, e);
        }
      }
    }

    /** Processes records by submitting them to the message processing executor. */
    private void processRecords(ConsumerRecords<String, String> records) {
      for (ConsumerRecord<String, String> record : records) {
        // Submit each message for processing in the thread pool
        messageProcessingExecutor.submit(
            () -> {
              try {
                messageHandler.accept(record.key(), record.value());
              } catch (Exception e) {
                log.error(
                    "Error processing message from topic '{}', partition {}, offset {}",
                    topic,
                    record.partition(),
                    record.offset(),
                    e);
                // Message processing error is logged but doesn't stop the consumer
              }
            });
      }
    }

    /** Initiates shutdown of this consumer worker. */
    public void shutdown() {
      log.info("Shutting down consumer for topic: {}", topic);
      running.set(false);
      consumer.wakeup();
    }
  }
}
