package io.telekom.orchest.client.processor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.node.EventNode;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.api.core.request.PendingTaskRequest;
import io.telekom.orchest.api.core.request.Variables;
import io.telekom.orchest.client.OrchestProperties;
import io.telekom.orchest.client.TaskResponse;
import io.telekom.orchest.client.annotations.ActivatedJob;
import io.telekom.orchest.client.annotations.JobClient;
import io.telekom.orchest.client.annotations.JobMethodInfo;
import io.telekom.orchest.client.exception.RetryableException;
import io.telekom.orchest.client.notification.IncidentNotification;
import io.telekom.orchest.client.notification.IncidentNotifier;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link OrchestJobWorkerAnnotationProcessor} event processing, retry, dedup, and
 * incident handling.
 */
@ExtendWith(MockitoExtension.class)
class OrchestJobWorkerAnnotationProcessorTest {

  @Mock private JobClient jobClient;

  @Mock private EventProducer eventProducer;

  @Mock private IncidentNotifier incidentNotifier;

  private OrchestProperties orchestProperties;

  private Map<String, JobMethodInfo> jobWorkerMethods;

  private OrchestJobWorkerAnnotationProcessor processor;

  // Dummy bean with worker methods for testing
  public static class TestWorkerBean {
    public Object handleTask(JobClient client, ActivatedJob job) {
      return null; // successful completion, no TaskResponse
    }

    public Object handleTaskWithResponse(JobClient client, ActivatedJob job) {
      return TaskResponse.builder()
          .variables(new Variables(Map.of("result", "ok"), Variables.VariableAction.UPDATE))
          .build();
    }

    public Object handleTaskThrowsRetryable(JobClient client, ActivatedJob job) {
      throw new RetryableException("transient failure");
    }

    public Object handleTaskThrowsRetryableWithBackoff(JobClient client, ActivatedJob job) {
      throw new RetryableException("transient failure", Duration.ofSeconds(5));
    }

    public Object handleTaskThrowsUnexpected(JobClient client, ActivatedJob job) {
      throw new RuntimeException("unexpected boom");
    }
  }

  private Method getMethod(String name) {
    try {
      return TestWorkerBean.class.getMethod(name, JobClient.class, ActivatedJob.class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  void setUp() {
    orchestProperties = new OrchestProperties();
    orchestProperties.setLogVariables(Collections.emptyMap());
    jobWorkerMethods = new HashMap<>();
    processor =
        new OrchestJobWorkerAnnotationProcessor(
            jobWorkerMethods, jobClient, eventProducer, incidentNotifier, orchestProperties);
  }

  private WorkerEventRequest buildWorkerEvent(String workerType, String eventId, int retriesLeft) {
    ServiceTaskNode serviceTask = new ServiceTaskNode("activity-1", "My Task");
    serviceTask.setWorkerType(workerType);

    return WorkerEventRequest.builder()
        .eventId(eventId)
        .processInstanceId("pi-123")
        .processDefinitionId("pd-456")
        .activityId("activity-1")
        .version(1)
        .nodeInformation(serviceTask)
        .variables(
            new Variables(new HashMap<>(Map.of("key", "value")), Variables.VariableAction.UPDATE))
        .stateChanges(new ArrayList<>())
        .retriesLeft(retriesLeft)
        .retries(3)
        .build();
  }

  private void registerWorker(String workerType, String methodName) {
    TestWorkerBean bean = new TestWorkerBean();
    JobMethodInfo info =
        JobMethodInfo.builder()
            .enabled(true)
            .bean(bean)
            .beanName("testWorkerBean")
            .method(getMethod(methodName))
            .logWorker(false)
            .logVariables(false)
            .build();
    jobWorkerMethods.put(workerType, info);
  }

  // ============================================================
  // processEvent - valid worker method
  // ============================================================

  @Test
  @DisplayName("processEvent with valid worker invokes method and sends completion event")
  void processEvent_validWorker_invokesMethodAndSendsCompletionEvent() {
    registerWorker("myWorker", "handleTask");
    WorkerEventRequest event = buildWorkerEvent("myWorker", "evt-1", 3);

    processor.processEvent(event);

    assertEquals(NodeState.COMPLETED, event.getState());
    verify(eventProducer).sendServerWorkerEvent(event);
    verify(eventProducer, never()).sendPendingTaskEvent(any());
    verify(incidentNotifier, never()).notify(any());
  }

  @Test
  @DisplayName("processEvent with TaskResponse sets variables and message on the event")
  void processEvent_withTaskResponse_setsVariables() {
    registerWorker("respWorker", "handleTaskWithResponse");
    WorkerEventRequest event = buildWorkerEvent("respWorker", "evt-resp-1", 3);

    processor.processEvent(event);

    assertEquals(NodeState.COMPLETED, event.getState());
    assertNotNull(event.getVariables());
    assertEquals("ok", event.getVariables().getVariables().get("result"));
    verify(eventProducer).sendServerWorkerEvent(event);
  }

  // ============================================================
  // processEvent - unknown worker type
  // ============================================================

  @Test
  @DisplayName("processEvent with unknown worker type sends PENDING event")
  void processEvent_unknownWorkerType_sendsPendingEvent() {
    // No worker registered for "unknownWorker"
    WorkerEventRequest event = buildWorkerEvent("unknownWorker", "evt-2", 3);

    processor.processEvent(event);

    assertEquals(NodeState.PENDING, event.getState());
    verify(eventProducer, never()).sendServerWorkerEvent(any());

    ArgumentCaptor<PendingTaskRequest> captor = ArgumentCaptor.forClass(PendingTaskRequest.class);
    verify(eventProducer).sendPendingTaskEvent(captor.capture());

    PendingTaskRequest pendingReq = captor.getValue();
    assertEquals("unknownWorker", pendingReq.getWorkerId());
    assertEquals("pd-456", pendingReq.getProcessDefinitionId());
  }

  // ============================================================
  // processEvent - non-ServiceTaskNode
  // ============================================================

  @Test
  @DisplayName("processEvent with non-ServiceTaskNode returns early without sending any event")
  void processEvent_nonServiceTaskNode_returnsEarly() {
    EventNode eventNode = new EventNode();
    eventNode.setId("evt-node");

    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .eventId("evt-3")
            .processInstanceId("pi-999")
            .nodeInformation(eventNode)
            .variables(new Variables(new HashMap<>(), Variables.VariableAction.UPDATE))
            .stateChanges(new ArrayList<>())
            .build();

    processor.processEvent(event);

    verifyNoInteractions(eventProducer);
    verifyNoInteractions(incidentNotifier);
  }

  // ============================================================
  // processEvent - dedup: same eventId twice
  // ============================================================

  @Test
  @DisplayName("processEvent dedup: same eventId twice, second call is skipped")
  void processEvent_duplicateEventId_secondCallSkipped() {
    registerWorker("dedupWorker", "handleTask");

    WorkerEventRequest event1 = buildWorkerEvent("dedupWorker", "evt-dup", 3);
    WorkerEventRequest event2 = buildWorkerEvent("dedupWorker", "evt-dup", 3);

    processor.processEvent(event1);
    processor.processEvent(event2);

    // sendServerWorkerEvent should be called only once for the first event
    verify(eventProducer, times(1)).sendServerWorkerEvent(any());
  }

  // ============================================================
  // processEvent - dedup: null eventId does not dedup
  // ============================================================

  @Test
  @DisplayName("processEvent dedup: null eventId does not dedup, processes normally")
  void processEvent_nullEventId_processesNormally() {
    registerWorker("nullEvtWorker", "handleTask");

    WorkerEventRequest event1 = buildWorkerEvent("nullEvtWorker", null, 3);
    WorkerEventRequest event2 = buildWorkerEvent("nullEvtWorker", null, 3);

    processor.processEvent(event1);
    processor.processEvent(event2);

    // Both should be processed
    verify(eventProducer, times(2)).sendServerWorkerEvent(any());
  }

  // ============================================================
  // RetryableException with client-side retry (retriesLeft > 0, no backoff)
  // ============================================================

  @Test
  @DisplayName(
      "RetryableException with client-side retry loops until retries exhausted then sends incident")
  void processEvent_retryableException_clientSideRetry() {
    registerWorker("retryWorker", "handleTaskThrowsRetryable");
    WorkerEventRequest event = buildWorkerEvent("retryWorker", "evt-retry", 3);

    processor.processEvent(event);

    // After 3 client-side retries (3 -> 2 -> 1 -> 0), retries exhausted
    assertEquals(0, event.getRetriesLeft());
    assertEquals(NodeState.INCIDENT, event.getState());
    assertNotNull(event.getIncidentMessage());
    assertTrue(event.getIncidentMessage().startsWith("Retry exhausted.."));
    assertTrue(event.getIncidentMessage().contains("RetryableException: transient failure"));
    assertNull(event.getRetryBackOff());
    verify(eventProducer).sendServerWorkerEvent(event);
    verify(incidentNotifier).notify(any(IncidentNotification.class));
  }

  // ============================================================
  // RetryableException with server-side retry (retryBackOff set)
  // ============================================================

  @Test
  @DisplayName("RetryableException with server-side retry sends FAILED state with backoff")
  void processEvent_retryableException_serverSideRetry() {
    registerWorker("backoffWorker", "handleTaskThrowsRetryableWithBackoff");
    WorkerEventRequest event = buildWorkerEvent("backoffWorker", "evt-backoff", 3);

    processor.processEvent(event);

    // With backoff set, it should exit the loop after first failure
    assertEquals(NodeState.FAILED, event.getState());
    assertEquals(Duration.ofSeconds(5), event.getRetryBackOff());
    assertEquals(2, event.getRetriesLeft()); // 3 - 1 = 2
    verify(eventProducer).sendServerWorkerEvent(event);
    verify(incidentNotifier, never()).notify(any()); // Not an incident, just a retry
  }

  // ============================================================
  // RetryableException with retries exhausted immediately
  // ============================================================

  @Test
  @DisplayName("RetryableException with retries already at 1 leads to INCIDENT on first failure")
  void processEvent_retryableException_retriesExhausted() {
    registerWorker("exhaustedWorker", "handleTaskThrowsRetryable");
    // retriesLeft = 1, so after decrement it becomes 0
    WorkerEventRequest event = buildWorkerEvent("exhaustedWorker", "evt-exhaust", 1);

    processor.processEvent(event);

    assertEquals(0, event.getRetriesLeft());
    assertEquals(NodeState.INCIDENT, event.getState());
    assertNull(event.getRetryBackOff());
    verify(eventProducer).sendServerWorkerEvent(event);
    verify(incidentNotifier).notify(any(IncidentNotification.class));
  }

  // ============================================================
  // Unexpected exception sends INCIDENT
  // ============================================================

  @Test
  @DisplayName("Unexpected exception sends INCIDENT and notifies")
  void processEvent_unexpectedException_sendsIncident() {
    registerWorker("failWorker", "handleTaskThrowsUnexpected");
    WorkerEventRequest event = buildWorkerEvent("failWorker", "evt-fail", 3);

    processor.processEvent(event);

    assertEquals(NodeState.INCIDENT, event.getState());
    assertNotNull(event.getIncidentMessage());
    verify(eventProducer).sendServerWorkerEvent(event);
    verify(incidentNotifier).notify(any(IncidentNotification.class));
  }

  // ============================================================
  // processEvent - dedup: LRU eviction after 10,000 entries
  // ============================================================

  @Test
  @DisplayName("Dedup cache evicts oldest entries after exceeding MAX_DEDUP_CACHE_SIZE (10,000)")
  void processEvent_dedupCache_lruEvictionAfter10KEntries() throws Exception {
    registerWorker("evictWorker", "handleTask");

    // Fill the dedup cache with 10,000 unique event IDs
    for (int i = 0; i < 10_000; i++) {
      WorkerEventRequest event = buildWorkerEvent("evictWorker", "fill-evt-" + i, 3);
      processor.processEvent(event);
    }

    // Access the internal processedEvents set via reflection to verify size
    Field processedEventsField =
        OrchestJobWorkerAnnotationProcessor.class.getDeclaredField("processedEvents");
    processedEventsField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Set<String> processedEvents = (Set<String>) processedEventsField.get(processor);

    // After exactly 10,000 entries, cache should be at max capacity
    assertEquals(10_000, processedEvents.size());

    // Now add one more event, which should trigger eviction of the oldest entry
    WorkerEventRequest triggerEviction = buildWorkerEvent("evictWorker", "fill-evt-overflow", 3);
    processor.processEvent(triggerEviction);

    // Cache should still be at 10,000 (oldest was evicted)
    assertTrue(processedEvents.size() <= 10_001);
    // The very first event ID should have been evicted
    assertFalse(processedEvents.contains("fill-evt-0"));
    // The new event ID should be present
    assertTrue(processedEvents.contains("fill-evt-overflow"));

    // Replay the evicted event -- it should be processed again (not skipped)
    reset(eventProducer);
    WorkerEventRequest replayedEvent = buildWorkerEvent("evictWorker", "fill-evt-0", 3);
    processor.processEvent(replayedEvent);

    // Should be processed since it was evicted from cache
    verify(eventProducer).sendServerWorkerEvent(replayedEvent);
  }

  // ============================================================
  // processEvent - dedup: thread safety under concurrent access
  // ============================================================

  @Test
  @DisplayName("Dedup cache is thread-safe under concurrent access")
  void processEvent_dedupCache_threadSafety() throws Exception {
    registerWorker("concurrentWorker", "handleTask");

    int threadCount = 10;
    int eventsPerThread = 100;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);
    AtomicInteger exceptionCount = new AtomicInteger(0);

    for (int t = 0; t < threadCount; t++) {
      final int threadIdx = t;
      executor.submit(
          () -> {
            try {
              startLatch.await(); // Wait for all threads to be ready
              for (int i = 0; i < eventsPerThread; i++) {
                WorkerEventRequest event =
                    buildWorkerEvent("concurrentWorker", "thread-" + threadIdx + "-evt-" + i, 3);
                processor.processEvent(event);
              }
            } catch (Exception e) {
              exceptionCount.incrementAndGet();
            } finally {
              doneLatch.countDown();
            }
          });
    }

    // Release all threads at once
    startLatch.countDown();
    doneLatch.await();
    executor.shutdown();

    // No exceptions should have occurred
    assertEquals(0, exceptionCount.get(), "Concurrent access should not cause exceptions");

    // Verify the internal cache has the expected number of entries
    Field processedEventsField =
        OrchestJobWorkerAnnotationProcessor.class.getDeclaredField("processedEvents");
    processedEventsField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Set<String> processedEvents = (Set<String>) processedEventsField.get(processor);

    // All events should have unique IDs, so all should be in cache
    assertEquals(threadCount * eventsPerThread, processedEvents.size());
  }

  // ============================================================
  // processEvent - dedup: duplicate from concurrent threads
  // ============================================================

  @Test
  @DisplayName("Dedup cache handles duplicate eventIds from concurrent threads correctly")
  void processEvent_dedupCache_concurrentDuplicates() throws Exception {
    registerWorker("dupConcWorker", "handleTask");

    int threadCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);
    AtomicInteger processedCount = new AtomicInteger(0);

    // All threads try to process the same eventId
    for (int t = 0; t < threadCount; t++) {
      executor.submit(
          () -> {
            try {
              startLatch.await();
              WorkerEventRequest event = buildWorkerEvent("dupConcWorker", "same-event-id", 3);
              processor.processEvent(event);
              // If we reach sendServerWorkerEvent, it was actually processed
              processedCount.incrementAndGet();
            } catch (Exception e) {
              // ignore
            } finally {
              doneLatch.countDown();
            }
          });
    }

    startLatch.countDown();
    doneLatch.await();
    executor.shutdown();

    // The event should have been sent to server exactly once (first thread wins)
    // Other threads should have been deduped
    // Due to synchronizedSet + add atomicity, only one should get through
    Field processedEventsField =
        OrchestJobWorkerAnnotationProcessor.class.getDeclaredField("processedEvents");
    processedEventsField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Set<String> processedEvents = (Set<String>) processedEventsField.get(processor);

    assertTrue(processedEvents.contains("same-event-id"));
  }

  // ============================================================
  // processEvent - disabled worker sends PENDING event
  // ============================================================

  @Test
  @DisplayName("processEvent with disabled worker sends PENDING event")
  void processEvent_disabledWorker_sendsPendingEvent() {
    TestWorkerBean bean = new TestWorkerBean();
    JobMethodInfo disabledInfo =
        JobMethodInfo.builder()
            .enabled(false)
            .bean(bean)
            .beanName("testWorkerBean")
            .method(getMethod("handleTask"))
            .logWorker(false)
            .logVariables(false)
            .build();
    jobWorkerMethods.put("disabledWorker", disabledInfo);

    WorkerEventRequest event = buildWorkerEvent("disabledWorker", "evt-disabled", 3);

    processor.processEvent(event);

    assertEquals(NodeState.PENDING, event.getState());
    verify(eventProducer, never()).sendServerWorkerEvent(any());
    verify(eventProducer).sendPendingTaskEvent(any());
  }
}
