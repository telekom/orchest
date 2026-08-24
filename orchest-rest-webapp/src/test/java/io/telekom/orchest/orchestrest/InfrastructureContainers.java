package io.telekom.orchest.orchestrest;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import java.time.Duration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/** Provides shared Testcontainers (MongoDB, Kafka) for integration tests. */
@Testcontainers
public class InfrastructureContainers {
  static final Network NETWORK = Network.newNetwork();

  private static final int KAFKA_EXTERNAL_PORT = 9094;

  @Container
  public static final GenericContainer<?> MONGODB =
      new GenericContainer<>(DockerImageName.parse("mongo:8"))
          .withNetwork(NETWORK)
          .withNetworkAliases("mongodb")
          .withExposedPorts(27017)
          .withCreateContainerCmdModifier(
              cmd -> {
                cmd.withName("mongodb-tc");
                cmd.withExposedPorts(ExposedPort.tcp(27017));
                cmd.getHostConfig()
                    .withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(27017), ExposedPort.tcp(27017)));
              })
          .withCreateContainerCmdModifier(cmd -> cmd.withName("tc-mongodb"))
          .withCommand(
              "bash",
              "-c",
              "mongod --noauth --replSet rs0 --bind_ip_all & "
                  + "MPID=$! && "
                  + "until mongosh --eval 'db.runCommand({ping:1})' 2>/dev/null; do sleep 1; done && "
                  + "mongosh --eval \"rs.initiate({_id:'rs0', members:[{_id:0, host:'mongodb:27017'}]})\" && "
                  + "wait $MPID")
          .waitingFor(
              Wait.forLogMessage(".*[Tt]ransition to primary.*", 1)
                  .withStartupTimeout(Duration.ofMinutes(2)));

  @Container
  public static final GenericContainer<?> KAFKA =
      new GenericContainer<>(DockerImageName.parse("confluentinc/cp-kafka:latest"))
          .withNetwork(NETWORK)
          .withNetworkAliases("kafka")
          .withCreateContainerCmdModifier(
              cmd -> {
                cmd.withName("kafka-tc");
                cmd.withExposedPorts(ExposedPort.tcp(9094));
                cmd.getHostConfig()
                    .withPortBindings(
                        new PortBinding(
                            Ports.Binding.bindPort(KAFKA_EXTERNAL_PORT), ExposedPort.tcp(9094)));
              })
          .withEnv("KAFKA_NODE_ID", "1")
          .withEnv("KAFKA_PROCESS_ROLES", "broker,controller")
          .withEnv("KAFKA_CONTROLLER_QUORUM_VOTERS", "1@kafka:9093")
          .withEnv(
              "KAFKA_LISTENERS",
              "INTERNAL://0.0.0.0:9092,EXTERNAL://0.0.0.0:9094,CONTROLLER://0.0.0.0:9093")
          .withEnv(
              "KAFKA_ADVERTISED_LISTENERS",
              "INTERNAL://kafka:9092,EXTERNAL://localhost:" + KAFKA_EXTERNAL_PORT)
          .withEnv(
              "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP",
              "CONTROLLER:PLAINTEXT,INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT")
          .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "INTERNAL")
          .withEnv("KAFKA_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
          .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
          .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
          .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
          .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));

  public static String kafkaBootstrapServers() {
    return "localhost:" + KAFKA_EXTERNAL_PORT;
  }
}
