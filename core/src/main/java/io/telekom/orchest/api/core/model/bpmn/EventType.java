package io.telekom.orchest.api.core.model.bpmn;

/**
 * Represents the type of event in a BPMN event node. Events are used to trigger or respond to
 * various conditions and external stimuli in a business process. This enum covers all standard BPMN
 * event types.
 *
 * <p>Event types are used in:
 *
 * <ul>
 *   <li>Start Events - to define how a process is triggered
 *   <li>End Events - to define how a process completes
 *   <li>Intermediate Catch Events - to wait for external triggers
 *   <li>Intermediate Throw Events - to trigger external events
 *   <li>Boundary Events - to handle events during activity execution
 * </ul>
 */
public enum EventType {
  NONE,
  TIMER,
  MESSAGE,
  SIGNAL,
  ERROR,
  ESCALATION,
  CONDITIONAL,
  COMPENSATION,
  LINK,
  TERMINATE,
  MULTIPLE,
  PARALLEL_MULTIPLE,
  CANCEL
}
