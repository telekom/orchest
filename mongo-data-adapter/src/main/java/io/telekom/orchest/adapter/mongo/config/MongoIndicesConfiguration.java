package io.telekom.orchest.adapter.mongo.config;

import com.mongodb.MongoCommandException;
import io.telekom.orchest.adapter.mongo.model.Alert;
import io.telekom.orchest.adapter.mongo.model.Connector;
import io.telekom.orchest.adapter.mongo.model.DecisionDefinition;
import io.telekom.orchest.adapter.mongo.model.DecisionInstance;
import io.telekom.orchest.adapter.mongo.model.DynamicProcessDefinition;
import io.telekom.orchest.adapter.mongo.model.Incident;
import io.telekom.orchest.adapter.mongo.model.MessageEventStore;
import io.telekom.orchest.adapter.mongo.model.MetricLifetimeTotalDocument;
import io.telekom.orchest.adapter.mongo.model.MetricTimeBucket;
import io.telekom.orchest.adapter.mongo.model.PendingTask;
import io.telekom.orchest.adapter.mongo.model.ProcessDefinition;
import io.telekom.orchest.adapter.mongo.model.ProcessEnvVariables;
import io.telekom.orchest.adapter.mongo.model.ProcessInstance;
import io.telekom.orchest.adapter.mongo.model.ProcessState;
import io.telekom.orchest.adapter.mongo.model.SignalEvent;
import io.telekom.orchest.adapter.mongo.model.TimedEvent;
import io.telekom.orchest.adapter.mongo.model.UserApiToken;
import io.telekom.orchest.adapter.mongo.model.UserTaskInstance;
import io.telekom.orchest.adapter.mongo.scheduler.ScheduledTaskDocument;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;

/**
 * Creates MongoDB indexes for all OrchesT collections at application startup. Includes compound
 * indexes, unique constraints, and TTL indexes for metrics retention.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MongoIndicesConfiguration {

  private final MongoOperations mongoOperations;
  private final OrchestMongoProperties orchestMongoProperties;

  @Value("${orchest.metrics.bucket-retention-days:365}")
  private int bucketRetentionDays;

  @PostConstruct
  public void init() {
    if (!orchestMongoProperties.isAutoIndexCreation()) {
      log.info("index creation is disabled");
      return;
    }
    createConnectorIndexes();
    createDecisionDefinitionIndexes();
    createDecisionInstanceIndexes();
    createDynamicProcessDefinitionIndexes();
    createIncidentIndexes();
    createMetricLifetimeTotalIndexes();
    createMetricTimeBucketIndexes();
    createTelemetryAlertIndexes();
    createMessageEventStoreIndexes();
    createPendingTaskIndexes();
    createProcessDefinitionIndexes();
    createProcessEnvVariablesIndexes();
    createProcessInstanceIndexes();
    createProcessStateIndexes();
    createSignalEventIndexes();
    createTimedEventIndexes();
    createScheduledTaskIndexes();
    createUserApiTokenIndexes();
    createUserTaskInstanceIndexes();
    log.info("MongoDB index bootstrap finished");
  }

  // --- connector ---
  private void createConnectorIndexes() {
    safeCreate(
        mongoOperations.indexOps(Connector.class),
        new Index().on("connectorName", Sort.Direction.ASC));
  }

  private void createDecisionDefinitionIndexes() {
    IndexOperations ops = mongoOperations.indexOps(DecisionDefinition.class);
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("definitionId", -1).append("version", -1))
            .named("definitionId_-1_version_-1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("decisionIds", -1).append("version", -1))
            .named("decisionIds_-1_version_-1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("definitionId", 1).append("version", -1))
            .named("definitionId_1_version_-1"));
  }

  // --- decisionInstance ---
  // NOTE: standalone state:1 removed (redundant — covered by compound state_1_executedAt_1 prefix)
  private void createDecisionInstanceIndexes() {
    IndexOperations ops = mongoOperations.indexOps(DecisionInstance.class);
    safeCreate(ops, new Index().on("decisionId", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("processInstanceId", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("executedAt", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("matchedRuleId", Sort.Direction.DESC));
    safeCreate(ops, new Index().on("decisionInstanceId", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("definitionId", Sort.Direction.ASC));
    safeCreate(
        ops,
        new CompoundIndexDefinition(
                new Document("decisionId", -1).append("version", -1).append("executedAt", -1))
            .named("decisionId-1_version_-1_executedAt-1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(
                new Document("definitionId", -1).append("version", -1).append("executedAt", -1))
            .named("definitionId-1_version_-1_executedAt-1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("state", 1).append("executedAt", 1))
            .named("state_1_executedAt_1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(
                new Document("state", 1).append("definitionId", 1).append("version", 1))
            .named("state_1_definitionId_1_version_1"));
  }

  // --- dynamicProcessDefinition ---
  private void createDynamicProcessDefinitionIndexes() {
    IndexOperations ops = mongoOperations.indexOps(DynamicProcessDefinition.class);
    safeCreate(ops, new Index().on("definitionId", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("createdAt", Sort.Direction.ASC));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("processId", -1).append("version", -1))
            .named("processId-1_version_-1"));
  }

  // --- incident ---
  private void createIncidentIndexes() {
    IndexOperations ops = mongoOperations.indexOps(Incident.class);
    safeCreate(ops, new Index().on("processInstanceId", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("processDefinitionId", Sort.Direction.ASC));
  }

  // --- messageEventStore ---
  private void createMessageEventStoreIndexes() {
    IndexOperations ops = mongoOperations.indexOps(MessageEventStore.class);
    safeCreate(ops, new Index().on("createdAt", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("processInstanceId", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("linkedEventId", Sort.Direction.ASC));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("correlationKey", -1).append("messageName", -1))
            .named("correlationKey_-1_messageName_-1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(
                new Document("processDefinitionId", -1).append("isStartEvent", -1))
            .named("processDefinitionId_-1_isStartEvent_-1"));
  }

  // --- metricLifetimeTotalDocument ---
  private void createMetricLifetimeTotalIndexes() {
    IndexOperations ops = mongoOperations.indexOps(MetricLifetimeTotalDocument.class);
    safeCreate(ops, new Index().on("metricType", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("definitionId", Sort.Direction.ASC));
  }

  // --- metricTimeBucket ---
  private void createMetricTimeBucketIndexes() {
    IndexOperations ops = mongoOperations.indexOps(MetricTimeBucket.class);
    long retentionSeconds = TimeUnit.DAYS.toSeconds(bucketRetentionDays);
    ensureTtlIndex(
        ops,
        MetricTimeBucket.class,
        "metric_bucket_ttl",
        "bucketStart",
        Sort.Direction.ASC,
        retentionSeconds);
    safeCreate(
        ops,
        new CompoundIndexDefinition(
                new Document("metricType", 1).append("definitionId", 1).append("bucketStart", 1))
            .named("type_def_bucket_idx"));
  }

  // --- telemetry_alerts ---
  private void createTelemetryAlertIndexes() {
    IndexOperations ops = mongoOperations.indexOps(Alert.class);
    safeCreate(
        ops,
        new Index().on("fingerprint", Sort.Direction.ASC).unique().named("fingerprint_unique"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("state", 1).append("lastTriggeredAt", 1))
            .named("state_1_lastTriggeredAt_1"));
    // list API: filter by state + processDefinitionId + createdAt, sort by count
    safeCreate(
        ops,
        new CompoundIndexDefinition(
                new Document("state", 1)
                    .append("metadata.processDefinitionId", 1)
                    .append("createdAt", -1))
            .named("state_1_metaProcDefId_1_createdAt_-1"));
    // list API: sort/filter by count
    safeCreate(ops, new Index().on("count", Sort.Direction.DESC).named("count_-1"));
    // stats aggregation: state FIRING grouped by processDefinitionId + version
    safeCreate(
        ops,
        new CompoundIndexDefinition(
                new Document("state", 1)
                    .append("metadata.processDefinitionId", 1)
                    .append("metadata.version", 1))
            .named("state_1_metaProcDefId_1_metaVersion_1"));
  }

  // --- pendingTask ---
  private void createPendingTaskIndexes() {
    IndexOperations ops = mongoOperations.indexOps(PendingTask.class);
    safeCreate(ops, new Index().on("workerId", Sort.Direction.ASC));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("processDefinitionId", -1).append("workerId", -1))
            .named("processDefinitionId_-1_workerId_-1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("processInstanceId", -1).append("workerId", -1))
            .named("processInstanceId_-1_workerId_-1"));
  }

  // --- processDefinition ---
  // NOTE: standalone definitionId:1 removed (redundant — covered by compound
  // definitionId_1_version_-1 prefix)
  private void createProcessDefinitionIndexes() {
    IndexOperations ops = mongoOperations.indexOps(ProcessDefinition.class);
    safeCreate(ops, new Index().on("processId", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("createdAt", Sort.Direction.ASC));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("processId", -1).append("version", -1))
            .named("processId-1_version_-1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("definitionId", 1).append("version", -1))
            .named("definitionId_1_version_-1"));
  }

  // --- processEnvVariables ---
  private void createProcessEnvVariablesIndexes() {
    safeCreate(
        mongoOperations.indexOps(ProcessEnvVariables.class),
        new Index().on("processDefinitionId", Sort.Direction.ASC));
  }

  // --- processInstance ---
  // NOTE: standalone processDefinitionId:1, createdAt:1, state:1 removed (redundant — each is a
  // prefix of a compound)
  private void createProcessInstanceIndexes() {
    IndexOperations ops = mongoOperations.indexOps(ProcessInstance.class);
    safeCreate(ops, new Index().on("processInstanceId", Sort.Direction.ASC).unique());
    safeCreate(ops, new Index().on("correlationId", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("correlationIds", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("parentProcesActivity.processInstanceId", Sort.Direction.ASC));

    safeCreate(
        ops,
        new CompoundIndexDefinition(
                new Document("processDefinitionId", -1)
                    .append("version", -1)
                    .append("createdAt", -1))
            .named("processDefinitionId-1_version_-1_createdAt-1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(
                new Document("processDefinitionId", -1)
                    .append("state", -1)
                    .append("version", -1)
                    .append("createdAt", -1))
            .named("processDefinitionId-1_state-1_version_-1_createdAt-1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("state", 1).append("completedAt", 1))
            .named("state_1_completedAt_1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(
                new Document("createdAt", 1)
                    .append("state", 1)
                    .append("processDefinitionId", 1)
                    .append("version", 1))
            .named("createdAt_1_state_1_processDefinitionId_1_version_1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(
                new Document("state", 1).append("processDefinitionId", 1).append("version", 1))
            .named("state_1_processDefinitionId_1_version_1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("processDefinitionId", 1).append("createdAt", -1))
            .named("processDefinitionId_1_createdAt_-1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("state", 1).append("createdAt", -1))
            .named("state_1_createdAt_-1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(
                new Document("processDefinitionId", 1).append("state", 1).append("createdAt", -1))
            .named("processDefinitionId_1_state_1_createdAt_-1"));
  }

  // --- processState ---
  private void createProcessStateIndexes() {
    safeCreate(
        mongoOperations.indexOps(ProcessState.class),
        new Index().on("processId", Sort.Direction.ASC).unique());
  }

  // --- scheduledTasks ---
  private void createScheduledTaskIndexes() {
    IndexOperations ops = mongoOperations.indexOps(ScheduledTaskDocument.class);
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("state", 1).append("triggerAt", 1))
            .named("scheduler_state_triggerAt"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("state", 1).append("leaseUntil", 1))
            .named("scheduler_state_leaseUntil"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("type", 1).append("businessKey", 1))
            .named("scheduler_type_businessKey")
            .unique()
            .sparse());
  }

  // --- signalEvent ---
  private void createSignalEventIndexes() {
    IndexOperations ops = mongoOperations.indexOps(SignalEvent.class);
    safeCreate(ops, new Index().on("signalName", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("createdAt", Sort.Direction.ASC));
    safeCreate(
        ops,
        new CompoundIndexDefinition(
                new Document("processDefinitionId", -1).append("isStartEvent", -1))
            .named("processDefinitionId_-1_isStartEvent_-1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("signalName", 1).append("state", 1))
            .named("signalName_1_state_1"));
  }

  // --- timedEvent ---
  private void createTimedEventIndexes() {
    IndexOperations ops = mongoOperations.indexOps(TimedEvent.class);
    safeCreate(ops, new Index().on("timedEventRegistryId", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("processDefinitionId", Sort.Direction.ASC));
    safeCreate(
        ops,
        new CompoundIndexDefinition(
                new Document("processDefinitionId", -1).append("isStartEvent", -1))
            .named("processDefinitionId_-1_isStartEvent_-1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(
                new Document("eventRequest.processInstanceId", -1)
                    .append("eventRequest.activityId", -1)
                    .append("type", -1))
            .named("eventRequest.processInstanceId_-1_eventRequest.activityId_-1_type_-1"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("processInstanceId", 1).append("type", 1))
            .named("processInstanceId_1_type_1"));
  }

  // --- user_api_tokens ---
  private void createUserApiTokenIndexes() {
    IndexOperations ops = mongoOperations.indexOps(UserApiToken.class);
    safeCreate(ops, new Index().on("tokenHash", Sort.Direction.ASC).unique());
    safeCreate(ops, new Index().on("userId", Sort.Direction.ASC));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("userId", 1).append("revoked", 1))
            .named("userId_1_revoked_1"));
  }

  // --- userTaskInstance ---
  private void createUserTaskInstanceIndexes() {
    IndexOperations ops = mongoOperations.indexOps(UserTaskInstance.class);
    safeCreate(ops, new Index().on("taskId", Sort.Direction.ASC).unique());
    safeCreate(ops, new Index().on("processInstanceId", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("state", Sort.Direction.ASC));
    safeCreate(ops, new Index().on("assignee", Sort.Direction.ASC));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("processInstanceId", 1).append("state", 1))
            .named("processInstanceId_state"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("assignee", 1).append("state", 1))
            .named("assignee_state"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("candidateUsers", 1).append("state", 1))
            .named("candidateUsers_state"));
    safeCreate(
        ops,
        new CompoundIndexDefinition(new Document("candidateGroups", 1).append("state", 1))
            .named("candidateGroups_state"));
  }

  private void safeCreate(IndexOperations ops, IndexDefinition definition) {
    try {
      definition.getIndexOptions().append("background", true);
      ops.createIndex(definition);
    } catch (Exception ex) {
      if (ex instanceof DataIntegrityViolationException
          && ex.getMessage().contains("Index already exists")) {
        log.debug(
            "Skipped index creation [{}]: {}", definition.getIndexKeys().toJson(), ex.getMessage());
      } else {
        log.error("Failed to create index for definition {}", definition, ex);
        throw ex;
      }
    }
  }

  private void ensureTtlIndex(
      IndexOperations ops,
      Class<?> entityType,
      String indexName,
      String fieldName,
      Sort.Direction direction,
      long expireSeconds) {
    Index ttlIndex =
        new Index()
            .on(fieldName, direction)
            .named(indexName)
            .expire(expireSeconds, TimeUnit.SECONDS);
    try {
      ops.createIndex(ttlIndex);
    } catch (MongoCommandException ex) {
      if (ex.getErrorCode() == 85) {
        reconcileTtl(entityType, indexName, expireSeconds);
      } else {
        log.warn("Skipped TTL index [{}] on {}: {}", indexName, fieldName, ex.getMessage());
      }
    } catch (Exception ex) {
      log.warn("Skipped TTL index [{}] on {}: {}", indexName, fieldName, ex.getMessage());
    }
  }

  private void reconcileTtl(Class<?> entityType, String indexName, long expireSeconds) {
    String collection = mongoOperations.getCollectionName(entityType);
    Document command =
        new Document("collMod", collection)
            .append(
                "index",
                new Document("name", indexName).append("expireAfterSeconds", expireSeconds));
    try {
      mongoOperations.executeCommand(command);
      log.info("Updated TTL on {}.{} to {}s via collMod", collection, indexName, expireSeconds);
    } catch (Exception ex) {
      log.warn("collMod failed for {}.{}: {}", collection, indexName, ex.getMessage());
    }
  }
}
