package io.telekom.orchest.api;

/**
 * Interface for implementing worker tasks that process activated jobs. Workers are responsible for
 * executing business logic associated with BPMN service tasks. Implementations should handle the
 * job processing and return a task response.
 */
public interface Worker {
  /**
   * Handles an activated job by executing the associated business logic.
   *
   * @param client The JobClient instance for interacting with the OrchesT engine.
   * @param job The activated job containing job metadata and variables.
   * @return A TaskResponse indicating the result of the job processing.
   */
  TaskResponse handle(JobClient client, ActivatedJob job);
}
