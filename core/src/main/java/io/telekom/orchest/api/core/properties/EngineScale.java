package io.telekom.orchest.api.core.properties;

import lombok.Getter;

/**
 * Enum defining cluster sizing configurations for the engine. Controls the number of Kafka
 * partitions and replication factors based on the deployment scale.
 *
 * <p>Factors:
 *
 * <ul>
 *   <li>Concurrency: Determines the partition count for various Kafka topics.
 *   <li>Factor: Multiplier applied to base concurrency for specific high-load topics.
 * </ul>
 */
@Getter
public enum EngineScale {

  /** Local development scale. */
  LOCAL(1, 3, 1),
  /** Small scale deployment. */
  SMALL(3, 10, 1),
  /** Medium scale deployment. */
  MEDIUM(3, 25, 1.5f),
  /** Large scale deployment. */
  LARGE(3, 100, 2),
  /** Extra large scale deployment. */
  X_LARGE(3, 300, 2.5f);

  private final int replicationCount;
  private final int partitionCount;
  private final float factor;

  private final int factoredPartitionCount;

  private final int defaultPartitionCount;

  EngineScale(int replicationCount, int partitionCount, float factor) {
    this.replicationCount = replicationCount;
    this.partitionCount = partitionCount;
    this.factor = factor;

    switch (name()) {
      case "MEDIUM" -> defaultPartitionCount = 5;
      case "LARGE", "X_LARGE" -> defaultPartitionCount = 10;
      default -> defaultPartitionCount = 3;
    }

    this.factoredPartitionCount = (int) (partitionCount / factor);
  }

  /**
   * Calculates the concurrency level based on partition and broker counts.
   *
   * @param partitionCount total number of Kafka partitions
   * @param brokerCount number of Kafka brokers in the cluster
   * @return the concurrency level (partitions per broker)
   */
  public static int getConcurrency(int partitionCount, int brokerCount) {
    return partitionCount / brokerCount;
  }
}
