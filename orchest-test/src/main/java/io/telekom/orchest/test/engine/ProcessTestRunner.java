package io.telekom.orchest.test.engine;

import io.telekom.orchest.api.core.utils.IDGenerator;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes entire process flows end-to-end for testing.
 *
 * <p>This runner simulates process execution by executing workers in sequence according to a
 * defined {@link ProcessFlow}. It handles gateways, variable propagation, and tracks execution
 * state.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * @Autowired ProcessTestRunner processTestRunner;
 *
 * @Test
 * void testFullProcess() {
 *     ProcessFlow flow = ProcessFlow.builder()
 *         .initialVariables(Map.of("orderId", "ORD-001", "test", true))
 *         .step("w1")
 *         .step("w2")
 *         .exclusiveGateway()
 *             .when("test", true).then("w5")
 *             .defaultPath("w4")
 *         .step("throw-error")
 *         .parallelGateway()
 *             .branch("w1")
 *             .branch("w2")
 *             .branch("w4")
 *         .step("test-worker")
 *         .build();
 *
 *     ProcessExecutionResult result = processTestRunner.run(flow);
 *
 *     assertThat(result).isCompleted();
 *     assertThat(result.getFinalVariable("orderId")).isEqualTo("ORD-001");
 * }
 * }</pre>
 */
public class ProcessTestRunner {

  private static final Logger log = LoggerFactory.getLogger(ProcessTestRunner.class);

  private final JobWorkerTester workerTester;

  /**
   * Constructs a new process test runner.
   *
   * @param workerTester the worker tester used to execute individual steps
   */
  public ProcessTestRunner(JobWorkerTester workerTester) {
    this.workerTester = workerTester;
  }

  /**
   * Executes a process flow end-to-end.
   *
   * @param flow The process flow to execute.
   * @return The execution result containing all step results and final state.
   */
  public ProcessExecutionResult run(ProcessFlow flow) {
    return run(flow, null);
  }

  /**
   * Executes a process flow with additional initial variables.
   *
   * @param flow The process flow to execute.
   * @param additionalVariables Additional variables to merge with flow's initial variables.
   * @return The execution result.
   */
  public ProcessExecutionResult run(ProcessFlow flow, Map<String, Object> additionalVariables) {
    String processInstanceId = IDGenerator.generate();
    long startTime = System.currentTimeMillis();

    // Merge initial variables
    Map<String, Object> variables = new HashMap<>(flow.getInitialVariables());
    if (additionalVariables != null) {
      variables.putAll(additionalVariables);
    }

    List<ProcessExecutionResult.StepExecutionResult> stepResults = new ArrayList<>();
    Exception failureException = null;
    boolean completed = false;

    try {
      int stepIndex = 0;
      List<ProcessFlow.FlowStep> steps = flow.getSteps();

      for (int i = 0; i < steps.size(); i++) {
        ProcessFlow.FlowStep step = steps.get(i);

        if (step instanceof ProcessFlow.SimpleStep simpleStep) {
          ProcessExecutionResult.StepExecutionResult result =
              executeSimpleStep(simpleStep, variables, processInstanceId, stepIndex++);
          stepResults.add(result);
          variables = new HashMap<>(result.getVariablesAfter());

          if (result.hasFailed()) {
            log.warn("Step {} failed, stopping process execution", simpleStep.workerType());
            break;
          }

        } else if (step instanceof ProcessFlow.GatewayStep gatewayStep) {
          List<ProcessExecutionResult.StepExecutionResult> gatewayResults =
              executeGatewayStep(gatewayStep, variables, processInstanceId, stepIndex);
          stepResults.addAll(gatewayResults);
          stepIndex += gatewayResults.size();

          // Merge variables from all branches
          variables = mergeVariables(gatewayResults);

          // Check if any branch failed
          if (gatewayResults.stream()
              .anyMatch(ProcessExecutionResult.StepExecutionResult::hasFailed)) {
            log.warn("Gateway step failed, stopping process execution");
            break;
          }

        } else {
          log.warn("Unknown step type: {}", step.getClass().getSimpleName());
        }
      }

      completed =
          failureException == null
              && stepResults.stream()
                  .noneMatch(ProcessExecutionResult.StepExecutionResult::hasFailed);

    } catch (Exception e) {
      failureException = e;
      log.error("Process execution failed", e);
    }

    long executionTime = System.currentTimeMillis() - startTime;

    return new ProcessExecutionResult(
        processInstanceId, stepResults, variables, completed, failureException, executionTime);
  }

  /** Executes a simple step (worker). */
  private ProcessExecutionResult.StepExecutionResult executeSimpleStep(
      ProcessFlow.SimpleStep step,
      Map<String, Object> variablesBefore,
      String processInstanceId,
      int stepIndex) {

    String stepId = "step-" + stepIndex;
    Map<String, Object> stepVariables =
        step.variables() != null
            ? mergeVariables(Map.of(), step.variables(), variablesBefore)
            : variablesBefore;

    log.debug(
        "Executing step {}: worker={}, variables={}",
        stepId,
        step.workerType(),
        stepVariables.keySet());

    WorkerTestResult workerResult = workerTester.execute(step.workerType(), stepVariables);

    // Merge output variables
    Map<String, Object> variablesAfter =
        mergeVariables(variablesBefore, workerResult.getOutputVariables());

    return new ProcessExecutionResult.StepExecutionResult(
        stepId, step.workerType(), workerResult, variablesBefore, variablesAfter, stepIndex);
  }

  /** Executes a gateway step (exclusive or parallel). */
  private List<ProcessExecutionResult.StepExecutionResult> executeGatewayStep(
      ProcessFlow.GatewayStep gatewayStep,
      Map<String, Object> variablesBefore,
      String processInstanceId,
      int startStepIndex) {

    List<ProcessExecutionResult.StepExecutionResult> results = new ArrayList<>();

    if (gatewayStep.type() == ProcessFlow.GatewayType.EXCLUSIVE) {
      // Exclusive gateway: evaluate conditions and take first matching path
      String selectedWorker = null;

      for (ProcessFlow.GatewayBranch branch : gatewayStep.branches()) {
        if (branch instanceof ProcessFlow.ConditionalBranch cb) {
          Object actualValue = variablesBefore.get(cb.variable());
          if (Objects.equals(actualValue, cb.expectedValue())) {
            selectedWorker = cb.targetWorker();
            break;
          }
        } else if (branch instanceof ProcessFlow.PredicateBranch pb) {
          if (pb.condition().test(variablesBefore)) {
            selectedWorker = pb.targetWorker();
            break;
          }
        }
      }

      // Use default path if no condition matched
      if (selectedWorker == null) {
        selectedWorker = gatewayStep.defaultPath();
      }

      if (selectedWorker != null) {
        ProcessExecutionResult.StepExecutionResult result =
            executeSimpleStep(
                new ProcessFlow.SimpleStep(selectedWorker, null),
                variablesBefore,
                processInstanceId,
                startStepIndex);
        results.add(result);
      }

    } else if (gatewayStep.type() == ProcessFlow.GatewayType.PARALLEL) {
      // Parallel gateway: execute all branches
      int branchIndex = 0;
      for (ProcessFlow.GatewayBranch branch : gatewayStep.branches()) {
        if (branch instanceof ProcessFlow.ParallelBranch pb) {
          Map<String, Object> branchVariables =
              pb.variables() != null
                  ? mergeVariables(variablesBefore, pb.variables())
                  : variablesBefore;

          ProcessExecutionResult.StepExecutionResult result =
              executeSimpleStep(
                  new ProcessFlow.SimpleStep(pb.workerType(), branchVariables),
                  variablesBefore,
                  processInstanceId,
                  startStepIndex + branchIndex++);
          results.add(result);
        }
      }
    }

    return results;
  }

  /** Merges multiple variable maps, with later maps overriding earlier ones. */
  private Map<String, Object> mergeVariables(Map<String, Object>... maps) {
    Map<String, Object> merged = new HashMap<>();
    for (Map<String, Object> map : maps) {
      if (map != null) {
        merged.putAll(map);
      }
    }
    return merged;
  }

  /** Merges variables from multiple step results. */
  private Map<String, Object> mergeVariables(
      List<ProcessExecutionResult.StepExecutionResult> results) {
    Map<String, Object> merged = new HashMap<>();
    for (ProcessExecutionResult.StepExecutionResult result : results) {
      merged.putAll(result.getVariablesAfter());
    }
    return merged;
  }
}
