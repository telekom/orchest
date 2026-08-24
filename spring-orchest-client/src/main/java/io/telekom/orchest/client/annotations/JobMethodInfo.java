package io.telekom.orchest.client.annotations;

import java.lang.reflect.Method;
import lombok.Builder;
import lombok.Getter;

/**
 * implementation of {@link BeanInfo} that holds information about a specific job worker method.
 * Contains details about the method, the bean instance, and worker configuration.
 */
@Getter
@Builder
public class JobMethodInfo implements BeanInfo {

  private boolean enabled;
  private Object bean;
  private String beanName;
  private Method method;
  private boolean logWorker;
  private boolean logVariables;
  private boolean isCommonWorker;

  @Override
  public Object getBean() {
    return bean;
  }

  @Override
  public String getBeanName() {
    return beanName;
  }
}
