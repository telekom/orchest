package io.telekom.orchest.api.core.model.bpmn;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Lifecycle states for an intermediate catch event registration. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum IntermediateEventState {
  REGISTERED,
  COMPLETED
}
