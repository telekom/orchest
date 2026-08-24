package io.telekom.orchest.client;

import io.micrometer.core.instrument.util.IOUtils;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.request.*;
import io.telekom.orchest.api.core.response.MessageEventResponse;
import io.telekom.orchest.api.core.response.ProcessInvocationResponse;
import io.telekom.orchest.api.core.response.SignalEventResponse;
import io.telekom.orchest.api.core.response.UpdateVariableResponse;
import io.telekom.orchest.api.core.utils.StateChangeUtils;
import io.telekom.orchest.client.annotations.JobClient;
import io.telekom.orchest.client.processor.EventProducer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

/**
 * Primary entry point for interacting with the OrchesT engine from a Spring Boot application.
 * Provides methods to deploy processes, start instances, and send signals/messages.
 */
@Component
@RequiredArgsConstructor
public class OrchesTClient implements JobClient {

  private final EventProducer eventProducer;

  /**
   * Deploys a process definition to the engine.
   *
   * @param deploymentEvent The deployment event containing the BPMN XML and other metadata.
   */
  @Override
  public void deployProcessDefinition(ResourceDeploymentRequest deploymentEvent) {
    eventProducer.sendDeploymentEvent(deploymentEvent);
  }

  /**
   * Deploys a process definition from a file.
   *
   * @param file The BPMN file.
   * @param partitionCount The number of partitions for the deployment.
   */
  @Override
  public void deployProcessDefinition(File file, int partitionCount) {
    try {
      String fileContent = IOUtils.toString(new FileInputStream(file), Charset.defaultCharset());
      eventProducer.sendDeploymentEvent(
          new ResourceDeploymentRequest(partitionCount, fileContent, false, null, false));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Deploys a process definition from a file path.
   *
   * @param filePath The path to the BPMN file.
   * @param partitionCount The number of partitions for the deployment.
   */
  @Override
  public void deployProcessDefinition(String filePath, int partitionCount) {
    try {
      File file = ResourceUtils.getFile(filePath);
      String fileContent = IOUtils.toString(new FileInputStream(file), Charset.defaultCharset());
      eventProducer.sendDeploymentEvent(
          new ResourceDeploymentRequest(partitionCount, fileContent, false, null, false));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a new process instance.
   *
   * @param processId The ID of the process definition.
   * @param version The version of the process definition.
   * @param variables Initial variables for the process instance.
   * @return The response containing the process instance ID.
   */
  @Override
  public ProcessInvocationResponse createProcessInstance(
      String processId, Integer version, Map<String, Object> variables) {
    return this.createProcessInstance(processId, null, UUID.randomUUID().toString(), variables);
  }

  /**
   * Creates a new process instance asynchronously.
   *
   * @param processId The ID of the process definition.
   * @param version The version of the process definition.
   * @param variables Initial variables for the process instance.
   * @return The response containing the process instance ID.
   */
  @Override
  public ProcessInvocationResponse createProcessInstanceAsync(
      String processId, Integer version, Map<String, Object> variables) {
    ProcessInvocationRequest processInvocationRequest =
        new ProcessInvocationRequest(
            processId, String.valueOf(Instant.now().getNano()), version, variables, null, null);
    eventProducer.sendProcessInvocationEvent(processInvocationRequest);
    return new ProcessInvocationResponse(
        processInvocationRequest.getProcessInstanceId(), processId, version);
  }

  /**
   * Creates a new process instance with a specific instance ID.
   *
   * @param processId The ID of the process definition.
   * @param version The version of the process definition.
   * @param processInstanceId The unique ID for the new process instance.
   * @param variables Initial variables for the process instance.
   * @return The response containing the process instance ID.
   */
  @Override
  public ProcessInvocationResponse createProcessInstance(
      String processId, Integer version, String processInstanceId, Map<String, Object> variables) {
    ProcessInvocationRequest processInvocationRequest =
        new ProcessInvocationRequest(processId, processInstanceId, version, variables, null, null);
    eventProducer.sendProcessInvocationEvent(processInvocationRequest);
    return new ProcessInvocationResponse(
        processInvocationRequest.getProcessInstanceId(), processId, version);
  }

  /**
   * Creates a new process instance with a specific instance ID, using the latest version.
   *
   * @param processId The ID of the process definition.
   * @param processInstanceId The unique ID for the new process instance.
   * @param variables Initial variables for the process instance.
   * @return The response containing the process instance ID.
   */
  @Override
  public ProcessInvocationResponse createProcessInstance(
      String processId, String processInstanceId, Map<String, Object> variables) {
    return this.createProcessInstance(processId, null, processInstanceId, variables);
  }

  /**
   * Creates a new process instance using the latest version and a generated instance ID.
   *
   * @param processId The ID of the process definition.
   * @param variables Initial variables for the process instance.
   * @return The response containing the process instance ID.
   */
  @Override
  public ProcessInvocationResponse createProcessInstance(
      String processId, Map<String, Object> variables) {
    return this.createProcessInstance(processId, null, UUID.randomUUID().toString(), variables);
  }

  /**
   * Sends a completion event for a worker task. Not yet implemented.
   *
   * @param processInstanceId the process instance ID
   * @param variables output variables
   * @return never returns normally
   * @throws UnsupportedOperationException always
   */
  @Override
  public WorkerEventRequest sendCompleteEvent(
      String processInstanceId, Map<String, Object> variables) {
    throw new UnsupportedOperationException("sendCompleteEvent is not yet implemented");
  }

  /**
   * Reports an incident for a worker task. Not yet implemented.
   *
   * @param processInstanceId the process instance ID
   * @param variables context variables
   * @param e the exception causing the incident
   * @return never returns normally
   * @throws UnsupportedOperationException always
   */
  @Override
  public WorkerEventRequest throwIncidentEvent(
      String processInstanceId, Map<String, Object> variables, Throwable e) {
    throw new UnsupportedOperationException("throwIncidentEvent is not yet implemented");
  }

  /**
   * Sends a BPMN error event. Not yet implemented.
   *
   * @param processInstanceId the process instance ID
   * @param variables context variables
   * @param errorMessage the error message
   * @param errorCode the BPMN error code
   * @return never returns normally
   * @throws UnsupportedOperationException always
   */
  @Override
  public WorkerEventRequest sendErrorEvent(
      String processInstanceId,
      Map<String, Object> variables,
      String errorMessage,
      String errorCode) {
    throw new UnsupportedOperationException("sendErrorEvent is not yet implemented");
  }

  /**
   * Sends a message event to the engine to correlate with a waiting process instance or start a new
   * one.
   *
   * @param messageName The name of the message.
   * @param correlationKey The correlation key to match the process instance.
   * @param variables Variables to pass with the message.
   * @return The response containing the message event details.
   */
  @Override
  public MessageEventResponse sendMessageEvent(
      String messageName, String correlationKey, Map<String, Object> variables) {
    MessageEventRequest messageEventRequest =
        new MessageEventRequest(
            UUID.randomUUID().toString(),
            messageName,
            correlationKey,
            Variables.builder().variables(variables).build(),
            List.of(StateChangeUtils.buildStateChanges(NodeState.TRIGGERED)));
    eventProducer.sendMessageEvent(messageEventRequest);
    return new MessageEventResponse(messageEventRequest.getId(), messageName, correlationKey);
  }

  /**
   * Broadcasts a signal event to all subscribing process instances.
   *
   * @param signalName The name of the signal.
   * @param variables Variables to pass with the signal.
   * @return The response containing the signal event details.
   */
  @Override
  public SignalEventResponse broadcastSignal(String signalName, Map<String, Object> variables) {
    SignalEventRequest signalEventRequest =
        new SignalEventRequest(
            UUID.randomUUID().toString(),
            signalName,
            Variables.builder().variables(variables).build(),
            List.of(StateChangeUtils.buildStateChanges(NodeState.TRIGGERED)));
    eventProducer.sendSignalEvent(signalEventRequest);
    return new SignalEventResponse(signalEventRequest.getSignalId(), signalName);
  }

  /**
   * Updates variables for a running process instance.
   *
   * @param processInstanceId the process instance ID
   * @param variables the variables to update
   * @return the update response
   */
  @Override
  public UpdateVariableResponse updateVariable(String processInstanceId, Variables variables) {
    UpdateVariableRequest updateVariableRequest =
        new UpdateVariableRequest(processInstanceId, variables);
    eventProducer.sendUpdateVariableEvent(updateVariableRequest);
    return new UpdateVariableResponse(UUID.randomUUID().toString(), processInstanceId, variables);
  }
}
