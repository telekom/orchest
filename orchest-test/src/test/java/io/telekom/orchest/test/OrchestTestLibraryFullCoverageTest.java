package io.telekom.orchest.test;

import static io.telekom.orchest.test.assertions.OrchestAssertions.assertThat;

import io.telekom.orchest.client.exception.RetryableException;
import io.telekom.orchest.test.annotation.OrchestSpringTest;
import io.telekom.orchest.test.engine.JobWorkerTester;
import io.telekom.orchest.test.engine.MockJobClient;
import io.telekom.orchest.test.engine.ProcessExecutionResult;
import io.telekom.orchest.test.engine.ProcessFlow;
import io.telekom.orchest.test.engine.ProcessTestRunner;
import io.telekom.orchest.test.engine.TestActivatedJob;
import io.telekom.orchest.test.engine.WorkerTestResult;
import io.telekom.orchest.test.internal.TestWorkerConfiguration;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Exercises the main surface of {@code orchest-test}: {@link JobWorkerTester}, {@link
 * TestActivatedJob}, fluent assertions, {@link MockJobClient} recording, and {@link
 * ProcessTestRunner} with {@link ProcessFlow} gateways.
 */
@OrchestSpringTest(classes = TestWorkerConfiguration.class)
class OrchestTestLibraryFullCoverageTest {

  @Autowired private JobWorkerTester jobWorkerTester;

  @Autowired private MockJobClient mockJobClient;

  @Autowired private ProcessTestRunner processTestRunner;

  @BeforeEach
  void resetMock() {
    mockJobClient.reset();
  }

  @Test
  void jobWorkerTesterExposesRegistry() {
    Assertions.assertThat(jobWorkerTester.getWorkerCount()).isPositive();
    Assertions.assertThat(jobWorkerTester.getRegisteredWorkerTypes())
        .contains("doc-sample-greet", "doc-sample-error");
    Assertions.assertThat(jobWorkerTester.hasWorker("doc-sample-greet")).isTrue();
    Assertions.assertThat(jobWorkerTester.hasWorker("non-existent-worker")).isFalse();
  }

  @Test
  void testActivatedJobPassesMetadataAndCustomHeaders() {
    WorkerTestResult result =
        jobWorkerTester.execute(
            TestActivatedJob.builder()
                .type("doc-sample-headers-echo")
                .processInstanceId("pi-header-1")
                .processDefinitionId("demo-process")
                .processDefinitionVersion(2)
                .retries(5)
                .customHeader("trace-id", "abc-123")
                .build());

    assertThat(result).isCompleted().hasVariable("traceEcho", "abc-123").executedWithin(30_000);
  }

  @Test
  void executeWithBuiltInActivatedJobUsesProcessInstanceForClientCalls() {
    jobWorkerTester.execute(
        TestActivatedJob.builder()
            .type("doc-sample-send-error")
            .processInstanceId("pi-error-target")
            .build());

    MockJobClient.ErrorEventRecord rec = mockJobClient.getErrorEvents().get(0);
    Assertions.assertThat(rec.processInstanceId()).isEqualTo("pi-error-target");
    Assertions.assertThat(rec.errorCode()).isEqualTo("ERR_BAD_STATE");
  }

  @Test
  void taskResponseMessageEventIsVisibleToAssertions() {
    WorkerTestResult result = jobWorkerTester.execute("doc-sample-message-event", Map.of());

    assertThat(result).isCompleted().hasMessageEvent().hasVariables().hasVariable("reserved", true);
  }

  @Test
  void taskResponseIncidentMessageIsDetected() {
    WorkerTestResult result = jobWorkerTester.execute("doc-sample-incident-response", Map.of());

    Assertions.assertThat(result.isCompleted()).isTrue();
    Assertions.assertThat(result.hasIncident()).isTrue();
    Assertions.assertThat(result.getOutputVariable("review")).isEqualTo(true);
  }

  @Test
  void retryableExceptionIsSurfacedOnWorkerResult() {
    WorkerTestResult result = jobWorkerTester.execute("doc-sample-retryable", Map.of());

    assertThat(result).hasFailed().hasFailedWith(RetryableException.class);
  }

  @Test
  void mockJobClientRecordsSignalMessageErrorIncidentDeploySpawnAndComplete() {
    jobWorkerTester.execute("doc-sample-broadcast-signal", Map.of());
    jobWorkerTester.execute("doc-sample-send-message", Map.of());
    jobWorkerTester.execute(
        TestActivatedJob.builder()
            .type("doc-sample-throw-incident")
            .processInstanceId("pi-inc")
            .build());
    jobWorkerTester.execute("doc-sample-deploy", Map.of());
    jobWorkerTester.execute("doc-sample-create-child", Map.of());
    jobWorkerTester.execute(
        TestActivatedJob.builder()
            .type("doc-sample-complete-event")
            .processInstanceId("pi-done")
            .build());

    Assertions.assertThat(mockJobClient.getSignalEvents())
        .singleElement()
        .satisfies(
            s -> {
              Assertions.assertThat(s.signalName()).isEqualTo("process-updated");
              Assertions.assertThat(s.variables()).containsEntry("version", 2);
            });

    Assertions.assertThat(mockJobClient.getMessageEvents())
        .singleElement()
        .satisfies(
            m -> {
              Assertions.assertThat(m.messageName()).isEqualTo("PAYMENT_CONFIRMED");
              Assertions.assertThat(m.correlationKey()).isEqualTo("pay-1");
              Assertions.assertThat(m.variables()).containsEntry("amount", 100);
            });

    Assertions.assertThat(mockJobClient.getIncidents())
        .singleElement()
        .satisfies(i -> Assertions.assertThat(i.processInstanceId()).isEqualTo("pi-inc"));

    Assertions.assertThat(mockJobClient.getInteractions())
        .anyMatch(
            i ->
                "deployProcessDefinition".equals(i.method())
                    && "/definitions/order.bpmn".equals(i.parameters().get("filePath"))
                    && Integer.valueOf(4).equals(i.parameters().get("partitionCount")));

    Assertions.assertThat(mockJobClient.getProcessCreations())
        .singleElement()
        .satisfies(
            p -> {
              Assertions.assertThat(p.processId()).isEqualTo("child-process");
              Assertions.assertThat(p.instanceId()).isEqualTo("child-inst-1");
              Assertions.assertThat(p.variables()).containsEntry("parentRef", "p-1");
            });

    Assertions.assertThat(mockJobClient.getInteractions())
        .anyMatch(
            i ->
                "sendCompleteEvent".equals(i.method())
                    && "pi-done".equals(i.parameters().get("processInstanceId")));
  }

  @Test
  void exclusiveGatewaySelectsMatchingBranch() {
    ProcessFlow flow =
        ProcessFlow.builder(Map.of("priority", "HIGH"))
            .exclusiveGateway()
            .when("priority", "HIGH")
            .then("doc-sample-branch-high")
            .when("priority", "LOW")
            .then("doc-sample-branch-low")
            .defaultPath("doc-sample-branch-default")
            .endGateway()
            .build();

    ProcessExecutionResult result = processTestRunner.run(flow);

    assertThat(result)
        .isCompleted()
        .hasFinalVariable("path", "HIGH")
        .stepCompleted("doc-sample-branch-high")
        .hasStepCount(1);
  }

  @Test
  void exclusiveGatewayFallsBackToDefaultPath() {
    ProcessFlow flow =
        ProcessFlow.builder(Map.of("priority", "MEDIUM"))
            .exclusiveGateway()
            .when("priority", "HIGH")
            .then("doc-sample-branch-high")
            .defaultPath("doc-sample-branch-default")
            .endGateway()
            .build();

    ProcessExecutionResult result = processTestRunner.run(flow);

    assertThat(result).isCompleted().hasFinalVariable("path", "DEFAULT");
  }

  @Test
  void exclusiveGatewayEvaluatesPredicateBranch() {
    ProcessFlow flow =
        ProcessFlow.builder(Map.of("score", 75))
            .exclusiveGateway()
            .when(
                vars -> ((Number) vars.getOrDefault("score", 0)).intValue() >= 50,
                "doc-sample-branch-high")
            .defaultPath("doc-sample-branch-low")
            .endGateway()
            .build();

    ProcessExecutionResult result = processTestRunner.run(flow);

    assertThat(result).isCompleted().hasFinalVariable("path", "HIGH");
  }

  @Test
  void parallelGatewayRunsAllBranchesAndMergesVariables() {
    ProcessFlow flow =
        ProcessFlow.builder()
            .parallelGateway()
            .branch("doc-sample-parallel-a")
            .branch("doc-sample-parallel-b")
            .endGateway()
            .build();

    ProcessExecutionResult result = processTestRunner.run(flow);

    assertThat(result)
        .isCompleted()
        .allStepsCompleted()
        .hasFinalVariables(
            Map.of(
                "branchA", "done",
                "branchB", "done"))
        .hasStepCount(2);

    Assertions.assertThat(result.getStepResults("doc-sample-parallel-a")).hasSize(1);
    Assertions.assertThat(result.getStepResults("doc-sample-parallel-b")).hasSize(1);
  }

  @Test
  void processTestRunnerMergesAdditionalInitialVariables() {
    ProcessFlow flow =
        ProcessFlow.builder()
            .initialVariables(Map.of("base", 1))
            .step("doc-sample-greet", Map.of("name", "extra-vars"))
            .build();

    ProcessExecutionResult result = processTestRunner.run(flow, Map.of("overlay", "x"));

    assertThat(result)
        .isCompleted()
        .hasFinalVariable("message", "Hello, extra-vars")
        .hasFinalVariable("base", 1)
        .hasFinalVariable("overlay", "x");
  }

  @Test
  void processStopsWhenWorkerThrowsRetryable() {
    ProcessFlow flow =
        ProcessFlow.builder()
            .step("doc-sample-greet", Map.of("name", "ok"))
            .step("doc-sample-retryable")
            .step("doc-sample-greet", Map.of("name", "skipped"))
            .build();

    ProcessExecutionResult result = processTestRunner.run(flow);

    assertThat(result).hasFailed();
    Assertions.assertThat(result.getTotalSteps()).isEqualTo(2);
    Assertions.assertThat(result.getFailedSteps()).isEqualTo(1);
    Assertions.assertThat(result.getStepResult(1).getWorkerResult().getException())
        .isInstanceOf(RetryableException.class);
  }

  @Test
  void processExecutionResultExposesStepDetailsAndTiming() {
    ProcessFlow flow =
        ProcessFlow.builder().step("doc-sample-greet", Map.of("name", "detail")).build();

    ProcessExecutionResult result = processTestRunner.run(flow);

    assertThat(result).isCompleted().executedWithin(60_000);

    ProcessExecutionResult.StepExecutionResult step = result.getStepResult("doc-sample-greet");
    Assertions.assertThat(step).isNotNull();
    Assertions.assertThat(step.getVariablesBefore()).isEmpty();
    Assertions.assertThat(step.getWorkerResult().getActivatedJob().getVariablesMap())
        .containsEntry("name", "detail");
    Assertions.assertThat(step.getVariablesAfter()).containsEntry("message", "Hello, detail");
    Assertions.assertThat(step.getStepIndex()).isZero();
    Assertions.assertThat(result.getProcessInstanceId()).isNotBlank();
  }
}
