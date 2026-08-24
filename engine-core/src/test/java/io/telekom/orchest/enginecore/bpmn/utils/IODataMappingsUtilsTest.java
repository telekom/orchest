package io.telekom.orchest.enginecore.bpmn.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.model.bpmn.DataMapping;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests IO data mapping utilities: variable evaluation, mapping application, and removal. */
class IODataMappingsUtilsTest {

  @Test
  @DisplayName("getDataMappingsVariables returns empty map when list is null or empty")
  void getDataMappingsVariables_emptyOrNull() {
    Map<String, Object> vars = Map.of("a", 1);
    assertTrue(IODataMappingsUtils.getDataMappingsVariables(null, vars).isEmpty());
    assertTrue(IODataMappingsUtils.getDataMappingsVariables(List.of(), vars).isEmpty());
  }

  @Test
  @DisplayName("getDataMappingsVariables copies literal mapping values")
  void getDataMappingsVariables_literalValue() {
    List<DataMapping> mappings = List.of(new DataMapping("out", "hello"));
    Map<String, Object> vars = new HashMap<>();
    Map<String, Object> out = IODataMappingsUtils.getDataMappingsVariables(mappings, vars);
    assertEquals("hello", out.get("out"));
  }

  @Test
  @DisplayName("getDataMappingValue finds expression by name")
  void getDataMappingValue_found() {
    List<DataMapping> mappings = List.of(new DataMapping("x", "1"), new DataMapping("y", "=x+1"));
    assertTrue(IODataMappingsUtils.getDataMappingValue("y", mappings).isPresent());
    assertEquals("=x+1", IODataMappingsUtils.getDataMappingValue("y", mappings).orElseThrow());
  }

  @Test
  @DisplayName("getDataMappingValue empty when name missing")
  void getDataMappingValue_missing() {
    assertTrue(
        IODataMappingsUtils.getDataMappingValue("nope", List.of(new DataMapping("a", "b")))
            .isEmpty());
  }

  @Test
  @DisplayName("removeDataMappings removes variable keys referenced by mappings")
  void removeDataMappings() {
    Map<String, Object> vars = new HashMap<>(Map.of("a", 1, "b", 2));
    IODataMappingsUtils.removeDataMappings(List.of(new DataMapping("a", "x")), vars);
    assertFalse(vars.containsKey("a"));
    assertTrue(vars.containsKey("b"));
  }

  @Test
  @DisplayName("setDataMappings on node applies input and output lists")
  void setDataMappings_fromNode() {
    ServiceTaskNode node = new ServiceTaskNode("t", "task");
    node.getInputMappings().add(new DataMapping("inVar", "literal"));
    node.getOutputMappings().add(new DataMapping("outVar", "also"));

    Map<String, Object> vars = new HashMap<>();
    IODataMappingsUtils.setDataMappings(node, vars);

    assertEquals("literal", vars.get("inVar"));
    assertEquals("also", vars.get("outVar"));
  }

  @Test
  @DisplayName("setDataMappings list skips null evaluated values")
  void setDataMappings_skipsNullEvaluated() {
    List<DataMapping> mappings = new ArrayList<>();
    mappings.add(new DataMapping("skip", "=nonexistentVar"));
    Map<String, Object> vars = new HashMap<>();
    IODataMappingsUtils.setDataMappings(mappings, vars);
    assertFalse(vars.containsKey("skip"));
  }
}
