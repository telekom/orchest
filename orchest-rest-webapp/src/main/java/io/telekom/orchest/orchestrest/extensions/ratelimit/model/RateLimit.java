package io.telekom.orchest.orchestrest.extensions.ratelimit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** MongoDB document representing rate-limit configuration for a process definition. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class RateLimit {

  @Id private String id;
  private boolean enabled;

  @Indexed(unique = true)
  private String processId;

  private int windowDuration = 60; // in seconds
  private int allowedSize = 10;
  private boolean switchToNewCamunda;
}
