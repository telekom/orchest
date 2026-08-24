package io.telekom.orchest.kafka;

import io.telekom.orchest.config.OrchestClientProperties;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * Enterprise-grade Kafka Producer Manager for pure Java client. Manages the lifecycle of Kafka
 * producers with safe multi-threading support. Provides methods for synchronous and asynchronous
 * message sending.
 */
@Slf4j
public class KafkaProducerManager implements AutoCloseable {

  private final Producer<String, String> producer;
  private final OrchestClientProperties properties;
  private final AtomicBoolean running = new AtomicBoolean(false);

  /**
   * Creates a new Kafka producer manager with the given configuration.
   *
   * @param properties The OrchesT client properties
   */
  public KafkaProducerManager(OrchestClientProperties properties) {
    this.properties = properties;
    this.producer = createProducer();
    this.running.set(true);
    log.info("Kafka Producer Manager initialized successfully");
  }

  /**
   * Creates and configures a Kafka producer instance.
   *
   * @return A configured Kafka producer
   */
  private Producer<String, String> createProducer() {
    Properties props = new Properties();

    // Basic configuration
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "ORCHEST_JAVA_CLIENT" + properties.getKafkaSuffix());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    // Performance and reliability configuration from properties
    props.putAll(properties.getKafkaProducer().toProperties());

    // Transactional configuration for exactly-once semantics (optional)
    // props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, properties.getClientId() + "-tx");

    // Metrics configuration
    props.put(ProducerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, "30000");
    props.put(ProducerConfig.METRICS_NUM_SAMPLES_CONFIG, "2");

    log.debug("Kafka Producer configuration: {}", sanitizeConfig(props));

    return new KafkaProducer<>(props);
  }

  /**
   * Sends a message synchronously to the specified topic.
   *
   * @param record The producer record message
   * @return RecordMetadata containing partition and offset information
   * @throws IllegalStateException if producer is closed
   */
  public RecordMetadata sendSync(ProducerRecord<String, String> record) {
    if (!running.get()) {
      throw new IllegalStateException("Producer is closed");
    }
    try {
      Future<RecordMetadata> future = producer.send(record);
      RecordMetadata metadata =
          future.get(properties.getKafkaProducer().getRequestTimeoutMs(), TimeUnit.MILLISECONDS);
      log.debug(
          "Message sent successfully to topic '{}', partition {}, offset {}",
          record.topic(),
          metadata.partition(),
          metadata.offset());
      return metadata;
    } catch (Exception e) {
      log.error("Failed to send message to topic '{}'", record.topic(), e);
      throw new RuntimeException("Failed to send message", e);
    }
  }

  /**
   * Sends a message asynchronously to the specified topic.
   *
   * @param topic The Kafka topic
   * @param key The message key (can be null)
   * @param value The message value
   * @param callback Callback to invoke on completion (can be null)
   * @throws IllegalStateException if producer is closed
   */
  public void sendAsync(String topic, String key, String value, ProducerCallback callback) {
    if (!running.get()) {
      throw new IllegalStateException("Producer is closed");
    }

    ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
    producer.send(
        record,
        (metadata, exception) -> {
          if (exception != null) {
            log.error("Failed to send message to topic '{}'", topic, exception);
            if (callback != null) {
              callback.onError(exception);
            }
          } else {
            log.debug(
                "Message sent successfully to topic '{}', partition {}, offset {}",
                topic,
                metadata.partition(),
                metadata.offset());
            if (callback != null) {
              callback.onSuccess(metadata);
            }
          }
        });
  }

  /**
   * Sends a message asynchronously without a callback.
   *
   * @param topic The Kafka topic
   * @param key The message key (can be null)
   * @param value The message value
   */
  public void sendAsync(String topic, String key, String value) {
    sendAsync(topic, key, value, null);
  }

  /** Flushes any buffered messages. */
  public void flush() {
    if (running.get()) {
      producer.flush();
      log.debug("Producer flushed successfully");
    }
  }

  /**
   * Checks if the producer is running.
   *
   * @return true if running
   */
  public boolean isRunning() {
    return running.get();
  }

  /** Closes the producer gracefully. */
  @Override
  public void close() {
    if (running.compareAndSet(true, false)) {
      try {
        log.info("Closing Kafka Producer Manager...");
        producer.flush();
        producer.close(Duration.of(10, ChronoUnit.SECONDS));
        log.info("Kafka Producer Manager closed successfully");
      } catch (Exception e) {
        log.error("Error closing Kafka producer", e);
      }
    }
  }

  /**
   * Sanitizes configuration for logging (removes sensitive data).
   *
   * @param props Properties to sanitize
   * @return Sanitized properties string
   */
  private String sanitizeConfig(Properties props) {
    Properties sanitized = new Properties();
    sanitized.putAll(props);
    sanitized.remove("sasl.jaas.config");
    sanitized.remove("ssl.truststore.password");
    sanitized.remove("ssl.keystore.password");
    return sanitized.toString();
  }

  /** Callback interface for asynchronous message sending. */
  public interface ProducerCallback {
    void onSuccess(RecordMetadata metadata);

    void onError(Exception exception);
  }
}
