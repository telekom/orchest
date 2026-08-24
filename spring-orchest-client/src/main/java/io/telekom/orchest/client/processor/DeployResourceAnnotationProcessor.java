package io.telekom.orchest.client.processor;

import io.telekom.orchest.client.DeployResource;
import io.telekom.orchest.client.OrchesTClient;
import io.telekom.orchest.client.OrchestProperties;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * Processor for the {@link DeployResource} annotation. Listens for the {@link DeployResourceEvent}
 * and scans beans for the annotation. If found, it reads the specified resource files and deploys
 * them using the client.
 */
@Order
@Component
@RequiredArgsConstructor
public class DeployResourceAnnotationProcessor implements ApplicationListener<DeployResourceEvent> {

  private final ResourceLoader resourceLoader;
  private final ApplicationContext applicationContext;
  private final OrchesTClient orchestClient;
  private final OrchestProperties orchestProperties;

  /**
   * Handles the deployment event. Scans all beans for {@link DeployResource} annotation and
   * processes them.
   *
   * @param event The deploy resource event.
   */
  @Override
  public void onApplicationEvent(DeployResourceEvent event) {
    Map<String, Object> annotatedBeans =
        this.applicationContext.getBeansWithAnnotation(DeployResource.class);
    annotatedBeans.forEach(
        (name, bean) -> {
          DeployResource annotation = null;
          Class<?> targetClass = AopUtils.getTargetClass(bean);
          if (targetClass.isAnnotationPresent(DeployResource.class)) {
            annotation = targetClass.getAnnotation(DeployResource.class);
          } else if (targetClass.getSuperclass() != null
              && targetClass.getSuperclass().isAnnotationPresent(DeployResource.class)) {
            annotation = targetClass.getSuperclass().getAnnotation(DeployResource.class);
          }
          processAnnotation(annotation);
        });
  }

  private void processAnnotation(DeployResource annotation) {
    if (annotation != null) {
      if (StringUtils.isNotEmpty(annotation.value())) {
        DeployResource(annotation.value());
      } else if (ArrayUtils.isNotEmpty(annotation.paths())) {
        for (String path : annotation.paths()) {
          DeployResource(path);
        }
      }
    }
  }

  private void DeployResource(String classpathLocation) {
    try {
      // Load the resource from classpath
      String resourcePath = "classpath:" + classpathLocation;
      Resource resource = resourceLoader.getResource(resourcePath);
      if (!resource.exists()) {
        throw new RuntimeException("File not found: " + classpathLocation);
      }
      orchestClient.deployProcessDefinition(
          resource.getFile(), orchestProperties.getDeployPartitions());

    } catch (Exception e) {
      throw new RuntimeException("Error processing Deployment file: " + classpathLocation, e);
    }
  }
}
