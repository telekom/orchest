package io.telekom.orchest.client.annotations;

import static org.springframework.util.ReflectionUtils.getAllDeclaredMethods;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.springframework.aop.support.AopUtils;

/**
 * Interface defining metadata retrieval for Spring beans. Provides default methods to check for
 * class and method annotations.
 */
public interface BeanInfo {

  /**
   * Helper to create an exception supplier when a required annotation is missing.
   *
   * @param type The annotation class type.
   * @return A supplier for IllegalStateException.
   */
  static Supplier<IllegalStateException> noAnnotationFound(final Class<? extends Annotation> type) {
    return () -> new IllegalStateException("no annotation found - " + type);
  }

  /**
   * @return The Spring bean instance.
   */
  Object getBean();

  /**
   * @return The name of the Spring bean.
   */
  String getBeanName();

  /**
   * Retrieves the target class of the bean (unwrapping AOP proxies if necessary).
   *
   * @return The target class.
   */
  default Class<?> getTargetClass() {
    return AopUtils.getTargetClass(getBean());
  }

  /**
   * Checks if the bean's class has a specific annotation.
   *
   * @param type The annotation class.
   * @return true if present.
   */
  default boolean hasClassAnnotation(final Class<? extends Annotation> type) {
    return getTargetClass().isAnnotationPresent(type);
  }

  /**
   * Checks if any method in the bean's class has a specific annotation.
   *
   * @param type The annotation class.
   * @return true if present on any method.
   */
  default boolean hasMethodAnnotation(final Class<? extends Annotation> type) {
    return Stream.of(getAllDeclaredMethods(getTargetClass()))
        .anyMatch(m -> m.isAnnotationPresent(type));
  }
}
