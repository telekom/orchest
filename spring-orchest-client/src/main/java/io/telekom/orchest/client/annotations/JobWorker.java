package io.telekom.orchest.client.annotations;

import java.lang.annotation.*;

/**
 * Annotation to mark a method as a worker for a specific service task in a BPMN process. The method
 * will be invoked when the process execution reaches the corresponding service task.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JobWorker {

  /**
   * The type of the job (service task topic). If not specified, the method name is used.
   *
   * @return The job type.
   */
  String[] type() default {}; // set to empty string which leads to method name being used (if not)

  /**
   * Alias for {@link #type()}.
   *
   * @return The job type.
   */
  String[] name() default {};

  /**
   * Whether to log worker execution details.
   *
   * @return true if logging is enabled.
   */
  boolean logWorker() default true;

  /**
   * Whether to log the variables passed to the worker.
   *
   * @return true if variable logging is enabled.
   */
  boolean logVariables() default false;

  /**
   * The lock duration for the job in milliseconds.
   *
   * @return The timeout duration.
   */
  long timeout() default -1L;

  /**
   * The request timeout in milliseconds.
   *
   * @return The request timeout.
   */
  long requestTimeout() default -1L;

  /**
   * Whether to automatically complete the job after successful method execution. If false, the
   * worker must manually complete the job.
   *
   * @return true if auto-completion is enabled.
   */
  boolean autoComplete() default true;

  /**
   * Whether this worker is enabled.
   *
   * @return true if enabled.
   */
  boolean enabled() default true;

  /**
   * Whether the worker is common for all processes, means available to all business process across
   * cluster.
   *
   * @return true if enabled.
   */
  boolean commonWorker() default false;
}
