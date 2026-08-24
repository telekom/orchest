package io.telekom.orchest.api.core.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for raising incidents on multiple process instances in batch. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchRaiseIncidentRequest {

  /** List of process instance IDs to raise incidents on. */
  private List<String> processInstanceIds;

  /** The incident message describing the error condition. */
  private String incidentMessage;
}
