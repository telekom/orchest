package io.telekom.orchest.config.kafka;

import static io.telekom.orchest.adapter.kafka.KafkaUtils.getServerWorkerEventTopic;
import static io.telekom.orchest.adapter.mongo.config.ProcessDefinitionChangeEvent.OperationType.*;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.adapter.kafka.WorkerEventConsumerLoader;
import io.telekom.orchest.adapter.kafka.config.WorkerConsumerConfig;
import io.telekom.orchest.adapter.mongo.config.ProcessDefinitionChangeEvent;
import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessDefinitionRepository;
import java.util.*;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task that synchronizes Kafka consumers for worker events based on deployed process
 * definitions.
 */
@Slf4j
@Component
@DependsOn("consumerRegisterConfig")
@RequiredArgsConstructor
public class ClientWorkerConsumerSyncTask {

  private final WorkerEventConsumerLoader workerEventConsumerLoader;
  private final ProcessDefinitionRepository processDefinitionRepository;
  private final KafkaAdmin kafkaAdmin;

  /**
   * Periodically synchronizes worker event consumers with currently deployed process definitions.
   */
  @Scheduled(initialDelay = 60_000, fixedDelay = 10 * 1_000)
  public void sync() {
    log.debug("starting client worker event consumer sync");
    List<WorkerConsumerConfig> workerConsumerConfigs =
        processDefinitionRepository.findDistinctProcessIds().stream()
            .map(ProcessDefinition::getDefinitionId)
            .map(this::mapWorkerConsumerConfig)
            .toList();
    registerWorkerEventConsumer(workerConsumerConfigs);
    log.debug("client worker event consumer sync finished");
  }

  /**
   * Handles process definition change events by creating or stopping worker consumers as needed.
   *
   * @param event the process definition change event
   */
  @EventListener
  public void onProcessDefinitionDeployed(ProcessDefinitionChangeEvent event) {
    ProcessDefinition processDefinition = event.getProcessDefinition();
    String definitionId = processDefinition.getDefinitionId();
    log.info(
        "received process definition {} event for definitionId: {}",
        event.getOperationType(),
        definitionId);
    if (INSERT.equals(event.getOperationType())) {
      // create client topic
      registerWorkerTopic(event.getProcessDefinition().getDefinitionId());
      registerWorkerEventConsumer(List.of(mapWorkerConsumerConfig(definitionId)));
    } else if (DELETE.equals(event.getOperationType())) {
      //  TODO: have to think on consumer shutdown
      //   if all the versions of a process is removed then shutdown is required
      boolean noVersionPresentForProcess =
          processDefinitionRepository.findDistinctProcessIds().stream()
              .noneMatch(definition -> definition.getDefinitionId().equals(definitionId));
      if (noVersionPresentForProcess) {
        // stop consumer
        String serverWorkerEventTopic = getServerWorkerEventTopic(definitionId);
        workerEventConsumerLoader.stopConsumer(serverWorkerEventTopic);
      }
    }
  }

  private void registerWorkerTopic(String processId) {
    try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
      // Check if a topic exists
      var existingTopics = adminClient.listTopics().names().get();
      String clientTopicName = KafkaUtils.getClientWorkerEventTopic(processId);
      String serverTopicName = KafkaUtils.getServerWorkerEventTopic(processId);
      if (!existingTopics.contains(clientTopicName)) {
        adminClient
            .createTopics(
                List.of(
                    TopicBuilder.name(clientTopicName)
                        .partitions(5)
                        .replicas(KafkaUtils.getReplicationCount())
                        .build()))
            .all()
            .get();
        log.info("Client worker topic: {} created for processId: {}", clientTopicName, processId);
      }
      if (!existingTopics.contains(serverTopicName)) {
        adminClient
            .createTopics(
                List.of(
                    TopicBuilder.name(serverTopicName)
                        .partitions(5)
                        .replicas(KafkaUtils.getReplicationCount())
                        .build()))
            .all()
            .get();
        log.info("Server worker topic: {} created for processId: {}", serverTopicName, processId);
      }

    } catch (InterruptedException | ExecutionException e) {
      log.error(e.getMessage(), e);
    }
  }

  private void registerWorkerEventConsumer(List<WorkerConsumerConfig> workerConsumerConfigs) {
    // check if the consumer is already running
    List<WorkerConsumerConfig> nonActiveConsumers =
        workerConsumerConfigs.stream()
            .filter(
                workerConsumerConfig ->
                    !workerEventConsumerLoader.isConsumerRunning(workerConsumerConfig.getTopic()))
            .toList();

    // create consumer for filtered topics only
    if (nonActiveConsumers.isEmpty()) {
      log.debug("no new process found to register.");
    } else {
      log.info("found {} new process found to register.", nonActiveConsumers.size());
      nonActiveConsumers.forEach(workerEventConsumerLoader::createConsumer);
    }
  }

  private WorkerConsumerConfig mapWorkerConsumerConfig(String processDefinitionId) {
    String serverWorkerEventTopic = getServerWorkerEventTopic(processDefinitionId);
    return WorkerConsumerConfig.builder()
        .id(serverWorkerEventTopic)
        .topic(serverWorkerEventTopic)
        .groupId(KafkaUtils.getGroupIdWithEnvSuffix(serverWorkerEventTopic))
        .build();
  }
}
