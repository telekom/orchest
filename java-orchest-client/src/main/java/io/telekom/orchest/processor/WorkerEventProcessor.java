package io.telekom.orchest.processor;

import static io.telekom.orchest.utils.WorkerHelper.*;

import io.telekom.orchest.annotation.JobMethodInfo;
import io.telekom.orchest.annotation.JobWorker;
import io.telekom.orchest.api.ActivatedJob;
import io.telekom.orchest.api.JobClient;
import io.telekom.orchest.api.TaskResponse;
import io.telekom.orchest.api.Worker;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.api.core.request.PendingTaskRequest;
import io.telekom.orchest.api.core.request.Variables;
import io.telekom.orchest.api.core.utils.StateChangeUtils;
import io.telekom.orchest.config.OrchestClientProperties;
import io.telekom.orchest.exception.RetryableException;
import io.telekom.orchest.producer.EventProducer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;

/**
 * Processor responsible for executing worker methods annotated with {@link JobWorker}. Handles the
 * actual method invocation, result processing, error handling, and retry logic. This is a pure Java
 * implementation without Spring dependencies.
 */
@Slf4j
@RequiredArgsConstructor
public class WorkerEventProcessor {

  private final JobClient jobClient;
  private final EventProducer eventProducer;
  private final OrchestClientProperties orchestProperties;
  private final Map<String, Worker> workers;

  /**
   * Processes an incoming worker event by invoking the corresponding job worker method.
   *
   * @param workerEventRequest The worker event details.
   */
  public void processEvent(WorkerEventRequest workerEventRequest) {
    BaseNode nodeInformation = workerEventRequest.getNodeInformation();
    if (!(nodeInformation instanceof ServiceTaskNode serviceTask)) {
      log.error("Invalid node information type - expected ServiceTaskNode");
      return;
    }

    log.debug("Received worker event request for worker: {}", serviceTask.getType());
    Worker worker = workers.get(serviceTask.getWorkerType());
    if (worker != null) {
      Variables unchangedVariablesForRetry =
          new Variables(
              new HashMap<>(workerEventRequest.getVariables().getVariables()),
              workerEventRequest.getVariables().getAction());
      workerEventRequest
          .getStateChanges()
          .add(StateChangeUtils.buildStateChanges(NodeState.STARTED));
      ActivatedJob activatedJob = buildActivatedJob(workerEventRequest);
      MDC.put("processInstanceKey", activatedJob.getProcessInstanceKey());

      try {
        // Log worker start
        logWorker(activatedJob, "Started");
        try {
          Object response = worker.handle(jobClient, activatedJob);
          if (response instanceof TaskResponse taskResponse) {
            workerEventRequest.setVariables(taskResponse.getVariables());
            workerEventRequest.setMessageEvent(taskResponse.getMessageEvent());
            workerEventRequest.setErrorEvent(taskResponse.getErrorEvent());
          }
        } catch (Exception e) {
          throw e.getCause() instanceof RetryableException ? (RetryableException) e.getCause() : e;
        }

        updateState(workerEventRequest, NodeState.COMPLETED);
        logWorker(activatedJob, "Completed");

      } catch (RetryableException e) {
        String stackTrace = ExceptionUtils.getStackTrace(e.getCause() != null ? e.getCause() : e);
        workerEventRequest.setIncidentMessage(stackTrace);
        updateState(workerEventRequest, NodeState.FAILED);
        workerEventRequest.setRetryBackOff(e.getRetryBackoff());
        log.error("Failed to execute job: {}", serviceTask.getWorkerType(), e);
        logWorker(activatedJob, "Failed");

        // Handle retries on client side for simple retries
        int retriesLeft =
            workerEventRequest.getRetriesLeft() > 0 ? workerEventRequest.getRetriesLeft() - 1 : 0;
        workerEventRequest.setRetriesLeft(retriesLeft);

        if (retriesLeft > 0 && workerEventRequest.getRetryBackOff() == null) {
          log.info("Retries left: {} for worker: {}", retriesLeft, serviceTask.getWorkerType());
          workerEventRequest.setVariables(unchangedVariablesForRetry);
          processEvent(workerEventRequest);
          return;
        } else if (retriesLeft == 0) {
          updateState(workerEventRequest, NodeState.INCIDENT);
          workerEventRequest.setIncidentMessage("Retry exhausted..\n" + stackTrace);
          workerEventRequest.setRetryBackOff(null);
        }
      } catch (Exception e) {
        String stackTrace = ExceptionUtils.getStackTrace(e.getCause());
        updateState(workerEventRequest, NodeState.INCIDENT);
        workerEventRequest.setIncidentMessage(stackTrace);
        log.error("Failed to execute job: {}", serviceTask.getWorkerType(), e);
        logWorker(activatedJob, "Failed");

      } finally {
        clearMDC();
      }

      eventProducer.sendServerWorkerEvent(workerEventRequest);
    } else if (workerEventRequest.isCommonWorker()) {
      // Do nothing if the worker is a common worker and no worker is defined in service, and no
      // processing needs to be done.
      log.info(
          "skipping the worker event as no commons worker defined with type: {} in the service",
          workerEventRequest.getType());
    } else {
      updateState(workerEventRequest, NodeState.PENDING);
      log.warn("No activated job found for type: {}", serviceTask.getWorkerType());
      PendingTaskRequest pendingTaskRequest =
          PendingTaskRequest.builder()
              .workerEvent(workerEventRequest)
              .workerId(serviceTask.getWorkerType())
              .processDefinitionId(workerEventRequest.getProcessDefinitionId())
              .build();
      eventProducer.sendPendingTaskEvent(pendingTaskRequest);
    }
  }

  /** Updates the state of a worker event request. */
  private void updateState(WorkerEventRequest workerEventRequest, NodeState state) {
    workerEventRequest.setState(state);
    workerEventRequest.getStateChanges().add(StateChangeUtils.buildStateChanges(state));
  }

  /** Logs worker execution status. */
  private void logWorker(ActivatedJob activatedJob, String status) {
    log.info("Worker {}: {}", status, activatedJob.getType());
  }

  /** Gets log map to add to MDC context. */
  private Map<String, String> getLogMapToContext(
      ActivatedJob activatedJob, JobMethodInfo methodInfo) {
    Map<String, String> logVariables = orchestProperties.getLogVariables();
    if (MapUtils.isEmpty(logVariables)) {
      return Collections.emptyMap();
    }
    Map<String, String> logContextMap = getLogContextMap(activatedJob, methodInfo.isLogVariables());
    Map<String, String> variablesLogMap = getLogVariablesMap(activatedJob, logVariables);
    if (!variablesLogMap.isEmpty()) {
      logContextMap.putAll(variablesLogMap);
    }
    return logContextMap;
  }

  /** Clears MDC context. */
  private void clearMDC() {
    MDC.remove("processInstanceKey");
  }
}
