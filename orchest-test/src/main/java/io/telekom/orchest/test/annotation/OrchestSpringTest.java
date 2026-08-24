package io.telekom.orchest.test.annotation;

import io.telekom.orchest.test.OrchestTestConfiguration;
import java.lang.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.TestPropertySource;

/**
 * Meta-annotation for testing Orchest {@code @JobWorker} methods in a Spring Boot test context.
 *
 * <p>This annotation provides a fully configured test environment that:
 *
 * <ul>
 *   <li>Boots a Spring context with your {@code @Worker} beans
 *   <li>Disables Kafka consumers/producers (no external infrastructure needed)
 *   <li>Replaces {@code JobClient} with a {@link io.telekom.orchest.test.engine.MockJobClient}
 *   <li>Provides a {@link io.telekom.orchest.test.engine.JobWorkerTester} bean for invoking workers
 *       directly
 * </ul>
 *
 * <h3>Usage Example</h3>
 *
 * <pre>{@code
 * @OrchestSpringTest(classes = MyWorkerConfig.class)
 * class MyWorkerTest {
 *
 *     @Autowired
 *     private JobWorkerTester workerTester;
 *
 *     @Test
 *     void testOrderWorker() {
 *         WorkerTestResult result = workerTester.execute("process-order",
 *             Map.of("orderId", "ORD-123", "amount", 99.99));
 *
 *         assertThat(result).isCompleted();
 *         assertThat(result).hasVariable("status", "PROCESSED");
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest
@Import(OrchestTestConfiguration.class)
@TestPropertySource(
    properties = {
      "orchest.enableWorkers=false",
      "orchest.bootstrapServers=localhost:9092",
      "orchest.processIds=test-process",
      "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
    })
public @interface OrchestSpringTest {

  /**
   * The component classes to use for loading an ApplicationContext. Alias for {@link
   * SpringBootTest#classes()}.
   */
  @AliasFor(annotation = SpringBootTest.class, attribute = "classes")
  Class<?>[] classes() default {};

  /**
   * Properties to add to the Spring Environment before the test runs. Alias for {@link
   * SpringBootTest#properties()}.
   */
  @AliasFor(annotation = SpringBootTest.class, attribute = "properties")
  String[] properties() default {};
}
