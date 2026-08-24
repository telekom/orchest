package io.telekom.orchest.enginecore.dmn;

import io.telekom.orchest.api.core.adapters.data.dto.DecisionEvaluationResponseDTO;
import io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition;
import io.telekom.orchest.api.core.model.dmn.*;
import io.telekom.orchest.enginecore.feel.FeelEvaluationEngine;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.camunda.feel.FeelEngine;
import org.camunda.feel.api.EvaluationResult;
import scala.util.Either;

/** DMN Engine that evaluates decision tables based on hit policy and input variables. */
@Slf4j
public class DmnEngine {

  /**
   * Evaluates a decision by decisionId with the provided input variables.
   *
   * @param definition the DecisionDefinition to be evaluated
   * @param inputVariables Map of input variable names to values
   * @return Map of output variable names to values
   * @throws IllegalArgumentException if decision is not found
   * @throws RuntimeException if evaluation fails
   */
  public DecisionEvaluationResponseDTO evaluate(
      DecisionDefinition definition, String decisionId, Map<String, Object> inputVariables) {
    if (definition == null) {
      throw new IllegalArgumentException("DecisionDefinition is null!");
    }

    Optional<Decision> decisionOpt = definition.getDecision(decisionId);
    if (decisionOpt.isEmpty()) {
      throw new IllegalArgumentException(
          "Decision not found: " + decisionId + " in definition: " + definition);
    }

    Decision decision = decisionOpt.get();
    DecisionTable decisionTable = decision.getDecisionTable();
    if (decisionTable == null) {
      throw new IllegalArgumentException("Decision " + decisionId + " has no decision table");
    }

    log.debug("Evaluating decision {} with variables: {}", decisionId, inputVariables);

    // Evaluate input expressions to get actual input values
    Map<String, Object> evaluatedInputs = evaluateInputs(decisionTable, inputVariables);
    log.debug("Evaluated inputs: {}", evaluatedInputs);

    // Find matching rules
    List<Rule> matchingRules = findMatchingRules(decisionTable, evaluatedInputs, inputVariables);
    log.debug("Found {} matching rules", matchingRules.size());

    if (matchingRules.isEmpty()) {
      log.warn("No matching rules found for decision {}", decisionId);
      return DecisionEvaluationResponseDTO.builder()
          .inputVariables(evaluatedInputs)
          .outputVariables(new HashMap<>())
          .build();
    }

    // Apply hit policy to select result
    Map<String, Object> result = applyHitPolicy(decisionTable, matchingRules, evaluatedInputs);
    log.info("Decision {} evaluation result: {}", decisionId, result);

    return DecisionEvaluationResponseDTO.builder()
        .inputVariables(evaluatedInputs)
        .outputVariables(result)
        .matchedRules(matchingRules)
        .build();
  }

  /** Evaluates input expressions to get actual input values. */
  private Map<String, Object> evaluateInputs(
      DecisionTable decisionTable, Map<String, Object> inputVariables) {
    Map<String, Object> evaluatedInputs = new HashMap<>();

    for (Input input : decisionTable.getInputs()) {
      String expression = input.getInputExpression();
      if (expression == null || expression.isEmpty()) {
        continue;
      }

      try {
        EvaluationResult evaluationResult =
            FeelEvaluationEngine.evaluateFeelExpression(expression, inputVariables);
        Object value =
            evaluationResult
                .toEither()
                .fold(
                    failure -> {
                      log.warn(
                          "Failed to evaluate input expression '{}' for input {}: {}",
                          expression,
                          input.getId(),
                          failure.message());
                      return null;
                    },
                    success -> success);
        evaluatedInputs.put(input.getName(), value);
        if (value != null) {
          log.trace("Input {} (expression: {}) evaluated to: {}", input.getId(), expression, value);
        }
      } catch (Exception e) {
        log.error(
            "Error evaluating input expression '{}' for input {}", expression, input.getId(), e);
        evaluatedInputs.put(input.getName(), null);
      }
    }

    return evaluatedInputs;
  }

  /** Finds all rules that match the evaluated inputs. */
  private List<Rule> findMatchingRules(
      DecisionTable decisionTable,
      Map<String, Object> evaluatedInputs,
      Map<String, Object> inputVariables) {
    List<Rule> matchingRules = new ArrayList<>();

    for (Rule rule : decisionTable.getRules()) {
      if (ruleMatches(rule, decisionTable.getInputs(), evaluatedInputs, inputVariables)) {
        matchingRules.add(rule);
      }
    }

    return matchingRules;
  }

  /** Checks if a rule matches the evaluated inputs. */
  private boolean ruleMatches(
      Rule rule,
      List<Input> inputs,
      Map<String, Object> evaluatedInputs,
      Map<String, Object> inputVariables) {
    List<String> inputEntries = rule.getInputEntries();

    // Rule must have same number of input entries as inputs
    if (inputEntries.size() != inputs.size()) {
      log.warn(
          "Rule {} has {} input entries but table has {} inputs",
          rule.getId(),
          inputEntries.size(),
          inputs.size());
      return false;
    }

    Map<String, Object> variables = inputVariables != null ? inputVariables : Map.of();

    // Check each input entry against the evaluated input value
    for (int i = 0; i < inputs.size(); i++) {
      Input input = inputs.get(i);
      String inputEntry = inputEntries.get(i);
      Object evaluatedValue = evaluatedInputs.get(input.getName());

      if (!inputEntryMatches(inputEntry, evaluatedValue, variables)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Checks if an input entry matches an evaluated value. Input entries are FEEL unary tests; {@code
   * ?} denotes the evaluated input value, other names come from {@code inputVariables}.
   */
  private boolean inputEntryMatches(
      String inputEntry, Object evaluatedValue, Map<String, Object> inputVariables) {
    if (inputEntry == null || inputEntry.trim().isEmpty()) {
      // Empty input entry means "any value" (matches everything)
      return true;
    }

    String trimmedEntry = inputEntry.trim();

    // "-" means "any value"
    if ("-".equals(trimmedEntry)) {
      return true;
    }

    try {
      EvaluationResult evaluationResult =
          FeelEvaluationEngine.evaluateFeelExpression(trimmedEntry, evaluatedValue, inputVariables);
      if (evaluationResult == null) {
        return fallbackInputEntryMatch(trimmedEntry, evaluatedValue);
      }
      if (evaluationResult.isFailure()) {
        log.debug(
            "Unary test failed for input entry '{}': {}",
            trimmedEntry,
            evaluationResult.failure().message());
        return fallbackInputEntryMatch(trimmedEntry, evaluatedValue);
      }
      Object matchResult = evaluationResult.result();
      if (matchResult instanceof Boolean) {
        return (Boolean) matchResult;
      }
      return fallbackInputEntryMatch(trimmedEntry, evaluatedValue);
    } catch (Exception e) {
      log.debug(
          "Error evaluating input entry '{}' against value '{}', trying fallback",
          inputEntry,
          evaluatedValue,
          e);
      return fallbackInputEntryMatch(trimmedEntry, evaluatedValue);
    }
  }

  /** Fallback method for simple input entry matching (exact matches, lists). */
  private boolean fallbackInputEntryMatch(String inputEntry, Object evaluatedValue) {
    if (evaluatedValue == null) {
      // Null only matches empty or "-"
      return inputEntry.trim().isEmpty() || "-".equals(inputEntry.trim());
    }

    String evaluatedStr = String.valueOf(evaluatedValue);

    // Handle quoted strings (exact match)
    if (inputEntry.startsWith("\"") && inputEntry.endsWith("\"")) {
      String expectedValue = inputEntry.substring(1, inputEntry.length() - 1);
      return expectedValue.equals(evaluatedStr);
    }

    // Handle comma-separated list of quoted values (e.g., "value1","value2")
    if (inputEntry.contains(",")) {
      String[] values = inputEntry.split(",");
      for (String value : values) {
        String trimmedValue = value.trim();
        // Remove quotes if present
        if (trimmedValue.startsWith("\"") && trimmedValue.endsWith("\"")) {
          trimmedValue = trimmedValue.substring(1, trimmedValue.length() - 1);
        }
        if (trimmedValue.equals(evaluatedStr)) {
          return true;
        }
      }
      return false;
    }

    // Handle comparison operators (>, <, >=, <=, =)
    if (inputEntry.startsWith(">=")
        || inputEntry.startsWith("<=")
        || inputEntry.startsWith(">")
        || inputEntry.startsWith("<")
        || inputEntry.startsWith("=")) {
      // Try to evaluate as FEEL expression with the value
      try {
        Map<String, Object> context = new HashMap<>();
        context.put("value", evaluatedValue);
        String expression = inputEntry.replaceFirst("^=", "value ==");
        if (expression.startsWith(">=")
            || expression.startsWith("<=")
            || expression.startsWith(">")
            || expression.startsWith("<")) {
          expression = "value " + expression;
        }
        Either<FeelEngine.Failure, Object> result =
            FeelEvaluationEngine.evaluateFeelExpression(expression, context).toEither();
        Object matchResult = result.fold(failure -> null, success -> success);
        if (matchResult instanceof Boolean) {
          return (Boolean) matchResult;
        }
      } catch (Exception e) {
        log.debug("Failed to evaluate comparison expression: {}", inputEntry, e);
      }
    }

    // Default: try exact match
    return inputEntry.equals(evaluatedStr);
  }

  /** Applies hit policy to select the result from matching rules. */
  private Map<String, Object> applyHitPolicy(
      DecisionTable decisionTable, List<Rule> matchingRules, Map<String, Object> evaluatedInputs) {
    String hitPolicy =
        decisionTable.getHitPolicy() != null
            ? decisionTable.getHitPolicy().toUpperCase()
            : "UNIQUE";

    List<Output> outputs = decisionTable.getOutputs();

    switch (hitPolicy) {
      case "UNIQUE":
        if (matchingRules.size() != 1) {
          throw new RuntimeException(
              "UNIQUE hit policy requires exactly one matching rule, but found: "
                  + matchingRules.size());
        }
        return evaluateOutputs(matchingRules.get(0), outputs, evaluatedInputs);

      case "FIRST":
        // Return first matching rule (already in order)
        return evaluateOutputs(matchingRules.get(0), outputs, evaluatedInputs);

      case "ANY":
        // All matching rules should have same outputs
        Map<String, Object> firstResult =
            evaluateOutputs(matchingRules.get(0), outputs, evaluatedInputs);
        for (int i = 1; i < matchingRules.size(); i++) {
          Map<String, Object> otherResult =
              evaluateOutputs(matchingRules.get(i), outputs, evaluatedInputs);
          if (!firstResult.equals(otherResult)) {
            throw new RuntimeException(
                "ANY hit policy requires all matching rules to have same outputs");
          }
        }
        return firstResult;

      case "PRIORITY":
        // Return rule with highest priority (assuming rule order is priority)
        return evaluateOutputs(matchingRules.get(0), outputs, evaluatedInputs);

      case "COLLECT":
        // Collect all outputs
        return collectOutputs(
            matchingRules, outputs, evaluatedInputs, decisionTable.getAggregation());

      case "RULE_ORDER":
        // Return outputs in rule order
        return collectOutputs(matchingRules, outputs, evaluatedInputs, null);

      case "OUTPUT_ORDER":
        // Return outputs sorted by output values
        return collectOutputsSorted(matchingRules, outputs, evaluatedInputs);

      default:
        log.warn("Unknown hit policy: {}, defaulting to FIRST", hitPolicy);
        return evaluateOutputs(matchingRules.getFirst(), outputs, evaluatedInputs);
    }
  }

  /** Evaluates output entries for a rule. */
  private Map<String, Object> evaluateOutputs(
      Rule rule, List<Output> outputs, Map<String, Object> context) {
    Map<String, Object> result = new HashMap<>();
    List<String> outputEntries = rule.getOutputEntries();

    for (int i = 0; i < outputs.size() && i < outputEntries.size(); i++) {
      Output output = outputs.get(i);
      String outputEntry = outputEntries.get(i);

      Object value = evaluateOutputEntry(outputEntry, context);
      result.put(output.getName() != null ? output.getName() : output.getId(), value);
    }

    return result;
  }

  /** Evaluates a single output entry (FEEL expression). */
  private Object evaluateOutputEntry(String outputEntry, Map<String, Object> context) {
    if (outputEntry == null || outputEntry.trim().isEmpty()) {
      return null;
    }

    try {
      Either<FeelEngine.Failure, Object> result =
          FeelEvaluationEngine.evaluateFeelExpression(outputEntry, context).toEither();
      return result.fold(
          failure -> {
            log.warn("Failed to evaluate output entry '{}': {}", outputEntry, failure.message());
            // Fallback: return as string
            return outputEntry;
          },
          success -> success);
    } catch (Exception e) {
      log.debug("Error evaluating output entry '{}', returning as string", outputEntry, e);
      // Fallback: return as string
      return outputEntry;
    }
  }

  /** Collects outputs from multiple rules (for COLLECT hit policy). */
  private Map<String, Object> collectOutputs(
      List<Rule> rules, List<Output> outputs, Map<String, Object> context, String aggregation) {
    List<Map<String, Object>> allResults = new ArrayList<>();
    for (Rule rule : rules) {
      allResults.add(evaluateOutputs(rule, outputs, context));
    }

    if (aggregation == null || aggregation.isEmpty()) {
      // Return list of all results
      Map<String, Object> result = new HashMap<>();
      for (Output output : outputs) {
        String outputName = output.getName() != null ? output.getName() : output.getId();
        List<Object> values =
            allResults.stream()
                .map(r -> r.get(outputName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        result.put(outputName, values);
      }
      return result;
    }

    // Apply aggregation
    Map<String, Object> result = new HashMap<>();
    for (Output output : outputs) {
      String outputName = output.getName() != null ? output.getName() : output.getId();
      List<Object> values =
          allResults.stream()
              .map(r -> r.get(outputName))
              .filter(Objects::nonNull)
              .collect(Collectors.toList());

      Object aggregated = applyAggregation(values, aggregation);
      result.put(outputName, aggregated);
    }
    return result;
  }

  /** Collects outputs sorted by output values (for OUTPUT_ORDER hit policy). */
  private Map<String, Object> collectOutputsSorted(
      List<Rule> rules, List<Output> outputs, Map<String, Object> context) {
    // For simplicity, return first output sorted
    // In a full implementation, this would sort by all outputs
    return collectOutputs(rules, outputs, context, null);
  }

  /** Applies aggregation function to a list of values. */
  @SuppressWarnings("unchecked")
  private Object applyAggregation(List<Object> values, String aggregation) {
    if (values.isEmpty()) {
      return null;
    }

    String aggUpper = aggregation.toUpperCase();
    return switch (aggUpper) {
      case "SUM" ->
          values.stream()
              .filter(v -> v instanceof Number)
              .mapToDouble(v -> ((Number) v).doubleValue())
              .sum();
      case "COUNT" -> values.size();
      case "MIN" ->
          values.stream()
              .filter(v -> v instanceof Comparable)
              .min((a, b) -> ((Comparable<Object>) a).compareTo(b))
              .orElse(null);
      case "MAX" ->
          values.stream()
              .filter(v -> v instanceof Comparable)
              .max((a, b) -> ((Comparable<Object>) a).compareTo(b))
              .orElse(null);
      default -> {
        log.warn("Unknown aggregation: {}, returning list", aggregation);
        yield values;
      }
    };
  }
}
