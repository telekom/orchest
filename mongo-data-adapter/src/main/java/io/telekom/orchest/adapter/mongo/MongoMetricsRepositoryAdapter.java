package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.model.MetricLifetimeTotalDocument;
import io.telekom.orchest.adapter.mongo.model.MetricTimeBucket;
import io.telekom.orchest.adapter.mongo.repository.MongoMetricLifetimeTotalRepository;
import io.telekom.orchest.adapter.mongo.repository.MongoMetricTimeBucketRepository;
import io.telekom.orchest.telemetry.metrics.MetricGranularity;
import io.telekom.orchest.telemetry.metrics.MetricIncrement;
import io.telekom.orchest.telemetry.metrics.MetricKey;
import io.telekom.orchest.telemetry.metrics.MetricLifetimeTotal;
import io.telekom.orchest.telemetry.metrics.MetricType;
import io.telekom.orchest.telemetry.metrics.MetricsRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * MongoDB-backed implementation of {@link MetricsRepository}.
 *
 * <h2>Why this is distributed-safe</h2>
 *
 * Every write is a single per-document {@code update({_id: stableId}, {$inc: {count: n}}, upsert:
 * true)}. MongoDB guarantees per-document atomicity for {@code update}, so two engine pods
 * incrementing the same document by 5 and 3 respectively land at exactly +8 — there is no need for
 * distributed locking, leader election, or compensation logic.
 *
 * <h2>Why {@link BulkOperations.BulkMode#UNORDERED}</h2>
 *
 * The flush sends a batch of N increments touching N distinct keys; we don't need ordering between
 * them and ordered execution would serialize them at the broker. Unordered batches let MongoDB
 * process the writes in parallel.
 *
 * <h2>Why {@code $setOnInsert} for timestamps</h2>
 *
 * On the first ever increment for a key the document doesn't exist yet, so {@code firstObservedAt}
 * / {@code bucketStart} are set via {@code $setOnInsert}. Subsequent flushes touch only the {@code
 * count} and {@code lastUpdatedAt} fields.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoMetricsRepositoryAdapter implements MetricsRepository {

  private static final String SEP = "::";
  private static final String NULL_TOKEN = "_";

  private final MongoOperations mongoOperations;
  private final MongoMetricLifetimeTotalRepository lifetimeRepository;
  private final MongoMetricTimeBucketRepository timeBucketRepository;

  /**
   * Returns the storage granularity used by this MongoDB implementation.
   *
   * @return {@link MetricGranularity#MINUTE}
   */
  @Override
  public MetricGranularity storageGranularity() {
    return MetricGranularity.MINUTE;
  }

  /**
   * Atomically applies a batch of metric increments using unordered bulk upserts.
   *
   * @param increments the metric increments to flush to MongoDB
   */
  @Override
  public void applyIncrements(Collection<MetricIncrement> increments) {
    if (increments == null || increments.isEmpty()) {
      return;
    }

    BulkOperations lifetimeOps =
        mongoOperations.bulkOps(
            BulkOperations.BulkMode.UNORDERED, MetricLifetimeTotalDocument.class);
    BulkOperations bucketOps =
        mongoOperations.bulkOps(BulkOperations.BulkMode.UNORDERED, MetricTimeBucket.class);

    for (MetricIncrement inc : increments) {
      MetricKey key = inc.key();
      long delta = inc.delta();
      Instant flushAt = inc.flushAt();
      Instant bucketStart = MetricGranularity.MINUTE.bucketStart(flushAt);

      String lifetimeId = key.stableId();
      Query lifetimeQuery = Query.query(Criteria.where("_id").is(lifetimeId));
      Update lifetimeUpdate =
          new Update()
              .inc("count", delta)
              .setOnInsert("metricType", key.type())
              .setOnInsert("definitionId", key.definitionId())
              .setOnInsert("version", key.version())
              .setOnInsert("firstObservedAt", flushAt)
              .set("lastUpdatedAt", flushAt);
      lifetimeOps.upsert(lifetimeQuery, lifetimeUpdate);

      String bucketId = bucketDocId(key, bucketStart);
      Query bucketQuery = Query.query(Criteria.where("_id").is(bucketId));
      Update bucketUpdate =
          new Update()
              .inc("count", delta)
              .setOnInsert("metricType", key.type())
              .setOnInsert("definitionId", key.definitionId())
              .setOnInsert("version", key.version())
              .setOnInsert("bucketStart", bucketStart)
              .set("lastUpdatedAt", flushAt);
      bucketOps.upsert(bucketQuery, bucketUpdate);
    }

    // Two round-trips total (one per collection) regardless of N. The Mongo driver
    // pipelines them; latency is ~one network hop * 2.
    lifetimeOps.execute();
    bucketOps.execute();

    log.debug("flushed {} metric increments to mongo", increments.size());
  }

  /**
   * Finds lifetime total metrics, optionally filtered by type and/or definition ID.
   *
   * @param type the metric type filter, or null for all types
   * @param definitionId the definition ID filter, or null for all definitions
   * @return list of matching lifetime totals
   */
  @Override
  public List<MetricLifetimeTotal> findLifetimeTotals(MetricType type, String definitionId) {
    List<MetricLifetimeTotalDocument> docs;
    if (type == null && definitionId == null) {
      docs = lifetimeRepository.findAll();
    } else if (type == null) {
      docs = lifetimeRepository.findAllByDefinitionId(definitionId);
    } else if (definitionId == null) {
      docs = lifetimeRepository.findAllByMetricType(type);
    } else {
      docs = lifetimeRepository.findAllByMetricTypeAndDefinitionId(type, definitionId);
    }
    List<MetricLifetimeTotal> result = new ArrayList<>(docs.size());
    for (MetricLifetimeTotalDocument doc : docs) {
      result.add(toDomain(doc));
    }
    return result;
  }

  /**
   * Finds the lifetime total for a specific metric key.
   *
   * @param key the metric key to look up
   * @return the lifetime total, or empty if not yet recorded
   */
  @Override
  public Optional<MetricLifetimeTotal> findLifetimeTotal(MetricKey key) {
    return lifetimeRepository.findById(key.stableId()).map(this::toDomain);
  }

  /**
   * Finds time-bucketed metrics within a time range, optionally filtered by definition ID.
   *
   * @param type the metric type to query
   * @param definitionId the definition ID filter, or null for all definitions
   * @param from the inclusive start of the time range
   * @param to the exclusive end of the time range
   * @return list of matching time buckets ordered by bucket start ascending
   */
  @Override
  public List<io.telekom.orchest.telemetry.metrics.MetricTimeBucket> findTimeBuckets(
      MetricType type, String definitionId, Instant from, Instant to) {
    List<MetricTimeBucket> docs =
        (definitionId == null)
            ? timeBucketRepository
                .findAllByMetricTypeAndBucketStartGreaterThanEqualAndBucketStartLessThanOrderByBucketStartAsc(
                    type, from, to)
            : timeBucketRepository
                .findAllByMetricTypeAndDefinitionIdAndBucketStartGreaterThanEqualAndBucketStartLessThanOrderByBucketStartAsc(
                    type, definitionId, from, to);

    List<io.telekom.orchest.telemetry.metrics.MetricTimeBucket> result =
        new ArrayList<>(docs.size());
    for (MetricTimeBucket doc : docs) {
      result.add(toDomain(doc));
    }
    return result;
  }

  private MetricLifetimeTotal toDomain(MetricLifetimeTotalDocument doc) {
    return new MetricLifetimeTotal(
        new MetricKey(doc.getMetricType(), doc.getDefinitionId(), doc.getVersion()),
        doc.getCount(),
        doc.getFirstObservedAt(),
        doc.getLastUpdatedAt());
  }

  private io.telekom.orchest.telemetry.metrics.MetricTimeBucket toDomain(MetricTimeBucket doc) {
    return new io.telekom.orchest.telemetry.metrics.MetricTimeBucket(
        new MetricKey(doc.getMetricType(), doc.getDefinitionId(), doc.getVersion()),
        doc.getBucketStart(),
        doc.getCount(),
        doc.getLastUpdatedAt());
  }

  private static String bucketDocId(MetricKey key, Instant bucketStart) {
    return key.type().name()
        + SEP
        + (key.definitionId() == null ? NULL_TOKEN : key.definitionId())
        + SEP
        + (key.version() == null ? NULL_TOKEN : key.version())
        + SEP
        + bucketStart.toEpochMilli();
  }
}
