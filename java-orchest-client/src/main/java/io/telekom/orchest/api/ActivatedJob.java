package io.telekom.orchest.api;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;

/**
 * Represents a job that has been activated for processing by a worker. Provides access to job
 * metadata and variables.
 */
public interface ActivatedJob {
  /**
   * @return The unique key of the job.
   */
  long getKey();

  /**
   * @return The job type.
   */
  String getType();

  /**
   * @return The key of the process instance this job belongs to.
   */
  String getProcessInstanceKey();

  /**
   * @return The BPMN process ID (definition ID).
   */
  String getBpmnProcessId();

  /**
   * @return The version of the process definition.
   */
  int getProcessDefinitionVersion();

  /**
   * @return The unique key of the process definition.
   */
  long getProcessDefinitionKey();

  /**
   * @return Custom headers associated with the job.
   */
  Map<String, String> getCustomHeaders();

  /**
   * @return The worker assigned to this job.
   */
  String getWorker();

  /**
   * @return The number of retries remaining for this job.
   */
  int getRetries();

  /**
   * @return The deadline for job completion (timestamp).
   */
  long getDeadline();

  /**
   * @return The job variables as a JSON string.
   */
  String getVariables();

  /**
   * @return The job variables as a Map.
   */
  Map<String, Object> getVariablesMap();

  /**
   * Retrieves a variable by key and converts it to the specified type.
   *
   * @param key The variable key.
   * @param variableType The target class type.
   * @param <T> The type of the variable.
   * @return The variable value.
   */
  <T> T getVariablesAsType(String key, Class<T> variableType);

  /**
   * Retrieves a variable by key and converts it using a TypeReference.
   *
   * @param key The variable key.
   * @param toValueTypeRef The TypeReference for the target type.
   * @param <T> The type of the variable.
   * @return The variable value.
   */
  <T> T getVariablesAsType(String key, TypeReference<T> toValueTypeRef);

  /**
   * Converts all variables to the specified type (typically a POJO).
   *
   * @param variableType The target class type.
   * @param <T> The type of the object.
   * @return The converted object.
   */
  <T> T getVariablesAsType(Class<T> variableType);

  /**
   * @return The job representation as a JSON string.
   */
  String toJson();
}
