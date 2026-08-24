package io.telekom.orchest.enginecore.bpmn;

import io.telekom.orchest.api.core.adapters.data.model.*;
import io.telekom.orchest.api.core.request.*;
import java.util.Map;
import java.util.Set;

/**
 * Core interface for the workflow engine. Defines methods to deploy, start, resume, and signal
 * processes. Serves as the primary API for interacting with the BPMN execution engine.
 */
public interface WorkflowEngine {

  /**
   * Deploys a BPMN process definition.
   *
   * @param xml The BPMN XML content.
   * @param isCompensateFlow true if the process compensation flow.
   * @return The deployed ProcessDefinition.
   */
  ProcessDefinition deployProcessDefinition(String xml, boolean isCompensateFlow);

  /**
   * Deploys a DMN decision definition.
   *
   * @param xml The DMN XML content.
   * @return The deployed DecisionDefinition.
   */
  DecisionDefinition deployDecisionDefinition(String xml);

  /**
   * Starts a new process instance using the latest version of the definition.
   *
   * @param processDefinitionId The ID of the process definition.
   * @param processInstanceId The unique ID for the new instance.
   * @param variables Initial variables.
   * @return The started ProcessInstance.
   */
  ProcessInstance startProcess(
      String processDefinitionId, String processInstanceId, Map<String, Object> variables);

  ProcessInstance startDynamicProcess(
      String utf8BpmnXML, String processInstanceId, Map<String, Object> variables);

  /**
   * Starts a new process instance using a specific version of the definition.
   *
   * @param processDefinitionId The ID of the process definition.
   * @param version The version of the process definition.
   * @param processInstanceId The unique ID for the new instance.
   * @param variables Initial variables.
   * @return The started ProcessInstance.
   */
  ProcessInstance startProcess(
      String processDefinitionId,
      Integer version,
      String processInstanceId,
      Map<String, Object> variables);

  /**
   * Starts a new process instance as a sub-process (call activity).
   *
   * @param processDefinitionId The ID of the process definition.
   * @param processInstanceId The unique ID for the new instance.
   * @param variables Initial variables.
   * @param parentProcessActivity Metadata about the parent process activity triggering this
   *     instance.
   * @return The started ProcessInstance.
   */
  ProcessInstance startProcess(
      String processDefinitionId,
      String processInstanceId,
      Map<String, Object> variables,
      ParentProcessActivity parentProcessActivity);

  /**
   * Resumes execution of a process instance at a specific activity. Typically used to complete a
   * wait state (e.g., User Task, Receive Task).
   *
   * @param processInstanceId The ID of the process instance.
   * @param activityId The ID of the activity to resume.
   * @param variables Variables to merge into the process instance.
   */
  void resumeActivity(String processInstanceId, String activityId, Variables variables);

  /**
   * Execute from a specific activityId Typically used to retry or modify activity instance;
   *
   * @param processInstance The process instance.
   * @param activityId The ID of the activity to resume.
   */
  void resumeActivityFromActivityId(ProcessInstance processInstance, String activityId);

  /**
   * Signals a process instance with a named signal event.
   *
   * @param eventName The name of the signal event.
   * @param variables Variables to pass with the signal.
   */
  void handleSignalEvent(String eventName, Variables variables);

  /**
   * Delivers a message event to a process instance.
   *
   * @param messageName The name of the message event.
   * @param correlationId The correlation key.
   * @param variables Variables to pass with the message.
   */
  void handleMessageEvent(String messageName, String correlationId, Variables variables);

  /**
   * Handles a timer event trigger.
   *
   * @param timedEvent The timer event details.
   */
  void handleTimerEvent(TimedEvent timedEvent);

  /**
   * Handles a progressive retry event triggered when a TASK_RETRY timed event fires. The {@link
   * TimedEvent} document is removed after processing (including early exits).
   *
   * @param timedEvent The timed event (TASK_RETRY) that fired; must include {@link
   *     TimedEvent#getEventRequest()}.
   */
  void handleProgressiveRetry(TimedEvent timedEvent);

  /**
   * Handles an error event for a specific activity.
   *
   * @param processInstanceId The ID of the process instance.
   * @param activityId The ID of the activity where the error occurred.
   * @param errorCode The error code.
   * @return true if the error was handled (e.g., by a boundary event), false otherwise.
   */
  boolean handleError(
      String processInstanceId, String activityId, String errorCode, Variables variables);

  /**
   * Handles any incident on a specific activity.
   *
   * @param processInstanceId The ID of the process instance.
   * @param activityId The ID of the activity where the error occurred.
   * @param incidentMessage The incident message.
   * @return true if the error was handled (e.g., by a boundary event), false otherwise.
   */
  boolean handleIncident(String processInstanceId, String activityId, String incidentMessage);

  /**
   * Updates the registry of active workers.
   *
   * @param workerRegistryRequest The worker registry update request.
   */
  void updateWorkerRegistry(WorkerRegistryRequest workerRegistryRequest);

  /**
   * Restarts missed tasks for a set of workers.
   *
   * @param workerInfos The set of workerInfos.
   */
  void startMissedTask(Set<WorkerRegistryRequest.WorkerInfo> workerInfos);

  /**
   * Registers a pending task that needs to be executed.
   *
   * @param data The pending task request.
   */
  void registerPendingTask(PendingTaskRequest data);

  /**
   * Retries a specific activity in a process instance.
   *
   * @param data The retry process event.
   */
  void retryActivity(RetryProcessEvent data);

  /**
   * Cleans up pending retry timers for a process instance. Should be called when a process is
   * cancelled, terminated, or enters incident state.
   *
   * @param processInstanceId The ID of the process instance.
   */
  void cleanupPendingRetryTimers(String processInstanceId);
}
