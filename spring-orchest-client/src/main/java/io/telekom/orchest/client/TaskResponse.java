package io.telekom.orchest.client;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.request.Variables;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Response object returned by job worker methods to communicate results back to the engine. Use the
 * builder to set output variables, error events, boundary message events, or incident messages.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse {
  private Variables variables;
  private WorkerEventRequest.BoundaryMessageEvent messageEvent;
  private WorkerEventRequest.ErrorEvent errorEvent;
  private String incidentMessage;
  private Map<String, Object> transactionData;
  private String transactionId;

  public static class TaskResponseBuilder {

    public TaskResponseBuilder errorEvent(String errorName) {
      return errorEvent(errorName, errorName);
    }

    public TaskResponseBuilder errorEvent(String errorName, String errorCode) {
      this.errorEvent =
          WorkerEventRequest.ErrorEvent.builder().errorName(errorName).errorCode(errorCode).build();
      return this;
    }

    public TaskResponseBuilder errorEvent(String errorName, String errorCode, Exception e) {
      this.errorEvent =
          WorkerEventRequest.ErrorEvent.builder()
              .errorName(errorName)
              .errorCode(errorCode)
              .incidentMessage(ExceptionUtils.getStackTrace(e))
              .build();
      return this;
    }

    public TaskResponseBuilder messageEvent(String messageName, String correlationId) {
      this.messageEvent =
          WorkerEventRequest.BoundaryMessageEvent.builder()
              .messageName(messageName)
              .correlation(correlationId)
              .build();
      return this;
    }

    public TaskResponseBuilder variables(Variables variables) {
      this.variables = variables;
      return this;
    }

    public TaskResponseBuilder variables(Map<String, Object> variables) {
      return variables(Variables.builder().variables(variables).build());
    }

    public TaskResponseBuilder variable(String key, Object value) {
      return variables(Variables.builder().variables(Map.of(key, value)).build());
    }

    public TaskResponseBuilder variable(Object variable) {
      if (variable instanceof Map map) {
        return variables(map);
      }
      String variableKey = getFieldName(variable.getClass().getSimpleName());
      return variable(variableKey, variable);
    }

    public TaskResponseBuilder transactionDataMap(Map<String, Object> transactionData) {
      this.transactionData = transactionData;
      return this;
    }

    public TaskResponseBuilder transaction(String key, Object value) {
      if (this.transactionData == null) {
        this.transactionData = new HashMap<>();
      }
      this.transactionData.put(key, value);
      return this;
    }

    public TaskResponseBuilder transactionId(String transactionId) {
      this.transactionId = transactionId;
      return this;
    }
  }

  private static String getFieldName(String className) {
    char[] fieldName = className.toCharArray();
    fieldName[0] = String.valueOf(fieldName[0]).toLowerCase().toCharArray()[0];
    return String.valueOf(fieldName);
  }
}
