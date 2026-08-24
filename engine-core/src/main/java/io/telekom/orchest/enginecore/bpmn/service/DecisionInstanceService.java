package io.telekom.orchest.enginecore.bpmn.service;

import io.telekom.orchest.api.core.adapters.data.model.DecisionInstance;
import io.telekom.orchest.api.core.adapters.data.repository.DecisionInstanceRepository;
import io.telekom.orchest.telemetry.metrics.MetricKey;
import io.telekom.orchest.telemetry.metrics.MetricType;
import io.telekom.orchest.telemetry.metrics.MetricsRecorder;
import io.telekom.orchest.telemetry.metrics.NoOpMetricsRecorder;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/** Manages DMN decision evaluation instances: persistence, retrieval, and metrics recording. */
@Slf4j
public class DecisionInstanceService {

  private final DecisionInstanceRepository decisionInstanceRepository;
  private final MetricsRecorder metricsRecorder;

  public DecisionInstanceService(
      DecisionInstanceRepository decisionInstanceRepository, MetricsRecorder metricsRecorder) {
    this.decisionInstanceRepository = decisionInstanceRepository;
    this.metricsRecorder = metricsRecorder == null ? NoOpMetricsRecorder.INSTANCE : metricsRecorder;
  }

  /**
   * Returns all decision instances.
   *
   * @return list of all decision instances
   */
  public List<DecisionInstance> getAll() {
    return decisionInstanceRepository.getDecisionInstances();
  }

  /**
   * Retrieves a decision instance by its ID.
   *
   * @param decisionInstanceId the decision instance ID
   * @return the decision instance, or empty if not found
   */
  public Optional<DecisionInstance> getDecisionById(String decisionInstanceId) {
    return decisionInstanceRepository.getById(decisionInstanceId);
  }

  /**
   * Persists a decision instance.
   *
   * @param decisionInstance the decision instance to save
   * @return the saved decision instance
   */
  public DecisionInstance save(DecisionInstance decisionInstance) {
    return decisionInstanceRepository.save(decisionInstance);
  }

  /**
   * Records a single DMN evaluation in the persistent metrics pipeline.
   *
   * <p>Kept as an explicit method (rather than hooking inside {@link #save(DecisionInstance)}) so
   * that future state-update saves on a {@code DecisionInstance} don't accidentally over-count
   * evaluations.
   */
  public void recordEvaluation(DecisionInstance decisionInstance) {
    if (decisionInstance == null) {
      return;
    }
    metricsRecorder.increment(
        MetricKey.of(
            MetricType.DECISION_INSTANCE_EVALUATED,
            decisionInstance.getDefinitionId(),
            decisionInstance.getVersion()));
  }
}
