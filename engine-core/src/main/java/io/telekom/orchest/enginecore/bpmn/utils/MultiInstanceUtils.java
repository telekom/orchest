package io.telekom.orchest.enginecore.bpmn.utils;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.MultiInstanceLoopCharacteristics;
import io.telekom.orchest.api.core.model.bpmn.node.ActivityNode;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.SubProcessNode;
import io.telekom.orchest.api.core.model.bpmn.state.ExecutionStateKey;
import io.telekom.orchest.enginecore.feel.FeelEvaluationEngine;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for handling Multi-Instance (MI) activities in BPMN. Provides helper methods to
 * determine MI characteristics (parallel vs sequential), manage loop counters, and check for
 * pending instances.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MultiInstanceUtils {

  /**
   * Retrieves the Multi-Instance loop characteristics for a given node.
   *
   * @param node The node to inspect.
   * @return An Optional containing the loop characteristics if the node is an activity with MI
   *     configuration.
   */
  public static Optional<MultiInstanceLoopCharacteristics> getMIData(BaseNode node) {
    if (node instanceof ActivityNode activityNode) {
      MultiInstanceLoopCharacteristics miData = activityNode.getMultiInstanceLoopCharacteristics();
      if (miData == null) {
        return Optional.empty();
      }
      return Optional.of(miData);
    }
    return Optional.empty();
  }

  /**
   * Checks if a node is configured as a parallel multi-instance activity.
   *
   * @param node The node to check.
   * @return true if parallel multi-instance.
   */
  public static boolean isParallelInstance(BaseNode node) {
    Optional<MultiInstanceLoopCharacteristics> miData = getMIData(node);
    return miData
        .filter(
            multiInstanceLoopCharacteristics -> !multiInstanceLoopCharacteristics.isSequential())
        .isPresent();
  }

  /**
   * Checks if a node is configured as a sequential multi-instance activity.
   *
   * @param node The node to check.
   * @return true if sequential multi-instance.
   */
  public static boolean isSequentialInstance(BaseNode node) {
    Optional<MultiInstanceLoopCharacteristics> miData = getMIData(node);
    return miData.map(MultiInstanceLoopCharacteristics::isSequential).orElse(false);
  }

  /**
   * Clears multi-instance execution state for a node. Used when a multi-instance activity
   * completes.
   *
   * @param instance The process instance.
   * @param node The multi-instance node.
   */
  public static void clearMultiInstance(ProcessInstance instance, BaseNode node) {
    Optional<MultiInstanceLoopCharacteristics> optionAlMiData = getMIData(node);
    optionAlMiData.ifPresent(
        miData -> {
          String inputCollectionVariableKey = miData.getCollection();
          Optional<Object> inputCollectionVariables =
              FeelEvaluationEngine.evaluateExpression(
                  inputCollectionVariableKey, instance.getVariables());
          if (inputCollectionVariables.isPresent()
              && (inputCollectionVariables.get() instanceof List<?> inputVariablesList)) {
            // TBC: have to think on this just clearing a random first can might cause problem
            IntStream.range(0, inputVariablesList.size())
                .filter(counter -> instance.containsState(loopCounterKey(node, counter)))
                .findFirst()
                .ifPresent(counter -> instance.removeState(loopCounterKey(node, counter)));
          }
        });
  }

  /**
   * Checks if a sequential multi-instance activity has pending iterations.
   *
   * @param instance The process instance.
   * @param node The sequential multi-instance node.
   * @return true if there are more iterations to execute.
   */
  public static boolean isSequentialAndPending(ProcessInstance instance, BaseNode node) {
    Optional<MultiInstanceLoopCharacteristics> optionAlMiData = getMIData(node);
    if (optionAlMiData.isPresent()) {
      MultiInstanceLoopCharacteristics miData = optionAlMiData.get();
      String inputCollectionVariableKey = miData.getCollection();
      Optional<Object> inputCollectionVariables =
          FeelEvaluationEngine.evaluateExpression(
              inputCollectionVariableKey, instance.getVariables());
      if (inputCollectionVariables.isPresent()
          && (inputCollectionVariables.get() instanceof List<?> inputVariablesList)) {
        if (miData.isSequential()) {
          OptionalInt currentLoopCounter =
              IntStream.range(0, inputVariablesList.size())
                  .filter(counter -> instance.containsState(loopCounterKey(node, counter)))
                  .findFirst();
          if (currentLoopCounter.isEmpty()) {
            return false; // No pending instance
          }
          return currentLoopCounter.getAsInt() < inputVariablesList.size() - 1;
        }
      }
    }
    return false;
  }

  /**
   * Checks if any multi-instance (parallel or sequential) iterations are pending.
   *
   * @param instance The process instance.
   * @param node The multi-instance node.
   * @return true if pending instances exist.
   */
  public static boolean isMultiInstancePending(ProcessInstance instance, BaseNode node) {
    Optional<MultiInstanceLoopCharacteristics> optionAlMiData = getMIData(node);
    if (optionAlMiData.isPresent()) {
      MultiInstanceLoopCharacteristics miData = optionAlMiData.get();
      String inputCollectionVariableKey = miData.getCollection();
      Optional<Object> inputCollectionVariables =
          FeelEvaluationEngine.evaluateExpression(
              inputCollectionVariableKey, instance.getVariables());
      if (inputCollectionVariables.isPresent()
          && (inputCollectionVariables.get() instanceof List<?> inputVariablesList)) {
        if (miData.isSequential()) {
          OptionalInt currentLoopCounter =
              IntStream.range(0, inputVariablesList.size())
                  .filter(counter -> instance.containsState(loopCounterKey(node, counter)))
                  .findFirst();
          if (currentLoopCounter.isEmpty()) {
            return false; // No pending instance
          }
          return currentLoopCounter.getAsInt() < inputVariablesList.size() - 1;
        } else {
          OptionalInt matched =
              IntStream.range(0, inputVariablesList.size())
                  .filter(counter -> instance.containsState(loopCounterKey(node, counter)))
                  .findFirst(); // any pending parallel execution
          return matched.isPresent();
        }
      }
    }
    return false;
  }

  /** Creates a typed key for tracking the total loop size of a multi-instance activity. */
  public static ExecutionStateKey.MultiInstanceLoopSize loopSizeKey(BaseNode node) {
    return new ExecutionStateKey.MultiInstanceLoopSize(node.getId());
  }

  /** Creates a typed key for tracking an individual loop iteration counter. */
  public static ExecutionStateKey.MultiInstanceLoopCounter loopCounterKey(
      BaseNode node, int counter) {
    return new ExecutionStateKey.MultiInstanceLoopCounter(counter, node.getId());
  }

  /**
   * Checks if a node is a multi-instance activity.
   *
   * @param node The node to check.
   * @return true if multi-instance.
   */
  public static boolean isMultiInstance(BaseNode node) {
    if (node instanceof ActivityNode activity) {
      MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics =
          activity.getMultiInstanceLoopCharacteristics();
      return multiInstanceLoopCharacteristics != null;
    }
    return false;
  }

  // ========================================================================================
  // Multi-instance subprocess utilities
  // ========================================================================================

  /**
   * Checks if a node is directly inside a multi-instance subprocess scope.
   *
   * @param node The node to check.
   * @param definition The process definition.
   * @return true if the node's immediate scope is an MI subprocess.
   */
  public static boolean isInsideMultiInstanceSubProcess(
      BaseNode node, ProcessDefinition definition) {
    return getContainingMiSubProcess(node, definition).isPresent();
  }

  /**
   * Gets the containing multi-instance SubProcessNode for a node, if any. Only checks the immediate
   * scope (not ancestors).
   *
   * @param node The node to check.
   * @param definition The process definition.
   * @return The MI SubProcessNode, or empty if not inside one.
   */
  public static Optional<SubProcessNode> getContainingMiSubProcess(
      BaseNode node, ProcessDefinition definition) {
    if (node == null || definition == null) return Optional.empty();
    String scopeId = node.getScopeId();
    if (scopeId == null) return Optional.empty();
    return definition
        .getNode(scopeId)
        .filter(scopeNode -> scopeNode instanceof SubProcessNode)
        .map(scopeNode -> (SubProcessNode) scopeNode)
        .filter(MultiInstanceUtils::isMultiInstance);
  }

  /**
   * Evaluates the input collection expression for a multi-instance configuration.
   *
   * @param mi The multi-instance loop characteristics.
   * @param variables The variables map.
   * @return The evaluated collection, or empty list if evaluation fails.
   */
  @SuppressWarnings("unchecked")
  public static List<Object> evaluateInputCollection(
      MultiInstanceLoopCharacteristics mi, java.util.Map<String, Object> variables) {
    String collectionExpr = mi.getCollection();
    Optional<Object> result = FeelEvaluationEngine.evaluateExpression(collectionExpr, variables);
    if (result.isPresent() && result.get() instanceof List<?> list) {
      return (List<Object>) list;
    }
    return List.of();
  }

  /**
   * Evaluates a FEEL completion condition expression.
   *
   * @param condition The FEEL completion condition expression.
   * @param variables The variables map.
   * @return true if the condition evaluates to true.
   */
  public static boolean evaluateCompletionCondition(
      String condition, java.util.Map<String, Object> variables) {
    if (condition == null || condition.isBlank()) return false;
    try {
      Optional<Object> result = FeelEvaluationEngine.evaluateExpression(condition, variables);
      return result.isPresent() && Boolean.TRUE.equals(result.get());
    } catch (Exception e) {
      log.warn("Failed to evaluate completion condition '{}': {}", condition, e.getMessage());
      return false;
    }
  }

  /**
   * Cleans up all multi-instance subprocess state for a given subprocess.
   *
   * @param instance The process instance.
   * @param subProcess The MI subprocess node.
   */
  public static void cleanupMultiInstanceSubProcess(
      ProcessInstance instance, SubProcessNode subProcess) {
    String spId = subProcess.getId();
    int loopSize = instance.getState(loopSizeKey(subProcess), 0);
    // Remove all loop counters
    for (int i = 0; i < loopSize; i++) {
      instance.removeState(loopCounterKey(subProcess, i));
    }
    instance.removeState(loopSizeKey(subProcess));
    instance.removeState(new ExecutionStateKey.MultiInstanceSubProcessOutput(spId));
    instance.removeState(new ExecutionStateKey.MultiInstanceSubProcessCompleted(spId));
    // Remove all MI-indexed active nodes for this subprocess
    instance.removeAllMiActiveNodes(spId);
  }
}
