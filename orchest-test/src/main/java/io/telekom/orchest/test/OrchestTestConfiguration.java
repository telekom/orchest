package io.telekom.orchest.test;

import io.telekom.orchest.client.annotations.JobMethodInfo;
import io.telekom.orchest.client.annotations.JobWorker;
import io.telekom.orchest.client.annotations.Worker;
import io.telekom.orchest.test.engine.JobWorkerTester;
import io.telekom.orchest.test.engine.MockJobClient;
import io.telekom.orchest.test.engine.ProcessTestRunner;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Auto-configuration for the Orchest test framework.
 *
 * <p>This configuration is imported by {@code @OrchestSpringTest} and provides:
 *
 * <ul>
 *   <li>{@link MockJobClient} - A mock {@code JobClient} that records all interactions
 *   <li>{@link JobWorkerTester} - The main utility for invoking workers in tests
 *   <li>Automatic scanning and registration of all {@code @JobWorker} methods in the context
 * </ul>
 *
 * <p>The configuration scans all beans annotated with {@code @Worker} and registers their
 * {@code @JobWorker} methods, mirroring the production registration logic in {@code
 * WorkerConfigurations} but without Kafka or event producers.
 */
@Configuration
public class OrchestTestConfiguration {

  private static final Logger log = LoggerFactory.getLogger(OrchestTestConfiguration.class);

  /**
   * Provides a {@link MockJobClient} that replaces the real {@code OrchesTClient}. Marked as
   * {@code @Primary} to override any existing {@code JobClient} bean.
   */
  @Bean
  @Primary
  public MockJobClient mockJobClient() {
    return new MockJobClient();
  }

  // Note: If your @Worker beans depend on OrchesTClient (e.g., via AsyncService),
  // use @MockBean OrchesTClient in your test class to provide a mock instance.

  /**
   * Scans the Spring context for all {@code @Worker} beans and extracts their {@code @JobWorker}
   * methods into a registry map.
   *
   * <p>This mirrors the production logic in {@code WorkerConfigurations} but is self-contained for
   * the test context without requiring Kafka infrastructure.
   */
  @Bean
  @ConditionalOnMissingBean(name = "testJobWorkerMethods")
  public Map<String, JobMethodInfo> testJobWorkerMethods(ApplicationContext applicationContext) {
    Map<String, JobMethodInfo> workerMethods = new HashMap<>();

    Map<String, Object> workerBeans = applicationContext.getBeansWithAnnotation(Worker.class);

    for (Map.Entry<String, Object> entry : workerBeans.entrySet()) {
      String beanName = entry.getKey();
      Object bean = entry.getValue();
      Class<?> beanClass = bean.getClass();

      // Handle CGLIB proxies — get the actual class
      if (beanClass.getName().contains("$$")) {
        beanClass = beanClass.getSuperclass();
      }

      for (Method method : beanClass.getDeclaredMethods()) {
        JobWorker annotation = method.getAnnotation(JobWorker.class);
        if (annotation == null) continue;

        String[] types = annotation.type().length > 0 ? annotation.type() : annotation.name();
        if (types.length == 0) {
          types = new String[] {method.getName()};
        }

        for (String type : types) {
          JobMethodInfo methodInfo =
              JobMethodInfo.builder()
                  .bean(bean)
                  .beanName(beanName)
                  .method(method)
                  .enabled(annotation.enabled())
                  .logWorker(annotation.logWorker())
                  .logVariables(annotation.logVariables())
                  .build();

          workerMethods.put(type, methodInfo);
          log.info(
              "[orchest-test] Registered @JobWorker: type='{}' → {}.{}()",
              type,
              beanClass.getSimpleName(),
              method.getName());
        }
      }
    }

    log.info("[orchest-test] Total workers registered: {}", workerMethods.size());
    return workerMethods;
  }

  /** Provides the {@link JobWorkerTester} bean — the main entry point for testing workers. */
  @Bean
  public JobWorkerTester jobWorkerTester(
      Map<String, JobMethodInfo> testJobWorkerMethods, MockJobClient mockJobClient) {
    return new JobWorkerTester(testJobWorkerMethods, mockJobClient);
  }

  /**
   * Provides the {@link ProcessTestRunner} bean — for executing entire process flows end-to-end.
   */
  @Bean
  public ProcessTestRunner processTestRunner(JobWorkerTester jobWorkerTester) {
    return new ProcessTestRunner(jobWorkerTester);
  }
}
