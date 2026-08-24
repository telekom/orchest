package io.telekom.orchest.client.annotations;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import io.telekom.orchest.api.core.request.Variables;
import io.telekom.orchest.api.core.response.MessageEventResponse;
import io.telekom.orchest.api.core.response.ProcessInvocationResponse;
import io.telekom.orchest.api.core.response.SignalEventResponse;
import io.telekom.orchest.api.core.response.UpdateVariableResponse;
import java.io.File;
import java.util.Map;

/**
 * Client interface for interacting with the OrchesT engine. Provides methods for deployment,
 * process instantiation, and event handling.
 */
public interface JobClient {

  /**
   * Deploys a process definition.
   *
   * @param deploymentEvent The deployment event data.
   */
  void deployProcessDefinition(ResourceDeploymentRequest deploymentEvent);

  /**
   * Deploys a process definition from a file.
   *
   * @param file The file containing the definition.
   * @param partitionCount The number of partitions.
   */
  void deployProcessDefinition(File file, int partitionCount);

  /**
   * Deploys a process definition from a file path.
   *
   * @param filePath The path to the definition file.
   * @param partitionCount The number of partitions.
   */
  void deployProcessDefinition(String filePath, int partitionCount);

  /**
   * Creates a process instance asynchronously.
   *
   * @param processId The process definition ID.
   * @param version The version.
   * @param variables Initial variables.
   * @return The invocation response.
   */
  ProcessInvocationResponse createProcessInstanceAsync(
      String processId, Integer version, Map<String, Object> variables);

  /**
   * Creates a process instance with a specific ID.
   *
   * @param processId The process definition ID.
   * @param version The version.
   * @param processInstanceId The unique process instance ID.
   * @param variables Initial variables.
   * @return The invocation response.
   */
  ProcessInvocationResponse createProcessInstance(
      String processId, Integer version, String processInstanceId, Map<String, Object> variables);

  /**
   * Creates a process instance with a specific ID using the latest version.
   *
   * @param processId The process definition ID.
   * @param processInstanceId The unique process instance ID.
   * @param variables Initial variables.
   * @return The invocation response.
   */
  ProcessInvocationResponse createProcessInstance(
      String processId, String processInstanceId, Map<String, Object> variables);

  /**
   * Creates a process instance with generated ID using the latest version.
   *
   * @param processId The process definition ID.
   * @param variables Initial variables.
   * @return The invocation response.
   */
  ProcessInvocationResponse createProcessInstance(String processId, Map<String, Object> variables);

  /**
   * Creates a process instance.
   *
   * @param processId The process definition ID.
   * @param version The version.
   * @param variables Initial variables.
   * @return The invocation response.
   */
  ProcessInvocationResponse createProcessInstance(
      String processId, Integer version, Map<String, Object> variables);

  /**
   * Sends a completion event for a worker task.
   *
   * @param processInstanceId The process instance ID.
   * @param variables Output variables.
   * @return The worker event request.
   */
  WorkerEventRequest sendCompleteEvent(String processInstanceId, Map<String, Object> variables);

  /**
   * Reports an incident (failure) for a worker task.
   *
   * @param processInstanceId The process instance ID.
   * @param variables Context variables.
   * @param e The exception causing the incident.
   * @return The worker event request.
   */
  WorkerEventRequest throwIncidentEvent(
      String processInstanceId, Map<String, Object> variables, Throwable e);

  /**
   * Sends an BPMN error event.
   *
   * @param processInstanceId The process instance ID.
   * @param variables Context variables.
   * @param errorMessage The error message.
   * @param errorCode The BPMN error code.
   * @return The worker event request.
   */
  WorkerEventRequest sendErrorEvent(
      String processInstanceId,
      Map<String, Object> variables,
      String errorMessage,
      String errorCode);

  /**
   * Sends a message event.
   *
   * @param messageId The message name.
   * @param correlationKey The correlation key.
   * @param variables Payload variables.
   * @return The message event response.
   */
  MessageEventResponse sendMessageEvent(
      String messageId, String correlationKey, Map<String, Object> variables);

  /**
   * Broadcasts a signal event.
   *
   * @param signalName The signal name.
   * @param variables Payload variables.
   * @return The signal event response.
   */
  SignalEventResponse broadcastSignal(String signalName, Map<String, Object> variables);

  /**
   * Updates variables on a running process instance.
   *
   * @param processInstanceId the process instance ID
   * @param variables the variables to set or merge
   * @return the update response
   */
  UpdateVariableResponse updateVariable(String processInstanceId, Variables variables);
}
