package io.telekom.orchest.api.core.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for manually raising an incident on a process instance. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaiseIncidentRequest {

  /** The process instance to raise the incident on. */
  private String processInstanceId;

  /** A human-readable message describing the incident cause. */
  private String incidentMessage;
}
