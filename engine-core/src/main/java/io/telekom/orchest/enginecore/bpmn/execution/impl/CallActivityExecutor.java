package io.telekom.orchest.enginecore.bpmn.execution.impl;

import static io.telekom.orchest.enginecore.bpmn.utils.MultiInstanceUtils.*;

import io.telekom.orchest.api.core.adapters.data.model.ParentProcessActivity;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.ExecutionLogEntry;
import io.telekom.orchest.api.core.model.bpmn.MultiInstanceLoopCharacteristics;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.CallActivityNode;
import io.telekom.orchest.api.core.model.bpmn.state.ExecutionStateKey;
import io.telekom.orchest.enginecore.bpmn.WorkflowEngine;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.utils.VariablesUtils;
import io.telekom.orchest.enginecore.feel.FeelEvaluationEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for Call Activity nodes. Spawns child process instances for the referenced process
 * definition and leaves the current process in a wait state until the child completes. Supports
 * multi-instance (parallel) execution.
 */
public class CallActivityExecutor implements NodeExecutor {
  private static final Logger log = LoggerFactory.getLogger(CallActivityExecutor.class);

  private final WorkflowEngine workflowEngine;

  /**
   * Creates a new CallActivityExecutor.
   *
   * @param workflowEngine the workflow engine used to start child process instances
   */
  public CallActivityExecutor(WorkflowEngine workflowEngine) {
    this.workflowEngine = workflowEngine;
  }

  /**
   * Executes the call activity by starting a child process instance. Does not proceed immediately
   * as the call activity is a wait state.
   *
   * @param instance the active process instance
   * @param node the call activity node to execute
   * @param context the execution context
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    if (node instanceof CallActivityNode callActivity) {
      String calledProcessId =
          getEvaluatedCalledProcessId(callActivity.getCalledProcessId(), instance);

      log.info(
          "Executing Call Activity: {}. Spawning process: {}", node.getName(), calledProcessId);
      //            IODataMappingsUtils.setDataMappings(node, instance.getVariables());

      ParentProcessActivity parentProcessActivity =
          new ParentProcessActivity(instance.getProcessInstanceId(), node);

      if (isMultiInstance(callActivity)) {
        handleMultiInstance(instance, callActivity, parentProcessActivity);
      } else {
        invokeActivity(instance, callActivity, parentProcessActivity);
      }

      // Do NOT proceed immediately. CallActivity is a wait state.
      // Completion of child process will trigger resumption.
    }
  }

  private void invokeActivity(
      ProcessInstance instance,
      CallActivityNode callActivity,
      ParentProcessActivity parentProcessActivity) {
    String calledProcessId =
        getEvaluatedCalledProcessId(callActivity.getCalledProcessId(), instance);
    ProcessInstance childProcessInstance =
        workflowEngine.startProcess(
            calledProcessId,
            null,
            VariablesUtils.getActivityInputVariables(callActivity, instance),
            parentProcessActivity);

    // Store child process instance ID in execution state for logging metadata
    String childProcessInstanceId =
        childProcessInstance != null ? childProcessInstance.getProcessInstanceId() : null;
    instance.putState(
        new ExecutionStateKey.CallActivityChild(callActivity.getId()), childProcessInstanceId);

    // Set metadata directly on the execution log entry
    ExecutionLogEntry logEntry = instance.getExecutionHistory().get(callActivity.getId());
    if (logEntry != null && childProcessInstanceId != null) {
      logEntry.getMetaData().put("childProcessInstanceId", childProcessInstanceId);
    }

    log.info(
        "Call Activity {} started child process {}. Waiting for completion.",
        callActivity.getName(),
        childProcessInstanceId);
  }

  private void handleMultiInstance(
      ProcessInstance instance,
      CallActivityNode callActivity,
      ParentProcessActivity parentProcessActivity) {
    MultiInstanceLoopCharacteristics mi = callActivity.getMultiInstanceLoopCharacteristics();
    String inputCollectionVariableKey = mi.getCollection();
    Optional<Object> inputCollectionVariables =
        FeelEvaluationEngine.evaluateExpression(
            inputCollectionVariableKey, instance.getVariables());
    String calledProcessId =
        getEvaluatedCalledProcessId(callActivity.getCalledProcessId(), instance);

    if (inputCollectionVariables.isPresent()
        && (inputCollectionVariables.get() instanceof List<?> inputVariablesList)) {
      instance.putState(loopSizeKey(callActivity), inputVariablesList.size());
      if (mi.isSequential()) {
        // TODO: not yet supported, Placeholder.
      } else {
        instance.addActiveNode(callActivity.getId());
        List<String> childProcessInstanceIds = new ArrayList<>();
        IntStream.range(0, inputVariablesList.size())
            .forEach(
                counter -> {
                  instance.putState(loopCounterKey(callActivity, counter), counter);
                  instance.getVariables().put("loopCounter", counter);
                  instance
                      .getVariables()
                      .put(mi.getElementVariable(), inputVariablesList.get(counter));
                  ProcessInstance childProcessInstance =
                      workflowEngine.startProcess(
                          calledProcessId,
                          null,
                          VariablesUtils.getActivityInputVariables(callActivity, instance),
                          parentProcessActivity);
                  instance.putState(
                      new ExecutionStateKey.CallActivityChildCounter(counter),
                      childProcessInstance.getProcessInstanceId());
                  childProcessInstanceIds.add(childProcessInstance.getProcessInstanceId());
                  // remove loop variables
                  instance.getVariables().remove("loopCounter");
                  instance.getVariables().remove(mi.getElementVariable());
                });

        // Set all child IDs directly on the execution log entry metadata
        ExecutionLogEntry logEntry = instance.getExecutionHistory().get(callActivity.getId());
        if (logEntry != null && !childProcessInstanceIds.isEmpty()) {
          logEntry.getMetaData().put("childProcessInstanceIds", childProcessInstanceIds);
        }
      }
    }
  }

  private String getEvaluatedCalledProcessId(
      String calledProcessIdValue, ProcessInstance processInstance) {
    if (calledProcessIdValue != null && !calledProcessIdValue.startsWith("=")) {
      return calledProcessIdValue;
    }
    try {
      Optional<Object> evaluatedProcessId =
          FeelEvaluationEngine.evaluateExpression(
              calledProcessIdValue, processInstance.getVariables());
      if (evaluatedProcessId.isEmpty()) {
        throw new RuntimeException(
            "Evaluated process id "
                + calledProcessIdValue
                + " is empty for process instance "
                + processInstance.getProcessInstanceId());
      }
      return (String) evaluatedProcessId.get();
    } catch (Exception e) {
      log.error(
          "Evaluated process id: {} is empty for process instance: {}",
          calledProcessIdValue,
          processInstance.getProcessInstanceId(),
          e);
      throw new RuntimeException(
          "Evaluated process id "
              + calledProcessIdValue
              + " is empty for process instance "
              + processInstance.getProcessInstanceId());
    }
  }
}
