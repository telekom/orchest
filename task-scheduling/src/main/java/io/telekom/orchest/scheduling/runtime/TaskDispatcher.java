package io.telekom.orchest.scheduling.runtime;

import io.telekom.orchest.scheduling.api.ScheduledTask;
import io.telekom.orchest.scheduling.api.TaskHandler;
import io.telekom.orchest.scheduling.spi.ScheduledTaskStore;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

/**
 * Polls the {@link ScheduledTaskStore} for due tasks and dispatches them to {@link TaskHandler}s.
 *
 * <h3>Distributed-correctness model</h3>
 *
 * <p>Multiple dispatcher instances can run in parallel against the same store — there is no leader
 * election, no shared coordination service, no oplog dependency. Safety is provided entirely by the
 * store's atomic claim operation: every poll cycle issues {@code claimDue(ownerId, now,
 * leaseDuration, batchSize)}, and only the dispatcher that wins the compare-and-set on a row gets
 * to execute it. This works on any backing store with single-document atomicity (Mongo, Postgres,
 * MySQL, DynamoDB, ...).
 *
 * <h3>Crash recovery</h3>
 *
 * <p>If a worker JVM dies between {@code claimDue} and {@code completeAndDelete}/{@code fail}, its
 * {@code leaseUntil} eventually expires; the next poll on any dispatcher sees the row as a stale
 * lease and re-claims it. Handlers are required to be idempotent because of this at-least- once
 * delivery model.
 *
 * <h3>Threading</h3>
 *
 * <p>One scheduled thread runs the polling loop; a separate fixed-size pool runs the handler
 * invocations so a slow handler can't block claim of the next batch. Pool sizes are tunable via
 * {@link SchedulerRuntimeProperties}.
 */
@Slf4j
public class TaskDispatcher {

  private final ScheduledTaskStore store;
  private final TaskHandlerRegistry handlers;
  private final SchedulerRuntimeProperties properties;
  private final BackoffPolicy backoffPolicy;
  private final Clock clock;

  private final String ownerId;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicLong droppedNoHandler = new AtomicLong();

  private ScheduledExecutorService pollExecutor;
  private ExecutorService workerExecutor;
  private ScheduledFuture<?> pollFuture;

  public TaskDispatcher(
      ScheduledTaskStore store,
      TaskHandlerRegistry handlers,
      SchedulerRuntimeProperties properties,
      @Qualifier("schedulerBackoffPolicy") BackoffPolicy backoffPolicy,
      Clock clock) {
    this.store = store;
    this.handlers = handlers;
    this.properties = properties;
    this.backoffPolicy = backoffPolicy;
    this.clock = clock;
    this.ownerId = buildOwnerId();
  }

  /** Starts the dispatcher when the Spring application context is ready. */
  @EventListener
  public void onApplicationReady(ApplicationReadyEvent event) {
    start();
  }

  /** Stops the dispatcher when the Spring application context is closing. */
  @EventListener
  public void onContextClosed(ContextClosedEvent event) {
    stop();
  }

  /** Visible for testing — start polling without waiting for {@code ApplicationReadyEvent}. */
  public synchronized void start() {
    if (!running.compareAndSet(false, true)) {
      return;
    }
    this.pollExecutor =
        Executors.newSingleThreadScheduledExecutor(named("orchest-timer-event-poll-"));
    this.workerExecutor =
        Executors.newFixedThreadPool(
            Math.max(1, properties.getDispatcherThreads()), named("orchest-timer-event-worker-"));
    long pollMs = Math.max(50, properties.getPollInterval().toMillis());
    this.pollFuture =
        pollExecutor.scheduleWithFixedDelay(this::pollOnce, 0L, pollMs, TimeUnit.MILLISECONDS);
    log.info(
        "TaskDispatcher started ownerId={} pollEvery={}ms batch={} lease={}s threads={}",
        ownerId,
        pollMs,
        properties.getBatchSize(),
        properties.getLeaseDuration().toSeconds(),
        properties.getDispatcherThreads());
  }

  /** Visible for testing — stop polling and drain workers. */
  public synchronized void stop() {
    if (!running.compareAndSet(true, false)) {
      return;
    }
    log.info("TaskDispatcher stopping ownerId={}", ownerId);
    if (pollFuture != null) {
      pollFuture.cancel(false);
    }
    shutdownGracefully(pollExecutor, "poll");
    shutdownGracefully(workerExecutor, "worker");
    log.info("TaskDispatcher stopped");
  }

  /**
   * Returns the unique owner identifier for this dispatcher instance.
   *
   * @return the owner id used for lease claims
   */
  public String getOwnerId() {
    return ownerId;
  }

  private void pollOnce() {
    if (!running.get()) {
      return;
    }
    try {
      List<ScheduledTask> claimed =
          store.claimDue(
              ownerId,
              Instant.now(clock),
              properties.getLeaseDuration(),
              properties.getBatchSize());
      if (claimed.isEmpty()) {
        return;
      }
      log.debug("Claimed {} due tasks", claimed.size());
      for (ScheduledTask task : claimed) {
        workerExecutor.execute(() -> dispatch(task));
      }
    } catch (Exception ex) {
      // Never propagate out of the poll thread — the scheduled executor would suppress all
      // future cycles after a single uncaught exception. Log and try again on the next tick.
      log.warn("Poll cycle failed, will retry", ex);
    }
  }

  private void dispatch(ScheduledTask task) {
    TaskHandler handler = handlers.find(task.getType()).orElse(null);
    if (handler == null) {
      droppedNoHandler.incrementAndGet();
      log.warn(
          "No handler registered for type='{}' (id={}). Marking task dead.",
          task.getType(),
          task.getId());
      store.markDead(
          task.getId(), task.getVersion(), "No handler registered for type=" + task.getType());
      return;
    }
    try {
      handler.handle(task);
      store.completeAndDelete(task.getId(), task.getVersion());
    } catch (Exception ex) {
      handleFailure(task, ex);
    }
  }

  private void handleFailure(ScheduledTask task, Exception failure) {
    int attempts = task.getAttempts() + 1;
    String message = failure.getClass().getSimpleName() + ": " + failure.getMessage();
    if (attempts >= task.getMaxAttempts()) {
      log.error(
          "Task id={} type='{}' exhausted retries ({} attempts). Marking dead.",
          task.getId(),
          task.getType(),
          attempts,
          failure);
      store.markDead(task.getId(), task.getVersion(), message);
      return;
    }
    Instant nextAt = Instant.now(clock).plus(backoffPolicy.nextDelay(attempts));
    log.warn(
        "Task id={} type='{}' attempt {}/{} failed; retrying at {}",
        task.getId(),
        task.getType(),
        attempts,
        task.getMaxAttempts(),
        nextAt,
        failure);
    store.fail(task.getId(), task.getVersion(), nextAt, message);
  }

  private static ThreadFactory named(String prefix) {
    return new ThreadFactory() {
      private final AtomicLong counter = new AtomicLong();

      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r, prefix + counter.incrementAndGet());
        t.setDaemon(true);
        return t;
      }
    };
  }

  private static String buildOwnerId() {
    String host = System.getenv().getOrDefault("HOSTNAME", "host");
    return host + "-" + UUID.randomUUID();
  }

  private static void shutdownGracefully(ExecutorService executor, String label) {
    if (executor == null) {
      return;
    }
    executor.shutdown();
    try {
      if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
        log.warn("{} executor did not terminate in 15s, forcing shutdown", label);
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      executor.shutdownNow();
    }
  }
}
