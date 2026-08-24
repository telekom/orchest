package io.telekom.orchest.enginecore.parser;

import static io.telekom.orchest.enginecore.parser.BPMNParser.*;

import io.camunda.zeebe.model.bpmn.impl.instance.zeebe.ZeebeSubscriptionImpl;
import io.camunda.zeebe.model.bpmn.instance.*;
import io.camunda.zeebe.model.bpmn.instance.zeebe.*;
import io.telekom.orchest.api.core.model.bpmn.DataMapping;
import io.telekom.orchest.api.core.model.bpmn.EventType;
import io.telekom.orchest.api.core.model.bpmn.node.*;
import io.telekom.orchest.connectors.BpmnConnectorParser;
import java.util.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Extracts Zeebe/Camunda extension element properties from BPMN model elements into the Orchest
 * domain node model. Handles IO mappings, task definitions, event properties, user task
 * assignments, and multi-instance configurations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BPMNExtensionExtractor {

  private static final String SERVICE_TASK_DEFAULT_RETRIES = "3";

  // ========================================================================================
  // Event type detection
  // ========================================================================================

  /** Extracts the event type from an event element's event definitions. */
  public static EventType extractEventType(Event event) {
    if (event == null) {
      return EventType.NONE;
    }

    Collection<EventDefinition> eventDefinitions = getEventDefinitions(event);

    if (hasEventDefinition(eventDefinitions, TimerEventDefinition.class)) {
      return EventType.TIMER;
    }
    if (hasEventDefinition(eventDefinitions, MessageEventDefinition.class)) {
      return EventType.MESSAGE;
    }
    if (hasEventDefinition(eventDefinitions, SignalEventDefinition.class)) {
      return EventType.SIGNAL;
    }
    if (hasEventDefinition(eventDefinitions, ErrorEventDefinition.class)) {
      return EventType.ERROR;
    }
    if (hasEventDefinition(eventDefinitions, EscalationEventDefinition.class)) {
      return EventType.ESCALATION;
    }
    if (hasEventDefinition(eventDefinitions, ConditionalEventDefinition.class)) {
      return EventType.CONDITIONAL;
    }
    if (hasEventDefinition(eventDefinitions, LinkEventDefinition.class)) {
      return EventType.LINK;
    }
    if (event instanceof EndEvent
        && hasEventDefinition(eventDefinitions, TerminateEventDefinition.class)) {
      return EventType.TERMINATE;
    }
    if (event instanceof BoundaryEvent
        && hasEventDefinition(eventDefinitions, CancelEventDefinition.class)) {
      return EventType.CANCEL;
    }

    return EventType.NONE;
  }

  /** Gets event definitions from an event element. */
  public static Collection<EventDefinition> getEventDefinitions(Event event) {
    if (event instanceof CatchEvent catchEvent) {
      return catchEvent.getEventDefinitions();
    } else if (event instanceof ThrowEvent throwEvent) {
      return throwEvent.getEventDefinitions();
    }
    return Collections.emptyList();
  }

  /** Checks if a subprocess is an event subprocess (has event-triggering start events). */
  public static boolean isEventSubprocess(SubProcess subProcess) {
    Collection<StartEvent> startEvents = subProcess.getChildElementsByType(StartEvent.class);
    for (StartEvent startEvent : startEvents) {
      Collection<EventDefinition> eventDefs = getEventDefinitions(startEvent);
      if (!eventDefs.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  // ========================================================================================
  // IO Data Mappings
  // ========================================================================================

  /** Extracts input and output data mappings from ZeebeIoMapping extension element. */
  public static void extractDataMappings(FlowNode flowNode, BaseNode node) {
    if (flowNode == null || node == null) {
      return;
    }

    if (flowNode.getExtensionElements() == null) {
      return;
    }

    Collection<ZeebeIoMapping> ioMappings =
        flowNode
            .getExtensionElements()
            .getElementsQuery()
            .filterByType(ZeebeIoMapping.class)
            .list();

    if (ioMappings.isEmpty()) {
      return;
    }

    ZeebeIoMapping ioMapping = ioMappings.iterator().next();

    Collection<ZeebeInput> zeebeInputs = ioMapping.getInputs();
    for (ZeebeInput zeebeInput : zeebeInputs) {
      String target = zeebeInput.getTarget();
      String source = zeebeInput.getSource();
      if (target != null && source != null) {
        node.getInputMappings().add(new DataMapping(target, source));
      }
    }

    Collection<ZeebeOutput> zeebeOutputs = ioMapping.getOutputs();
    for (ZeebeOutput zeebeOutput : zeebeOutputs) {
      String target = zeebeOutput.getTarget();
      String source = zeebeOutput.getSource();
      if (target != null && source != null) {
        node.getOutputMappings().add(new DataMapping(target, source));
      }
    }
  }

  // ========================================================================================
  // Event Properties
  // ========================================================================================

  /** Extracts event-specific properties and stores them in the event node's properties map. */
  public static void extractEventProperties(Event event, EventNode eventNode) {
    if (event == null || eventNode == null) {
      return;
    }

    Collection<EventDefinition> eventDefinitions = getEventDefinitions(event);

    // Timer
    TimerEventDefinition timerDef =
        getEventDefinition(eventDefinitions, TimerEventDefinition.class);
    if (timerDef != null) {
      if (timerDef.getTimeDuration() != null) {
        String val = timerDef.getTimeDuration().getTextContent();
        eventNode.setTimerDuration(val);
        eventNode.getProperties().put(TIMER_DURATION_NAME_PROPERTY, val);
      }
      if (timerDef.getTimeDate() != null) {
        String val = timerDef.getTimeDate().getTextContent();
        eventNode.setTimerDate(val);
        eventNode.getProperties().put(TIMER_DATE_NAME_PROPERTY, val);
      }
      if (timerDef.getTimeCycle() != null) {
        String val = timerDef.getTimeCycle().getTextContent();
        eventNode.setTimerCycle(val);
        eventNode.getProperties().put(TIMER_CYCLE_NAME_PROPERTY, val);
      }
    }

    // Message
    MessageEventDefinition messageDef =
        getEventDefinition(eventDefinitions, MessageEventDefinition.class);
    if (messageDef != null) {
      Message message = messageDef.getMessage();
      if (message != null) {
        eventNode.setMessageName(message.getName());
        eventNode.getProperties().put(MESSAGE_NAME_PROPERTY, message.getName());
        if (message.getExtensionElements() != null) {
          Collection<ZeebeSubscriptionImpl> subscriptions =
              message
                  .getExtensionElements()
                  .getElementsQuery()
                  .filterByType(ZeebeSubscriptionImpl.class)
                  .stream()
                  .toList();
          ZeebeSubscriptionImpl messageSubscription = subscriptions.iterator().next();
          String correlationKey = messageSubscription.getCorrelationKey();
          eventNode.setMessageCorrelationKey(correlationKey);
          eventNode.getProperties().put(MESSAGE_CORRELATION_PROPERTY, correlationKey);
        }
      }
    }

    // Signal
    SignalEventDefinition signalDef =
        getEventDefinition(eventDefinitions, SignalEventDefinition.class);
    if (signalDef != null) {
      Signal signal = signalDef.getSignal();
      eventNode.setSignalRef(signal.getName());
      eventNode.getProperties().put(SIGNAL_NAME_PROPERTY, signal.getName());
    }

    // Error
    ErrorEventDefinition errorDef =
        getEventDefinition(eventDefinitions, ErrorEventDefinition.class);
    if (errorDef != null && errorDef.getError() != null) {
      eventNode.setErrorRef(errorDef.getError().getId());
      eventNode.setErrorCode(errorDef.getError().getErrorCode());
      eventNode.getProperties().put("errorRef", errorDef.getError().getId());
      eventNode.getProperties().put("errorCode", errorDef.getError().getErrorCode());
    }

    // Escalation
    EscalationEventDefinition escalationDef =
        getEventDefinition(eventDefinitions, EscalationEventDefinition.class);
    if (escalationDef != null && escalationDef.getEscalation() != null) {
      eventNode.setEscalationRef(escalationDef.getEscalation().getId());
      eventNode.setEscalationCode(escalationDef.getEscalation().getEscalationCode());
      eventNode.getProperties().put("escalationRef", escalationDef.getEscalation().getId());
      eventNode
          .getProperties()
          .put("escalationCode", escalationDef.getEscalation().getEscalationCode());
    }

    // Conditional
    ConditionalEventDefinition conditionalDef =
        getEventDefinition(eventDefinitions, ConditionalEventDefinition.class);
    if (conditionalDef != null && conditionalDef.getCondition() != null) {
      String condition = conditionalDef.getCondition().getTextContent();
      String finalCondition = condition.startsWith("=") ? condition.substring(1) : condition;
      eventNode.setCondition(finalCondition);
      eventNode.getProperties().put("condition", finalCondition);
    }

    // Link
    LinkEventDefinition linkDef = getEventDefinition(eventDefinitions, LinkEventDefinition.class);
    if (linkDef != null && linkDef.getName() != null) {
      eventNode.setLinkName(linkDef.getName());
      eventNode.getProperties().put("linkName", linkDef.getName());
    }

    // Zeebe subscription
    if (event.getExtensionElements() != null) {
      Collection<ZeebeSubscription> subscriptions =
          event
              .getExtensionElements()
              .getElementsQuery()
              .filterByType(ZeebeSubscription.class)
              .list();
      if (!subscriptions.isEmpty()) {
        ZeebeSubscription subscription = subscriptions.iterator().next();
        eventNode.setZeebeCorrelationKey(subscription.getCorrelationKey());
        eventNode.getProperties().put("zeebeCorrelationKey", subscription.getCorrelationKey());
      }
    }

    // Cancel activity for boundary events
    if (event instanceof BoundaryEvent) {
      eventNode.setCancelActivity(true);
      eventNode.getProperties().put("cancelActivity", true);
    }
  }

  // ========================================================================================
  // Task Definitions
  // ========================================================================================

  /** Extracts Zeebe task definition (type + retries) from a service task. */
  public static void extractZeebeTaskDefinition(ServiceTask serviceTask, ServiceTaskNode node) {
    if (serviceTask.getExtensionElements() != null) {
      Collection<ZeebeTaskDefinition> taskDefs =
          serviceTask
              .getExtensionElements()
              .getElementsQuery()
              .filterByType(ZeebeTaskDefinition.class)
              .list();
      if (!taskDefs.isEmpty()) {
        ZeebeTaskDefinition taskDef = taskDefs.iterator().next();
        if (BpmnConnectorParser.isConnector(taskDef.getType())) {
          BpmnConnectorParser.parseConnector(taskDef.getType(), serviceTask, node);
        } else {
          node.setWorkerType(taskDef.getType());
          node.getProperties().put("taskType", taskDef.getType());
          int retries =
              StringUtils.isNumeric(taskDef.getRetries())
                  ? Integer.parseInt(taskDef.getRetries())
                  : Integer.parseInt(SERVICE_TASK_DEFAULT_RETRIES);
          node.setRetries(retries);
          node.getProperties().put("retries", String.valueOf(retries));
        }
      }
    }
  }

  /** Extracts Zeebe called decision from a business rule task. */
  public static void extractZeebeCalledDecision(
      BusinessRuleTask businessRuleTask, BusinessRuleTaskNode node) {
    if (businessRuleTask.getExtensionElements() != null) {
      Collection<ZeebeCalledDecision> calledDecisions =
          businessRuleTask
              .getExtensionElements()
              .getElementsQuery()
              .filterByType(ZeebeCalledDecision.class)
              .list();
      if (!calledDecisions.isEmpty()) {
        ZeebeCalledDecision calledDecision = calledDecisions.iterator().next();
        node.setDecisionId(calledDecision.getDecisionId());
        node.setResultVariable(calledDecision.getResultVariable());
      }
    }
  }

  /** Extracts Zeebe called element from a call activity. */
  public static void extractZeebeCalledElement(CallActivity callActivity, CallActivityNode node) {
    if (callActivity.getExtensionElements() != null) {
      Collection<ZeebeCalledElement> calledElements =
          callActivity
              .getExtensionElements()
              .getElementsQuery()
              .filterByType(ZeebeCalledElement.class)
              .list();
      if (!calledElements.isEmpty()) {
        ZeebeCalledElement calledElement = calledElements.iterator().next();
        node.setCalledProcessId(calledElement.getProcessId());
        node.setPropagateAllParentVariables(calledElement.isPropagateAllParentVariablesEnabled());
        node.setPropagateAllChildVariables(calledElement.isPropagateAllChildVariablesEnabled());
        node.getProperties()
            .put(
                PROPAGATE_ALL_PARENT_VARIABLES_PROPERTY,
                calledElement.isPropagateAllParentVariablesEnabled());
        node.getProperties()
            .put(
                PROPAGATE_ALL_CHILD_VARIABLES_PROPERTY,
                calledElement.isPropagateAllChildVariablesEnabled());
      }
    }
  }

  // ========================================================================================
  // User Task Properties
  // ========================================================================================

  /** Extracts Camunda 8 zeebe:AssignmentDefinition (assignee, candidateUsers, candidateGroups). */
  public static void extractUserTaskAssignment(UserTask userTask, UserTaskNode node) {
    if (userTask.getExtensionElements() == null) return;

    Collection<ZeebeAssignmentDefinition> assignments =
        userTask
            .getExtensionElements()
            .getElementsQuery()
            .filterByType(ZeebeAssignmentDefinition.class)
            .list();
    if (assignments.isEmpty()) return;

    ZeebeAssignmentDefinition assignment = assignments.iterator().next();

    String assignee = assignment.getAssignee();
    if (assignee != null && !assignee.isBlank()) {
      node.setAssignee(assignee.trim());
    }

    String candidateUsers = assignment.getCandidateUsers();
    if (candidateUsers != null && !candidateUsers.isBlank()) {
      List<String> users =
          Arrays.stream(candidateUsers.split(","))
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .toList();
      node.setCandidateUsers(new ArrayList<>(users));
    }

    String candidateGroups = assignment.getCandidateGroups();
    if (candidateGroups != null && !candidateGroups.isBlank()) {
      List<String> groups =
          Arrays.stream(candidateGroups.split(","))
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .toList();
      node.setCandidateGroups(new ArrayList<>(groups));
    }
  }

  /** Extracts Camunda 8 zeebe:TaskSchedule (dueDate, followUpDate). */
  public static void extractUserTaskSchedule(UserTask userTask, UserTaskNode node) {
    if (userTask.getExtensionElements() == null) return;

    Collection<ZeebeTaskSchedule> schedules =
        userTask
            .getExtensionElements()
            .getElementsQuery()
            .filterByType(ZeebeTaskSchedule.class)
            .list();
    if (schedules.isEmpty()) return;

    ZeebeTaskSchedule schedule = schedules.iterator().next();

    String dueDate = schedule.getDueDate();
    if (dueDate != null && !dueDate.isBlank()) {
      node.setDueDate(dueDate.trim());
    }

    String followUpDate = schedule.getFollowUpDate();
    if (followUpDate != null && !followUpDate.isBlank()) {
      node.setFollowUpDate(followUpDate.trim());
    }
  }

  /** Extracts Camunda 8 zeebe:FormDefinition (formKey, formId). */
  public static void extractUserTaskFormDefinition(UserTask userTask, UserTaskNode node) {
    if (userTask.getExtensionElements() == null) return;

    Collection<ZeebeFormDefinition> formDefs =
        userTask
            .getExtensionElements()
            .getElementsQuery()
            .filterByType(ZeebeFormDefinition.class)
            .list();
    if (formDefs.isEmpty()) return;

    ZeebeFormDefinition formDef = formDefs.iterator().next();
    String formKey = formDef.getFormKey();
    if (formKey != null && !formKey.isBlank()) {
      node.setFormKey(formKey.trim());
    }
    String formId = formDef.getFormId();
    if (formId != null && !formId.isBlank()) {
      node.getProperties().put("formId", formId.trim());
    }
  }

  // ========================================================================================
  // Multi-Instance
  // ========================================================================================

  /** Extracts Multi-Instance Loop Characteristics. */
  public static void extractMultiInstanceLoopCharacteristics(Activity activity, ActivityNode node) {
    LoopCharacteristics loopCharacteristics = activity.getLoopCharacteristics();
    if (loopCharacteristics
        instanceof io.camunda.zeebe.model.bpmn.instance.MultiInstanceLoopCharacteristics miLoop) {
      io.telekom.orchest.api.core.model.bpmn.MultiInstanceLoopCharacteristics mi =
          new io.telekom.orchest.api.core.model.bpmn.MultiInstanceLoopCharacteristics();
      mi.setSequential(miLoop.isSequential());

      if (miLoop.getCompletionCondition() != null) {
        mi.setCompletionCondition(miLoop.getCompletionCondition().getTextContent());
      }

      if (miLoop.getExtensionElements() != null) {
        Collection<ZeebeLoopCharacteristics> zeebeLoops =
            miLoop
                .getExtensionElements()
                .getElementsQuery()
                .filterByType(ZeebeLoopCharacteristics.class)
                .list();
        if (!zeebeLoops.isEmpty()) {
          ZeebeLoopCharacteristics zeebeLoop = zeebeLoops.iterator().next();
          mi.setCollection(zeebeLoop.getInputCollection());
          mi.setElementVariable(zeebeLoop.getInputElement());
          mi.setOutputCollection(zeebeLoop.getOutputCollection());
          mi.setOutputElement(zeebeLoop.getOutputElement());
        }
      }
      node.setMultiInstanceLoopCharacteristics(mi);
    }
  }

  // ========================================================================================
  // Private helpers
  // ========================================================================================

  private static <T extends EventDefinition> boolean hasEventDefinition(
      Collection<EventDefinition> eventDefinitions, Class<T> type) {
    if (eventDefinitions == null || eventDefinitions.isEmpty()) {
      return false;
    }
    return eventDefinitions.stream().anyMatch(type::isInstance);
  }

  private static <T extends EventDefinition> T getEventDefinition(
      Collection<EventDefinition> eventDefinitions, Class<T> type) {
    if (eventDefinitions == null || eventDefinitions.isEmpty()) {
      return null;
    }
    return eventDefinitions.stream()
        .filter(type::isInstance)
        .map(type::cast)
        .findFirst()
        .orElse(null);
  }
}
