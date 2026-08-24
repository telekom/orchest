package io.telekom.orchest.test;

import static io.telekom.orchest.test.assertions.OrchestAssertions.assertThat;

import io.telekom.orchest.test.annotation.OrchestSpringTest;
import io.telekom.orchest.test.engine.JobWorkerTester;
import io.telekom.orchest.test.engine.MockJobClient;
import io.telekom.orchest.test.engine.ProcessExecutionResult;
import io.telekom.orchest.test.engine.ProcessFlow;
import io.telekom.orchest.test.engine.ProcessTestRunner;
import io.telekom.orchest.test.engine.WorkerTestResult;
import io.telekom.orchest.test.internal.TestWorkerConfiguration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Demonstrates {@code orchest-test} usage: {@link JobWorkerTester}, fluent assertions, and {@link
 * ProcessTestRunner} with {@link ProcessFlow}.
 */
@OrchestSpringTest(classes = TestWorkerConfiguration.class)
class OrchestTestLibraryDemoTest {

  @Autowired private JobWorkerTester jobWorkerTester;

  @Autowired private MockJobClient mockJobClient;

  @Autowired private ProcessTestRunner processTestRunner;

  @BeforeEach
  void resetMock() {
    mockJobClient.reset();
  }

  @Test
  void greetWorkerProducesOutputVariable() {
    WorkerTestResult result =
        jobWorkerTester.execute("doc-sample-greet", Map.of("name", "OrchesT"));

    assertThat(result).isCompleted().hasVariable("message", "Hello, OrchesT").hasNoErrorEvent();
  }

  @Test
  void validationWorkerReturnsErrorEvent() {
    WorkerTestResult result = jobWorkerTester.execute("doc-sample-error", Map.of());

    assertThat(result).isCompleted().hasErrorEvent().hasErrorCode("INVALID_INPUT");
  }

  @Test
  void processFlowRunsSequentialWorkerAndMergesVariables() {
    ProcessFlow flow =
        ProcessFlow.builder()
            .initialVariables(Map.of("name", "flow"))
            .step("doc-sample-greet")
            .build();

    ProcessExecutionResult result = processTestRunner.run(flow);

    assertThat(result).isCompleted().hasFinalVariable("message", "Hello, flow");
  }
}
