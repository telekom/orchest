package io.telekom.orchest.adapter.kafka.client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Bean name constants for OrchesT Kafka infrastructure components. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KafkaConstant {

  /* Engine constants */
  public static final String ORCHEST_ENGINE_PRODUCER_FACTORY_BEAN_NAME =
      "orchestEngineProducerFactory";
  public static final String ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME =
      "orchestEngineKafkaListenerContainerFactory";
  public static final String ORCHEST_ENGINE_KAFKA_TEMPLATE_BEAN_NAME = "orchestEngineKafkaTemplate";
  public static final String ORCHEST_ENGINE_KAFKA_ADMIN_BEAN_NAME = "orchestEngineKafkaAdmin";
  public static final String ORCHEST_KAFKA_CRYPTO_SERVICE_BEAN_NAME = "orchestCryptoService";

  /* Spring client constants*/
  public static final String ORCHEST_SPRING_CLIENT_PRODUCER_FACTORY_BEAN_NAME =
      "orchestSpringClientProducerFactory";
  public static final String ORCHEST_SPRING_CLIENT_CONSUMER_FACTORY_BEAN_NAME =
      "orchestSpringClientKafkaListenerContainerFactory";
  public static final String ORCHEST_SPRING_CLIENT_KAFKA_TEMPLATE_BEAN_NAME =
      "orchestSpringClientKafkaTemplate";
  public static final String ORCHEST_SPRING_CLIENT_KAFKA_ADMIN_BEAN_NAME =
      "orchestSpringClientKafkaAdmin";
}
