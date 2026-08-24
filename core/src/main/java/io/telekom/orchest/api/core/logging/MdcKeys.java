package io.telekom.orchest.api.core.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants for MDC (Mapped Diagnostic Context) keys used across the platform. Centralizes
 * scattered string literals for structured logging.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MdcKeys {
  public static final String PROCESS_INSTANCE_ID = "processInstanceId";
  public static final String PROCESS_DEFINITION_ID = "processDefinitionId";
  public static final String ACTIVITY_ID = "activityId";
  public static final String WORKER_TYPE = "workerType";
  public static final String TRACE_ID = "traceId";
  public static final String CORRELATION_ID = "correlationId";
}
