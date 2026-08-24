package io.telekom.orchest.orchestrest.configurations;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;
import org.springframework.boot.health.contributor.AbstractReactiveHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** Custom health indicator that reports Kafka broker connectivity status. */
@Component("kafka")
public class KafkaHealthIndicator extends AbstractReactiveHealthIndicator {

  private final AdminClient adminClient;

  public KafkaHealthIndicator(AdminClient adminClient) {
    super("Kafka health check failed");
    this.adminClient = adminClient;
  }

  @Override
  protected Mono<Health> doHealthCheck(Health.Builder builder) {
    return Mono.fromCallable(
            () -> {
              DescribeClusterResult result = adminClient.describeCluster();
              String clusterId = result.clusterId().get(5, TimeUnit.SECONDS);
              Collection<Node> nodes = result.nodes().get(5, TimeUnit.SECONDS);
              return builder
                  .up()
                  .withDetail("clusterId", clusterId)
                  .withDetail("nodeCount", nodes.size())
                  .build();
            })
        .subscribeOn(Schedulers.boundedElastic());
  }
}
