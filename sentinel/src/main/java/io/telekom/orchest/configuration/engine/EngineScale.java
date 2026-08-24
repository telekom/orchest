package io.telekom.orchest.configuration.engine;

import lombok.Getter;

/**
 * This enum will provide the cluster sizing for Kafka Concurrency: Server Kafka event topic
 * partition count factor: partition multiplier for topics
 */
@Getter
public enum EngineScale {
  LOCAL(1, 3, 1),
  SMALL(3, 20, 1.5f),
  MEDIUM(3, 50, 2f),
  LARGE(3, 100, 2.5f),
  X_LARGE(3, 300, 2.5f);

  private final int replicationCount;
  private final int processInvocationConcurrency;
  private final float factor;
  private final int serverWorkerEventConcurrency;
  private final int processDeploymentConcurrency;
  private final int pendingTaskConcurrency;
  private final int messageEventConcurrency;

  EngineScale(int replicationCount, int processInvocationConcurrency, float factor) {
    this.replicationCount = replicationCount;
    this.processInvocationConcurrency = processInvocationConcurrency;
    this.factor = factor;
    this.processDeploymentConcurrency = (int) (processInvocationConcurrency / factor);
    this.serverWorkerEventConcurrency = (int) (processInvocationConcurrency * factor);
    this.pendingTaskConcurrency = (int) (processInvocationConcurrency * factor);
    this.messageEventConcurrency = (int) (processInvocationConcurrency * factor);
  }
}
