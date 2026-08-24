package io.telekom.orchest.enginecore.dmn;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.camunda.dmn.DmnEngine;
import org.camunda.dmn.parser.ParsedDmn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Service that deploys and evaluates DMN models at runtime using the Camunda DMN engine. */
public class DynamicDmnService {
  private static final Logger log = LoggerFactory.getLogger(DynamicDmnService.class);

  private final DmnEngine dmnEngine;
  private final Map<String, ParsedDmn> dmnCache = new ConcurrentHashMap<>();

  public DynamicDmnService() {
    this.dmnEngine = new DmnEngine.Builder().build();
  }

  /**
   * Deploys a DMN resource and caches all its decisions.
   *
   * @param resourceName the name of the DMN resource for logging
   * @param inputStream the DMN XML input stream
   * @throws RuntimeException if parsing fails
   */
  public void deployDmn(String resourceName, InputStream inputStream) {
    log.info("Deploying DMN: {}", resourceName);
    var parseResult = dmnEngine.parse(inputStream);

    if (parseResult.isRight()) {
      ParsedDmn parsed = parseResult.right().get();
      parsed
          .decisions()
          .foreach(
              decision -> {
                log.info("Registered decision: {}", decision.id());
                dmnCache.put(decision.id(), parsed);
                return null;
              });
    } else {
      DmnEngine.Failure failure = parseResult.left().get();
      log.error("Failed to parse DMN {}: {}", resourceName, failure.message());
      throw new RuntimeException("DMN parsing failed: " + failure.message());
    }
  }

  /**
   * Evaluates a previously deployed decision by ID.
   *
   * @param decisionId the decision ID to evaluate
   * @param variables the input variables for evaluation
   * @return map of output variable names to their evaluated values
   * @throws IllegalArgumentException if the decision is not found
   * @throws RuntimeException if evaluation fails
   */
  public Map<String, Object> evaluate(String decisionId, Map<String, Object> variables) {
    ParsedDmn parsedDmn = dmnCache.get(decisionId);
    if (parsedDmn == null) {
      throw new IllegalArgumentException("Decision not found: " + decisionId);
    }

    log.debug("Evaluating decision {} with variables {}", decisionId, variables);

    var result = dmnEngine.eval(parsedDmn, decisionId, variables);

    if (result.isRight()) {
      DmnEngine.EvalResult evalResult = result.right().get();

      if (evalResult.isNil()) {
        return new HashMap<>();
      } else {
        Object value = evalResult.value();
        if (value instanceof Map) {
          return (Map<String, Object>) value;
        } else {
          Map<String, Object> wrapper = new HashMap<>();
          wrapper.put("result", value);
          return wrapper;
        }
      }
    } else {
      // EvalFailure usually has a message() method similar to Failure
      var failure = result.left().get();
      // We assume failure has a message() or toString() that is useful
      String msg = failure.toString();
      // Try to see if it matches Failure interface or has message()
      // But safely just toString it if type is unknown to us here
      throw new RuntimeException("DMN evaluation failed: " + msg);
    }
  }
}
