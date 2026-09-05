import { FilterValue } from '@/shared/constants/status';
import { buildSearchParams } from '../buildSearchParams';
import { parseLocalDateParam, serializeLocalDate } from '../serializers/dates';
import { parseEnum, parseString, serializeEnum, serializeString } from '../serializers/primitives';
import type { UrlStateSchema } from '../types';

export const DECISION_LIST_STATUS_VALUES = ['EVALUATED', 'FAILED', 'UNKNOWN'] as const;

export interface DecisionListUrlState {
  decisionId: string | null;
  version: string | null;
  searchText: string;
  status: string | null;
  from: string | null;
  to: string | null;
  timezone: string | null;
}

export const DEFAULT_DECISION_LIST_URL_STATE: DecisionListUrlState = {
  decisionId: null,
  version: null,
  searchText: '',
  status: null,
  from: null,
  to: null,
  timezone: null,
};

const isEmptyString = (value: string | null | undefined) => !value?.trim();

export const decisionListUrlSchema: UrlStateSchema<DecisionListUrlState> = {
  decisionId: {
    parse: parseString,
    serialize: serializeString,
    isEmpty: isEmptyString,
  },
  version: {
    parse: parseString,
    serialize: serializeString,
    isEmpty: isEmptyString,
  },
  searchText: {
    parse: (value) => parseString(value) ?? '',
    serialize: (value) => serializeString(value || null),
    isEmpty: (value) => !value,
  },
  status: {
    parse: (value) => {
      if (value === FilterValue.ALL || value.toLowerCase() === 'all') return null;
      return parseEnum(value, DECISION_LIST_STATUS_VALUES);
    },
    serialize: (value) => serializeEnum(value, DECISION_LIST_STATUS_VALUES),
    isEmpty: isEmptyString,
  },
  from: {
    parse: parseLocalDateParam,
    serialize: serializeLocalDate,
    isEmpty: isEmptyString,
  },
  to: {
    parse: parseLocalDateParam,
    serialize: serializeLocalDate,
    isEmpty: isEmptyString,
  },
  timezone: {
    parse: parseString,
    serialize: serializeString,
    isEmpty: isEmptyString,
  },
};

export function buildDecisionListSearchParams(
  patch: Partial<DecisionListUrlState> = {},
): URLSearchParams {
  return buildSearchParams(
    { ...DEFAULT_DECISION_LIST_URL_STATE, ...patch },
    decisionListUrlSchema,
    DEFAULT_DECISION_LIST_URL_STATE,
  );
}

export function buildDecisionListPath(patch: Partial<DecisionListUrlState> = {}): string {
  const params = buildDecisionListSearchParams(patch);
  const query = params.toString();
  return query ? `/decisions?${query}` : '/decisions';
}
