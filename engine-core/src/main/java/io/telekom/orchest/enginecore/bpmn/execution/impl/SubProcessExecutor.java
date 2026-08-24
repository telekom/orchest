package io.telekom.orchest.enginecore.bpmn.execution.impl;

import static io.telekom.orchest.enginecore.bpmn.utils.MultiInstanceUtils.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.MultiInstanceLoopCharacteristics;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.SubProcessNode;
import io.telekom.orchest.api.core.model.bpmn.state.ExecutionStateKey;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.utils.IODataMappingsUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for expanded SubProcess nodes. Supports standard (single-instance) and multi-instance
 * (sequential and parallel) execution.
 *
 * <p>For multi-instance subprocesses:
 *
 * <ul>
 *   <li><b>Sequential:</b> Executes one instance at a time. On completion, starts the next
 *       iteration.
 *   <li><b>Parallel:</b> Starts all instances immediately. Each instance runs independently until
 *       hitting wait states. Uses MI-indexed active node keys to distinguish instances.
 * </ul>
 *
 * <p>Multi-instance configuration supports:
 *
 * <ul>
 *   <li>Input collection: FEEL expression evaluating to a list
 *   <li>Element variable: variable name bound to the current element
 *   <li>Output collection: variable name to store collected outputs
 *   <li>Output element: FEEL expression evaluated per instance for output
 *   <li>Completion condition: FEEL expression for early completion
 * </ul>
 */
public class SubProcessExecutor implements NodeExecutor {
  private static final Logger log = LoggerFactory.getLogger(SubProcessExecutor.class);

  /**
   * Executes the subprocess node, entering either single-instance or multi-instance execution.
   *
   * @param instance the active process instance
   * @param node the subprocess node to execute
   * @param context the execution context
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    if (!(node instanceof SubProcessNode subProcess)) {
      log.warn("SubProcessExecutor received non-SubProcess node: {}", node.getType());
      return;
    }

    ProcessDefinition processDefinition = instance.getProcessDefinition();

    if (isMultiInstance(subProcess)) {
      handleMultiInstance(instance, subProcess, context, processDefinition);
    } else {
      // Standard single-instance subprocess execution
      log.info("Entering SubProcess: {}", node.getName());
      IODataMappingsUtils.setDataMappings(node, instance.getVariables());

      if (subProcess.getStartNodeId() != null) {
        context.proceed(
            instance, processDefinition.getNodes().get(subProcess.getStartNodeId()), node.getId());
      } else {
        log.warn("SubProcess {} has no Start Event!", node.getName());
      }
    }
  }

  /** Handles multi-instance subprocess execution (both sequential and parallel). */
  private void handleMultiInstance(
      ProcessInstance instance,
      SubProcessNode subProcess,
      ExecutionContext context,
      ProcessDefinition definition) {
    MultiInstanceLoopCharacteristics mi = subProcess.getMultiInstanceLoopCharacteristics();
    List<Object> inputCollection = evaluateInputCollection(mi, instance.getVariables());

    if (inputCollection.isEmpty()) {
      log.info(
          "Multi-instance SubProcess {} has empty input collection. Completing immediately.",
          subProcess.getName());
      // Initialize empty output collection if configured
      if (mi.getOutputCollection() != null) {
        instance.getVariables().put(mi.getOutputCollection(), new ArrayList<>());
      }
      // Remove subprocess from active and let engine process outgoing
      instance.removeActiveNode(subProcess.getId());
      return;
    }

    int collectionSize = inputCollection.size();
    log.info(
        "Multi-instance SubProcess {} starting with {} instances (sequential={})",
        subProcess.getName(),
        collectionSize,
        mi.isSequential());

    // Initialize MI state
    instance.putState(loopSizeKey(subProcess), collectionSize);
    instance.putState(
        new ExecutionStateKey.MultiInstanceSubProcessCompleted(subProcess.getId()), 0);

    // Initialize output collection with nulls
    List<Object> outputCollection = new ArrayList<>(Collections.nCopies(collectionSize, null));
    instance.putState(
        new ExecutionStateKey.MultiInstanceSubProcessOutput(subProcess.getId()), outputCollection);

    // Also set the output collection variable so completion conditions can reference it
    if (mi.getOutputCollection() != null) {
      instance.getVariables().put(mi.getOutputCollection(), outputCollection);
    }

    if (mi.isSequential()) {
      // Sequential: execute first instance only; next will be triggered on completion
      executeSubProcessInstance(instance, subProcess, context, definition, mi, inputCollection, 0);
    } else {
      // Parallel: execute all instances
      for (int i = 0; i < collectionSize; i++) {
        executeSubProcessInstance(
            instance, subProcess, context, definition, mi, inputCollection, i);
      }
    }
  }

  /**
   * Executes a single instance of a multi-instance subprocess. Sets the MI context on the
   * ProcessInstance so all child node operations use MI-indexed active node keys.
   */
  private void executeSubProcessInstance(
      ProcessInstance instance,
      SubProcessNode subProcess,
      ExecutionContext context,
      ProcessDefinition definition,
      MultiInstanceLoopCharacteristics mi,
      List<Object> inputCollection,
      int instanceIndex) {
    log.info(
        "Starting MI SubProcess {} instance {} of {}",
        subProcess.getName(),
        instanceIndex,
        inputCollection.size());

    // Store loop counter in execution state
    instance.putState(loopCounterKey(subProcess, instanceIndex), instanceIndex);

    // Set element variable and loop counter in process variables
    instance.getVariables().put("loopCounter", instanceIndex);
    if (mi.getElementVariable() != null) {
      instance.getVariables().put(mi.getElementVariable(), inputCollection.get(instanceIndex));
    }

    // Apply subprocess input data mappings (e.g., =elem -> orderItem)
    IODataMappingsUtils.setDataMappings(subProcess, instance.getVariables());

    // Set MI context so all child node add/removeActiveNode use MI-indexed keys
    instance.setMultiInstanceContext(subProcess.getId(), instanceIndex);
    try {
      BaseNode startNode = definition.getNodes().get(subProcess.getStartNodeId());
      if (startNode != null) {
        context.proceed(instance, startNode, subProcess.getId());
      } else {
        log.warn(
            "MI SubProcess {} instance {} has no Start Event!",
            subProcess.getName(),
            instanceIndex);
      }
    } finally {
      instance.clearMultiInstanceContext();
    }

    // Clean up temporary loop variables from main variables
    instance.getVariables().remove("loopCounter");
    if (mi.getElementVariable() != null) {
      instance.getVariables().remove(mi.getElementVariable());
    }
  }
}
