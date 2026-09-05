import { describe, expect, it } from 'vitest';
import {
  COMPENSATION_FLOW_VERSION,
  getCompensationProcessDefinitionIds,
} from './compensateVariables';

describe('getCompensationProcessDefinitionIds', () => {
  it('returns empty set for undefined / empty definitions', () => {
    expect(getCompensationProcessDefinitionIds(undefined).size).toBe(0);
    expect(getCompensationProcessDefinitionIds([]).size).toBe(0);
  });

  it('includes only definitionIds that have version -1', () => {
    const ids = getCompensationProcessDefinitionIds([
      { definitionId: 'order', version: 1 },
      { definitionId: 'order', version: COMPENSATION_FLOW_VERSION },
      { definitionId: 'billing', version: 2 },
      { definitionId: 'refund', version: COMPENSATION_FLOW_VERSION },
    ]);

    expect([...ids].sort()).toEqual(['order', 'refund']);
  });
});
