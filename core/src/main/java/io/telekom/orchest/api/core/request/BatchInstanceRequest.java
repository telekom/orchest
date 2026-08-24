package io.telekom.orchest.api.core.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for batch processing of process instances. Validation (non-empty, max 500 items) is
 * performed at the API layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchInstanceRequest {

  /**
   * List of process instance IDs to retry/cancel. Must not be empty and limited to 500 items per
   * request.
   */
  private List<String> processInstanceIds;
}
