package io.telekom.orchest.enginecore.bpmn.execution.impl;

import static io.telekom.orchest.enginecore.bpmn.utils.MultiInstanceUtils.*;

import io.telekom.orchest.api.core.adapters.data.dto.DecisionEvaluationResponseDTO;
import io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition;
import io.telekom.orchest.api.core.adapters.data.model.DecisionInstance;
import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.MultiInstanceLoopCharacteristics;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.BusinessRuleTaskNode;
import io.telekom.orchest.api.core.model.bpmn.state.ExecutionStateKey;
import io.telekom.orchest.api.core.model.dmn.DIState;
import io.telekom.orchest.api.core.model.dmn.Rule;
import io.telekom.orchest.api.core.utils.IDGenerator;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.service.DecisionDefinitionService;
import io.telekom.orchest.enginecore.dmn.DmnEngine;
import io.telekom.orchest.enginecore.feel.FeelEvaluationEngine;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Executor for Business Rule Tasks. Evaluates DMN decision tables against process variables and
 * stores the results. Supports both single-instance and multi-instance (parallel) execution.
 */
// Only parallel is supported right now not a big problem for sequential also just the matter of
// usage
@Slf4j
public class BusinessRuleTaskExecutor implements NodeExecutor {

  private final DmnEngine dmnEngine = new DmnEngine();

  /**
   * Executes a business rule task by evaluating the referenced DMN decision definition.
   *
   * @param instance the active process instance
   * @param node the business rule task node to execute
   * @param context the execution context
   * @throws IllegalArgumentException if the node is not a BusinessRuleTaskNode
   * @throws RuntimeException if the referenced decision definition cannot be found
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    if (!(node instanceof BusinessRuleTaskNode ruleNode)) {
      throw new IllegalArgumentException("BusinessRuleTaskExecutor requires BusinessRuleTaskNode");
    }
    String decisionId = ruleNode.getDecisionId();
    String processInstanceId = instance.getProcessInstanceId();
    log.info("Executing Business Rule Task: {} (Decision: {})", node.getName(), decisionId);
    try {
      DecisionDefinitionService decisionDefService = context.getDecisionDefinitionService();
      Optional<DecisionDefinition> latestDecisionDefinition =
          decisionDefService.getLatestDecisionDefinition(decisionId);
      if (latestDecisionDefinition.isPresent()) {
        DecisionDefinition decisionDefinition = latestDecisionDefinition.get();

        if (isMultiInstance(node)) {
          handleMultiInstance(instance, ruleNode, decisionDefinition, context);
        } else {
          executeSingleDecision(instance, ruleNode, decisionDefinition, context);
        }

        ProcessDefinition definition = instance.getProcessDefinition();
        for (BaseNode outgoing : definition.getOutgoingNodes(node.getId())) {
          context.proceed(instance, outgoing, node.getId());
        }
      } else {
        // stop the execution
        throw new RuntimeException(
            "No dmn found with id: " + decisionId + "  for instanceId: " + processInstanceId);
      }

    } catch (Exception e) {
      log.error("Failed to execute DMN", e);
      throw e;
    }
  }

  /**
   * Handles multi-instance execution by iterating over the input collection and evaluating the
   * decision for each element.
   *
   * @param instance the active process instance
   * @param businessRuleTaskNode the business rule task node with multi-instance configuration
   * @param decisionDefinition the decision definition to evaluate
   * @param context the execution context
   */
  public void handleMultiInstance(
      ProcessInstance instance,
      BusinessRuleTaskNode businessRuleTaskNode,
      DecisionDefinition decisionDefinition,
      ExecutionContext context) {
    if (!isMultiInstance(businessRuleTaskNode)) {
      return;
    }
    Optional<MultiInstanceLoopCharacteristics> optionalMIData = getMIData(businessRuleTaskNode);
    var decisionKey = new ExecutionStateKey.DecisionInstance(businessRuleTaskNode.getId());
    optionalMIData.ifPresent(
        miData -> {
          String inputCollectionVariableKey = miData.getCollection();
          String elementVariable = miData.getElementVariable();
          Map<String, Object> instanceVariables = new HashMap<>(instance.getVariables());
          Optional<Object> inputCollectionVariables =
              FeelEvaluationEngine.evaluateExpression(
                  inputCollectionVariableKey, instanceVariables);
          String decisionInstanceId = IDGenerator.generate();
          List<String> decisionInstanceIds = new ArrayList<>();
          List<Object> resultOutputs = new ArrayList<>();
          if (inputCollectionVariables.isPresent()
              && (inputCollectionVariables.get() instanceof List<?> inputVariablesList)) {
            for (int counter = 0; counter < inputVariablesList.size(); counter++) {
              String activeInstanceId = decisionInstanceId + "_" + (counter + 1);
              instanceVariables.put(elementVariable, inputVariablesList.get(counter));

              DecisionEvaluationResponseDTO result =
                  executeMultiDecision(
                      instance,
                      businessRuleTaskNode,
                      decisionDefinition,
                      context,
                      activeInstanceId,
                      instanceVariables,
                      miData);
              if (businessRuleTaskNode.getResultVariable() != null
                  && !businessRuleTaskNode.getResultVariable().isEmpty()) {
                resultOutputs.add(result.getOutputVariables());
              } else {
                resultOutputs.add(new HashMap<>());
              }
              decisionInstanceIds.add(activeInstanceId);
            }

            // add merged variables
            instance.getVariables().put(miData.getOutputCollection(), resultOutputs);

            // Store decision instance ID in execution state for logging metadata
            instance.putState(decisionKey, decisionInstanceIds);
          }
        });
  }

  /**
   * Evaluates a single decision and persists the resulting decision instance.
   *
   * @param instance the active process instance
   * @param businessRuleTaskNode the business rule task node referencing the decision
   * @param decisionDefinition the decision definition to evaluate
   * @param context the execution context
   * @return the persisted decision instance
   */
  public DecisionInstance executeSingleDecision(
      ProcessInstance instance,
      BusinessRuleTaskNode businessRuleTaskNode,
      DecisionDefinition decisionDefinition,
      ExecutionContext context) {
    String decisionId = businessRuleTaskNode.getDecisionId();
    DecisionEvaluationResponseDTO result =
        dmnEngine.evaluate(decisionDefinition, decisionId, instance.getVariables());
    String processInstanceId = instance.getProcessInstanceId();
    log.trace("DMN Result: {} for instanceId: {}", result, processInstanceId);
    if (businessRuleTaskNode.getResultVariable() != null
        && !businessRuleTaskNode.getResultVariable().isEmpty()) {
      instance
          .getVariables()
          .put(businessRuleTaskNode.getResultVariable(), result.getOutputVariables());
    } else {
      instance.getVariables().put(businessRuleTaskNode.getResultVariable(), new HashMap<>());
    }

    DecisionInstance decisionInstance =
        createDecisionInstance(
            decisionDefinition,
            null,
            businessRuleTaskNode.getDecisionId(),
            processInstanceId,
            result,
            context);
    // Store decision instance ID in execution state for logging metadata
    instance.putState(
        new ExecutionStateKey.DecisionInstance(businessRuleTaskNode.getId()),
        decisionInstance.getDecisionInstanceId());
    return decisionInstance;
  }

  /**
   * Evaluates a decision for a single iteration within a multi-instance loop.
   *
   * @param instance the active process instance
   * @param businessRuleTaskNode the business rule task node referencing the decision
   * @param decisionDefinition the decision definition to evaluate
   * @param context the execution context
   * @param decisionInstanceId the unique ID for this decision instance iteration
   * @param variables the variables for this iteration (includes element variable)
   * @param miData the multi-instance loop characteristics
   * @return the evaluation response containing output variables
   */
  public DecisionEvaluationResponseDTO executeMultiDecision(
      ProcessInstance instance,
      BusinessRuleTaskNode businessRuleTaskNode,
      DecisionDefinition decisionDefinition,
      ExecutionContext context,
      String decisionInstanceId,
      Map<String, Object> variables,
      MultiInstanceLoopCharacteristics miData) {
    String decisionId = businessRuleTaskNode.getDecisionId();
    DecisionEvaluationResponseDTO result =
        dmnEngine.evaluate(decisionDefinition, decisionId, variables);
    String processInstanceId = instance.getProcessInstanceId();
    log.trace("DMN Result: {} for instanceId: {}", result, processInstanceId);
    String elementVariable = miData.getElementVariable();
    HashMap<String, Object> inputVariables = new HashMap<>(result.getInputVariables());
    result
        .getInputVariables()
        .forEach(
            (s, o) -> {
              // clean the element variables
              inputVariables.put(s.replace(elementVariable + ".", ""), o);
              inputVariables.remove(s);
            });

    result.setInputVariables(inputVariables);

    createDecisionInstance(
        decisionDefinition,
        decisionInstanceId,
        businessRuleTaskNode.getDecisionId(),
        processInstanceId,
        result,
        context);

    return result;
  }

  private DecisionInstance createDecisionInstance(
      DecisionDefinition decisionDefinition,
      String decisionInstanceId,
      String decisionId,
      String processInstanceId,
      DecisionEvaluationResponseDTO result,
      ExecutionContext context) {
    DecisionInstance decisionInstance = new DecisionInstance();
    decisionInstance.setDecisionInstanceId(
        decisionInstanceId != null ? decisionInstanceId : IDGenerator.generate());
    decisionInstance.setResourceXMLUTF8String(decisionDefinition.getDefinitionXML());
    decisionInstance.setDefinitionId(decisionId);
    decisionInstance.setState(DIState.EXECUTED);
    decisionInstance.setProcessInstanceId(processInstanceId);
    decisionInstance.setInputVariables(result.getInputVariables());
    decisionInstance.setOutputVariables(result.getOutputVariables());
    decisionInstance.setVersion(decisionDefinition.getVersion());
    decisionInstance.setMatchedRuleIds(result.getMatchedRules().stream().map(Rule::getId).toList());
    decisionInstance.setExecutedAt(OffsetDateTime.now(ZoneOffset.UTC));
    DecisionInstance saved = context.getDecisionInstanceService().save(decisionInstance);
    // Lifetime accounting for DMN evaluation. Recorded once per evaluation, regardless
    // of whether this is single-instance or one iteration of multi-instance.
    context.getDecisionInstanceService().recordEvaluation(saved);
    return saved;
  }
}
