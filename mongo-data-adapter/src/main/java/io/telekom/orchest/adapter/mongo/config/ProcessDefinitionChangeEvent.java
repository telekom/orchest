package io.telekom.orchest.adapter.mongo.config;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Spring application event published when a process definition is inserted or deleted. Allows
 * listeners to react to process definition changes detected via MongoDB change streams.
 */
@Getter
public class ProcessDefinitionChangeEvent extends ApplicationEvent {

  private final ProcessDefinition processDefinition;
  private final OperationType operationType;

  /**
   * Creates a new process definition change event.
   *
   * @param source the object that published the event
   * @param processDefinition the process definition that was changed
   * @param operationType the type of operation (INSERT or DELETE)
   */
  public ProcessDefinitionChangeEvent(
      Object source, ProcessDefinition processDefinition, OperationType operationType) {
    super(source);
    this.processDefinition = processDefinition;
    this.operationType = operationType;
  }

  public enum OperationType {
    INSERT,
    DELETE
  }
}
