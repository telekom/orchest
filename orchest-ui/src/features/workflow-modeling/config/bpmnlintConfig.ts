import endEventRequired from 'bpmnlint/rules/end-event-required';
import startEventRequired from 'bpmnlint/rules/start-event-required';
import noDisconnected from 'bpmnlint/rules/no-disconnected';
import noDuplicateSequenceFlows from 'bpmnlint/rules/no-duplicate-sequence-flows';
import singleBlankStartEvent from 'bpmnlint/rules/single-blank-start-event';
import noImplicitSplit from 'bpmnlint/rules/no-implicit-split';

const rules: Record<string, unknown> = {
  'end-event-required': endEventRequired,
  'start-event-required': startEventRequired,
  'no-disconnected': noDisconnected,
  'no-duplicate-sequence-flows': noDuplicateSequenceFlows,
  'single-blank-start-event': singleBlankStartEvent,
  'no-implicit-split': noImplicitSplit,
};

export const bpmnlintResolver = {
  resolveRule(_pkg: string, ruleName: string) {
    return rules[ruleName] ?? null;
  },
  resolveConfig(_pkg: string, _configName: string) {
    return lintConfig;
  },
};

export const lintConfig = {
  rules: {
    'end-event-required': 'error',
    'start-event-required': 'error',
    'no-disconnected': 'warn',
    'no-duplicate-sequence-flows': 'error',
    'single-blank-start-event': 'warn',
    'no-implicit-split': 'warn',
  },
};
