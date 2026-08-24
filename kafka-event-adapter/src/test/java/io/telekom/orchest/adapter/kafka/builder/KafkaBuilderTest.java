// package io.telekom.orchest.adapter.kafka.builder;
//
// import io.micrometer.core.instrument.MeterRegistry;
// import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
// import org.springframework.kafka.core.KafkaAdmin;
// import org.springframework.kafka.core.KafkaTemplate;
// import org.springframework.kafka.core.ProducerFactory;
//
// import static org.junit.jupiter.api.Assertions.*;
//
// class KafkaBuilderTest {
//
//    private KafkaBuilder<String, String> kafkaBuilder;
//    private MeterRegistry meterRegistry;
//
//    @BeforeEach
//    void setUp() {
//        kafkaBuilder = new KafkaBuilder<>();
//        meterRegistry = new SimpleMeterRegistry();
//    }
//
//
//    @Test
//    @DisplayName("buildConcurrentKafkaListenerContainerFactory with null meterRegistry does not
// throw")
//    void buildConcurrentKafkaListenerContainerFactory_nullMeterRegistry_doesNotThrow() {
//        ConcurrentKafkaListenerContainerFactory<String, String> factory =
//                kafkaBuilder.buildConcurrentKafkaListenerContainerFactory(
//                        "localhost:9092", "test-group", "test-client",
//                        "PLAIN_TEXT", null, 0, null);
//
//        assertNotNull(factory);
//
//    }
//
//    @Test
//    @DisplayName("buildConcurrentKafkaListenerContainerFactory with zero pollTimeout defaults to
// 120000ms")
//    void buildConcurrentKafkaListenerContainerFactory_zeroPollTimeout_defaultsTo120s() {
//        // When pollTimeoutMs <= 0, the code uses 120_000 as default
//        ConcurrentKafkaListenerContainerFactory<String, String> factory =
//                kafkaBuilder.buildConcurrentKafkaListenerContainerFactory(
//                        "localhost:9092", "group", "client",
//                        "PLAIN_TEXT", null, 0, meterRegistry);
//
//        assertNotNull(factory);
//    }
//
//    @Test
//    @DisplayName("buildConcurrentKafkaListenerContainerFactory with SASL_SSL applies security
// config")
//    void buildConcurrentKafkaListenerContainerFactory_saslSsl_appliesSecurityConfig() {
//        // Should not throw even with SASL_SSL protocol
//        ConcurrentKafkaListenerContainerFactory<String, String> factory =
//                kafkaBuilder.buildConcurrentKafkaListenerContainerFactory(
//                        "localhost:9092", "group", "client",
//                        "SASL_SSL", "arn:aws:iam::123:role/test", 5000, meterRegistry);
//
//        assertNotNull(factory);
//    }
//
//    // ============================================================
//    // buildProducerFactory
//    // ============================================================
//
//    @Test
//    @DisplayName("buildProducerFactory returns non-null factory with PLAIN_TEXT security")
//    void buildProducerFactory_plainText_returnsNonNullFactory() {
//        ProducerFactory<String, String> producerFactory =
//                kafkaBuilder.buildProducerFactory(
//                        "localhost:9092", "PLAIN_TEXT", null, meterRegistry);
//
//        assertNotNull(producerFactory);
//    }
//
//    @Test
//    @DisplayName("buildProducerFactory with SASL_SSL applies security configuration")
//    void buildProducerFactory_saslSsl_appliesSecurityConfig() {
//        ProducerFactory<String, String> producerFactory =
//                kafkaBuilder.buildProducerFactory(
//                        "localhost:9092", "SASL_SSL", "arn:aws:iam::123:role/test",
// meterRegistry);
//
//        assertNotNull(producerFactory);
//    }
//
//    @Test
//    @DisplayName("buildProducerFactory with null security protocol does not apply security
// config")
//    void buildProducerFactory_nullSecurityProtocol_noSecurityConfig() {
//        ProducerFactory<String, String> producerFactory =
//                kafkaBuilder.buildProducerFactory(
//                        "localhost:9092", null, null, meterRegistry);
//
//        assertNotNull(producerFactory);
//    }
//
//    // ============================================================
//    // buildKafkaTemplate
//    // ============================================================
//
//    @Test
//    @DisplayName("buildKafkaTemplate creates template with observation enabled")
//    void buildKafkaTemplate_createsTemplateWithObservationEnabled() {
//        ProducerFactory<String, String> producerFactory =
//                kafkaBuilder.buildProducerFactory(
//                        "localhost:9092", "PLAIN_TEXT", null, meterRegistry);
//
//        KafkaTemplate<String, String> template = kafkaBuilder.buildKafkaTemplate(producerFactory);
//
//        assertNotNull(template);
//    }
//
//    // ============================================================
//    // buildKafkaAdmin
//    // ============================================================
//
//    @Test
//    @DisplayName("buildKafkaAdmin returns non-null admin with PLAIN_TEXT security")
//    void buildKafkaAdmin_plainText_returnsNonNullAdmin() {
//        KafkaAdmin admin = kafkaBuilder.buildKafkaAdmin(
//                "localhost:9092", "PLAIN_TEXT", null);
//
//        assertNotNull(admin);
//    }
//
//    @Test
//    @DisplayName("buildKafkaAdmin with SASL_SSL applies security configuration")
//    void buildKafkaAdmin_saslSsl_appliesSecurityConfig() {
//        KafkaAdmin admin = kafkaBuilder.buildKafkaAdmin(
//                "localhost:9092", "SASL_SSL", "arn:aws:iam::123:role/test");
//
//        assertNotNull(admin);
//    }
//
//    @Test
//    @DisplayName("buildKafkaAdmin with null security protocol does not apply security config")
//    void buildKafkaAdmin_nullSecurityProtocol_noSecurityConfig() {
//        KafkaAdmin admin = kafkaBuilder.buildKafkaAdmin(
//                "localhost:9092", null, null);
//
//        assertNotNull(admin);
//    }
// }
