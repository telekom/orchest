package io.telekom.orchest.enginecore.bpmn.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.node.CallActivityNode;
import io.telekom.orchest.api.core.request.Variables;
import io.telekom.orchest.api.core.utils.JsonMapper;
import io.telekom.orchest.enginecore.feel.FeelEvaluationEngine;
import io.telekom.orchest.enginecore.parser.BPMNParser;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.camunda.feel.api.EvaluationResult;

/**
 * Utility class for processing and manipulating BPMN process variables. Provides methods for
 * merging variables, evaluating FEEL expressions, and managing input/output variable mappings for
 * call activities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VariablesUtils {

  /**
   * Merges received variables into current variables based on the action type. Supports UPDATE
   * (add/update variables) and DELETE (remove variables) actions.
   *
   * @param recievedVariables The variables object containing the action type and variables to
   *     merge.
   * @param currentVariables The current variables map to merge into.
   * @return The merged variables map (same reference as currentVariables).
   */
  public static Map<String, Object> getMergedVariables(
      Variables recievedVariables, Map<String, Object> currentVariables) {
    if (recievedVariables == null
        || recievedVariables.getVariables() == null
        || currentVariables == null) {
      return currentVariables;
    }
    switch (recievedVariables.getAction()) {
      case UPDATE -> currentVariables.putAll(recievedVariables.getVariables());
      case DELETE -> recievedVariables.getVariables().keySet().forEach(currentVariables::remove);
    }
    return currentVariables;
  }

  /**
   * Evaluates a FEEL expression against the provided variables. Extracts the expression from the
   * input string and evaluates it using the FEEL engine. Returns null if the expression is empty,
   * invalid, or evaluation fails.
   *
   * @param fx The input expression string (may contain FEEL expression).
   * @param variables The variables map to evaluate the expression against.
   * @return The evaluated result, or null if evaluation fails or expression is invalid.
   */
  public static Object getEvaluatedVariable(String fx, Map<String, Object> variables) {
    if (fx == null || fx.isEmpty()) {
      return null;
    }

    if (!fx.startsWith("=")) {
      return fx;
    }
    Optional<String> extractExpression = FeelEvaluationEngine.extractExpression(fx);
    if (extractExpression.isPresent()) {
      EvaluationResult evaluationResult =
          FeelEvaluationEngine.evaluateFeelExpression(extractExpression.get(), variables);
      if (evaluationResult.suppressedFailures().isEmpty() || !evaluationResult.isFailure()) {
        return JsonMapper.readFromJson(
            JsonMapper.writeToJsonWithScalaSupport(evaluationResult.result()),
            new TypeReference<>() {});
      }
      // TBC: should we raise incident if the feel expression fails
      return null;
    }
    return null;
  }

  /**
   * Retrieves input variables for a call activity node. Applies input mappings and optionally
   * propagates all parent variables based on the propagateAllParentVariables property.
   *
   * @param callActivity The call activity node.
   * @param instance The process instance containing variables.
   * @return A map of input variables for the call activity.
   */
  public static Map<String, Object> getActivityInputVariables(
      CallActivityNode callActivity, ProcessInstance instance) {
    Map<String, Object> dataMappingsVariables =
        IODataMappingsUtils.getDataMappingsVariables(
            callActivity.getInputMappings(), instance.getVariables());

    Boolean isPropagateAllParentVars =
        (Boolean)
            callActivity
                .getProperties()
                .getOrDefault(BPMNParser.PROPAGATE_ALL_PARENT_VARIABLES_PROPERTY, false);
    if (isPropagateAllParentVars != null && isPropagateAllParentVars) {
      Map<String, Object> callActivityInputVars = new HashMap<>(instance.getVariables());
      callActivityInputVars.putAll(dataMappingsVariables);
      return callActivityInputVars;
    }

    return dataMappingsVariables.isEmpty() ? instance.getVariables() : dataMappingsVariables;
  }

  /**
   * Retrieves output variables for a call activity node. Applies output mappings and optionally
   * propagates all child variables based on the propagateAllChildVariables property.
   *
   * @param callActivity The call activity node.
   * @param instance The process instance containing variables.
   * @return A map of output variables from the call activity.
   */
  public static Map<String, Object> getActivityOutputVariables(
      CallActivityNode callActivity, ProcessInstance instance) {
    Map<String, Object> dataMappingsVariables =
        IODataMappingsUtils.getDataMappingsVariables(
            callActivity.getOutputMappings(), instance.getVariables());

    Boolean isPropagateAllChildVars =
        (Boolean)
            callActivity
                .getProperties()
                .getOrDefault(BPMNParser.PROPAGATE_ALL_CHILD_VARIABLES_PROPERTY, false);
    if (isPropagateAllChildVars != null && isPropagateAllChildVars) {
      Map<String, Object> callActivityOutputVars = new HashMap<>(instance.getVariables());
      callActivityOutputVars.putAll(dataMappingsVariables);
      return callActivityOutputVars;
    }

    return dataMappingsVariables.isEmpty() ? instance.getVariables() : dataMappingsVariables;
  }
}
