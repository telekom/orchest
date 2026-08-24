package io.telekom.orchest.enginecore.bpmn.execution.impl;

import static io.telekom.orchest.enginecore.bpmn.utils.MultiInstanceUtils.*;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.MultiInstanceLoopCharacteristics;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.node.ActivityNode;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.api.core.request.Variables;
import io.telekom.orchest.api.core.utils.StateChangeUtils;
import io.telekom.orchest.enginecore.bpmn.ServiceTaskHandlerAdapter;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.utils.IODataMappingsUtils;
import io.telekom.orchest.enginecore.feel.FeelEvaluationEngine;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Executor for Service Tasks. Responsible for creating worker events and dispatching them via the
 * {@link ServiceTaskHandlerAdapter}. Supports both standard and multi-instance (sequential and
 * parallel) execution.
 */
@Slf4j
@RequiredArgsConstructor
public class ServiceTaskExecutor implements NodeExecutor {

  private final ServiceTaskHandlerAdapter serviceTaskHandlerAdapter;
  private final Map<String, Boolean> workerRegistryCache;

  /**
   * Executes the service task node.
   *
   * @param instance The process instance.
   * @param node The service task node to execute.
   * @param context The execution context.
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    if (!(node instanceof ServiceTaskNode serviceTaskNode)) {
      throw new IllegalStateException("failed, invalid node in executor: " + node.getType());
    }
    if (serviceTaskNode.getWorkerType() == null) {
      throw new IllegalStateException("failed, worker type missing in the: " + node.getType());
    }

    log.info(
        "Sending Service Task event for processInstanceId: {} with worker:{}",
        instance.getProcessInstanceId(),
        serviceTaskNode.getWorkerType());
    IODataMappingsUtils.setDataMappings(node, instance.getVariables());

    // add connector handling
    //        if((boolean) node.getProperties().getOrDefault(IS_CONNECTOR, false)){
    //            CONNECTOR_TYPE
    //        }

    if (isMultiInstance(serviceTaskNode)) {
      handleMultiInstance(instance, serviceTaskNode);
    } else {
      sendWorkerEvent(instance, serviceTaskNode);
    }
  }

  private void sendWorkerEvent(ProcessInstance instance, ServiceTaskNode serviceTaskNode) {
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .nodeInformation(serviceTaskNode)
            .processInstanceId(instance.getProcessInstanceId())
            .processDefinitionId(instance.getProcessDefinitionId())
            .activityName(serviceTaskNode.getName())
            .variables(Variables.builder().variables(instance.getVariables()).build())
            .activityId(serviceTaskNode.getId())
            .isCommonWorker(
                workerRegistryCache.getOrDefault(serviceTaskNode.getWorkerType(), false))
            .state(NodeState.TRIGGERED)
            .version(instance.getVersion())
            .retries(serviceTaskNode.getRetries())
            .type(serviceTaskNode.getWorkerType())
            .stateChanges(List.of(StateChangeUtils.buildStateChanges(NodeState.TRIGGERED)))
            .build();
    serviceTaskHandlerAdapter.handle(event);
    IODataMappingsUtils.removeDataMappings(
        serviceTaskNode.getInputMappings(), instance.getVariables());
  }

  private void handleMultiInstance(ProcessInstance instance, ActivityNode node) {
    MultiInstanceLoopCharacteristics mi = node.getMultiInstanceLoopCharacteristics();
    String inputCollectionVariableKey = mi.getCollection();
    Optional<Object> inputCollectionVariables =
        FeelEvaluationEngine.evaluateExpression(
            inputCollectionVariableKey, instance.getVariables());

    if (inputCollectionVariables.isPresent()
        && (inputCollectionVariables.get() instanceof List<?> inputVariablesList)) {
      if (mi.isSequential()) {
        // Sequential
        OptionalInt currentLoopCounter =
            IntStream.range(0, inputVariablesList.size())
                .filter(counter -> instance.containsState(loopCounterKey(node, counter)))
                .findFirst();
        int loopCounter;
        if (currentLoopCounter.isEmpty()) {
          loopCounter = 0;
        } else {
          loopCounter = currentLoopCounter.getAsInt() + 1;
        }

        if (loopCounter >= inputVariablesList.size()) {
          return; // should not come here else resume the code
        } else {
          instance.putState(loopCounterKey(node, loopCounter), loopCounter);
          instance.getVariables().put("loopCounter", loopCounter);
          instance.getVariables().put(mi.getElementVariable(), inputVariablesList.get(loopCounter));
          instance.addActiveNode(node.getId());
          sendWorkerEvent(instance, (ServiceTaskNode) node);
          // remove loop variables
          instance.getVariables().remove("loopCounter");
          instance.getVariables().remove(mi.getElementVariable());
        }
      } else {
        instance.addActiveNode(node.getId());
        IntStream.range(0, inputVariablesList.size())
            .forEach(
                counter -> {
                  instance.putState(loopCounterKey(node, counter), counter);
                  instance.getVariables().put("loopCounter", counter);
                  instance
                      .getVariables()
                      .put(mi.getElementVariable(), inputVariablesList.get(counter));
                  sendWorkerEvent(instance, (ServiceTaskNode) node);
                  // remove loop variables
                  instance.getVariables().remove("loopCounter");
                  instance.getVariables().remove(mi.getElementVariable());
                });
      }
    } else {
      log.warn(
          "Multi-instance collection expression '{}' did not evaluate to a collection",
          inputCollectionVariableKey);
      // TBC: or should we fail here??
    }
  }
}
