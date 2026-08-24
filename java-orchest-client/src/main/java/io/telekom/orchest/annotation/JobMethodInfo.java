package io.telekom.orchest.annotation;

import java.lang.reflect.Method;
import lombok.Builder;
import lombok.Getter;

/**
 * Holds metadata about a job worker method. Contains details about the method, the instance, and
 * worker configuration.
 */
@Builder
@Getter
public class JobMethodInfo {

  private boolean enabled;
  private Object instance;
  private String instanceName;
  private Method method;
  private boolean logWorker;
  private boolean logVariables;
  private Class<?> targetClass;

  /** Gets the target class of the instance. */
  public Class<?> getTargetClass() {
    return targetClass != null ? targetClass : instance.getClass();
  }
}
