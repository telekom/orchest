package io.telekom.orchest.test.internal;

import io.telekom.orchest.client.TaskResponse;
import io.telekom.orchest.client.annotations.ActivatedJob;
import io.telekom.orchest.client.annotations.JobClient;
import io.telekom.orchest.client.annotations.JobWorker;
import io.telekom.orchest.client.annotations.Worker;
import io.telekom.orchest.client.exception.RetryableException;
import java.time.Duration;
import java.util.Map;

/** Sample {@code @JobWorker} beans for module self-tests and documentation examples. */
@Worker
public class SampleWorkers {

  /** Greets a user by name, producing a "message" output variable. */
  @JobWorker(type = "doc-sample-greet")
  public TaskResponse greet(JobClient jobClient, ActivatedJob activatedJob) {
    String name = (String) activatedJob.getVariablesMap().getOrDefault("name", "world");
    return TaskResponse.builder().variables(Map.of("message", "Hello, " + name)).build();
  }

  /** Returns an error event with code INVALID_INPUT. */
  @JobWorker(type = "doc-sample-error")
  public TaskResponse validationError(JobClient jobClient, ActivatedJob activatedJob) {
    return TaskResponse.builder().errorEvent("Validation failed", "INVALID_INPUT").build();
  }

  /** Returns a task response containing a boundary message event. */
  @JobWorker(type = "doc-sample-message-event")
  public TaskResponse boundaryMessageInResponse(JobClient jobClient, ActivatedJob activatedJob) {
    return TaskResponse.builder()
        .messageEvent("INVENTORY_RESERVED", "order-42")
        .variables(Map.of("reserved", true))
        .build();
  }

  /** Returns a task response containing an incident message. */
  @JobWorker(type = "doc-sample-incident-response")
  public TaskResponse incidentInResponse(JobClient jobClient, ActivatedJob activatedJob) {
    return TaskResponse.builder()
        .incidentMessage("needs manual review")
        .variables(Map.of("review", true))
        .build();
  }

  /** Throws a {@link RetryableException} to simulate transient failure. */
  @JobWorker(type = "doc-sample-retryable")
  public TaskResponse throwsRetryable(JobClient jobClient, ActivatedJob activatedJob) {
    throw new RetryableException("downstream timeout", Duration.ofMillis(250));
  }

  /** Broadcasts a signal via the job client. */
  @JobWorker(type = "doc-sample-broadcast-signal")
  public TaskResponse broadcastSignal(JobClient jobClient, ActivatedJob activatedJob) {
    jobClient.broadcastSignal("process-updated", Map.of("version", 2));
    return TaskResponse.builder().variables(Map.of("signaled", true)).build();
  }

  /** Sends a message event via the job client. */
  @JobWorker(type = "doc-sample-send-message")
  public TaskResponse sendEngineMessage(JobClient jobClient, ActivatedJob activatedJob) {
    jobClient.sendMessageEvent("PAYMENT_CONFIRMED", "pay-1", Map.of("amount", 100));
    return TaskResponse.builder().variables(Map.of("sent", true)).build();
  }

  /** Sends an error event via the job client. */
  @JobWorker(type = "doc-sample-send-error")
  public TaskResponse sendEngineError(JobClient jobClient, ActivatedJob activatedJob) {
    String pid = activatedJob.getProcessInstanceKey();
    jobClient.sendErrorEvent(pid, Map.of("ctx", "x"), "bad state", "ERR_BAD_STATE");
    return TaskResponse.builder().variables(Map.of("errorSent", true)).build();
  }

  /** Raises an incident via the job client. */
  @JobWorker(type = "doc-sample-throw-incident")
  public TaskResponse throwIncident(JobClient jobClient, ActivatedJob activatedJob) {
    jobClient.throwIncidentEvent(
        activatedJob.getProcessInstanceKey(),
        Map.of("reason", "stale"),
        new IllegalStateException("stale data"));
    return TaskResponse.builder().variables(Map.of("incidentRaised", true)).build();
  }

  /** Deploys a process definition via the job client. */
  @JobWorker(type = "doc-sample-deploy")
  public TaskResponse deployDefinition(JobClient jobClient, ActivatedJob activatedJob) {
    jobClient.deployProcessDefinition("/definitions/order.bpmn", 4);
    return TaskResponse.builder().variables(Map.of("deployed", true)).build();
  }

  /** Creates a child process instance via the job client. */
  @JobWorker(type = "doc-sample-create-child")
  public TaskResponse createChildProcess(JobClient jobClient, ActivatedJob activatedJob) {
    jobClient.createProcessInstance("child-process", "child-inst-1", Map.of("parentRef", "p-1"));
    return TaskResponse.builder().variables(Map.of("childStarted", true)).build();
  }

  /** Sends a complete event via the job client. */
  @JobWorker(type = "doc-sample-complete-event")
  public TaskResponse completeProcess(JobClient jobClient, ActivatedJob activatedJob) {
    jobClient.sendCompleteEvent(activatedJob.getProcessInstanceKey(), Map.of("finished", true));
    return TaskResponse.builder().variables(Map.of("completedEvent", true)).build();
  }

  /** Returns path=HIGH for exclusive gateway testing. */
  @JobWorker(type = "doc-sample-branch-high")
  public TaskResponse branchHigh(JobClient jobClient, ActivatedJob activatedJob) {
    return TaskResponse.builder().variables(Map.of("path", "HIGH")).build();
  }

  /** Returns path=LOW for exclusive gateway testing. */
  @JobWorker(type = "doc-sample-branch-low")
  public TaskResponse branchLow(JobClient jobClient, ActivatedJob activatedJob) {
    return TaskResponse.builder().variables(Map.of("path", "LOW")).build();
  }

  /** Returns path=DEFAULT for exclusive gateway default path testing. */
  @JobWorker(type = "doc-sample-branch-default")
  public TaskResponse branchDefault(JobClient jobClient, ActivatedJob activatedJob) {
    return TaskResponse.builder().variables(Map.of("path", "DEFAULT")).build();
  }

  /** Returns branchA=done for parallel gateway testing. */
  @JobWorker(type = "doc-sample-parallel-a")
  public TaskResponse parallelA(JobClient jobClient, ActivatedJob activatedJob) {
    return TaskResponse.builder().variables(Map.of("branchA", "done")).build();
  }

  /** Returns branchB=done for parallel gateway testing. */
  @JobWorker(type = "doc-sample-parallel-b")
  public TaskResponse parallelB(JobClient jobClient, ActivatedJob activatedJob) {
    return TaskResponse.builder().variables(Map.of("branchB", "done")).build();
  }

  /** Echoes the "trace-id" custom header as an output variable. */
  @JobWorker(type = "doc-sample-headers-echo")
  public TaskResponse headersEcho(JobClient jobClient, ActivatedJob activatedJob) {
    String trace = activatedJob.getCustomHeaders().getOrDefault("trace-id", "");
    return TaskResponse.builder().variables(Map.of("traceEcho", trace)).build();
  }
}
