package io.telekom.orchest.enginecore.dmn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.telekom.orchest.api.core.adapters.data.dto.DecisionEvaluationResponseDTO;
import io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition;
import io.telekom.orchest.api.core.model.dmn.Decision;
import io.telekom.orchest.api.core.model.dmn.DecisionTable;
import io.telekom.orchest.api.core.model.dmn.Input;
import io.telekom.orchest.api.core.model.dmn.Output;
import io.telekom.orchest.api.core.model.dmn.Rule;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests DMN engine decision table evaluation including hit policies and unary test matching. */
class DmnEngineTest {

  @Test
  @DisplayName("input entry unary tests: ?.path on evaluated input with variables context")
  void unaryInputEntry_optionalPath() {
    Input input = new Input();
    input.setId("in1");
    input.setName("col1");
    input.setInputExpression("row");

    Output output = new Output();
    output.setId("out1");
    output.setName("result");

    Rule rule = new Rule();
    rule.setId("r1");
    rule.addInputEntry("?.specificationId.name != null");
    rule.addOutputEntry("\"matched\"");

    DecisionTable table = new DecisionTable();
    table.setId("t1");
    table.setHitPolicy("UNIQUE");
    table.addInput(input);
    table.addOutput(output);
    table.addRule(rule);

    Decision decision = new Decision("dec1");
    decision.setDecisionTable(table);

    DecisionDefinition definition = new DecisionDefinition();
    definition.addDecision(decision);

    Map<String, Object> vars =
        Map.of("row", Map.of("specificationId", Map.of("name", "n1")), "unused", 1);

    DmnEngine engine = new DmnEngine();
    DecisionEvaluationResponseDTO response = engine.evaluate(definition, "dec1", vars);

    assertEquals("matched", response.getOutputVariables().get("result"));
  }

  @Test
  @DisplayName("input entry unary tests: ? < 0 on numeric evaluated input")
  void unaryInputEntry_numericComparison() {
    Input input = new Input();
    input.setId("in1");
    input.setName("amount");
    input.setInputExpression("amount");

    Output output = new Output();
    output.setId("out1");
    output.setName("result");

    Rule rule = new Rule();
    rule.setId("r1");
    rule.addInputEntry("? < 0");
    rule.addOutputEntry("\"negative\"");

    DecisionTable table = new DecisionTable();
    table.setId("t1");
    table.setHitPolicy("UNIQUE");
    table.addInput(input);
    table.addOutput(output);
    table.addRule(rule);

    Decision decision = new Decision("dec1");
    decision.setDecisionTable(table);

    DecisionDefinition definition = new DecisionDefinition();
    definition.addDecision(decision);

    Map<String, Object> vars = Map.of("amount", -5);

    DmnEngine engine = new DmnEngine();
    DecisionEvaluationResponseDTO response = engine.evaluate(definition, "dec1", vars);

    assertEquals("negative", response.getOutputVariables().get("result"));
  }
}
