package io.telekom.orchest.test.engine;

import java.util.*;
import java.util.function.Predicate;

/**
 * Builder/DSL for defining process flows for end-to-end testing.
 *
 * <p>Allows you to define a process flow programmatically and execute it using {@link
 * ProcessTestRunner}.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * ProcessFlow flow = ProcessFlow.builder()
 *     .step("w1", Map.of("orderId", "ORD-001"))
 *     .step("w2")
 *     .exclusiveGateway()
 *         .when("test", true).then("w5")
 *         .defaultPath("w4")
 *     .step("throw-error")
 *     .parallelGateway()
 *         .branch("w1")
 *         .branch("w2")
 *         .branch("w4")
 *     .step("test-worker")
 *     .build();
 *
 * ProcessExecutionResult result = processTestRunner.run(flow);
 * }</pre>
 */
public class ProcessFlow {

  private final List<FlowStep> steps;
  private final Map<String, Object> initialVariables;

  private ProcessFlow(List<FlowStep> steps, Map<String, Object> initialVariables) {
    this.steps = new ArrayList<>(steps);
    this.initialVariables = new HashMap<>(initialVariables != null ? initialVariables : Map.of());
  }

  /**
   * Returns the ordered list of flow steps.
   *
   * @return unmodifiable list of flow steps
   */
  public List<FlowStep> getSteps() {
    return Collections.unmodifiableList(steps);
  }

  /**
   * Returns the initial variables for the flow.
   *
   * @return unmodifiable map of initial variables
   */
  public Map<String, Object> getInitialVariables() {
    return Collections.unmodifiableMap(initialVariables);
  }

  /**
   * Creates a new process flow builder.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new process flow builder with initial variables.
   *
   * @param initialVariables the initial variables for the process
   * @return a new builder instance
   */
  public static Builder builder(Map<String, Object> initialVariables) {
    return new Builder().initialVariables(initialVariables);
  }

  /** Builder for creating ProcessFlow instances. */
  public static class Builder {
    private final List<FlowStep> steps = new ArrayList<>();
    private final Map<String, Object> initialVariables = new HashMap<>();
    private GatewayBuilder currentGateway;

    /** Sets initial variables for the process. */
    public Builder initialVariables(Map<String, Object> variables) {
      if (variables != null) {
        this.initialVariables.putAll(variables);
      }
      return this;
    }

    /** Adds a simple step (worker execution). */
    public Builder step(String workerType) {
      return step(workerType, null);
    }

    /** Adds a step with specific variables. */
    public Builder step(String workerType, Map<String, Object> variables) {
      closeCurrentGateway();
      steps.add(new SimpleStep(workerType, variables));
      return this;
    }

    /** Starts an exclusive gateway definition. */
    public GatewayBuilder exclusiveGateway() {
      closeCurrentGateway();
      currentGateway = new GatewayBuilder(this, GatewayType.EXCLUSIVE);
      return currentGateway;
    }

    /** Starts a parallel gateway definition. */
    public GatewayBuilder parallelGateway() {
      closeCurrentGateway();
      currentGateway = new GatewayBuilder(this, GatewayType.PARALLEL);
      return currentGateway;
    }

    /** Builds the ProcessFlow. */
    public ProcessFlow build() {
      closeCurrentGateway();
      return new ProcessFlow(steps, initialVariables);
    }

    private void closeCurrentGateway() {
      if (currentGateway != null) {
        steps.add(currentGateway.build());
        currentGateway = null;
      }
    }
  }

  /** Builder for gateway definitions. */
  public static class GatewayBuilder {
    private final Builder parent;
    private final GatewayType type;
    private final List<GatewayBranch> branches = new ArrayList<>();
    private String defaultPath;

    private GatewayBuilder(Builder parent, GatewayType type) {
      this.parent = parent;
      this.type = type;
    }

    /**
     * Adds a conditional branch (for exclusive gateways). Use {@link #then(String)} to set the
     * target worker.
     */
    public GatewayBuilder when(String variable, Object expectedValue) {
      branches.add(new ConditionalBranch(variable, expectedValue, null));
      return this;
    }

    /** Adds a conditional branch using a predicate. */
    public GatewayBuilder when(Predicate<Map<String, Object>> condition, String targetWorker) {
      branches.add(new PredicateBranch(condition, targetWorker));
      return this;
    }

    /** Sets the default path for exclusive gateways. */
    public GatewayBuilder defaultPath(String workerType) {
      this.defaultPath = workerType;
      return this;
    }

    /** Adds a parallel branch. */
    public GatewayBuilder branch(String workerType) {
      return branch(workerType, null);
    }

    /** Adds a parallel branch with variables. */
    public GatewayBuilder branch(String workerType, Map<String, Object> variables) {
      branches.add(new ParallelBranch(workerType, variables));
      return this;
    }

    /**
     * Sets the target worker for the last conditional branch. Use this after {@link #when(String,
     * Object)} to specify which worker to execute. Returns this GatewayBuilder to allow chaining
     * more conditions or calling defaultPath().
     */
    public GatewayBuilder then(String workerType) {
      // Set the last branch's target
      if (!branches.isEmpty()) {
        GatewayBranch lastBranch = branches.get(branches.size() - 1);
        if (lastBranch instanceof ConditionalBranch cb) {
          branches.set(
              branches.size() - 1,
              new ConditionalBranch(cb.variable(), cb.expectedValue(), workerType));
        }
      }
      return this;
    }

    /**
     * Closes the gateway and returns to the parent builder. This is called automatically when
     * starting a new step, but can be called explicitly.
     */
    public Builder endGateway() {
      return parent;
    }

    private GatewayStep build() {
      return new GatewayStep(type, branches, defaultPath);
    }
  }

  // ---- Flow Step Types ----

  /** Marker interface for all flow step types. */
  public interface FlowStep {}

  /** A simple step that executes a single worker with optional variables. */
  public record SimpleStep(String workerType, Map<String, Object> variables) implements FlowStep {}

  /** A gateway step that routes execution based on conditions or parallelism. */
  public record GatewayStep(GatewayType type, List<GatewayBranch> branches, String defaultPath)
      implements FlowStep {}

  /** The type of gateway routing. */
  public enum GatewayType {
    /** Only one branch is taken based on conditions. */
    EXCLUSIVE,
    /** All branches are executed. */
    PARALLEL
  }

  /** Marker interface for gateway branch definitions. */
  public interface GatewayBranch {}

  /** A branch that evaluates a variable against an expected value. */
  public record ConditionalBranch(String variable, Object expectedValue, String targetWorker)
      implements GatewayBranch {}

  /** A branch that evaluates a predicate against the current variables. */
  public record PredicateBranch(Predicate<Map<String, Object>> condition, String targetWorker)
      implements GatewayBranch {}

  /** A branch in a parallel gateway that executes a worker. */
  public record ParallelBranch(String workerType, Map<String, Object> variables)
      implements GatewayBranch {}
}
