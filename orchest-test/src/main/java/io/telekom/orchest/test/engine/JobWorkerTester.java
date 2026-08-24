package io.telekom.orchest.test.engine;

import io.telekom.orchest.client.TaskResponse;
import io.telekom.orchest.client.annotations.ActivatedJob;
import io.telekom.orchest.client.annotations.JobClient;
import io.telekom.orchest.client.annotations.JobMethodInfo;
import io.telekom.orchest.client.exception.RetryableException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The primary utility for testing {@code @JobWorker} methods.
 *
 * <p>Provides methods to directly invoke registered worker methods with test data, bypassing Kafka
 * and the engine infrastructure entirely. Workers are invoked in-process with the actual Spring
 * beans, making this suitable for unit and integration testing.
 *
 * <h3>Usage in Tests</h3>
 *
 * <pre>{@code
 * @OrchestSpringTest(classes = MyApp.class)
 * class OrderWorkerTest {
 *
 *     @Autowired
 *     private JobWorkerTester workerTester;
 *
 *     @Autowired
 *     private MockJobClient mockJobClient;
 *
 *     @BeforeEach
 *     void setup() {
 *         mockJobClient.reset();
 *     }
 *
 *     @Test
 *     void shouldProcessOrder() {
 *         WorkerTestResult result = workerTester.execute("process-order",
 *             Map.of("orderId", "ORD-123", "amount", 99.99));
 *
 *         assertThat(result).isCompleted();
 *         assertThat(result).hasVariable("status", "PROCESSED");
 *     }
 *
 *     @Test
 *     void shouldThrowErrorForInvalidOrder() {
 *         WorkerTestResult result = workerTester.execute("process-order",
 *             Map.of("orderId", "INVALID", "amount", -1));
 *
 *         assertThat(result).hasErrorCode("INVALID_ORDER");
 *     }
 *
 *     @Test
 *     void shouldSendMessageOnCompletion() {
 *         workerTester.execute("notify-worker", Map.of("orderId", "ORD-123"));
 *
 *         assertThat(mockJobClient.getMessageEvents()).hasSize(1);
 *         assertThat(mockJobClient.getMessageEvents().get(0).messageName()).isEqualTo("ORDER_READY");
 *     }
 *
 *     @Test
 *     void shouldHandleRetryableException() {
 *         WorkerTestResult result = workerTester.execute("flaky-worker", Map.of());
 *
 *         assertThat(result).hasFailed();
 *         assertThat(result).hasFailedWith(RetryableException.class);
 *     }
 * }
 * }</pre>
 *
 * <h3>Advanced: Custom ActivatedJob</h3>
 *
 * <pre>{@code
 * ActivatedJob customJob = TestActivatedJob.builder()
 *     .type("my-worker")
 *     .processInstanceId("instance-abc")
 *     .processDefinitionId("my-process")
 *     .variable("key", "value")
 *     .retries(5)
 *     .build();
 *
 * WorkerTestResult result = workerTester.execute(customJob);
 * }</pre>
 */
public class JobWorkerTester {

  private final Map<String, JobMethodInfo> jobWorkerMethods;
  private final JobClient jobClient;

  /**
   * Constructs a new tester with the registered worker methods and job client.
   *
   * @param jobWorkerMethods the map of worker type to method info
   * @param jobClient the job client to pass to workers during invocation
   */
  public JobWorkerTester(Map<String, JobMethodInfo> jobWorkerMethods, JobClient jobClient) {
    this.jobWorkerMethods = jobWorkerMethods;
    this.jobClient = jobClient;
  }

  /**
   * Executes a registered {@code @JobWorker} method by its worker type with the given variables.
   *
   * @param workerType The worker type (as defined in {@code @JobWorker(type = "...")}).
   * @param variables The input variables to pass to the worker.
   * @return A {@link WorkerTestResult} containing the execution outcome.
   * @throws IllegalArgumentException if no worker is registered for the given type.
   */
  public WorkerTestResult execute(String workerType, Map<String, Object> variables) {
    ActivatedJob activatedJob =
        TestActivatedJob.builder()
            .type(workerType)
            .variables(variables != null ? variables : new HashMap<>())
            .build();
    return execute(activatedJob);
  }

  /**
   * Executes a registered {@code @JobWorker} method with a fully constructed {@link ActivatedJob}.
   * Use this when you need to control process instance ID, retries, custom headers, etc.
   *
   * @param activatedJob The activated job containing type, variables, and metadata.
   * @return A {@link WorkerTestResult} containing the execution outcome.
   * @throws IllegalArgumentException if no worker is registered for the job's type.
   */
  public WorkerTestResult execute(ActivatedJob activatedJob) {
    String workerType = activatedJob.getType();
    JobMethodInfo methodInfo = jobWorkerMethods.get(workerType);

    if (methodInfo == null) {
      throw new IllegalArgumentException(
          "No @JobWorker registered for type '"
              + workerType
              + "'. "
              + "Available workers: "
              + getRegisteredWorkerTypes()
              + ". "
              + "Ensure your @Worker class is in the Spring context.");
    }

    methodInfo.getMethod().setAccessible(true);

    long startTime = System.currentTimeMillis();
    TaskResponse taskResponse = null;
    Exception caughtException = null;

    try {
      Object response =
          methodInfo.getMethod().invoke(methodInfo.getBean(), jobClient, activatedJob);
      if (response instanceof TaskResponse tr) {
        taskResponse = tr;
      }
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RetryableException re) {
        caughtException = re;
      } else if (cause instanceof Exception ex) {
        caughtException = ex;
      } else {
        caughtException = new RuntimeException("Worker threw a non-Exception Throwable", cause);
      }
    } catch (IllegalAccessException e) {
      caughtException =
          new RuntimeException("Failed to invoke worker method: " + e.getMessage(), e);
    }

    long executionTime = System.currentTimeMillis() - startTime;
    return new WorkerTestResult(
        taskResponse, caughtException, activatedJob, workerType, executionTime);
  }

  /** Returns all registered worker types. Useful for debugging when a worker type is not found. */
  public Set<String> getRegisteredWorkerTypes() {
    return jobWorkerMethods.keySet();
  }

  /** Returns {@code true} if a worker is registered for the given type. */
  public boolean hasWorker(String workerType) {
    return jobWorkerMethods.containsKey(workerType);
  }

  /** Returns the number of registered workers. */
  public int getWorkerCount() {
    return jobWorkerMethods.size();
  }
}
