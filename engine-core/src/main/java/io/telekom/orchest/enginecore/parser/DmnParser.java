package io.telekom.orchest.enginecore.parser;

import io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition;
import io.telekom.orchest.api.core.model.dmn.*;
import io.telekom.orchest.enginecore.bpmn.utils.XMLUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Parses DMN 1.3 XML into the Orchest {@link DecisionDefinition} domain model. */
@Slf4j
public class DmnParser {
  private static final String DMN_NAMESPACE = "https://www.omg.org/spec/DMN/20191111/MODEL/";

  /**
   * Parses a DMN 1.3 XML string into a DecisionDefinition.
   *
   * @param dmnXML the raw DMN XML content
   * @return the parsed decision definition with all decisions, tables, and rules resolved
   * @throws RuntimeException if parsing fails
   */
  public static DecisionDefinition parse(String dmnXML) {
    String minifiedXml = XMLUtils.minifyXml(dmnXML);
    DecisionDefinition parse =
        parse(new ByteArrayInputStream(minifiedXml.getBytes(StandardCharsets.UTF_8)));
    parse.setDefinitionXML(minifiedXml);
    return parse;
  }

  /**
   * Parses a DMN 1.3 XML input stream into a DecisionDefinition.
   *
   * @param inputStream the DMN XML input stream
   * @return the parsed decision definition
   * @throws RuntimeException if parsing fails
   */
  public static DecisionDefinition parse(InputStream inputStream) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(inputStream);

      Element root = document.getDocumentElement();

      // Parse definitions element
      String definitionsId = getAttribute(root, "id");
      String definitionsName = getAttribute(root, "name");
      String namespace = root.getNamespaceURI();

      DecisionDefinition definition = new DecisionDefinition();
      definition.setDefinitionId(definitionsId);
      definition.setName(definitionsName);
      definition.setNamespace(namespace != null ? namespace : DMN_NAMESPACE);

      log.info("Parsing DMN definitions: {}", definition.getName());

      // Parse all decisions
      NodeList decisionNodes = root.getElementsByTagNameNS(DMN_NAMESPACE, "decision");
      for (int i = 0; i < decisionNodes.getLength(); i++) {
        Node decisionNode = decisionNodes.item(i);
        if (decisionNode.getNodeType() == Node.ELEMENT_NODE) {
          Decision decision = parseDecision((Element) decisionNode);
          definition.addDecision(decision);
          log.debug("Parsed decision: {} ({})", decision.getName(), decision.getId());
        }
      }

      definition.setDecisionIds(definition.getDecisions().keySet().stream().toList());

      return definition;

    } catch (Exception e) {
      log.error("Failed to parse DMN file", e);
      throw new RuntimeException("DMN parsing failed", e);
    }
  }

  private static Decision parseDecision(Element decisionElement) {
    String decisionId = getAttribute(decisionElement, "id");
    String decisionName = getAttribute(decisionElement, "name");

    Decision decision = new Decision(decisionId);
    decision.setName(decisionName);

    // Parse question
    NodeList questionNodes = decisionElement.getElementsByTagNameNS(DMN_NAMESPACE, "question");
    if (questionNodes.getLength() > 0) {
      decision.setQuestion(getTextContent(questionNodes.item(0)));
    }

    // Parse allowedAnswers
    NodeList allowedAnswersNodes =
        decisionElement.getElementsByTagNameNS(DMN_NAMESPACE, "allowedAnswers");
    if (allowedAnswersNodes.getLength() > 0) {
      decision.setAllowedAnswers(getTextContent(allowedAnswersNodes.item(0)));
    }

    // Parse decision table
    NodeList decisionTableNodes =
        decisionElement.getElementsByTagNameNS(DMN_NAMESPACE, "decisionTable");
    if (decisionTableNodes.getLength() > 0) {
      DecisionTable decisionTable = parseDecisionTable((Element) decisionTableNodes.item(0));
      decision.setDecisionTable(decisionTable);
    }

    return decision;
  }

  private static DecisionTable parseDecisionTable(Element decisionTableElement) {
    DecisionTable decisionTable = new DecisionTable();

    String id = getAttribute(decisionTableElement, "id");
    decisionTable.setId(id);

    // Parse hit policy
    String hitPolicy = getAttribute(decisionTableElement, "hitPolicy");
    if (hitPolicy != null && !hitPolicy.isEmpty()) {
      decisionTable.setHitPolicy(hitPolicy);
    } else {
      decisionTable.setHitPolicy("UNIQUE"); // Default hit policy
    }

    // Parse aggregation (for COLLECT hit policy)
    String aggregation = getAttribute(decisionTableElement, "aggregation");
    if (aggregation != null && !aggregation.isEmpty()) {
      decisionTable.setAggregation(aggregation);
    }

    // Parse inputs
    NodeList inputNodes = decisionTableElement.getElementsByTagNameNS(DMN_NAMESPACE, "input");
    for (int i = 0; i < inputNodes.getLength(); i++) {
      Element inputElement = (Element) inputNodes.item(i);
      Input input = parseInput(inputElement);
      decisionTable.addInput(input);
    }

    // Parse outputs
    NodeList outputNodes = decisionTableElement.getElementsByTagNameNS(DMN_NAMESPACE, "output");
    for (int i = 0; i < outputNodes.getLength(); i++) {
      Element outputElement = (Element) outputNodes.item(i);
      Output output = parseOutput(outputElement);
      decisionTable.addOutput(output);
    }

    // Parse rules
    NodeList ruleNodes = decisionTableElement.getElementsByTagNameNS(DMN_NAMESPACE, "rule");
    for (int i = 0; i < ruleNodes.getLength(); i++) {
      Element ruleElement = (Element) ruleNodes.item(i);
      Rule rule = parseRule(ruleElement, i + 1);
      decisionTable.addRule(rule);
    }

    return decisionTable;
  }

  private static Input parseInput(Element inputElement) {
    Input input = new Input();

    String id = getAttribute(inputElement, "id");
    input.setId(id);

    String label = getAttribute(inputElement, "label");
    input.setLabel(label);

    // Parse inputExpression
    NodeList expressionNodes =
        inputElement.getElementsByTagNameNS(DMN_NAMESPACE, "inputExpression");
    if (expressionNodes.getLength() > 0) {
      Element expressionElement = (Element) expressionNodes.item(0);
      String typeRef = getAttribute(expressionElement, "typeRef");
      input.setName(expressionElement.getTextContent());
      input.setTypeRef(typeRef);

      // Look for <text> element first (DMN 1.3 format)
      NodeList textElements = expressionElement.getElementsByTagNameNS(DMN_NAMESPACE, "text");
      if (textElements.getLength() > 0) {
        String text = getTextContent(textElements.item(0));
        if (text != null && !text.isEmpty()) {
          input.setInputExpression(text);
        }
      } else {
        // Fallback: get direct text content
        String text = getTextContent(expressionElement);
        if (text != null && !text.isEmpty()) {
          input.setInputExpression(text);
        }
      }
    }

    // Parse inputVariable (if present as attribute)
    String inputVariable = getAttribute(inputElement, "inputVariable");
    if (inputVariable != null && !inputVariable.isEmpty()) {
      input.setInputVariable(inputVariable);
    }

    return input;
  }

  private static Output parseOutput(Element outputElement) {
    Output output = new Output();

    String id = getAttribute(outputElement, "id");
    output.setId(id);

    String label = getAttribute(outputElement, "label");
    output.setLabel(label);

    String name = getAttribute(outputElement, "name");
    output.setName(name);

    String typeRef = getAttribute(outputElement, "typeRef");
    output.setTypeRef(typeRef);

    return output;
  }

  private static Rule parseRule(Element ruleElement) {
    return parseRule(ruleElement, 0);
  }

  private static Rule parseRule(Element ruleElement, int ruleNumber) {
    Rule rule = new Rule();

    String id = getAttribute(ruleElement, "id");
    rule.setId(id);
    rule.setRuleNumber(ruleNumber);

    // Parse description
    NodeList descriptionNodes = ruleElement.getElementsByTagNameNS(DMN_NAMESPACE, "description");
    if (descriptionNodes.getLength() > 0) {
      rule.setDescription(getTextContent(descriptionNodes.item(0)));
    }

    // Parse input entries
    NodeList inputEntryNodes = ruleElement.getElementsByTagNameNS(DMN_NAMESPACE, "inputEntry");
    for (int i = 0; i < inputEntryNodes.getLength(); i++) {
      Element inputEntryElement = (Element) inputEntryNodes.item(i);
      String text = parseEntryText(inputEntryElement);
      rule.addInputEntry(text);
    }

    // Parse output entries
    NodeList outputEntryNodes = ruleElement.getElementsByTagNameNS(DMN_NAMESPACE, "outputEntry");
    for (int i = 0; i < outputEntryNodes.getLength(); i++) {
      Element outputEntryElement = (Element) outputEntryNodes.item(i);
      String text = parseEntryText(outputEntryElement);
      rule.addOutputEntry(text);
    }

    return rule;
  }

  /**
   * Parses text content from an entry element (inputEntry or outputEntry). Handles both <text>
   * child elements and direct text content.
   */
  private static String parseEntryText(Element entryElement) {
    // Look for <text> element first (DMN 1.3 format)
    NodeList textElements = entryElement.getElementsByTagNameNS(DMN_NAMESPACE, "text");
    if (textElements.getLength() > 0) {
      String text = getTextContent(textElements.item(0));
      return text != null ? text : "";
    }

    // Fallback: get direct text content
    String text = getTextContent(entryElement);
    return text != null ? text : "";
  }

  private static String getAttribute(Element element, String attributeName) {
    if (element.hasAttribute(attributeName)) {
      return element.getAttribute(attributeName);
    }
    return null;
  }

  private static String getTextContent(Node node) {
    if (node == null) {
      return null;
    }

    // First try to get text from <text> child element if it exists
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      Element element = (Element) node;
      NodeList textElements = element.getElementsByTagNameNS(DMN_NAMESPACE, "text");
      if (textElements.getLength() > 0) {
        return getDirectTextContent(textElements.item(0));
      }
    }

    // Fallback: get direct text content
    return getDirectTextContent(node);
  }

  /**
   * Gets direct text content from a node (text nodes and CDATA sections). This method extracts text
   * from TEXT_NODE and CDATA_SECTION_NODE children only.
   */
  private static String getDirectTextContent(Node node) {
    if (node == null) {
      return null;
    }

    // If it's a text node or CDATA section, return its content directly
    if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
      String text = node.getTextContent().trim();
      return text.isEmpty() ? null : text;
    }

    // Otherwise, collect text from child text/CDATA nodes
    StringBuilder text = new StringBuilder();
    NodeList childNodes = node.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node child = childNodes.item(i);
      if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
        String childText = child.getTextContent();
        if (childText != null) {
          text.append(childText);
        }
      }
    }

    String result = text.toString().trim();
    return result.isEmpty() ? null : result;
  }
}
