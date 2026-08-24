package io.telekom.orchest.enginecore.bpmn.service;

import io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition;
import io.telekom.orchest.api.core.adapters.data.repository.DecisionDefinitionRepository;
import io.telekom.orchest.enginecore.bpmn.utils.XMLUtils;
import io.telekom.orchest.enginecore.parser.DmnParser;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Manages lifecycle of DMN decision definitions: deployment, versioning, and retrieval. */
@Slf4j
@RequiredArgsConstructor
public class DecisionDefinitionService {

  private final DecisionDefinitionRepository decisionDefinitionRepository;
  private final OrchestEngineTelemetryService telemetryService;

  /**
   * Deploys a DMN decision definition, auto-versioning if the XML differs from the stored version.
   *
   * @param dmnXML the DMN XML content
   * @return the deployed (or existing identical) decision definition
   */
  public DecisionDefinition deployDecisionDefinition(String dmnXML) {
    DecisionDefinition decision = null;
    try {
      decision = DmnParser.parse(dmnXML);
      Optional<DecisionDefinition> currentStoredVersionIfAny =
          decisionDefinitionRepository.getLatestByDefinitionId(decision.getDefinitionId());
      if (currentStoredVersionIfAny.isPresent()) {
        DecisionDefinition storedDecisionDefinition = currentStoredVersionIfAny.get();
        // compare with current XML
        if (XMLUtils.areDefinitionsEqual(
            storedDecisionDefinition.getDefinitionXML(), decision.getDefinitionXML())) {
          // XML is identical, return existing definition
          log.info(
              "decisionDefinition: '{}' already present with version: {} ",
              storedDecisionDefinition.getDefinitionId(),
              storedDecisionDefinition.getVersion());
          return storedDecisionDefinition;
        } else {
          // XML is different, increment version
          decision.setVersion(storedDecisionDefinition.getVersion() + 1);
          log.info("creating new decisionDefinition with version: {} ", decision.getVersion());
        }
      } else {
        // First deployment, set version to 1
        log.info("new decisionDefinition deployment for version: 1");
        decision.setVersion(1);
      }
      telemetryService.incrementResourceDeploymentMetrics(
          decision.getDefinitionId(), decision.getVersion(), "DMN", "success");
      return decisionDefinitionRepository.save(decision);
    } catch (Exception ex) {
      telemetryService.incrementResourceDeploymentMetrics(
          decision.getDefinitionId(), decision.getVersion(), "DMN", "failed");
      throw ex;
    }
  }

  /**
   * Retrieves the latest version of a decision definition by its ID.
   *
   * @param decisionId the decision definition ID
   * @return the latest decision definition, or empty if not found
   */
  public Optional<DecisionDefinition> getLatestDecisionDefinition(String decisionId) {
    return decisionDefinitionRepository.getLatestById(decisionId);
  }

  /**
   * Persists a decision definition.
   *
   * @param decisionInstance the decision definition to save
   * @return the saved decision definition
   */
  public DecisionDefinition save(DecisionDefinition decisionInstance) {
    return decisionDefinitionRepository.save(decisionInstance);
  }
}
