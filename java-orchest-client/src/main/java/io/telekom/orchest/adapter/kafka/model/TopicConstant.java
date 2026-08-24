package io.telekom.orchest.adapter.kafka.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants for Kafka topic names and prefixes used within the OrchesT platform. Defines standard
 * topic names for events like deployment, invocation, and worker communication.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TopicConstant {

  /** Prefix for all OrchesT related topics. */
  public static final String ORCHEST_PREFIX = "ORCHEST_";

  /** Topic for deployment events. */
  public static final String DEPLOYMENT_TOPIC =
      ORCHEST_PREFIX + "PROCESS_DEFINITION_DEPLOYMENT_EVENT";

  /** Topic for process invocation events. */
  public static final String PROCESS_INVOCATION_EVENT_TOPIC =
      ORCHEST_PREFIX + "PROCESS_INVOCATION_EVENT";

  /** Prefix for client worker event topics. */
  public static final String CLIENT_WORKER_EVENT_TOPIC_PREFIX =
      ORCHEST_PREFIX + "CLIENT_WORKER_EVENT_TOPIC_";

  /** Topic for Common worker events. */
  public static final String CLIENT_COMMON_WORKER_EVENT_TOPIC =
      ORCHEST_PREFIX + "CLIENT_COMMON_WORKER_EVENT_TOPIC";

  /** Topic for worker registration events. */
  public static final String CLIENT_WORKER_REGISTER_EVENT_TOPIC =
      ORCHEST_PREFIX + "CLIENT_WORKER_REGISTER_EVENT_TOPIC";

  /** Topic for worker events sent to the server. */
  public static final String SERVER_WORKER_EVENT_TOPIC_PREFIX =
      ORCHEST_PREFIX + "SERVER_WORKER_EVENT_TOPIC_";

  /** Topic for pending task events. */
  public static final String SERVER_PENDING_TASK_EVENT_TOPIC =
      ORCHEST_PREFIX + "SERVER_PENDING_TASK_EVENT_TOPIC";

  /** Topic for variable update events. */
  public static final String SERVER_UPDATE_VARIABLE_EVENT_TOPIC =
      ORCHEST_PREFIX + "SERVER_UPDATE_VARIABLE_EVENT_TOPIC";

  /** Topic for retry process events. */
  public static final String SERVER_RETRY_PROCESS_EVENT_TOPIC =
      ORCHEST_PREFIX + "SERVER_RETRY_PROCESS_EVENT_TOPIC";

  /** Topic for intermediate message throw events. */
  public static final String SERVER_INTERMEDIATE_MESSAGE_THROW_EVENT_TOPIC =
      ORCHEST_PREFIX + "SERVER_INTERMEDIATE_MESSAGE_THROW_EVENT_TOPIC";

  /** Topic for intermediate signal throw events. */
  public static final String SERVER_INTERMEDIATE_SIGNAL_THROW_EVENT_TOPIC =
      ORCHEST_PREFIX + "SERVER_INTERMEDIATE_SIGNAL_THROW_EVENT_TOPIC";
}
