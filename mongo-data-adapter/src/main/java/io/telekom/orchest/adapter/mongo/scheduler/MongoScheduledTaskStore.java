package io.telekom.orchest.adapter.mongo.scheduler;

import com.mongodb.client.result.DeleteResult;
import io.telekom.orchest.scheduling.api.ScheduledTask;
import io.telekom.orchest.scheduling.api.TaskScheduleRequest;
import io.telekom.orchest.scheduling.api.TaskState;
import io.telekom.orchest.scheduling.spi.ScheduledTaskStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * MongoDB-backed reference implementation of {@link ScheduledTaskStore}.
 *
 * <p>Atomicity strategy — every mutating operation lands as a single {@code findAndModify} on the
 * {@code scheduledTasks} collection, which Mongo guarantees to be atomic on a single document. That
 * is sufficient for distributed safety because the entire task row is one document. We deliberately
 * avoid multi-document transactions so the store also works on standalone Mongo deployments (only a
 * replica set was previously required because of the TTL + change-stream design).
 *
 * <p>Claim semantics — {@link #claimDue(String, Instant, Duration, int)} loops {@code batchSize}
 * times, each iteration calling {@code findAndModify} with the predicate {@code (state == PENDING
 * AND triggerAt <= now) OR (state == RUNNING AND leaseUntil < now)}, sorted by {@code triggerAt
 * ASC}. Because each iteration is its own atomic operation, two dispatchers polling concurrently
 * will simply walk past each other — the loser sees the row already flipped to {@code RUNNING} with
 * a fresh lease and skips it.
 *
 * <p>Optimistic concurrency — {@link #completeAndDelete(String, long)}, {@link #fail(String, long,
 * Instant, String)} and {@link #markDead(String, long, String)} each include {@code version ==
 * expected} in their query. If the lease was stolen by another dispatcher (because the original
 * worker stalled past {@code leaseUntil}), the version will have advanced and the operation becomes
 * a no-op — matching the SPI contract.
 */
@Slf4j
@Repository
public class MongoScheduledTaskStore implements ScheduledTaskStore {

  private final MongoOperations mongoOperations;
  private final Clock clock;

  /**
   * Creates a new MongoScheduledTaskStore.
   *
   * @param mongoOperations the MongoOperations for database access
   * @param clock the clock used for timestamp generation
   */
  public MongoScheduledTaskStore(MongoOperations mongoOperations, Clock clock) {
    this.mongoOperations = mongoOperations;
    this.clock = clock;
  }

  @Override
  public ScheduledTask insert(TaskScheduleRequest request) {
    Instant now = Instant.now(clock);
    ScheduledTaskDocument doc =
        ScheduledTaskDocument.builder()
            .type(request.getType())
            .businessKey(request.getBusinessKey())
            .payload(request.getPayload())
            .triggerAt(request.getTriggerAt())
            .state(TaskState.PENDING)
            .attempts(0)
            .maxAttempts(request.getMaxAttempts())
            .createdAt(now)
            .updatedAt(now)
            .build();
    return toDomain(mongoOperations.insert(doc));
  }

  @Override
  public ScheduledTask upsertByBusinessKey(TaskScheduleRequest request) {
    if (request.getBusinessKey() == null) {
      return insert(request);
    }
    Instant now = Instant.now(clock);
    Query query =
        Query.query(
            Criteria.where("type")
                .is(request.getType())
                .and("businessKey")
                .is(request.getBusinessKey()));
    Update update =
        new Update()
            .set("type", request.getType())
            .set("businessKey", request.getBusinessKey())
            .set("payload", request.getPayload())
            .set("triggerAt", request.getTriggerAt())
            .set("state", TaskState.PENDING)
            .set("attempts", 0)
            .set("maxAttempts", request.getMaxAttempts())
            .set("ownerId", null)
            .set("leaseUntil", null)
            .set("lastError", null)
            .set("updatedAt", now)
            .setOnInsert("createdAt", now);
    FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(true);
    ScheduledTaskDocument saved =
        mongoOperations.findAndModify(query, update, options, ScheduledTaskDocument.class);
    return toDomain(saved);
  }

  @Override
  public List<ScheduledTask> claimDue(
      String ownerId, Instant now, Duration leaseDuration, int batchSize) {
    if (batchSize <= 0) {
      return List.of();
    }
    Instant leaseUntil = now.plus(leaseDuration);
    List<ScheduledTask> claimed = new ArrayList<>(batchSize);

    for (int i = 0; i < batchSize; i++) {
      // Two predicates in OR — fresh PENDING work, plus stale RUNNING leases that need recovery.
      Criteria duePending = Criteria.where("state").is(TaskState.PENDING).and("triggerAt").lte(now);
      Criteria staleRunning =
          Criteria.where("state").is(TaskState.RUNNING).and("leaseUntil").lt(now);
      Query query =
          Query.query(new Criteria().orOperator(duePending, staleRunning))
              .with(
                  org.springframework.data.domain.Sort.by(
                      org.springframework.data.domain.Sort.Direction.ASC, "triggerAt"));

      Update update =
          new Update()
              .set("state", TaskState.RUNNING)
              .set("ownerId", ownerId)
              .set("leaseUntil", leaseUntil)
              .set("updatedAt", now)
              .inc("attempts", 1);

      ScheduledTaskDocument leased =
          mongoOperations.findAndModify(
              query,
              update,
              FindAndModifyOptions.options().returnNew(true),
              ScheduledTaskDocument.class);
      if (leased == null) {
        break; // nothing else due in this cycle
      }
      claimed.add(toDomain(leased));
    }
    return claimed;
  }

  @Override
  public void completeAndDelete(String id, long expectedVersion) {
    Query query = Query.query(Criteria.where("_id").is(id).and("version").is(expectedVersion));
    DeleteResult result = mongoOperations.remove(query, ScheduledTaskDocument.class);
    if (result.getDeletedCount() == 0) {
      // Either the lease was stolen (version moved on) or the row was already removed.
      log.debug(
          "completeAndDelete no-op for id={} expectedVersion={} (lease likely stolen)",
          id,
          expectedVersion);
    }
  }

  @Override
  public void fail(String id, long expectedVersion, Instant nextTriggerAt, String lastError) {
    Query query = Query.query(Criteria.where("_id").is(id).and("version").is(expectedVersion));
    Update update =
        new Update()
            .set("state", TaskState.PENDING)
            .set("triggerAt", nextTriggerAt)
            .set("ownerId", null)
            .set("leaseUntil", null)
            .set("lastError", lastError)
            .set("updatedAt", Instant.now(clock));
    ScheduledTaskDocument updated =
        mongoOperations.findAndModify(
            query,
            update,
            FindAndModifyOptions.options().returnNew(true),
            ScheduledTaskDocument.class);
    if (updated == null) {
      log.debug(
          "fail() no-op for id={} expectedVersion={} (lease likely stolen)", id, expectedVersion);
    }
  }

  @Override
  public void markDead(String id, long expectedVersion, String lastError) {
    Query query = Query.query(Criteria.where("_id").is(id).and("version").is(expectedVersion));
    Update update =
        new Update()
            .set("state", TaskState.DEAD)
            .set("ownerId", null)
            .set("leaseUntil", null)
            .set("lastError", lastError)
            .set("updatedAt", Instant.now(clock));
    ScheduledTaskDocument updated =
        mongoOperations.findAndModify(
            query,
            update,
            FindAndModifyOptions.options().returnNew(true),
            ScheduledTaskDocument.class);
    if (updated == null) {
      log.debug(
          "markDead() no-op for id={} expectedVersion={} (lease likely stolen)",
          id,
          expectedVersion);
    }
  }

  @Override
  public void deleteByBusinessKey(String type, String businessKey) {
    if (type == null || businessKey == null) {
      return;
    }
    Query query = Query.query(Criteria.where("type").is(type).and("businessKey").is(businessKey));
    mongoOperations.remove(query, ScheduledTaskDocument.class);
  }

  @Override
  public void deleteById(String id) {
    if (id == null) {
      return;
    }
    Query query = Query.query(Criteria.where("_id").is(id));
    mongoOperations.remove(query, ScheduledTaskDocument.class);
  }

  @Override
  public Optional<ScheduledTask> findById(String id) {
    return Optional.ofNullable(mongoOperations.findById(id, ScheduledTaskDocument.class))
        .map(this::toDomain);
  }

  @Override
  public Optional<ScheduledTask> findByBusinessKey(String type, String businessKey) {
    Query query = Query.query(Criteria.where("type").is(type).and("businessKey").is(businessKey));
    return Optional.ofNullable(mongoOperations.findOne(query, ScheduledTaskDocument.class))
        .map(this::toDomain);
  }

  private ScheduledTask toDomain(ScheduledTaskDocument doc) {
    if (doc == null) {
      return null;
    }
    return ScheduledTask.builder()
        .id(doc.getId())
        .type(doc.getType())
        .businessKey(doc.getBusinessKey())
        .payload(doc.getPayload())
        .triggerAt(doc.getTriggerAt())
        .state(doc.getState())
        .attempts(doc.getAttempts())
        .maxAttempts(doc.getMaxAttempts())
        .ownerId(doc.getOwnerId())
        .leaseUntil(doc.getLeaseUntil())
        .lastError(doc.getLastError())
        .version(doc.getVersion() == null ? 0L : doc.getVersion())
        .createdAt(doc.getCreatedAt())
        .updatedAt(doc.getUpdatedAt())
        .build();
  }
}
