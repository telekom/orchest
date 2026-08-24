package io.telekom.orchest.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for the AI chat endpoint, containing the user message and desired analysis type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
  private String userMessage;
  @Builder.Default private ChatType chatType = ChatType.DEFAULT_ANALYSIS;

  /** The type of AI analysis to perform on the user message. */
  public enum ChatType {
    DETAILED_ANALYSIS,
    INCIDENT_ANALYSIS,
    SUGGEST_FIX,
    DEFAULT_ANALYSIS
  }
}
