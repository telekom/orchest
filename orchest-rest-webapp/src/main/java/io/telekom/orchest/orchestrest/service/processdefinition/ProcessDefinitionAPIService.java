package io.telekom.orchest.orchestrest.service.processdefinition;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import io.telekom.orchest.api.core.response.ResourceDeploymentResponse;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.enginecore.bpmn.utils.XMLUtils;
import io.telekom.orchest.enginecore.parser.BPMNParser;
import io.telekom.orchest.orchestrest.api.dto.DefinitionInfo;
import io.telekom.orchest.orchestrest.api.dto.ResourceDefinitionDTO;
import io.telekom.orchest.telemetry.OrchestRestTelemetryService;
import java.util.*;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

/** Service for deploying, listing, and managing BPMN process definitions. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessDefinitionAPIService {

  private final OrchestWorkflowEngine orchestWorkflowEngine;
  private final ProcessDefinitionDataService processDefinitionDataService;
  private final KafkaAdmin kafkaAdmin;
  private final OrchestRestTelemetryService orchestRestTelemetryService;

  public ResourceDeploymentResponse deployProcessDefinition(
      ResourceDeploymentRequest resourceDeploymentRequest) {
    log.info("Process deployment request received");
    ProcessDefinition processDefinition =
        orchestWorkflowEngine.deployProcessDefinition(
            resourceDeploymentRequest.getResourceUTF8XML(),
            resourceDeploymentRequest.getCompensateFlow());
    String definitionId = processDefinition.getDefinitionId();
    orchestRestTelemetryService.incrementResourceDeploymentMetrics(
        definitionId, processDefinition.getVersion(), "BPMN");
    registerWorkerTopic(
        definitionId,
        resourceDeploymentRequest.getPartitionCount() != null
            ? resourceDeploymentRequest.getPartitionCount()
            : 3);
    // have to think on kafka partition count
    return ResourceDeploymentResponse.builder()
        .processId(definitionId)
        .version(processDefinition.getVersion())
        .build();
  }

  public List<ResourceDefinitionDTO> listProcessIds() {
    return processDefinitionDataService.findDistinctProcessIds().stream()
        .map(this::buildProcessDefinition)
        .toList();
  }

  public Optional<ResourceDefinitionDTO> getProcessDefinition(
      String processDefinitionId, Integer version) {
    Optional<ProcessDefinition> processDefinitionsWithVersion =
        processDefinitionDataService.getProcessDefinitionsWithVersion(processDefinitionId, version);
    return processDefinitionsWithVersion.map(this::buildProcessDefinition);
  }

  public Page<ResourceDefinitionDTO> getProcessDefinitions(int page, int size) {
    List<ProcessDefinition> processDefinitions =
        processDefinitionDataService.getProcessDefinitions(
            page, size, List.of("name", "definitionId", "version"));

    // FIXME: for dynamic process, pull this via repo instead of adding it here
    processDefinitions.add(
        ProcessDefinition.builder().definitionId("main-orchestrator-process").version(1).build());
    return PageableExecutionUtils.getPage(
        processDefinitions.stream().map(this::buildProcessDefinition).toList(),
        Pageable.ofSize(size),
        processDefinitions::size);
  }

  public boolean deleteProcessDefinition(String definitionId, Integer version) {
    log.info(
        "Delete request received for process definition: {} version: {}", definitionId, version);
    boolean deleted = processDefinitionDataService.deleteProcessDefinition(definitionId, version);
    if (deleted) {
      log.info("Deleted process definition: {} version: {}", definitionId, version);
    }
    return deleted;
  }

  private void registerWorkerTopic(String processId, int partitionCount) {
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
                        .partitions(partitionCount)
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
                        .partitions(partitionCount)
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

  private ResourceDefinitionDTO buildProcessDefinition(ProcessDefinition def) {
    return ResourceDefinitionDTO.builder()
        .definitionId(def.getDefinitionId())
        .resourceXML(def.getDefinitionXML())
        .version(def.getVersion() != null ? def.getVersion() : null)
        .resourceMetadata(def.isCompensationFlow() ? Map.of("isCompensationFlow", true) : null)
        .createdAt(def.getCreatedAt())
        .build();
  }

  public DefinitionInfo isNewVersion(ResourceDeploymentRequest deploymentRequest) {
    ProcessDefinition process = BPMNParser.parse(deploymentRequest.getResourceUTF8XML());
    Boolean compensateFlow = deploymentRequest.getCompensateFlow();
    Optional<ProcessDefinition> currentStoredVersionIfAny;
    if (compensateFlow) {
      currentStoredVersionIfAny =
          processDefinitionDataService.getProcessDefinitionsWithVersion(
              process.getDefinitionId(), -1);
    } else {
      currentStoredVersionIfAny =
          processDefinitionDataService.getProcessDefinition(process.getDefinitionId());
    }

    if (currentStoredVersionIfAny.isPresent()) {
      ProcessDefinition storedProcessDefinition = currentStoredVersionIfAny.get();
      boolean areDefinitionsEqual =
          XMLUtils.areDefinitionsEqual(
              storedProcessDefinition.getDefinitionXML(), process.getDefinitionXML());

      if (areDefinitionsEqual) {
        // XML is identical, return existing definition
        log.debug(
            "processDefinition: '{}' already present with version: {} ",
            storedProcessDefinition.getDefinitionId(),
            storedProcessDefinition.getVersion());
        return new DefinitionInfo(
            storedProcessDefinition.getDefinitionId(), storedProcessDefinition.getVersion(), false);
      } else {
        // XML is different, increment version
        if (compensateFlow) {
          return new DefinitionInfo(process.getDefinitionId(), -1, true);
        } else {
          process.setVersion(storedProcessDefinition.getVersion() + 1);
        }
        log.debug("found new processDefinition with version: {} ", process.getVersion());
        return new DefinitionInfo(
            storedProcessDefinition.getDefinitionId(), process.getVersion(), true);
      }
    }
    return new DefinitionInfo(process.getDefinitionId(), 1, true);
  }
}
