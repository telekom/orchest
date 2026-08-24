package io.telekom.orchest.enginecore.bpmn.execution.impl;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.adapters.data.model.UserTaskInstance;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.UserTaskNode;
import io.telekom.orchest.api.core.model.bpmn.state.ExecutionStateKey;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.service.UserTaskService;
import io.telekom.orchest.enginecore.bpmn.utils.IODataMappingsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for User Tasks.
 *
 * <p>When a process instance reaches a User Task, this executor:
 *
 * <ol>
 *   <li>Applies input data mappings to prepare task-level variables
 *   <li>Creates a persistent {@link
 *       io.telekom.orchest.api.core.adapters.data.model.UserTaskInstance} with assignment
 *       information (assignee, candidateUsers, candidateGroups)
 *   <li>Leaves the process in a wait state until the task is completed via the REST API
 * </ol>
 *
 * <p>If an assignee is defined on the User Task, the task is automatically claimed for that user
 * upon creation (Camunda 8 compatible behavior).
 */
public class UserTaskExecutor implements NodeExecutor {
  private static final Logger log = LoggerFactory.getLogger(UserTaskExecutor.class);

  /**
   * Executes the user task by creating a persistent task instance and entering a wait state.
   *
   * @param instance the active process instance
   * @param node the user task node to execute
   * @param context the execution context
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    if (!(node instanceof UserTaskNode userTaskNode)) {
      log.warn(
          "UserTaskExecutor received non-UserTaskNode: {}. Treating as generic wait state.",
          node.getClass().getSimpleName());
      return;
    }

    // Apply input data mappings
    IODataMappingsUtils.setDataMappings(node, instance.getVariables());

    log.info(
        "Executing User Task '{}' ({}) for process instance {}",
        userTaskNode.getName(),
        userTaskNode.getId(),
        instance.getProcessInstanceId());

    // Create persistent user task instance
    UserTaskService userTaskService = context.getUserTaskService();
    if (userTaskService != null) {
      UserTaskInstance createdTask =
          userTaskService.createUserTask(
              instance.getProcessInstanceId(),
              instance.getProcessDefinitionId(),
              userTaskNode,
              instance.getVariables());
      instance.putState(
          new ExecutionStateKey.UserTaskId(userTaskNode.getId()), createdTask.getTaskId());
    } else {
      log.warn(
          "UserTaskService is not available. User task '{}' created as wait state only (no persistence).",
          userTaskNode.getName());
    }

    // The node remains active (wait state) until completed via REST API → resumeActivity()
    log.info(
        "User Task '{}' is waiting for completion. Assignee: {}, CandidateUsers: {}, CandidateGroups: {}",
        userTaskNode.getName(),
        userTaskNode.getAssignee(),
        userTaskNode.getCandidateUsers(),
        userTaskNode.getCandidateGroups());
  }
}
