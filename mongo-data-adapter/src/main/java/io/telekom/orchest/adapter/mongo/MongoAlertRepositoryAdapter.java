package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.AlertMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoTelemetryAlertRepository;
import io.telekom.orchest.api.core.adapters.data.model.Alert;
import io.telekom.orchest.api.core.adapters.data.model.AlertState;
import io.telekom.orchest.api.core.adapters.data.repository.AlertRepository;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * MongoDB implementation of the {@link AlertRepository}. Delegates to Spring Data MongoDB and
 * MongoTemplate for alert persistence, querying, and aggregation.
 */
@Component
@RequiredArgsConstructor
public class MongoAlertRepositoryAdapter implements AlertRepository {

  private final MongoTelemetryAlertRepository repository;
  private final AlertMapper mapper;
  private final MongoTemplate mongoTemplate;

  /**
   * Persists an alert to the database.
   *
   * @param alert the alert to save
   * @return the persisted alert
   */
  @Override
  public Alert save(Alert alert) {
    return mapper.toDomain(repository.save(mapper.toDocument(alert)));
  }

  /**
   * Finds an alert by its unique identifier.
   *
   * @param id the alert identifier
   * @return the alert if found, or empty
   */
  @Override
  public Optional<Alert> findById(String id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  /**
   * Finds an alert by its fingerprint.
   *
   * @param fingerprint the alert fingerprint
   * @return the alert if found, or empty
   */
  @Override
  public Optional<Alert> findByFingerprint(String fingerprint) {
    return repository.findByFingerprint(fingerprint).map(mapper::toDomain);
  }

  /**
   * Retrieves all alerts from the database.
   *
   * @return list of all alerts
   */
  @Override
  public List<Alert> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  /**
   * Finds all alerts matching the given state.
   *
   * @param state the alert state to filter by
   * @return list of matching alerts
   */
  @Override
  public List<Alert> findByState(AlertState state) {
    return repository.findAllByState(state).stream().map(mapper::toDomain).toList();
  }

  /**
   * Finds alerts matching any of the given states with pagination, sorted by creation date
   * descending.
   *
   * @param states the collection of alert states to filter by
   * @param page the zero-based page index
   * @param size the page size
   * @return a paginated result of matching alerts
   */
  @Override
  public AlertPage findByStates(Collection<AlertState> states, int page, int size) {
    Query query = new Query();
    query.addCriteria(Criteria.where("state").in(states));
    query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
    long total = mongoTemplate.count(query, io.telekom.orchest.adapter.mongo.model.Alert.class);
    query.with(PageRequest.of(page, size));
    List<Alert> content =
        mongoTemplate.find(query, io.telekom.orchest.adapter.mongo.model.Alert.class).stream()
            .map(mapper::toDomain)
            .toList();
    int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
    return new AlertPage(content, total, totalPages, page, size);
  }

  /**
   * Finds alerts with optional filtering by state, process definition, and creation date range.
   *
   * @param state the alert state filter, or null to skip
   * @param processDefinitionId the process definition filter, or null to skip
   * @param createdFrom the earliest creation time (inclusive), or null to skip
   * @param createdTo the latest creation time (inclusive), or null to skip
   * @param page the zero-based page index
   * @param size the page size
   * @param sort the sort specification (e.g. "+createdAt" or "-count")
   * @return a paginated result of matching alerts
   */
  @Override
  public AlertPage findAll(
      AlertState state,
      String processDefinitionId,
      Instant createdFrom,
      Instant createdTo,
      int page,
      int size,
      String sort) {
    Query query = new Query();
    if (state != null) {
      query.addCriteria(Criteria.where("state").is(state));
    }
    if (processDefinitionId != null) {
      query.addCriteria(Criteria.where("metadata.processDefinitionId").is(processDefinitionId));
    }
    if (createdFrom != null || createdTo != null) {
      Criteria c = Criteria.where("createdAt");
      if (createdFrom != null) c = c.gte(createdFrom);
      if (createdTo != null) c = c.lte(createdTo);
      query.addCriteria(c);
    }
    query.with(parseSort(sort));

    long total = mongoTemplate.count(query, io.telekom.orchest.adapter.mongo.model.Alert.class);
    query.with(PageRequest.of(page, size));
    List<Alert> content =
        mongoTemplate.find(query, io.telekom.orchest.adapter.mongo.model.Alert.class).stream()
            .map(mapper::toDomain)
            .toList();

    int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
    return new AlertPage(content, total, totalPages, page, size);
  }

  /**
   * Aggregates firing alert statistics grouped by process definition ID and version.
   *
   * @return list of alert statistics sorted by total count descending
   */
  @Override
  public List<AlertStats> firingStats() {
    Aggregation agg =
        Aggregation.newAggregation(
            Aggregation.match(Criteria.where("state").is(AlertState.FIRING)),
            Aggregation.group("metadata.processDefinitionId", "metadata.version")
                .sum("count")
                .as("totalCount"),
            Aggregation.project("totalCount")
                .and("_id.processDefinitionId")
                .as("processDefinitionId")
                .and("_id.version")
                .as("version"),
            Aggregation.sort(Sort.Direction.DESC, "totalCount"));
    AggregationResults<Document> results =
        mongoTemplate.aggregate(
            agg, io.telekom.orchest.adapter.mongo.model.Alert.class, Document.class);
    return results.getMappedResults().stream()
        .map(
            doc ->
                new AlertStats(
                    doc.getString("processDefinitionId"),
                    doc.getString("version"),
                    doc.get("totalCount") instanceof Number n ? n.longValue() : 0L))
        .toList();
  }

  private static final java.util.Set<String> SORTABLE_FIELDS =
      java.util.Set.of("count", "createdAt", "updatedAt", "severity", "state");

  private static Sort parseSort(String sort) {
    if (sort == null || sort.isBlank()) {
      return Sort.by(Sort.Direction.DESC, "count");
    }
    Sort.Direction dir = sort.startsWith("+") ? Sort.Direction.ASC : Sort.Direction.DESC;
    String field = sort.startsWith("+") || sort.startsWith("-") ? sort.substring(1) : sort;
    if (!SORTABLE_FIELDS.contains(field)) {
      return Sort.by(Sort.Direction.DESC, "count");
    }
    return Sort.by(dir, field);
  }
}
