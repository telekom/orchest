package io.telekom.orchest.client.processor;

import static io.telekom.orchest.client.utils.WorkerHelper.*;
import static net.logstash.logback.argument.StructuredArguments.entries;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.api.core.request.PendingTaskRequest;
import io.telekom.orchest.api.core.request.Variables;
import io.telekom.orchest.api.core.utils.StateChangeUtils;
import io.telekom.orchest.client.OrchestProperties;
import io.telekom.orchest.client.TaskResponse;
import io.telekom.orchest.client.annotations.ActivatedJob;
import io.telekom.orchest.client.annotations.JobClient;
import io.telekom.orchest.client.annotations.JobMethodInfo;
import io.telekom.orchest.client.exception.RetryableException;
import io.telekom.orchest.client.notification.IncidentNotification;
import io.telekom.orchest.client.notification.IncidentNotifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;

/**
 * Processor responsible for executing the logic associated with {@link
 * io.telekom.orchest.client.annotations.JobWorker} annotations. Handles the actual method
 * invocation, result processing, error handling, and retry logic.
 */
@Slf4j
@RequiredArgsConstructor
public class OrchestJobWorkerAnnotationProcessor {

  private static final int MAX_DEDUP_CACHE_SIZE = 10_000;
  private final Set<String> processedEvents =
      Collections.synchronizedSet(
          Collections.newSetFromMap(
              new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                  return size() > MAX_DEDUP_CACHE_SIZE;
                }
              }));

  private final Map<String, JobMethodInfo> jobWorkerMethods;
  private final JobClient jobClient;
  private final EventProducer eventProducer;
  private final IncidentNotifier incidentNotifier;
  private final OrchestProperties orchestProperties;

  /**
   * Processes an incoming worker event by invoking the corresponding job worker method.
   *
   * @param workerEventRequest The worker event details.
   */
  public void processEvent(WorkerEventRequest workerEventRequest) {
    if (workerEventRequest.getEventId() != null
        && !processedEvents.add(workerEventRequest.getEventId())) {
      log.warn(
          "Duplicate worker event detected, skipping. eventId={}, processInstanceId={}, activityId={}",
          workerEventRequest.getEventId(),
          workerEventRequest.getProcessInstanceId(),
          workerEventRequest.getActivityId());
      return;
    }

    BaseNode nodeInformation = workerEventRequest.getNodeInformation();
    if (!(nodeInformation instanceof ServiceTaskNode serviceTask)) {
      log.error("something bad with engine.. this should never happen");
      return;
    }
    log.debug("received worker event request for worker: {}", serviceTask.getType());
    JobMethodInfo methodInfo = jobWorkerMethods.get(serviceTask.getWorkerType());
    if (methodInfo != null && methodInfo.isEnabled()) {
      methodInfo.getMethod().setAccessible(true);
      Variables unchangedVariablesForRetry =
          new Variables(
              new HashMap<>(workerEventRequest.getVariables().getVariables()),
              workerEventRequest.getVariables().getAction());
      workerEventRequest
          .getStateChanges()
          .add(StateChangeUtils.buildStateChanges(NodeState.STARTED));
      ActivatedJob activatedJob = buildActivatedJob(workerEventRequest);
      boolean sendIncident = false;
      String incidentMessage = null;
      Map<String, String> logMDCMap = getLogMapToContext(activatedJob, methodInfo);
      MDC.setContextMap(logMDCMap);
      try {
        boolean succeeded = false;
        while (!succeeded) {
          try {
            logWorker(methodInfo, activatedJob, logMDCMap, "Started");
            try {
              Object response =
                  methodInfo.getMethod().invoke(methodInfo.getBean(), jobClient, activatedJob);
              if (response instanceof TaskResponse taskResponse) {
                workerEventRequest.setVariables(taskResponse.getVariables());
                workerEventRequest.setMessageEvent(taskResponse.getMessageEvent());
                workerEventRequest.setErrorEvent(taskResponse.getErrorEvent());
              }
            } catch (Exception e) {
              throw e.getCause() instanceof RetryableException
                  ? (RetryableException) e.getCause()
                  : e;
            }

            updateState(workerEventRequest, NodeState.COMPLETED);
            logWorker(methodInfo, activatedJob, logMDCMap, "Completed");
            succeeded = true;
          } catch (RetryableException e) {
            String stackTrace =
                ExceptionUtils.getStackTrace(e.getCause() != null ? e.getCause() : e);
            workerEventRequest.setIncidentMessage(stackTrace);
            updateState(workerEventRequest, NodeState.FAILED);
            workerEventRequest.setRetryBackOff(e.getRetryBackoff());
            log.error("Failed to execute job: {}", serviceTask.getWorkerType(), e);
            logWorker(methodInfo, activatedJob, logMDCMap, "Failed");

            int retriesLeft =
                workerEventRequest.getRetriesLeft() > 0
                    ? workerEventRequest.getRetriesLeft() - 1
                    : 0;
            workerEventRequest.setRetriesLeft(retriesLeft);
            if (retriesLeft > 0 && workerEventRequest.getRetryBackOff() == null) {
              // Client-side retry: reset variables and loop again
              log.info("retries left: {} for worker: {}", retriesLeft, serviceTask.getWorkerType());
              workerEventRequest.setVariables(unchangedVariablesForRetry);
              activatedJob = buildActivatedJob(workerEventRequest);
              continue;
            } else if (retriesLeft == 0) {
              updateState(workerEventRequest, NodeState.INCIDENT);
              workerEventRequest.setRetryBackOff(null);
              sendIncident = true;
              incidentMessage = "Retry exhausted.. \n" + stackTrace;
              workerEventRequest.setIncidentMessage(incidentMessage);
            }
            // Server-side retry (with backoff) or incident — exit loop
            succeeded = true;
          }
        }
      } catch (Exception e) {
        String stackTrace = ExceptionUtils.getStackTrace(e.getCause());
        updateState(workerEventRequest, NodeState.INCIDENT);
        workerEventRequest.setIncidentMessage(stackTrace);
        log.error("Failed to execute job: {}", serviceTask.getWorkerType(), e);
        logWorker(methodInfo, activatedJob, logMDCMap, "Failed");
        sendIncident = true;
        incidentMessage = stackTrace;

      } finally {
        if (sendIncident) {
          incidentNotifier.notify(
              new IncidentNotification(
                  activatedJob.getProcessInstanceKey(),
                  activatedJob.getProcessInstanceKey(),
                  KafkaUtils.ENVIRONMENT,
                  incidentMessage));
        }
      }
      eventProducer.sendServerWorkerEvent(workerEventRequest);
      clearMDC(logMDCMap);
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

  private void updateState(WorkerEventRequest workerEventRequest, NodeState state) {
    workerEventRequest.setState(state);
    workerEventRequest.getStateChanges().add(StateChangeUtils.buildStateChanges(state));
  }

  private void logWorker(
      JobMethodInfo jobMethodInfo,
      ActivatedJob activatedJob,
      Map<String, String> logMDCMap,
      String status) {
    if (jobMethodInfo.isLogWorker())
      log.info("Worker {}: {}", status, activatedJob.getType(), entries(logMDCMap));
  }

  private Map<String, String> getLogMapToContext(
      ActivatedJob activatedJob, JobMethodInfo methodInfo) {
    Map<String, String> logVariables = orchestProperties.getLogVariables();
    if (MapUtils.isEmpty(logVariables)) return Collections.emptyMap();
    Map<String, String> logContextMap = getLogContextMap(activatedJob, methodInfo.isLogVariables());
    Map<String, String> variablesLogMap = getLogVariablesMap(activatedJob, logVariables);
    if (!variablesLogMap.isEmpty()) {
      logContextMap.putAll(variablesLogMap);
    }
    logContextMap.put("processInstanceKey", activatedJob.getProcessInstanceKey());
    return logContextMap;
  }

  private void clearMDC(Map<String, String> logMDCMap) {
    logMDCMap.keySet().forEach(MDC::remove);
  }
}
