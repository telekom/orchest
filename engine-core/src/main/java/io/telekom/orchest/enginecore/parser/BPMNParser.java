package io.telekom.orchest.enginecore.parser;

import static io.telekom.orchest.enginecore.parser.BPMNExtensionExtractor.*;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.*;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.model.bpmn.EventType;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.model.bpmn.ScopeType;
import io.telekom.orchest.api.core.model.bpmn.SequenceFlow;
import io.telekom.orchest.api.core.model.bpmn.node.*;
import io.telekom.orchest.enginecore.bpmn.utils.XMLUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

/** Parses BPMN 2.0 XML into the Orchest {@link ProcessDefinition} domain model. */
@Slf4j
public class BPMNParser {

  public static final String MESSAGE_NAME_PROPERTY = "messageName";
  public static final String SIGNAL_NAME_PROPERTY = "signalRef";
  public static final String TIMER_DURATION_NAME_PROPERTY = "timerDuration";
  public static final String TIMER_CYCLE_NAME_PROPERTY = "timerCycle";
  public static final String TIMER_DATE_NAME_PROPERTY = "timerDate";
  public static final String MESSAGE_CORRELATION_PROPERTY = "messageCorrelationKey";
  public static final String PROPAGATE_ALL_PARENT_VARIABLES_PROPERTY =
      "propagateAllParentVariables";
  public static final String PROPAGATE_ALL_CHILD_VARIABLES_PROPERTY = "propagateAllChildVariables";

  /**
   * Parses a BPMN 2.0 XML string into a ProcessDefinition.
   *
   * @param bpmnXML the raw BPMN XML content
   * @return the parsed process definition with all nodes, flows, and extensions resolved
   */
  public static ProcessDefinition parse(String bpmnXML) {
    String minifiedXml = XMLUtils.minifyXml(bpmnXML);
    ProcessDefinition parse =
        parse(new ByteArrayInputStream(minifiedXml.getBytes(StandardCharsets.UTF_8)));
    parse.setDefinitionXML(minifiedXml);
    return parse;
  }

  private static ProcessDefinition parse(InputStream inputStream) {
    BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);

    Process process = modelInstance.getModelElementsByType(Process.class).iterator().next();
    String processId = process.getId();
    String processName = process.getName() != null ? process.getName() : processId;
    Boolean isExecutable = readIsExecutableAttribute(process);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId(processId)
            .name(processName)
            .isExecutable(isExecutable)
            .build();

    log.info("Parsing BPMN process: {} ({})", processId, processName);

    Collection<FlowNode> flowNodes = modelInstance.getModelElementsByType(FlowNode.class);
    Collection<io.camunda.zeebe.model.bpmn.instance.SequenceFlow> bpmnSequenceFlows =
        modelInstance.getModelElementsByType(
            io.camunda.zeebe.model.bpmn.instance.SequenceFlow.class);

    // Phase 1: Create all nodes
    for (FlowNode flowNode : flowNodes) {
      BaseNode node = createNodeFromFlowNode(flowNode);
      if (node != null) {
        node.setScopeId(processId);
        node.setScopeType(ScopeType.PROCESS);
        definition.getNodes().put(node.getId(), node);
      }
    }

    // Phase 2: Process subprocesses and assign scopes
    processSubprocesses(modelInstance, definition, processId);

    // Phase 3: Process boundary events
    processBoundaryEvents(modelInstance, definition);

    // Phase 4: Process sequence flows
    processSequenceFlows(bpmnSequenceFlows, definition);

    // Phase 5: Identify start node
    String startNodeId = findStartNodeId(definition);
    if (startNodeId != null) {
      definition.setStartNodeId(startNodeId);
    }

    return definition;
  }

  /**
   * Creates a BaseNode from a BPMN FlowNode element. Delegates extension property extraction to
   * {@link BPMNExtensionExtractor}.
   */
  private static BaseNode createNodeFromFlowNode(FlowNode flowNode) {
    String id = flowNode.getId();
    String name = flowNode.getName() != null ? flowNode.getName() : id;

    return switch (flowNode) {
      case StartEvent startEvent -> {
        EventType eventType = extractEventType(startEvent);
        EventNode eventNode = new EventNode(id, name, NodeType.START_EVENT, eventType);
        extractDataMappings(startEvent, eventNode);
        extractEventProperties(startEvent, eventNode);
        yield eventNode;
      }

      case EndEvent endEvent -> {
        EventType eventType = extractEventType(endEvent);
        EventNode eventNode = new EventNode(id, name, NodeType.END_EVENT, eventType);
        extractDataMappings(endEvent, eventNode);
        extractEventProperties(endEvent, eventNode);
        yield eventNode;
      }

      case IntermediateCatchEvent catchEvent -> {
        EventType eventType = extractEventType(catchEvent);
        EventNode eventNode = new EventNode(id, name, NodeType.INTERMEDIATE_CATCH_EVENT, eventType);
        extractDataMappings(catchEvent, eventNode);
        extractEventProperties(catchEvent, eventNode);
        yield eventNode;
      }

      case IntermediateThrowEvent throwEvent -> {
        EventType eventType = extractEventType(throwEvent);
        EventNode eventNode = new EventNode(id, name, NodeType.INTERMEDIATE_THROW_EVENT, eventType);
        extractDataMappings(throwEvent, eventNode);
        extractEventProperties(throwEvent, eventNode);
        yield eventNode;
      }

      case BoundaryEvent boundaryEvent -> {
        EventType eventType = extractEventType(boundaryEvent);
        EventNode eventNode = new EventNode(id, name, NodeType.BOUNDARY_EVENT, eventType);
        extractDataMappings(boundaryEvent, eventNode);
        extractEventProperties(boundaryEvent, eventNode);
        yield eventNode;
      }

      case ServiceTask serviceTask -> {
        ServiceTaskNode node = new ServiceTaskNode(id, name);
        extractZeebeTaskDefinition(serviceTask, node);
        extractDataMappings(serviceTask, node);
        extractMultiInstanceLoopCharacteristics(serviceTask, node);
        yield node;
      }

      case UserTask userTask -> {
        UserTaskNode node = new UserTaskNode(id, name);
        extractUserTaskAssignment(userTask, node);
        extractUserTaskSchedule(userTask, node);
        extractUserTaskFormDefinition(userTask, node);
        extractDataMappings(userTask, node);
        extractMultiInstanceLoopCharacteristics(userTask, node);
        yield node;
      }

      case ReceiveTask receiveTask -> {
        ReceiveTaskNode node = new ReceiveTaskNode(id, name);
        if (receiveTask.getMessage() != null) {
          node.setMessageRef(receiveTask.getMessage().getName());
        }
        extractDataMappings(receiveTask, node);
        extractMultiInstanceLoopCharacteristics(receiveTask, node);
        yield node;
      }

      case SendTask sendTask -> {
        SendTaskNode node = new SendTaskNode(id, name);
        if (sendTask.getMessage() != null) {
          node.setMessageRef(sendTask.getMessage().getName());
          node.getProperties().put("messageRef", sendTask.getMessage().getName());
        }
        extractDataMappings(sendTask, node);
        extractMultiInstanceLoopCharacteristics(sendTask, node);
        yield node;
      }

      case ManualTask manualTask -> {
        ManualTaskNode node = new ManualTaskNode(id, name);
        extractDataMappings(manualTask, node);
        extractMultiInstanceLoopCharacteristics(manualTask, node);
        yield node;
      }

      case ScriptTask scriptTask -> {
        ScriptTaskNode node = new ScriptTaskNode(id, name);
        node.setScriptFormat(scriptTask.getScriptFormat());
        if (scriptTask.getExtensionElements() != null) {
          Collection<io.camunda.zeebe.model.bpmn.impl.instance.zeebe.ZeebeScriptImpl>
              scriptMappings =
                  scriptTask
                      .getExtensionElements()
                      .getElementsQuery()
                      .filterByType(
                          io.camunda.zeebe.model.bpmn.impl.instance.zeebe.ZeebeScriptImpl.class)
                      .list();

          if (scriptMappings != null) {
            io.camunda.zeebe.model.bpmn.impl.instance.zeebe.ZeebeScriptImpl scriptMapping =
                scriptMappings.iterator().next();
            node.setScript(scriptMapping.getExpression());
            node.setResultVariable(scriptMapping.getResultVariable());
            node.setScriptFormat(scriptMapping.getTextContent());
          }
        }
        extractDataMappings(scriptTask, node);
        extractMultiInstanceLoopCharacteristics(scriptTask, node);
        yield node;
      }

      case BusinessRuleTask businessRuleTask -> {
        BusinessRuleTaskNode node = new BusinessRuleTaskNode(id, name);
        extractZeebeCalledDecision(businessRuleTask, node);
        extractDataMappings(businessRuleTask, node);
        extractMultiInstanceLoopCharacteristics(businessRuleTask, node);
        yield node;
      }

      case Task task -> {
        TaskNode node = new TaskNode(id, name);
        extractDataMappings(task, node);
        yield node;
      }

      case CallActivity callActivity -> {
        CallActivityNode node = new CallActivityNode(id, name);
        extractZeebeCalledElement(callActivity, node);
        extractDataMappings(callActivity, node);
        extractMultiInstanceLoopCharacteristics(callActivity, node);
        yield node;
      }

      case SubProcess subProcess -> {
        SubProcessNode node = new SubProcessNode(id, name);
        if (isEventSubprocess(subProcess)) {
          node.setTriggeredByEvent(true);
          node.getProperties().put("triggeredByEvent", true);
        }
        extractDataMappings(subProcess, node);
        extractMultiInstanceLoopCharacteristics(subProcess, node);
        yield node;
      }

      case ExclusiveGateway exclusiveGateway ->
          new GatewayNode(id, name, NodeType.EXCLUSIVE_GATEWAY);
      case ParallelGateway parallelGateway -> new GatewayNode(id, name, NodeType.PARALLEL_GATEWAY);
      case InclusiveGateway inclusiveGateway ->
          new GatewayNode(id, name, NodeType.INCLUSIVE_GATEWAY);
      case EventBasedGateway eventBasedGateway ->
          new GatewayNode(id, name, NodeType.EVENT_BASED_GATEWAY);

      default -> {
        log.warn(
            "Unsupported BPMN element type: {} (ID: {})",
            flowNode.getElementType().getTypeName(),
            id);
        yield null;
      }
    };
  }

  /**
   * Reads the {@code isExecutable} attribute literally from the BPMN {@code <bpmn:process>} tag.
   * Returns {@code null} when the attribute is absent so that callers can distinguish "legacy /
   * unspecified" from an explicit {@code true}/{@code false}; callers may treat {@code null} as
   * executable for backward compatibility.
   */
  private static Boolean readIsExecutableAttribute(Process process) {
    String raw = process.getAttributeValue("isExecutable");
    if (raw == null || raw.isBlank()) {
      return true;
    }
    return Boolean.parseBoolean(raw.trim());
  }

  private static void processSubprocesses(
      BpmnModelInstance modelInstance, ProcessDefinition definition, String processId) {
    Collection<SubProcess> subProcessElements =
        modelInstance.getModelElementsByType(SubProcess.class);

    for (SubProcess subProcessElement : subProcessElements) {
      String subProcessId = subProcessElement.getId();
      BaseNode subProcessNode = definition.getNodes().get(subProcessId);

      if (subProcessNode instanceof SubProcessNode subProcessNodeInstance) {
        boolean eventSubprocess = isEventSubprocess(subProcessElement);

        subProcessNodeInstance.setScopeId(processId);
        subProcessNodeInstance.setScopeType(
            eventSubprocess ? ScopeType.EVENT_SUBPROCESS : ScopeType.SUBPROCESS);

        Collection<FlowNode> childFlowNodes =
            subProcessElement.getChildElementsByType(FlowNode.class);
        for (FlowNode childFlowNode : childFlowNodes) {
          String childNodeId = childFlowNode.getId();
          BaseNode childNode = definition.getNodes().get(childNodeId);
          if (childNode != null) {
            subProcessNodeInstance.addChildNode(childNodeId, childNode.getType());
            childNode.setScopeId(subProcessId);
            childNode.setScopeType(
                eventSubprocess ? ScopeType.EVENT_SUBPROCESS : ScopeType.SUBPROCESS);
          }
        }
      }
    }
  }

  private static void processBoundaryEvents(
      BpmnModelInstance modelInstance, ProcessDefinition definition) {
    Collection<BoundaryEvent> boundaryEvents =
        modelInstance.getModelElementsByType(BoundaryEvent.class);

    for (BoundaryEvent boundaryEvent : boundaryEvents) {
      Activity attachedActivity = boundaryEvent.getAttachedTo();
      if (attachedActivity != null) {
        String boundaryEventId = boundaryEvent.getId();
        String attachedActivityId = attachedActivity.getId();

        BaseNode boundaryNode = definition.getNodes().get(boundaryEventId);
        BaseNode activityNode = definition.getNodes().get(attachedActivityId);

        if (boundaryNode instanceof EventNode eventNode && activityNode != null) {
          eventNode.attachTo(attachedActivityId);
          if (activityNode instanceof ActivityNode activityNodeInstance) {
            activityNodeInstance.addBoundaryEvent(boundaryEventId);
          }
          boundaryNode.setScopeId(activityNode.getScopeId());
          boundaryNode.setScopeType(activityNode.getScopeType());
        }
      }
    }
  }

  private static void processSequenceFlows(
      Collection<io.camunda.zeebe.model.bpmn.instance.SequenceFlow> bpmnSequenceFlows,
      ProcessDefinition definition) {
    for (io.camunda.zeebe.model.bpmn.instance.SequenceFlow bpmnFlow : bpmnSequenceFlows) {
      String flowId = bpmnFlow.getId();
      String sourceId = bpmnFlow.getSource().getId();
      String targetId = bpmnFlow.getTarget().getId();

      SequenceFlow sequenceFlow = new SequenceFlow(flowId, sourceId, targetId);
      definition.getSequenceFlows().put(flowId, sequenceFlow);

      BaseNode sourceNode = definition.getNodes().get(sourceId);
      if (sourceNode != null) {
        sourceNode.getOutgoingSequenceFlowIds().put(flowId, targetId);
      }

      BaseNode targetNode = definition.getNodes().get(targetId);
      if (targetNode != null) {
        targetNode.getIncomingSequenceFlowIds().put(flowId, sourceId);
      }

      if (bpmnFlow.getSource() instanceof ExclusiveGateway
          || bpmnFlow.getSource() instanceof InclusiveGateway) {
        processGatewayCondition(bpmnFlow, definition, sourceId, targetId);
      }
    }
  }

  private static void processGatewayCondition(
      io.camunda.zeebe.model.bpmn.instance.SequenceFlow bpmnFlow,
      ProcessDefinition definition,
      String sourceId,
      String targetId) {
    ConditionExpression conditionExpression = bpmnFlow.getConditionExpression();
    String defaultNodeId = bpmnFlow.getSource().getAttributeValue("default");

    BaseNode sourceNode = definition.getNodes().get(sourceId);
    if (sourceNode instanceof GatewayNode gatewayNode) {
      if (conditionExpression != null) {
        String rawCondition = conditionExpression.getTextContent();
        String finalCondition =
            rawCondition.startsWith("=") ? rawCondition.substring(1) : rawCondition;
        gatewayNode.setCondition(targetId, finalCondition);
      }
      if (defaultNodeId != null) {
        gatewayNode.setDefaultNode(defaultNodeId);
      }
    }
  }

  private static String findStartNodeId(ProcessDefinition definition) {
    Set<String> subprocessChildNodeIds = new HashSet<>();
    for (BaseNode node : definition.getNodes().values()) {
      if (node instanceof SubProcessNode subProcessNode) {
        subprocessChildNodeIds.addAll(subProcessNode.getChildNodeIds());
      }
    }

    final Set<String> finalSubprocessChildNodeIds = subprocessChildNodeIds;
    return definition.getNodes().values().stream()
        .filter(node -> node.getType() == NodeType.START_EVENT)
        .filter(node -> !finalSubprocessChildNodeIds.contains(node.getId()))
        .map(BaseNode::getId)
        .findFirst()
        .orElse(null);
  }
}
