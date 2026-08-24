package io.telekom.orchest.orchestrest.service.decisiondefinition;

import io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition;
import io.telekom.orchest.api.core.adapters.data.repository.DecisionDefinitionRepository;
import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import io.telekom.orchest.api.core.response.ResourceDeploymentResponse;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.enginecore.bpmn.utils.XMLUtils;
import io.telekom.orchest.enginecore.parser.DmnParser;
import io.telekom.orchest.orchestrest.api.dto.DefinitionInfo;
import io.telekom.orchest.orchestrest.api.dto.ResourceDefinitionDTO;
import io.telekom.orchest.telemetry.OrchestRestTelemetryService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

/**
 * Service for managing decision definitions via the REST API. Handles deployment, versioning,
 * retrieval, and listing of decision definitions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DecisionDefinitionAPIService {

  private final OrchestWorkflowEngine orchestWorkflowEngine;
  private final OrchestRestTelemetryService orchestRestTelemetryService;
  private final DecisionDefinitionRepository decisionDefinitionRepository;

  /**
   * Deploys a new decision definition (DMN).
   *
   * @param resourceDeploymentRequest The deployment request containing the DMN XML.
   * @return The deployment response.
   */
  public ResourceDeploymentResponse deploy(ResourceDeploymentRequest resourceDeploymentRequest) {
    DecisionDefinition decisionDefinition =
        orchestWorkflowEngine.deployDecisionDefinition(
            resourceDeploymentRequest.getResourceUTF8XML());
    String definitionId = decisionDefinition.getDefinitionId();
    orchestRestTelemetryService.incrementResourceDeploymentMetrics(
        definitionId, decisionDefinition.getVersion(), "DMN");
    log.info(
        "deployed decision definition {} with version: {}",
        definitionId,
        decisionDefinition.getVersion());
    return ResourceDeploymentResponse.builder()
        .processId(definitionId)
        .version(decisionDefinition.getVersion())
        .build();
  }

  private ResourceDefinitionDTO buildProcessDefinition(DecisionDefinition def) {
    return ResourceDefinitionDTO.builder()
        .definitionId(def.getDefinitionId())
        .resourceXML(def.getDefinitionXML())
        .version(def.getVersion())
        .resourceMetadata(Map.of("decisionIds", def.getDecisionIds()))
        .createdAt(def.getCreatedAt())
        .build();
  }

  /**
   * Lists all available decision definitions.
   *
   * @return A list of resource definition DTOs.
   */
  public List<ResourceDefinitionDTO> listDecisionIds() {
    return decisionDefinitionRepository.getDecisionDefinition().stream()
        .map(this::buildProcessDefinition)
        .toList();
  }

  /**
   * Retrieves a specific version of a decision definition.
   *
   * @param decisionDefinitionId The decision ID.
   * @param version The version number.
   * @return An Optional containing the resource definition DTO.
   */
  public Optional<ResourceDefinitionDTO> getDecisionDefinition(
      String decisionDefinitionId, Integer version) {
    Optional<DecisionDefinition> processDefinitionsWithVersion =
        decisionDefinitionRepository.getByDefinitionIdAndVersion(decisionDefinitionId, version);
    return processDefinitionsWithVersion.map(this::buildProcessDefinition);
  }

  /**
   * Retrieves a paginated list of decision definitions.
   *
   * @param page The page number.
   * @param size The page size.
   * @return A page of resource definition DTOs.
   */
  public Page<ResourceDefinitionDTO> getDecisionDefinitions(int page, int size) {
    List<DecisionDefinition> processDefinitions =
        decisionDefinitionRepository.findAllDefinitions(page, size);
    return PageableExecutionUtils.getPage(
        processDefinitions.stream().map(this::buildProcessDefinition).toList(),
        Pageable.ofSize(size),
        processDefinitions::size);
  }

  /**
   * Deletes a specific version of a decision definition.
   *
   * @param definitionId The decision definition ID.
   * @param version The version number to delete.
   * @return true if the definition was found and deleted, false otherwise.
   */
  public boolean deleteDecisionDefinition(String definitionId, Integer version) {
    log.info(
        "Delete request received for decision definition: {} version: {}", definitionId, version);
    boolean deleted =
        decisionDefinitionRepository.deleteByDefinitionIdAndVersion(definitionId, version);
    if (deleted) {
      log.info("Deleted decision definition: {} version: {}", definitionId, version);
    }
    return deleted;
  }

  /**
   * Checks if a deployment request represents a new version of an existing definition.
   *
   * @param deploymentRequest The deployment request.
   * @return Information about the potential new version.
   */
  public DefinitionInfo isNewVersion(ResourceDeploymentRequest deploymentRequest) {
    DecisionDefinition decisionDefinition = DmnParser.parse(deploymentRequest.getResourceUTF8XML());
    Optional<DecisionDefinition> currentStoredVersionIfAny =
        decisionDefinitionRepository.getLatestByDefinitionId(decisionDefinition.getDefinitionId());
    if (currentStoredVersionIfAny.isPresent()) {
      DecisionDefinition storedDecisionDefinition = currentStoredVersionIfAny.get();
      if (XMLUtils.areDefinitionsEqual(
          storedDecisionDefinition.getDefinitionXML(), decisionDefinition.getDefinitionXML())) {
        // XML is identical, return existing definition
        log.debug(
            "decisionDefinition: '{}' already present with version: {} ",
            storedDecisionDefinition.getDefinitionId(),
            storedDecisionDefinition.getVersion());
        return new DefinitionInfo(
            storedDecisionDefinition.getDefinitionId(),
            storedDecisionDefinition.getVersion(),
            false);
      } else {
        // XML is different, increment version
        decisionDefinition.setVersion(storedDecisionDefinition.getVersion() + 1);
        log.debug(
            "found new decisionDefinition with version: {} ", decisionDefinition.getVersion());
        return new DefinitionInfo(
            storedDecisionDefinition.getDefinitionId(), decisionDefinition.getVersion(), true);
      }
    }
    return new DefinitionInfo(decisionDefinition.getDefinitionId(), 1, true);
  }
}
