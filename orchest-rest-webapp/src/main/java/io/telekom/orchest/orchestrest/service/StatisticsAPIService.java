package io.telekom.orchest.orchestrest.service;

import io.telekom.orchest.adapter.mongo.model.DecisionInstance;
import io.telekom.orchest.adapter.mongo.model.ProcessInstance;
import io.telekom.orchest.orchestrest.api.dto.StatsDTO;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

/** Service for computing and caching aggregated process/decision instance statistics. */
@Slf4j
@Component
public class StatisticsAPIService {

  private final StatsDTO statsDTO;
  private final MongoTemplate mongoTemplate;

  public record ProcessStatRecord(
      String definitionId, String processDefinitionId, String version, String state, int count) {}

  public StatisticsAPIService(
      StatsDTO statsDTO,
      @Qualifier("analyticsMongoTemplate") MongoTemplate analyticsMongoTemplate) {
    this.statsDTO = statsDTO;
    this.mongoTemplate = analyticsMongoTemplate;
  }

  private static final AggregationOptions PI_STATE_INDEX_HINT =
      AggregationOptions.builder().hint("state_1_createdAt_-1").build();
  private static final AggregationOptions DI_STATE_INDEX_HINT =
      AggregationOptions.builder().hint("state_1_executedAt_1").build();

  // $sort before $group with $sum is wasted work — removed. Index on (state, key, version) is
  // enough.
  public static Function<String, Aggregation> instanceCountAggregationFunction =
      key ->
          Aggregation.newAggregation(
              Aggregation.group("state", key, "version").count().as("count"),
              Aggregation.project("count")
                  .and("_id.state")
                  .as("state")
                  .and("_id." + key)
                  .as(key)
                  .and("_id.version")
                  .as("version")
                  .andExclude("_id"));

  public static Aggregation totalCountAggregation =
      Aggregation.newAggregation(
          Aggregation.group("state").count().as("count"),
          Aggregation.project("count").and("_id").as("state").andExclude("_id"),
          Aggregation.sort(Sort.by(Sort.Order.desc("count"))));

  public static Function<String, Aggregation> piInstanceCountAggregationFunction =
      instanceCountAggregationFunction.andThen(
          aggregation -> aggregation.withOptions(PI_STATE_INDEX_HINT));
  public static Function<String, Aggregation> diInstanceCountAggregationFunction =
      instanceCountAggregationFunction.andThen(
          aggregation -> aggregation.withOptions(DI_STATE_INDEX_HINT));
  public static Aggregation piTotalCountAggregation =
      totalCountAggregation.withOptions(PI_STATE_INDEX_HINT);
  public static Aggregation diTotalCountAggregation =
      totalCountAggregation.withOptions(DI_STATE_INDEX_HINT);

  public StatsDTO getStats(boolean hardReload) {
    return getStats(hardReload, null, null, ZoneOffset.UTC);
  }

  /**
   * Returns aggregated stats. When either {@code from} or {@code to} is supplied (or {@code
   * hardReload} is true), the cache is bypassed and counts are computed on demand restricted to
   * {@code ProcessInstance.createdAt} / {@code DecisionInstance.executedAt} within the half-open
   * range {@code [from, to)} (open on the missing side). The {@code zone} controls the offset used
   * to render {@code lastUpdatedAt} on the response.
   */
  public StatsDTO getStats(
      boolean hardReload, OffsetDateTime from, OffsetDateTime to, ZoneId zone) {
    ZoneId effectiveZone = zone != null ? zone : ZoneOffset.UTC;
    boolean rangeRequested = from != null || to != null;
    if (hardReload || rangeRequested) {
      return reloadStats(from, to, effectiveZone);
    }
    StatsDTO cached = new StatsDTO();
    cached.copy(statsDTO);
    if (cached.getLastUpdatedAt() != null) {
      cached.setLastUpdatedAt(
          cached.getLastUpdatedAt().atZoneSameInstant(effectiveZone).toOffsetDateTime());
    }
    return cached;
  }

  public StatsDTO reloadStats() {
    // All-time counts: no date filter so the aggregations run as covered index
    // scans over the whole collection (see {state, def, version} indexes).
    return reloadStats(null, null, ZoneOffset.UTC);
  }

  public StatsDTO reloadStats(OffsetDateTime from, OffsetDateTime to, ZoneId zone) {
    log.debug("Reloading stats from {} to {}", from, to);
    Aggregation processTotal = totalCountAggregationFor("createdAt", from, to, true);
    Aggregation decisionTotal = totalCountAggregationFor("executedAt", from, to, false);
    Aggregation processByDef =
        instanceCountAggregationFor("processDefinitionId", "createdAt", from, to, true)
            .withOptions(
                AggregationOptions.builder()
                    .hint("state_1_processDefinitionId_1_version_1")
                    .build());
    Aggregation decisionByDef =
        instanceCountAggregationFor("definitionId", "executedAt", from, to, true)
            .withOptions(
                AggregationOptions.builder().hint("state_1_definitionId_1_version_1").build());

    AggregationResults<StatsDTO.Stats> processInstanceStats =
        mongoTemplate.aggregate(processTotal, ProcessInstance.class, StatsDTO.Stats.class);
    AggregationResults<StatsDTO.Stats> decisionInstanceStats =
        mongoTemplate.aggregate(decisionTotal, DecisionInstance.class, StatsDTO.Stats.class);
    AggregationResults<ProcessStatRecord> processInstanceStatsByStateAndVersion =
        mongoTemplate.aggregate(processByDef, ProcessInstance.class, ProcessStatRecord.class);
    AggregationResults<ProcessStatRecord> decisionStatsByStateAndVersion =
        mongoTemplate.aggregate(decisionByDef, DecisionInstance.class, ProcessStatRecord.class);

    Map<String, Map<String, List<StatsDTO.Stats>>> processStats =
        mapProcessStats(
            processInstanceStatsByStateAndVersion.getMappedResults(), ProcessInstance.class);
    Map<String, Map<String, List<StatsDTO.Stats>>> decisionStats =
        mapProcessStats(decisionStatsByStateAndVersion.getMappedResults(), DecisionInstance.class);
    ZoneId effectiveZone = zone != null ? zone : ZoneOffset.UTC;
    log.debug("Reloaded stats from {} to {}", from, to);
    return new StatsDTO(
        processInstanceStats.getMappedResults(),
        decisionInstanceStats.getMappedResults(),
        processStats,
        decisionStats,
        OffsetDateTime.now(effectiveZone));
  }

  private static Aggregation totalCountAggregationFor(
      String dateField, OffsetDateTime from, OffsetDateTime to, boolean iSPIAggregation) {
    if (from == null && to == null) {
      return iSPIAggregation ? piTotalCountAggregation : diTotalCountAggregation;
    }
    List<org.springframework.data.mongodb.core.aggregation.AggregationOperation> ops =
        new ArrayList<>();
    ops.add(Aggregation.match(rangeCriteria(dateField, from, to)));
    ops.add(Aggregation.group("state").count().as("count"));
    ops.add(Aggregation.project("count").and("_id").as("state").andExclude("_id"));
    ops.add(Aggregation.sort(Sort.by(Sort.Order.desc("count"))));

    return iSPIAggregation
        ? Aggregation.newAggregation(ops).withOptions(PI_STATE_INDEX_HINT)
        : Aggregation.newAggregation(ops).withOptions(DI_STATE_INDEX_HINT);
  }

  private static Aggregation instanceCountAggregationFor(
      String key,
      String dateField,
      OffsetDateTime from,
      OffsetDateTime to,
      boolean iSPIAggregation) {
    if (from == null && to == null) {
      return iSPIAggregation
          ? piInstanceCountAggregationFunction.apply(key)
          : diInstanceCountAggregationFunction.apply(key);
    }
    List<org.springframework.data.mongodb.core.aggregation.AggregationOperation> ops =
        new ArrayList<>();
    ops.add(Aggregation.match(rangeCriteria(dateField, from, to)));
    ops.add(Aggregation.group("state", key, "version").count().as("count"));
    ops.add(
        Aggregation.project("count")
            .and("_id.state")
            .as("state")
            .and("_id." + key)
            .as(key)
            .and("_id.version")
            .as("version")
            .andExclude("_id"));
    return Aggregation.newAggregation(ops);
  }

  private static Criteria rangeCriteria(String field, OffsetDateTime from, OffsetDateTime to) {
    Criteria criteria = Criteria.where(field);
    if (from != null) {
      criteria = criteria.gte(from.toInstant());
    }
    if (to != null) {
      criteria = criteria.lt(to.toInstant());
    }
    return criteria;
  }

  public static Map<String, Map<String, List<StatsDTO.Stats>>> mapProcessStats(
      List<ProcessStatRecord> records, Class<?> clazz) {
    return records.stream()
        .filter(
            o -> {
              String key =
                  (clazz == ProcessInstance.class) ? o.processDefinitionId() : o.definitionId();
              return key != null && o.version() != null;
            })
        .collect(
            Collectors.groupingBy(
                o -> clazz == ProcessInstance.class ? o.processDefinitionId() : o.definitionId(),
                Collectors.groupingBy(
                    ProcessStatRecord::version,
                    Collectors.mapping(
                        record -> new StatsDTO.Stats(record.state(), record.count()),
                        Collectors.toList()))));
  }
}
