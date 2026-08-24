package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.MetricLifetimeTotalDocument;
import io.telekom.orchest.telemetry.metrics.MetricType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data Mongo handle for the {@code metric_lifetime_totals} collection. The write path does
 * not go through this interface — flushes use raw {@code BulkOperations} with {@code $inc} for
 * atomicity. This interface only serves the read API.
 */
public interface MongoMetricLifetimeTotalRepository
    extends MongoRepository<MetricLifetimeTotalDocument, String> {

  /**
   * Finds all lifetime total documents for a given metric type.
   *
   * @param metricType the metric type to filter by
   * @return list of matching lifetime totals
   */
  List<MetricLifetimeTotalDocument> findAllByMetricType(MetricType metricType);

  /**
   * Finds lifetime totals for a given metric type scoped to a specific definition.
   *
   * @param metricType the metric type to filter by
   * @param definitionId the process or decision definition identifier
   * @return list of matching lifetime totals
   */
  List<MetricLifetimeTotalDocument> findAllByMetricTypeAndDefinitionId(
      MetricType metricType, String definitionId);

  /**
   * Finds all lifetime total documents for a given definition regardless of metric type.
   *
   * @param definitionId the process or decision definition identifier
   * @return list of matching lifetime totals
   */
  List<MetricLifetimeTotalDocument> findAllByDefinitionId(String definitionId);

  /**
   * Finds a lifetime total document by its unique ID.
   *
   * @param id the document identifier
   * @return the matching document, or empty if not found
   */
  Optional<MetricLifetimeTotalDocument> findById(String id);
}
