package io.telekom.orchest.api.core.request;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Event payload for a progressive (backoff-based) retry of a failed worker task. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressiveRetryEvent {

  /** The worker event to retry with updated retry metadata. */
  private WorkerEventRequest retryWorkerEvent;
}
