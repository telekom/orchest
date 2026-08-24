package io.telekom.orchest.runners;

import com.mongodb.client.result.DeleteResult;
import io.telekom.orchest.adapter.mongo.model.DecisionInstance;
import io.telekom.orchest.adapter.mongo.repository.MongoDecisionInstanceRepository;
import io.telekom.orchest.adapter.mongo.repository.MongoProcessInstanceRepository;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.PIState;
import io.telekom.orchest.api.core.model.dmn.DIState;
import io.telekom.orchest.config.HouseKeepingProperties;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * Purges completed process instances and decision instances older than the configured retention
 * interval. Operates in batches to avoid excessive memory usage.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HousekeepingRunner extends StartupTaskRunner {

  private static final int BATCH_SIZE = 5000;
  private static final List<PIState> TERMINAL_STATES =
      List.of(PIState.COMPLETED, PIState.CANCELLED, PIState.TERMINATED);

  private final HouseKeepingProperties houseKeepingProperties;
  private final MongoProcessInstanceRepository processInstanceRepository;
  private final MongoDecisionInstanceRepository decisionInstanceRepository;
  private final MongoTemplate mongoTemplate;

  /** {@inheritDoc} */
  @Override
  public void startTask() {
    runHousekeeping();
  }

  /** Runs the housekeeping job if enabled in configuration; no-op otherwise. */
  public void runHousekeeping() {
    if (!houseKeepingProperties.isEnabled()) {
      log.info("Purging not enabled");
      return;
    }
    purge();
  }

  /** Purges process and decision instances that exceed the retention period. */
  public void purge() {
    OffsetDateTime cutoff =
        OffsetDateTime.now(ZoneOffset.UTC).minus(houseKeepingProperties.getInterval());
    purgeProcessInstances(cutoff);
    purgeDecisionInstances(cutoff);
  }

  private void purgeProcessInstances(OffsetDateTime cutoff) {
    log.info("Purging process instances completed before {}", cutoff);

    Query query =
        new Query(Criteria.where("state").in(TERMINAL_STATES).and("completedAt").lt(cutoff))
            .with(Sort.by("completedAt"))
            .limit(BATCH_SIZE);
    query.fields().include("_id", "processInstanceId", "parentProcesActivity.processInstanceId");

    List<ProcessInstance> batch;
    while (!(batch = mongoTemplate.find(query, ProcessInstance.class)).isEmpty()) {
      List<String> parentInstances =
          batch.stream()
              .filter(pi -> pi.getParentProcesActivity() == null)
              .map(ProcessInstance::getProcessInstanceId)
              .toList();

      List<String> orphanIds =
          batch.stream()
              .filter(pi -> pi.getParentProcesActivity() != null)
              .map(ProcessInstance::getProcessInstanceId)
              .toList();

      if (!parentInstances.isEmpty()) {
        DeleteResult deletedParents =
            mongoTemplate.remove(
                new Query(Criteria.where("processInstanceId").in(parentInstances)),
                ProcessInstance.class);

        DeleteResult deletedChildren =
            mongoTemplate.remove(
                new Query(
                    Criteria.where("parentProcesActivity.processInstanceId").in(parentInstances)),
                ProcessInstance.class);

        log.info(
            "Purged {} parent and {} child process instances",
            deletedParents.getDeletedCount(),
            deletedChildren.getDeletedCount());
      }

      if (!orphanIds.isEmpty()) {
        DeleteResult deletedOrphans =
            mongoTemplate.remove(
                new Query(Criteria.where("processInstanceId").in(orphanIds)),
                ProcessInstance.class);
        log.info("Purged {} orphaned child process instances", deletedOrphans.getDeletedCount());
      }

      if (batch.size() < BATCH_SIZE) {
        break;
      }
    }
  }

  private void purgeDecisionInstances(OffsetDateTime cutoff) {
    log.info("Purging decision instances executed before {}", cutoff);
    int count = 0;
    int skip = 0;

    List<DecisionInstance> batch;
    while (!(batch = findDecisionInstanceBatch(cutoff, skip)).isEmpty()) {
      int skippedInBatch = 0;
      for (DecisionInstance candidate : batch) {
        if (processInstanceRepository
            .findByProcessInstanceId(candidate.getProcessInstanceId())
            .isEmpty()) {
          decisionInstanceRepository.delete(candidate);
          count++;
        } else {
          skippedInBatch++;
        }
      }
      skip += skippedInBatch;

      if (batch.size() < BATCH_SIZE) {
        break;
      }
    }
    log.info("Purged {} decision instances", count);
  }

  private List<DecisionInstance> findDecisionInstanceBatch(OffsetDateTime cutoff, int skip) {
    Query query =
        new Query(Criteria.where("state").is(DIState.EXECUTED).and("executedAt").lt(cutoff))
            .with(Sort.by("executedAt"))
            .skip(skip)
            .limit(BATCH_SIZE);
    query.fields().include("_id", "decisionInstanceId", "processInstanceId");
    return mongoTemplate.find(query, DecisionInstance.class);
  }
}
