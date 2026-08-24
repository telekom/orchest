package io.telekom.orchest.adapter.kafka;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.adapter.kafka.config.OrchestKafkaProperties;
import java.lang.reflect.Field;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests Kafka topic naming, environment suffix resolution, and partition/replication configuration.
 */
class KafkaUtilsTest {

  private String originalEnvironment;
  private OrchestKafkaProperties originalStaticProps;
  private OrchestKafkaProperties testKafkaProperties;

  private static void setStaticOrchestKafkaProperties(OrchestKafkaProperties props)
      throws Exception {
    Field f = KafkaUtils.class.getDeclaredField("staticOrchestKafkaProperties");
    f.setAccessible(true);
    f.set(null, props);
  }

  private static OrchestKafkaProperties getStaticOrchestKafkaProperties() throws Exception {
    Field f = KafkaUtils.class.getDeclaredField("staticOrchestKafkaProperties");
    f.setAccessible(true);
    return (OrchestKafkaProperties) f.get(null);
  }

  @BeforeEach
  void saveOriginals() throws Exception {
    originalEnvironment = KafkaUtils.ENVIRONMENT;
    originalStaticProps = getStaticOrchestKafkaProperties();
    testKafkaProperties = new OrchestKafkaProperties();
    setStaticOrchestKafkaProperties(testKafkaProperties);
  }

  @AfterEach
  void restoreOriginals() throws Exception {
    KafkaUtils.ENVIRONMENT = originalEnvironment;
    setStaticOrchestKafkaProperties(originalStaticProps);
  }

  @Test
  @DisplayName("getEnvSuffix returns empty string for null environment")
  void getKafkaSuffix_nullEnvironment_returnsEmptyString() {
    KafkaUtils.ENVIRONMENT = null;

    assertEquals("", KafkaUtils.getKafkaSuffix());
  }

  @Test
  @DisplayName("getEnvSuffix returns _PROD when suffix is PROD and environment is not LOCAL")
  void getKafkaSuffix_prodEnvironment_returnsSeparatorProd() {
    KafkaUtils.ENVIRONMENT = "PROD";
    testKafkaProperties.setSuffix("PROD");

    assertEquals("_PROD", KafkaUtils.getKafkaSuffix());
  }

  @Test
  @DisplayName("getEnvSuffix returns _TEST when suffix is TEST and environment is not LOCAL")
  void getKafkaSuffix_testEnvironment_returnsSeparatorTest() {
    KafkaUtils.ENVIRONMENT = "TEST";
    testKafkaProperties.setSuffix("TEST");

    assertEquals("_TEST", KafkaUtils.getKafkaSuffix());
  }

  // ============================================================
  // getTopicWithEnvSuffix
  // ============================================================

  @Test
  @DisplayName("getTopicWithEnvSuffix appends configured suffix for non-LOCAL")
  void getTopicWithEnvSuffix_nonLocal_appendsSuffix() {
    KafkaUtils.ENVIRONMENT = "STAGING";
    testKafkaProperties.setSuffix("STAGING");

    String result = KafkaUtils.getTopicWithEnvSuffix("MY_TOPIC");

    assertEquals("MY_TOPIC_STAGING", result);
  }

  // ============================================================
  // getClientWorkerEventTopic
  // ============================================================

  @Test
  @DisplayName("getClientWorkerEventTopic generates correct topic name for LOCAL")
  void getClientWorkerEventTopic_local_generatesCorrectName() {
    KafkaUtils.ENVIRONMENT = "LOCAL";

    String topic = KafkaUtils.getClientWorkerEventTopic("my-process-id");

    assertEquals("ORCHEST_CLIENT_WORKER_EVENT_TOPIC_MY_PROCESS_ID", topic);
  }

  @Test
  @DisplayName("getClientWorkerEventTopic generates topic with env suffix for PROD")
  void getClientWorkerEventTopic_prod_includesEnvSuffix() {
    KafkaUtils.ENVIRONMENT = "PROD";
    testKafkaProperties.setSuffix("PROD");

    String topic = KafkaUtils.getClientWorkerEventTopic("order-service");

    assertEquals("ORCHEST_CLIENT_WORKER_EVENT_TOPIC_ORDER_SERVICE_PROD", topic);
  }

  @Test
  @DisplayName("getClientWorkerEventTopic replaces hyphens with underscores and uppercases")
  void getClientWorkerEventTopic_replacesHyphensAndUppercases() {
    KafkaUtils.ENVIRONMENT = "LOCAL";

    String topic = KafkaUtils.getClientWorkerEventTopic("my-complex-process-id");

    assertEquals("ORCHEST_CLIENT_WORKER_EVENT_TOPIC_MY_COMPLEX_PROCESS_ID", topic);
  }

  // ============================================================
  // getIncidentEventTopic
  // ============================================================

  @Test
  @DisplayName("getIncidentEventTopic returns correct topic for LOCAL")
  void getIncidentEventTopic_local_returnsCorrectTopic() {
    KafkaUtils.ENVIRONMENT = "LOCAL";

    String topic = KafkaUtils.getIncidentEventTopic();

    assertEquals("ORCHEST_SERVER_INCIDENT_EVENT_TOPIC", topic);
  }

  @Test
  @DisplayName("getIncidentEventTopic includes env suffix for non-LOCAL")
  void getIncidentEventTopic_nonLocal_includesEnvSuffix() {
    KafkaUtils.ENVIRONMENT = "DEV";
    testKafkaProperties.setSuffix("DEV");

    String topic = KafkaUtils.getIncidentEventTopic();

    assertEquals("ORCHEST_SERVER_INCIDENT_EVENT_TOPIC_DEV", topic);
  }

  // ============================================================
  // getGroupIdWithEnvSuffix
  // ============================================================

  @Test
  @DisplayName("getGroupIdWithEnvSuffix generates group ID for LOCAL")
  void getGroupIdWithEnvSuffix_local_generatesGroupId() {
    KafkaUtils.ENVIRONMENT = "LOCAL";

    String groupId = KafkaUtils.getGroupIdWithEnvSuffix("MY_TOPIC");

    assertEquals("MY_TOPIC_CONSUMER_GROUP", groupId);
  }

  @Test
  @DisplayName("getGroupIdWithEnvSuffix appends env suffix for non-LOCAL")
  void getGroupIdWithEnvSuffix_nonLocal_appendsEnvSuffix() {
    KafkaUtils.ENVIRONMENT = "PROD";
    testKafkaProperties.setSuffix("PROD");

    String groupId = KafkaUtils.getGroupIdWithEnvSuffix("MY_TOPIC");

    assertEquals("MY_TOPIC_CONSUMER_GROUP_PROD", groupId);
  }

  // ============================================================
  // getPartitionCount
  // ============================================================

  @Test
  @DisplayName("getPartitionCount returns 1 for LOCAL environment")
  void getPartitionCount_local_returnsOne() {
    KafkaUtils.ENVIRONMENT = "LOCAL";

    assertEquals(1, KafkaUtils.getPartitionCount());
  }

  @Test
  @DisplayName("getPartitionCount returns 1 for TEST environment")
  void getPartitionCount_test_returnsOne() {
    KafkaUtils.ENVIRONMENT = "TEST";

    assertEquals(1, KafkaUtils.getPartitionCount());
  }

  @Test
  @DisplayName("getPartitionCount returns configured value for PROD environment")
  void getPartitionCount_prod_returnsConfiguredValue() {
    KafkaUtils.ENVIRONMENT = "PROD";
    OrchestKafkaProperties props = new OrchestKafkaProperties();
    props.setDefaultPartitions(15);
    KafkaUtils kafkaUtils = new KafkaUtils(props);
    kafkaUtils.init();

    KafkaUtils.ENVIRONMENT = "PROD";
    assertEquals(15, KafkaUtils.getPartitionCount());
  }

  // ============================================================
  // getReplicationCount
  // ============================================================

  @Test
  @DisplayName("getReplicationCount returns 1 for LOCAL environment")
  void getReplicationCount_local_returnsOne() {
    KafkaUtils.ENVIRONMENT = "LOCAL";

    assertEquals(1, KafkaUtils.getReplicationCount());
  }

  @Test
  @DisplayName("getReplicationCount returns 1 for TEST environment")
  void getReplicationCount_test_returnsOne() {
    KafkaUtils.ENVIRONMENT = "TEST";

    assertEquals(1, KafkaUtils.getReplicationCount());
  }

  @Test
  @DisplayName("getReplicationCount returns configured value for PROD environment")
  void getReplicationCount_prod_returnsConfiguredValue() {
    OrchestKafkaProperties props = new OrchestKafkaProperties();
    props.setReplicaCount(5);
    KafkaUtils kafkaUtils = new KafkaUtils(props);
    kafkaUtils.init();

    KafkaUtils.ENVIRONMENT = "PROD";
    assertEquals(5, KafkaUtils.getReplicationCount());
  }

  // ============================================================
  // getActiveProfile
  // ============================================================

  // ============================================================
  // init method
  // ============================================================

  @Test
  @DisplayName("init sets ENVIRONMENT to uppercase and replaces hyphens with underscores")
  void init_setsEnvironmentCorrectly() throws Exception {
    OrchestKafkaProperties props = new OrchestKafkaProperties();
    KafkaUtils kafkaUtils = new KafkaUtils(props);

    try {
      Field field = KafkaUtils.class.getDeclaredField("activeProfile");
      field.setAccessible(true);
      field.set(kafkaUtils, "pre-prod");
    } catch (Exception e) {
      fail("Failed to set activeProfile: " + e.getMessage());
    }

    kafkaUtils.init();

    assertEquals("PRE_PROD", KafkaUtils.ENVIRONMENT);
  }

  @Test
  @DisplayName("init defaults to LOCAL when activeProfile is null")
  void init_nullProfile_defaultsToLocal() throws Exception {
    OrchestKafkaProperties props = new OrchestKafkaProperties();
    KafkaUtils kafkaUtils = new KafkaUtils(props);

    try {
      Field field = KafkaUtils.class.getDeclaredField("activeProfile");
      field.setAccessible(true);
      field.set(kafkaUtils, null);
    } catch (Exception e) {
      fail("Failed to set activeProfile: " + e.getMessage());
    }

    kafkaUtils.init();

    assertEquals("LOCAL", KafkaUtils.ENVIRONMENT);
  }
}
