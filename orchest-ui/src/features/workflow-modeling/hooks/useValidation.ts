import Linter from 'bpmnlint/lib/linter';
import { useCallback, useState } from 'react';
import { bpmnlintResolver, lintConfig } from '../config/bpmnlintConfig';
import type { DiagramType } from '../components/BaseModeler/BaseModeler';

export interface ValidationIssue {
  id: string;
  elementName?: string;
  message: string;
  severity: 'error' | 'warning';
  rule: string;
}

interface UseValidationReturn {
  issues: ValidationIssue[];
  hasErrors: boolean;
  hasWarnings: boolean;
  isValidating: boolean;
  validate: (modeler: unknown, diagramType: DiagramType, xml: string) => Promise<ValidationIssue[]>;
  clearValidation: () => void;
}

function getElementName(modeler: unknown, elementId: string): string | undefined {
  try {
    const registry = (modeler as any).get?.('elementRegistry');
    if (!registry) return undefined;
    const element = registry.get(elementId);
    return element?.businessObject?.name || undefined;
  } catch {
    return undefined;
  }
}

async function validateBpmn(modeler: unknown): Promise<ValidationIssue[]> {
  const definitions = (modeler as any).getDefinitions?.();
  if (!definitions) return [];

  const linter = new Linter({ resolver: bpmnlintResolver });
  const results = await linter.lint(definitions, lintConfig);
  const issues: ValidationIssue[] = [];

  for (const [ruleName, reports] of Object.entries(results)) {
    for (const report of reports as any[]) {
      const severity = report.category === 'error' ? 'error' : 'warning';
      const elementId = report.id || report.node?.id || 'unknown';
      issues.push({
        id: elementId,
        elementName: getElementName(modeler, elementId),
        message: report.message || `Rule violation: ${ruleName}`,
        severity,
        rule: ruleName,
      });
    }
  }

  return issues;
}

async function validateDmn(_modeler: unknown, xml: string): Promise<ValidationIssue[]> {
  const issues: ValidationIssue[] = [];

  try {
    const parser = new DOMParser();
    const doc = parser.parseFromString(xml, 'application/xml');

    const parseError = doc.querySelector('parsererror');
    if (parseError) {
      issues.push({
        id: 'xml',
        message: 'Invalid XML: ' + (parseError.textContent?.slice(0, 100) || 'Parse error'),
        severity: 'error',
        rule: 'valid-xml',
      });
      return issues;
    }

    const decisions = doc.querySelectorAll('decision');
    decisions.forEach((decision) => {
      const id = decision.getAttribute('id') || 'unknown';
      const name = decision.getAttribute('name') || undefined;

      if (!decision.getAttribute('name')) {
        issues.push({
          id,
          elementName: name,
          message: 'Decision element is missing a name',
          severity: 'warning',
          rule: 'decision-name-required',
        });
      }

      const hasDecisionTable = decision.querySelector('decisionTable');
      const hasLiteralExpression = decision.querySelector('literalExpression');
      const hasContext = decision.querySelector('context');

      if (!hasDecisionTable && !hasLiteralExpression && !hasContext) {
        issues.push({
          id,
          elementName: name,
          message: 'Decision has no logic defined (missing decision table, literal expression, or context)',
          severity: 'error',
          rule: 'decision-logic-required',
        });
      }
    });

    if (decisions.length === 0) {
      issues.push({
        id: 'definitions',
        message: 'No decision elements found in DMN',
        severity: 'warning',
        rule: 'decision-required',
      });
    }
  } catch (e) {
    issues.push({
      id: 'xml',
      message: `DMN validation failed: ${e instanceof Error ? e.message : 'Unknown error'}`,
      severity: 'error',
      rule: 'validation-error',
    });
  }

  return issues;
}

export function useValidation(): UseValidationReturn {
  const [issues, setIssues] = useState<ValidationIssue[]>([]);
  const [isValidating, setIsValidating] = useState(false);

  const validate = useCallback(async (modeler: unknown, diagramType: DiagramType, xml: string): Promise<ValidationIssue[]> => {
    setIsValidating(true);
    try {
      let result: ValidationIssue[];
      if (diagramType === 'bpmn') {
        result = await validateBpmn(modeler);
      } else if (diagramType === 'dmn') {
        result = await validateDmn(modeler, xml);
      } else {
        result = [];
      }
      setIssues(result);
      return result;
    } finally {
      setIsValidating(false);
    }
  }, []);

  const clearValidation = useCallback(() => {
    setIssues([]);
  }, []);

  const hasErrors = issues.some((i) => i.severity === 'error');
  const hasWarnings = issues.some((i) => i.severity === 'warning');

  return { issues, hasErrors, hasWarnings, isValidating, validate, clearValidation };
}