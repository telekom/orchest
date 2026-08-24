package io.telekom.orchest.api;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.request.Variables;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response object returned by worker methods to provide output variables and events. */
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

  /** Custom builder extensions for constructing a {@link TaskResponse}. */
  public static class TaskResponseBuilder {

    /**
     * Sets a BPMN error event using the error name as both name and code.
     *
     * @param errorName the error name
     * @return this builder
     */
    public TaskResponseBuilder errorEvent(String errorName) {
      return errorEvent(errorName, errorName);
    }

    /**
     * Sets a BPMN error event with a distinct name and code.
     *
     * @param errorName the error name
     * @param errorCode the BPMN error code
     * @return this builder
     */
    public TaskResponseBuilder errorEvent(String errorName, String errorCode) {
      this.errorEvent =
          WorkerEventRequest.ErrorEvent.builder().errorName(errorName).errorCode(errorCode).build();
      return this;
    }

    /**
     * Sets a boundary message event to be thrown during task completion.
     *
     * @param messageName the BPMN message name
     * @param correlationId the correlation key
     * @return this builder
     */
    public TaskResponseBuilder messageEvent(String messageName, String correlationId) {
      this.messageEvent =
          WorkerEventRequest.BoundaryMessageEvent.builder()
              .messageName(messageName)
              .correlation(correlationId)
              .build();
      return this;
    }

    /**
     * Sets the output variables from a Variables instance.
     *
     * @param variables the output variables
     * @return this builder
     */
    public TaskResponseBuilder variables(Variables variables) {
      this.variables = variables;
      return this;
    }

    /**
     * Sets the output variables from a map.
     *
     * @param variables the output variables map
     * @return this builder
     */
    public TaskResponseBuilder variables(Map<String, Object> variables) {
      return variables(Variables.builder().variables(variables).build());
    }

    /**
     * Sets a single output variable by key.
     *
     * @param key the variable key
     * @param value the variable value
     * @return this builder
     */
    public TaskResponseBuilder variable(String key, Object value) {
      return variables(Variables.builder().variables(Map.of(key, value)).build());
    }

    /**
     * Sets a single output variable using the object's class name as the key.
     *
     * @param variable the variable value (or a Map)
     * @return this builder
     */
    public TaskResponseBuilder variable(Object variable) {
      if (variable instanceof Map map) {
        return variables(map);
      }
      String variableKey = getFieldName(variable.getClass().getSimpleName());
      return variable(variableKey, variable);
    }

    /**
     * Sets the full transaction data map.
     *
     * @param transactionData the transaction data
     * @return this builder
     */
    public TaskResponseBuilder transactionDataMap(Map<String, Object> transactionData) {
      this.transactionData = transactionData;
      return this;
    }

    /**
     * Adds a single key-value entry to the transaction data.
     *
     * @param key the transaction data key
     * @param value the transaction data value
     * @return this builder
     */
    public TaskResponseBuilder transaction(String key, Object value) {
      if (this.transactionData == null) {
        this.transactionData = new HashMap<>();
      }
      this.transactionData.put(key, value);
      return this;
    }

    /**
     * Sets the transaction identifier for compensation/saga support.
     *
     * @param transactionId the transaction ID
     * @return this builder
     */
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
