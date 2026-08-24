package io.telekom.orchest.enginecore.bpmn.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.request.Variables;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests VariablesUtils: variable merging (UPDATE/DELETE), FEEL evaluation, and call activity
 * mappings.
 */
class VariablesUtilsTest {

  // ========================================================================================
  // getMergedVariables: UPDATE action with non-null variables
  // ========================================================================================

  @Test
  void getMergedVariables_updateAction_mergesIntoCurrentVariables() {
    Map<String, Object> currentVars = new HashMap<>();
    currentVars.put("existing", "value1");
    currentVars.put("toOverwrite", "old");

    Variables received =
        Variables.builder()
            .variables(Map.of("newKey", "newVal", "toOverwrite", "new"))
            .action(Variables.VariableAction.UPDATE)
            .build();

    Map<String, Object> result = VariablesUtils.getMergedVariables(received, currentVars);

    assertSame(currentVars, result, "Should return the same reference as currentVariables");
    assertEquals("value1", result.get("existing"));
    assertEquals("newVal", result.get("newKey"));
    assertEquals("new", result.get("toOverwrite"));
  }

  // ========================================================================================
  // getMergedVariables: DELETE action
  // ========================================================================================

  @Test
  void getMergedVariables_deleteAction_removesKeysFromCurrentVariables() {
    Map<String, Object> currentVars = new HashMap<>();
    currentVars.put("keep", "keepVal");
    currentVars.put("remove1", "removeVal1");
    currentVars.put("remove2", "removeVal2");

    Variables received =
        Variables.builder()
            .variables(Map.of("remove1", "", "remove2", ""))
            .action(Variables.VariableAction.DELETE)
            .build();

    Map<String, Object> result = VariablesUtils.getMergedVariables(received, currentVars);

    assertEquals("keepVal", result.get("keep"));
    assertFalse(result.containsKey("remove1"));
    assertFalse(result.containsKey("remove2"));
  }

  @Test
  void getMergedVariables_deleteAction_onlyRemovesMatchingKeys() {
    Map<String, Object> currentVars = new HashMap<>();
    currentVars.put("a", 1);
    currentVars.put("b", 2);
    currentVars.put("c", 3);

    Variables received =
        Variables.builder()
            .variables(Map.of("b", ""))
            .action(Variables.VariableAction.DELETE)
            .build();

    Map<String, Object> result = VariablesUtils.getMergedVariables(received, currentVars);

    assertEquals(2, result.size());
    assertEquals(1, result.get("a"));
    assertEquals(3, result.get("c"));
    assertFalse(result.containsKey("b"));
  }

  @Test
  void getMergedVariables_deleteAction_nonExistentKey_noError() {
    Map<String, Object> currentVars = new HashMap<>();
    currentVars.put("keep", "val");

    Variables received =
        Variables.builder()
            .variables(Map.of("nonExistent", ""))
            .action(Variables.VariableAction.DELETE)
            .build();

    Map<String, Object> result = VariablesUtils.getMergedVariables(received, currentVars);

    assertEquals(1, result.size());
    assertEquals("val", result.get("keep"));
  }

  // ========================================================================================
  // getMergedVariables: null receivedVariables
  // ========================================================================================

  @Test
  void getMergedVariables_nullReceivedVariables_returnsCurrentVariablesUnchanged() {
    Map<String, Object> currentVars = new HashMap<>(Map.of("key", "val"));

    Map<String, Object> result = VariablesUtils.getMergedVariables(null, currentVars);

    assertSame(currentVars, result);
    assertEquals(1, result.size());
  }

  // ========================================================================================
  // getMergedVariables: null variables inside Variables object
  // ========================================================================================

  @Test
  void getMergedVariables_nullVariablesInside_returnsCurrentVariablesUnchanged() {
    Map<String, Object> currentVars = new HashMap<>(Map.of("key", "val"));

    Variables received = Variables.builder().variables(null).build();

    Map<String, Object> result = VariablesUtils.getMergedVariables(received, currentVars);

    assertSame(currentVars, result);
  }

  // ========================================================================================
  // getMergedVariables: null currentVariables
  // ========================================================================================

  @Test
  void getMergedVariables_nullCurrentVariables_returnsNull() {
    Variables received = Variables.builder().variables(Map.of("key", "val")).build();

    Map<String, Object> result = VariablesUtils.getMergedVariables(received, null);

    assertNull(result);
  }

  // ========================================================================================
  // getMergedVariables: all nulls
  // ========================================================================================

  @Test
  void getMergedVariables_allNull_returnsNull() {
    Map<String, Object> result = VariablesUtils.getMergedVariables(null, null);

    assertNull(result);
  }

  // ========================================================================================
  // getMergedVariables: empty received variables (UPDATE does nothing harmful)
  // ========================================================================================

  @Test
  void getMergedVariables_emptyReceivedVariables_leavesCurrentUnchanged() {
    Map<String, Object> currentVars = new HashMap<>(Map.of("existing", "val"));

    Variables received =
        Variables.builder().variables(Map.of()).action(Variables.VariableAction.UPDATE).build();

    Map<String, Object> result = VariablesUtils.getMergedVariables(received, currentVars);

    assertEquals(1, result.size());
    assertEquals("val", result.get("existing"));
  }

  // ========================================================================================
  // getMergedVariables: default action is UPDATE
  // ========================================================================================

  @Test
  void getMergedVariables_defaultAction_isUpdate() {
    Map<String, Object> currentVars = new HashMap<>();

    Variables received =
        Variables.builder().variables(Map.of("added", "val")).build(); // default action is UPDATE

    Map<String, Object> result = VariablesUtils.getMergedVariables(received, currentVars);

    assertEquals("val", result.get("added"));
  }

  // ========================================================================================
  // getMergedVariables: UPDATE overwrites existing keys
  // ========================================================================================

  @Test
  void getMergedVariables_updateAction_overwritesExistingKeys() {
    Map<String, Object> currentVars = new HashMap<>();
    currentVars.put("key", "oldValue");

    Variables received =
        Variables.builder()
            .variables(Map.of("key", "newValue"))
            .action(Variables.VariableAction.UPDATE)
            .build();

    Map<String, Object> result = VariablesUtils.getMergedVariables(received, currentVars);

    assertEquals("newValue", result.get("key"));
  }

  // ========================================================================================
  // getMergedVariables: UPDATE with various value types
  // ========================================================================================

  @Test
  void getMergedVariables_updateAction_worksWithVariousValueTypes() {
    Map<String, Object> currentVars = new HashMap<>();

    Map<String, Object> receivedMap = new HashMap<>();
    receivedMap.put("stringVal", "hello");
    receivedMap.put("intVal", 42);
    receivedMap.put("doubleVal", 3.14);
    receivedMap.put("boolVal", true);
    receivedMap.put("nullVal", null);

    Variables received =
        Variables.builder().variables(receivedMap).action(Variables.VariableAction.UPDATE).build();

    Map<String, Object> result = VariablesUtils.getMergedVariables(received, currentVars);

    assertEquals("hello", result.get("stringVal"));
    assertEquals(42, result.get("intVal"));
    assertEquals(3.14, result.get("doubleVal"));
    assertEquals(true, result.get("boolVal"));
    assertTrue(result.containsKey("nullVal"));
    assertNull(result.get("nullVal"));
  }

  // ========================================================================================
  // getMergedVariables: returns same reference (mutates in place)
  // ========================================================================================

  @Test
  void getMergedVariables_returnsSameReferenceAsCurrentVariables() {
    Map<String, Object> currentVars = new HashMap<>();
    currentVars.put("a", 1);

    Variables received = Variables.builder().variables(Map.of("b", 2)).build();

    Map<String, Object> result = VariablesUtils.getMergedVariables(received, currentVars);

    assertSame(
        currentVars, result, "Method should mutate and return the same currentVariables map");
  }

  // ========================================================================================
  // getEvaluatedVariable: null and empty inputs
  // ========================================================================================

  @Test
  void getEvaluatedVariable_nullFx_returnsNull() {
    Object result = VariablesUtils.getEvaluatedVariable(null, Map.of("key", "val"));
    assertNull(result);
  }

  @Test
  void getEvaluatedVariable_emptyFx_returnsNull() {
    Object result = VariablesUtils.getEvaluatedVariable("", Map.of("key", "val"));
    assertNull(result);
  }

  // ========================================================================================
  // getEvaluatedVariable: literal string (no = prefix) returned as-is
  // ========================================================================================

  @Test
  void getEvaluatedVariable_literalString_returnedAsIs() {
    Object result = VariablesUtils.getEvaluatedVariable("hello", Map.of());
    assertEquals("hello", result);
  }

  @Test
  void getEvaluatedVariable_stringWithoutEqualsPrefix_returnedAsIs() {
    Object result = VariablesUtils.getEvaluatedVariable("some literal value", Map.of());
    assertEquals("some literal value", result);
  }

  // ========================================================================================
  // getEvaluatedVariable: FEEL expression (= prefix)
  // ========================================================================================

  @Test
  void getEvaluatedVariable_simpleFeelVariableReference_returnsValue() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("myVar", "resolvedValue");

    Object result = VariablesUtils.getEvaluatedVariable("=myVar", variables);

    assertEquals("resolvedValue", result);
  }

  @Test
  void getEvaluatedVariable_feelExpressionWithNumber_returnsEvaluatedValue() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("amount", 100);

    Object result = VariablesUtils.getEvaluatedVariable("=amount", variables);

    // FEEL engine returns the numeric value
    assertNotNull(result);
  }

  // ========================================================================================
  // getEvaluatedVariable: non-existent variable in FEEL -> returns null
  // ========================================================================================

  @Test
  void getEvaluatedVariable_nonExistentVariable_returnsNull() {
    Map<String, Object> variables = new HashMap<>();

    Object result = VariablesUtils.getEvaluatedVariable("=nonExistent", variables);

    assertNull(result);
  }

  // ========================================================================================
  // DELETE action with empty received map does not affect current variables
  // ========================================================================================

  @Test
  void getMergedVariables_deleteAction_emptyReceivedVariables_noChange() {
    Map<String, Object> currentVars = new HashMap<>();
    currentVars.put("key1", "val1");
    currentVars.put("key2", "val2");

    Variables received =
        Variables.builder().variables(Map.of()).action(Variables.VariableAction.DELETE).build();

    Map<String, Object> result = VariablesUtils.getMergedVariables(received, currentVars);

    assertEquals(2, result.size());
    assertEquals("val1", result.get("key1"));
    assertEquals("val2", result.get("key2"));
  }
}
