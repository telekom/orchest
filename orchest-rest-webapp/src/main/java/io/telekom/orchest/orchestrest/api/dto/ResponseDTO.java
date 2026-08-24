package io.telekom.orchest.orchestrest.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Generic REST API response wrapper containing metadata and a typed data payload. */
@Data
@NoArgsConstructor
public class ResponseDTO<T> {
  /** Response metadata (HTTP status code and message). */
  private Meta meta;

  /** The response payload. */
  private T data;

  /** Metadata portion of the API response carrying status code and message. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Meta {
    private int code;
    private String message;
  }
}
