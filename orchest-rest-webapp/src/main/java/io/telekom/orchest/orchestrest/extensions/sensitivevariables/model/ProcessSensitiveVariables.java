package io.telekom.orchest.orchestrest.extensions.sensitivevariables.model;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** MongoDB document defining which variables should be hidden or shown for a process definition. */
@Data
@Document
@NoArgsConstructor
@AllArgsConstructor
public class ProcessSensitiveVariables {

  public static final String DEFAULT_PLACEHOLDER =
      "[**hidden**, enable it from OrchestSetting to see the value]";

  private String id;

  @Indexed(unique = true)
  private String processDefinitionId;

  private List<SensitiveVariables> variables;
  private boolean enabled;

  @CreatedDate private OffsetDateTime createdAt;
  @LastModifiedDate private OffsetDateTime updatedAt;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SensitiveVariables {
    private String name;
    private String value = DEFAULT_PLACEHOLDER;
    private Type type;
  }

  public enum Type {
    HIDE,
    SHOW
  }
}
