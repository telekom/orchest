package io.telekom.orchest.scheduling.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import io.telekom.orchest.scheduling.api.ScheduledTask;
import io.telekom.orchest.scheduling.api.TaskHandler;
import io.telekom.orchest.scheduling.api.TaskScheduleRequest;
import io.telekom.orchest.scheduling.api.TaskState;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link TaskDispatcher} covering dispatch, retry, dead-lettering, and concurrency. */
class TaskDispatcherTest {

  private InMemoryScheduledTaskStore store;
  private DefaultTaskScheduler scheduler;
  private TaskHandlerRegistry registry;
  private TaskDispatcher dispatcher;
  private final Clock clock = Clock.systemUTC();

  @BeforeEach
  void setUp() {
    store = new InMemoryScheduledTaskStore(clock);
    SchedulerRuntimeProperties props =
        SchedulerRuntimeProperties.builder()
            .pollInterval(Duration.ofMillis(50))
            .batchSize(20)
            .leaseDuration(Duration.ofSeconds(2))
            .dispatcherThreads(2)
            .defaultMaxAttempts(3)
            .backoffBase(Duration.ofMillis(50))
            .backoffMax(Duration.ofMillis(200))
            .build();
    scheduler = new DefaultTaskScheduler(store, props, clock);
    registry = new TaskHandlerRegistry();
    BackoffPolicy backoff =
        BackoffPolicy.exponential(props.getBackoffBase(), props.getBackoffMax());
    dispatcher = new TaskDispatcher(store, registry, props, backoff, clock);
  }

  @AfterEach
  void tearDown() {
    dispatcher.stop();
  }

  @Test
  void firesDueTasksAndDeletesThem() {
    CopyOnWriteArrayList<String> seen = new CopyOnWriteArrayList<>();
    registry.register("test.fire", task -> seen.add(task.getPayload()));
    scheduler.schedule(
        TaskScheduleRequest.builder()
            .type("test.fire")
            .payload("hello")
            .triggerAt(Instant.now(clock).minusSeconds(1))
            .build());
    dispatcher.start();
    Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> seen.contains("hello"));
    Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> store.size() == 0);
  }

  @Test
  void retriesFailedTasksWithBackoff() {
    AtomicInteger attempts = new AtomicInteger();
    registry.register(
        "test.retry",
        task -> {
          int n = attempts.incrementAndGet();
          if (n < 3) {
            throw new RuntimeException("boom #" + n);
          }
        });
    scheduler.schedule(
        TaskScheduleRequest.builder()
            .type("test.retry")
            .payload("p")
            .triggerAt(Instant.now(clock).minusMillis(10))
            .maxAttempts(5)
            .build());
    dispatcher.start();
    Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> attempts.get() >= 3);
    Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> store.size() == 0);
  }

  @Test
  void marksDeadAfterMaxAttempts() {
    registry.register(
        "test.dead",
        task -> {
          throw new RuntimeException("always fails");
        });
    ScheduledTask t =
        scheduler.schedule(
            TaskScheduleRequest.builder()
                .type("test.dead")
                .payload("p")
                .triggerAt(Instant.now(clock).minusMillis(10))
                .maxAttempts(2)
                .build());
    dispatcher.start();
    Awaitility.await()
        .atMost(5, TimeUnit.SECONDS)
        .until(
            () -> store.findById(t.getId()).map(s -> s.getState() == TaskState.DEAD).orElse(false));
  }

  @Test
  void upsertByBusinessKeyDeduplicates() {
    registry.register("test.dedup", task -> {});
    ScheduledTask first =
        scheduler.schedule(
            TaskScheduleRequest.builder()
                .type("test.dedup")
                .businessKey("bk-1")
                .triggerAt(Instant.now(clock).plusSeconds(60))
                .build());
    ScheduledTask second =
        scheduler.schedule(
            TaskScheduleRequest.builder()
                .type("test.dedup")
                .businessKey("bk-1")
                .triggerAt(Instant.now(clock).plusSeconds(120))
                .build());
    assertThat(first.getId()).isEqualTo(second.getId());
    assertThat(store.size()).isEqualTo(1);
  }

  @Test
  void cancelByBusinessKeyRemovesTask() {
    scheduler.schedule(
        TaskScheduleRequest.builder()
            .type("test.cancel")
            .businessKey("bk-2")
            .triggerAt(Instant.now(clock).plusSeconds(60))
            .build());
    scheduler.cancelByBusinessKey("test.cancel", "bk-2");
    assertThat(store.size()).isEqualTo(0);
  }

  @Test
  void multipleDispatchersDoNotDoubleFire() throws Exception {
    ConcurrentHashMap<String, AtomicInteger> firings = new ConcurrentHashMap<>();
    TaskHandler counter =
        task ->
            firings.computeIfAbsent(task.getPayload(), k -> new AtomicInteger()).incrementAndGet();

    TaskHandlerRegistry r1 = new TaskHandlerRegistry().register("test.shared", counter);
    TaskHandlerRegistry r2 = new TaskHandlerRegistry().register("test.shared", counter);
    SchedulerRuntimeProperties props =
        SchedulerRuntimeProperties.builder()
            .pollInterval(Duration.ofMillis(20))
            .batchSize(5)
            .leaseDuration(Duration.ofSeconds(2))
            .dispatcherThreads(2)
            .defaultMaxAttempts(3)
            .backoffBase(Duration.ofMillis(50))
            .backoffMax(Duration.ofMillis(200))
            .build();
    BackoffPolicy backoff =
        BackoffPolicy.exponential(props.getBackoffBase(), props.getBackoffMax());
    TaskDispatcher d1 = new TaskDispatcher(store, r1, props, backoff, clock);
    TaskDispatcher d2 = new TaskDispatcher(store, r2, props, backoff, clock);

    for (int i = 0; i < 50; i++) {
      scheduler.schedule(
          TaskScheduleRequest.builder()
              .type("test.shared")
              .payload("payload-" + i)
              .triggerAt(Instant.now(clock).minusMillis(10))
              .build());
    }
    try {
      d1.start();
      d2.start();
      Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> store.size() == 0);
    } finally {
      d1.stop();
      d2.stop();
    }

    assertThat(firings).hasSize(50);
    firings.forEach(
        (k, v) -> assertThat(v.get()).as("payload %s should fire exactly once", k).isEqualTo(1));
  }

  @Test
  void noHandlerForTypeMarksDead() {
    ScheduledTask t =
        scheduler.schedule(
            TaskScheduleRequest.builder()
                .type("test.unrouted")
                .payload("p")
                .triggerAt(Instant.now(clock).minusMillis(10))
                .maxAttempts(3)
                .build());
    dispatcher.start();
    Awaitility.await()
        .atMost(5, TimeUnit.SECONDS)
        .until(
            () -> store.findById(t.getId()).map(s -> s.getState() == TaskState.DEAD).orElse(false));
  }

  @Test
  void backoffPolicyHonoursMonotonicGrowth() {
    BackoffPolicy policy =
        BackoffPolicy.exponential(Duration.ofMillis(100), Duration.ofSeconds(10));
    Duration d1 = policy.nextDelay(1);
    Duration d10 = policy.nextDelay(10);
    assertThat(d1.toMillis()).isLessThanOrEqualTo(10_000L);
    assertThat(d10.toMillis()).isLessThanOrEqualTo(10_000L);
  }
}
