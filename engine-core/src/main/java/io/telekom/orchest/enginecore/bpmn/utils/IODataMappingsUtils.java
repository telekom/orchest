package io.telekom.orchest.enginecore.bpmn.utils;

import io.telekom.orchest.api.core.model.bpmn.DataMapping;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for managing input/output data mappings in BPMN nodes. Provides methods to set,
 * remove, and retrieve data mappings for process variables. Data mappings allow transforming
 * variables between different scopes (e.g., process to activity).
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IODataMappingsUtils {

  /**
   * Applies both input and output data mappings for a node to the variables map. Evaluates
   * expressions and sets the mapped variables.
   *
   * @param baseNode The node containing input and output mappings.
   * @param variables The variables map to apply mappings to.
   */
  public static void setDataMappings(BaseNode baseNode, Map<String, Object> variables) {
    setDataMappings(baseNode.getInputMappings(), variables);
    setDataMappings(baseNode.getOutputMappings(), variables);
  }

  /**
   * Removes variables from the map based on the data mappings. All variables referenced in the data
   * mappings are removed from the variables map.
   *
   * @param dataMappings The list of data mappings defining which variables to remove.
   * @param variables The variables map to remove variables from.
   */
  public static void removeDataMappings(
      List<DataMapping> dataMappings, Map<String, Object> variables) {
    dataMappings.forEach(
        dataMapping -> {
          variables.remove(dataMapping.getName());
        });
  }

  /**
   * Applies data mappings to the variables map. Evaluates each mapping's expression and sets the
   * result as a variable with the mapping's name. Only sets variables if the evaluation result is
   * not null.
   *
   * @param dataMappings The list of data mappings to apply.
   * @param variables The variables map to apply mappings to.
   */
  public static void setDataMappings(
      List<DataMapping> dataMappings, Map<String, Object> variables) {
    dataMappings.forEach(
        dataMapping -> {
          Object evaluatedVariable =
              VariablesUtils.getEvaluatedVariable(dataMapping.getValue(), variables);
          if (evaluatedVariable != null) {
            variables.put(dataMapping.getName(), evaluatedVariable);
          }
        });
  }

  /**
   * Creates a new map containing only the variables defined by data mappings. Evaluates each
   * mapping's expression and adds the result to the returned map. Returns an empty map if
   * dataMappings is null or empty.
   *
   * @param dataMappings The list of data mappings to process.
   * @param variables The source variables map to evaluate expressions against.
   * @return A new map containing only the mapped variables, or empty map if no mappings provided.
   */
  public static Map<String, Object> getDataMappingsVariables(
      List<DataMapping> dataMappings, Map<String, Object> variables) {
    Map<String, Object> mappedVariables = new HashMap<>();
    if (dataMappings == null || dataMappings.isEmpty()) {
      return mappedVariables;
    }
    dataMappings.forEach(
        dataMapping -> {
          Object evaluatedVariable =
              VariablesUtils.getEvaluatedVariable(dataMapping.getValue(), variables);
          if (evaluatedVariable != null) {
            mappedVariables.put(dataMapping.getName(), evaluatedVariable);
          }
        });
    return mappedVariables;
  }

  /**
   * Retrieves the value expression for a data mapping by name.
   *
   * @param name The name of the data mapping to find.
   * @param dataMappings The list of data mappings to search.
   * @return An Optional containing the value expression if found, empty otherwise.
   */
  public static Optional<String> getDataMappingValue(String name, List<DataMapping> dataMappings) {
    return dataMappings.stream()
        .filter(dataMapping -> dataMapping.getName().equals(name))
        .map(DataMapping::getValue)
        .findFirst();
  }
}
