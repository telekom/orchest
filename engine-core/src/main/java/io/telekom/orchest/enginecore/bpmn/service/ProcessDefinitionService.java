package io.telekom.orchest.enginecore.bpmn.service;

import io.telekom.orchest.api.core.adapters.data.model.DynamicProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.repository.DynamicProcessDefinitionRepository;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessDefinitionRepository;
import io.telekom.orchest.enginecore.bpmn.utils.XMLUtils;
import io.telekom.orchest.enginecore.parser.BPMNParser;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import io.telekom.orchest.telemetry.OrchestTelemetryService;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing process definitions. Handles parsing, versioning, and persistence of BPMN
 * process models.
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class ProcessDefinitionService {

  private final ProcessDefinitionRepository processDefinitionRepository;
  private final DynamicProcessDefinitionRepository dynamicProcessDefinitionRepository;
  private final EventRegisterService eventRegisterService;
  private final OrchestEngineTelemetryService telemetryService;

  /**
   * Deploys a new process definition from BPMN XML.
   *
   * <p>Parses the XML, checks for existing versions, increments the version number if necessary,
   * registers any start events (e.g., Message/Signal), and saves the definition.
   *
   * @param bpmnXML The raw BPMN XML string.
   * @return The deployed ProcessDefinition.
   */
  public ProcessDefinition deployProcessDefinition(String bpmnXML) {
    ProcessDefinition process = null;
    try {
      process = BPMNParser.parse(bpmnXML);
      Optional<ProcessDefinition> currentStoredVersionIfAny =
          processDefinitionRepository.getLatestById(process.getDefinitionId());
      if (currentStoredVersionIfAny.isPresent()) {
        ProcessDefinition storedProcessDefinition = currentStoredVersionIfAny.get();
        // compare with current XML
        if (XMLUtils.areDefinitionsEqual(
            storedProcessDefinition.getDefinitionXML(), process.getDefinitionXML())) {
          // XML is identical, return existing definition
          log.info(
              "processDefinition: '{}' already present with version: {} ",
              storedProcessDefinition.getDefinitionId(),
              storedProcessDefinition.getVersion());
          return storedProcessDefinition;
        } else {
          // XML is different, increment version
          process.setVersion(storedProcessDefinition.getVersion() + 1);
          log.info("creating new processDefinition with version: {} ", process.getVersion());
        }
      } else {
        // First deployment, set version to 1
        log.info("new processDefinition deployment for version: 1");
        process.setVersion(1);
      }

      telemetryService.incrementResourceDeploymentMetrics(
          process.getDefinitionId(), process.getVersion(), OrchestTelemetryService.BPMN, "success");
      // register Message/Signal start event
      eventRegisterService.handleStartEventIfAny(process);

      return processDefinitionRepository.save(process);
    } catch (Exception e) {
      telemetryService.incrementResourceDeploymentMetrics(
          process.getDefinitionId(), process.getVersion(), OrchestTelemetryService.BPMN, "failed");
      throw new RuntimeException(e);
    }
  }

  /**
   * Deploys a compensation process definition (stored with version -1).
   *
   * @param bpmnXML the BPMN XML string
   * @return the deployed compensation process definition
   */
  public ProcessDefinition deployCompensateProcessDefinition(String bpmnXML) {
    ProcessDefinition process = null;
    try {
      process = BPMNParser.parse(bpmnXML);
      process.setVersion(-1);
      Optional<ProcessDefinition> currentStoredVersionIfAny =
          processDefinitionRepository.getByIdAndVersion(process.getDefinitionId(), -1);
      if (currentStoredVersionIfAny.isPresent()) {
        ProcessDefinition storedProcessDefinition = currentStoredVersionIfAny.get();
        // compare with current XML
        if (XMLUtils.areDefinitionsEqual(
            storedProcessDefinition.getDefinitionXML(), process.getDefinitionXML())) {
          // XML is identical, return existing definition
          log.info(
              "processDefinition: '{}' already present with version: {} ",
              storedProcessDefinition.getDefinitionId(),
              storedProcessDefinition.getVersion());
          return storedProcessDefinition;
        }
      }
      telemetryService.incrementResourceDeploymentMetrics(
          process.getDefinitionId(), process.getVersion(), OrchestTelemetryService.BPMN, "success");
      return processDefinitionRepository.save(process);
    } catch (Exception e) {
      telemetryService.incrementResourceDeploymentMetrics(
          process.getDefinitionId(), process.getVersion(), OrchestTelemetryService.BPMN, "failed");
      throw new RuntimeException(e);
    }
  }

  /**
   * Deploys a dynamic (inline) process definition tied to a specific process instance.
   *
   * @param bpmnXML the BPMN XML string
   * @param processInstanceId the process instance this dynamic definition belongs to
   * @return the deployed dynamic process definition
   */
  public DynamicProcessDefinition deployDynamicProcessDefinition(
      String bpmnXML, String processInstanceId) {
    ProcessDefinition process = BPMNParser.parse(bpmnXML);
    // always one as its dynamic and new everytime
    process.setVersion(1);
    // register Message/Signal start event
    eventRegisterService.handleStartEventIfAny(process);
    DynamicProcessDefinition processDefinition =
        DynamicProcessDefinition.fromProcessDefinition(process);
    processDefinition.setProcessInstanceId(processInstanceId);
    return dynamicProcessDefinitionRepository.save(processDefinition);
  }

  /**
   * Retrieves a process definition by ID and optional version. If version is null, returns the
   * latest.
   *
   * @param definitionId the process definition ID
   * @param version the version number, or null for latest
   * @return the matching process definition, or empty if not found
   */
  public Optional<ProcessDefinition> getProcessDefinition(String definitionId, Integer version) {
    if (version == null) {
      return getLatestProcessDefinition(definitionId);
    } else {
      return getProcessDefWithVersion(definitionId, version);
    }
  }

  /**
   * Retrieves the latest version of a process definition by its ID.
   *
   * @param definitionId The process definition ID.
   * @return An Optional containing the ProcessDefinition if found.
   */
  public Optional<ProcessDefinition> getLatestProcessDefinition(String definitionId) {
    return processDefinitionRepository.getLatestById(definitionId);
  }

  /**
   * Retrieves a specific version of a process definition.
   *
   * @param definitionId The process definition ID.
   * @param version The version number.
   * @return An Optional containing the ProcessDefinition if found.
   */
  public Optional<ProcessDefinition> getProcessDefWithVersion(
      String definitionId, Integer version) {
    return processDefinitionRepository.getByIdAndVersion(definitionId, version);
  }
}
