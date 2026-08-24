package io.telekom.orchest.test.engine;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import io.telekom.orchest.api.core.request.Variables;
import io.telekom.orchest.api.core.response.MessageEventResponse;
import io.telekom.orchest.api.core.response.ProcessInvocationResponse;
import io.telekom.orchest.api.core.response.SignalEventResponse;
import io.telekom.orchest.api.core.response.UpdateVariableResponse;
import io.telekom.orchest.api.core.utils.IDGenerator;
import io.telekom.orchest.client.annotations.JobClient;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of {@link JobClient} that records all interactions for test verification.
 *
 * <p>This mock captures every call made through the {@code JobClient} during worker execution,
 * allowing tests to verify that workers correctly interact with the engine.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * @Autowired MockJobClient mockJobClient;
 *
 * @Test
 * void testWorkerSendsMessage() {
 *     workerTester.execute("notify-worker", Map.of("orderId", "123"));
 *
 *     assertThat(mockJobClient.getMessageEvents()).hasSize(1);
 *     assertThat(mockJobClient.getMessageEvents().get(0).messageName()).isEqualTo("ORDER_READY");
 * }
 *
 * @BeforeEach
 * void resetMock() {
 *     mockJobClient.reset();
 * }
 * }</pre>
 */
public class MockJobClient implements JobClient {

  private final List<Interaction> interactions = Collections.synchronizedList(new ArrayList<>());
  private final List<ProcessCreation> processCreations =
      Collections.synchronizedList(new ArrayList<>());
  private final List<MessageEventRecord> messageEvents =
      Collections.synchronizedList(new ArrayList<>());
  private final List<SignalEventRecord> signalEvents =
      Collections.synchronizedList(new ArrayList<>());
  private final List<ErrorEventRecord> errorEvents =
      Collections.synchronizedList(new ArrayList<>());
  private final List<IncidentRecord> incidents = Collections.synchronizedList(new ArrayList<>());

  /**
   * Resets all recorded interactions. Call this in {@code @BeforeEach} to ensure test isolation.
   */
  public void reset() {
    interactions.clear();
    processCreations.clear();
    messageEvents.clear();
    signalEvents.clear();
    errorEvents.clear();
    incidents.clear();
  }

  // ---- Recorded data access ----

  /**
   * Returns all recorded interactions as an unmodifiable list.
   *
   * @return the list of all method interactions
   */
  public List<Interaction> getInteractions() {
    return Collections.unmodifiableList(interactions);
  }

  /**
   * Returns all recorded process creations as an unmodifiable list.
   *
   * @return the list of process creation records
   */
  public List<ProcessCreation> getProcessCreations() {
    return Collections.unmodifiableList(processCreations);
  }

  /**
   * Returns all recorded message events as an unmodifiable list.
   *
   * @return the list of message event records
   */
  public List<MessageEventRecord> getMessageEvents() {
    return Collections.unmodifiableList(messageEvents);
  }

  /**
   * Returns all recorded signal events as an unmodifiable list.
   *
   * @return the list of signal event records
   */
  public List<SignalEventRecord> getSignalEvents() {
    return Collections.unmodifiableList(signalEvents);
  }

  /**
   * Returns all recorded error events as an unmodifiable list.
   *
   * @return the list of error event records
   */
  public List<ErrorEventRecord> getErrorEvents() {
    return Collections.unmodifiableList(errorEvents);
  }

  /**
   * Returns all recorded incidents as an unmodifiable list.
   *
   * @return the list of incident records
   */
  public List<IncidentRecord> getIncidents() {
    return Collections.unmodifiableList(incidents);
  }

  // ---- JobClient implementation ----

  @Override
  public void deployProcessDefinition(ResourceDeploymentRequest deploymentEvent) {
    interactions.add(new Interaction("deployProcessDefinition", Map.of("event", deploymentEvent)));
  }

  @Override
  public void deployProcessDefinition(File file, int partitionCount) {
    interactions.add(
        new Interaction(
            "deployProcessDefinition",
            Map.of("file", file.getName(), "partitionCount", partitionCount)));
  }

  @Override
  public void deployProcessDefinition(String filePath, int partitionCount) {
    interactions.add(
        new Interaction(
            "deployProcessDefinition",
            Map.of("filePath", filePath, "partitionCount", partitionCount)));
  }

  @Override
  public ProcessInvocationResponse createProcessInstanceAsync(
      String processId, Integer version, Map<String, Object> variables) {
    String instanceId = IDGenerator.generate();
    processCreations.add(new ProcessCreation(processId, version, instanceId, variables));
    return new ProcessInvocationResponse(instanceId, processId, version != null ? version : -1);
  }

  @Override
  public ProcessInvocationResponse createProcessInstance(
      String processId, Integer version, String processInstanceId, Map<String, Object> variables) {
    processCreations.add(new ProcessCreation(processId, version, processInstanceId, variables));
    return new ProcessInvocationResponse(
        processInstanceId, processId, version != null ? version : -1);
  }

  @Override
  public ProcessInvocationResponse createProcessInstance(
      String processId, String processInstanceId, Map<String, Object> variables) {
    return createProcessInstance(processId, null, processInstanceId, variables);
  }

  @Override
  public ProcessInvocationResponse createProcessInstance(
      String processId, Map<String, Object> variables) {
    return createProcessInstanceAsync(processId, null, variables);
  }

  @Override
  public ProcessInvocationResponse createProcessInstance(
      String processId, Integer version, Map<String, Object> variables) {
    return createProcessInstanceAsync(processId, version, variables);
  }

  @Override
  public WorkerEventRequest sendCompleteEvent(
      String processInstanceId, Map<String, Object> variables) {
    interactions.add(
        new Interaction("sendCompleteEvent", Map.of("processInstanceId", processInstanceId)));
    return null;
  }

  @Override
  public WorkerEventRequest throwIncidentEvent(
      String processInstanceId, Map<String, Object> variables, Throwable e) {
    incidents.add(new IncidentRecord(processInstanceId, e.getMessage(), variables));
    return null;
  }

  @Override
  public WorkerEventRequest sendErrorEvent(
      String processInstanceId,
      Map<String, Object> variables,
      String errorMessage,
      String errorCode) {
    errorEvents.add(new ErrorEventRecord(processInstanceId, errorMessage, errorCode, variables));
    return null;
  }

  @Override
  public MessageEventResponse sendMessageEvent(
      String messageId, String correlationKey, Map<String, Object> variables) {
    messageEvents.add(new MessageEventRecord(messageId, correlationKey, variables));
    return MessageEventResponse.builder()
        .messageName(messageId)
        .correlationKey(correlationKey)
        .build();
  }

  @Override
  public SignalEventResponse broadcastSignal(String signalName, Map<String, Object> variables) {
    signalEvents.add(new SignalEventRecord(signalName, variables));
    return SignalEventResponse.builder().signalName(signalName).build();
  }

  @Override
  public UpdateVariableResponse updateVariable(String processInstanceId, Variables variables) {
    return null;
  }

  // ---- Record types for captured interactions ----

  /** Records a generic method interaction with the job client. */
  public record Interaction(String method, Map<String, Object> parameters) {}

  /** Records a process instance creation. */
  public record ProcessCreation(
      String processId, Integer version, String instanceId, Map<String, Object> variables) {}

  /** Records a message event sent through the job client. */
  public record MessageEventRecord(
      String messageName, String correlationKey, Map<String, Object> variables) {}

  /** Records a signal broadcast through the job client. */
  public record SignalEventRecord(String signalName, Map<String, Object> variables) {}

  /** Records an error event sent through the job client. */
  public record ErrorEventRecord(
      String processInstanceId,
      String errorMessage,
      String errorCode,
      Map<String, Object> variables) {}

  /** Records an incident raised through the job client. */
  public record IncidentRecord(
      String processInstanceId, String incidentMessage, Map<String, Object> variables) {}
}
