package io.telekom.orchest.api.core.model.bpmn.node;

import io.telekom.orchest.api.core.model.bpmn.EventType;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an Event node in a BPMN process. Events can be start events, end events, intermediate
 * catch/throw events, or boundary events.
 */
@Setter
@NoArgsConstructor
public class EventNode extends BaseNode {
  /** The specific event type (MESSAGE, TIMER, ERROR, etc.). */
  @Getter private EventType eventType;

  /** ID of the activity this boundary event is attached to, null for non-boundary events. */
  @Getter private String attachedToId;

  // Typed event properties (replace properties map access with compile-time safety)
  /** Message name for message events. */
  private String messageName;

  /** Correlation key expression for message events. */
  private String messageCorrelationKey;

  /** Signal reference for signal events. */
  private String signalRef;

  /** ISO 8601 duration for timer events. */
  private String timerDuration;

  /** ISO 8601 date/time for timer events. */
  private String timerDate;

  /** ISO 8601 cycle expression for recurring timer events. */
  private String timerCycle;

  /** Error reference identifier for error events. */
  private String errorRef;

  /** Error code for error events. */
  private String errorCode;

  /** Escalation reference identifier for escalation events. */
  private String escalationRef;

  /** Escalation code for escalation events. */
  private String escalationCode;

  /** FEEL condition expression for conditional events. */
  private String condition;

  /** Link name for link intermediate events. */
  private String linkName;

  /** Zeebe-compatible correlation key expression. */
  private String zeebeCorrelationKey;

  /** Whether this boundary event cancels the attached activity (default true per BPMN spec). */
  private Boolean cancelActivity;

  /**
   * Constructs a new EventNode.
   *
   * @param id the unique identifier for the node
   * @param name the human-readable name of the node
   * @param type the BPMN node type (START_EVENT, END_EVENT, etc.)
   * @param eventType the specific event type (MESSAGE, TIMER, etc.)
   */
  public EventNode(String id, String name, NodeType type, EventType eventType) {
    super(id, name, type);
    this.eventType = eventType;
  }

  /**
   * Attaches this event as a boundary event to the specified activity node.
   *
   * @param targetNodeId the ID of the activity to attach to
   */
  public void attachTo(String targetNodeId) {
    this.attachedToId = targetNodeId;
  }

  // Typed getters with fallback to properties map for backward compat with existing MongoDB
  // documents.
  // Old documents store these values only in the properties map. New documents have both.

  public String getMessageName() {
    return stringProp(messageName, "messageName");
  }

  public String getMessageCorrelationKey() {
    return stringProp(messageCorrelationKey, "messageCorrelationKey");
  }

  public String getSignalRef() {
    return stringProp(signalRef, "signalRef");
  }

  public String getTimerDuration() {
    return stringProp(timerDuration, "timerDuration");
  }

  public String getTimerDate() {
    return stringProp(timerDate, "timerDate");
  }

  public String getTimerCycle() {
    return stringProp(timerCycle, "timerCycle");
  }

  public String getErrorRef() {
    return stringProp(errorRef, "errorRef");
  }

  public String getErrorCode() {
    return stringProp(errorCode, "errorCode");
  }

  public String getEscalationRef() {
    return stringProp(escalationRef, "escalationRef");
  }

  public String getEscalationCode() {
    return stringProp(escalationCode, "escalationCode");
  }

  public String getCondition() {
    return stringProp(condition, "condition");
  }

  public String getLinkName() {
    return stringProp(linkName, "linkName");
  }

  public String getZeebeCorrelationKey() {
    return stringProp(zeebeCorrelationKey, "zeebeCorrelationKey");
  }

  public boolean isCancelActivity() {
    if (cancelActivity != null) return cancelActivity;
    Object val = getProperties().get("cancelActivity");
    return val instanceof Boolean b ? b : true; // default true per BPMN spec
  }

  private String stringProp(String field, String propertyKey) {
    if (field != null) return field;
    Object val = getProperties().get(propertyKey);
    return val != null ? val.toString() : null;
  }
}
