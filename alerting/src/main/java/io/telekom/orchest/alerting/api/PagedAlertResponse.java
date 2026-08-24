package io.telekom.orchest.alerting.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Paginated response wrapper for alert list queries. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated alert list response")
public class PagedAlertResponse {

  @Schema(description = "List of alerts for the current page")
  private List<AlertResponse> content;

  @Schema(description = "Total number of alerts matching the filter criteria", example = "42")
  private long totalElements;

  @Schema(description = "Total number of pages available", example = "5")
  private int totalPages;

  @Schema(description = "Current zero-based page index", example = "0")
  private int page;

  @Schema(description = "Requested page size", example = "10")
  private int size;
}
