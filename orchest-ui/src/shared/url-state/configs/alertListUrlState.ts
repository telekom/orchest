import { buildSearchParams } from '../buildSearchParams';
import { parseLocalDateParam, serializeLocalDate } from '../serializers/dates';
import {
  parseEnum,
  parseNumber,
  parseString,
  serializeEnum,
  serializeNumber,
  serializeString,
} from '../serializers/primitives';
import type { UrlStateSchema } from '../types';

export const ALERT_STATES = ['FIRING', 'SILENCED', 'DISABLED', 'ACKNOWLEDGED', 'RESOLVED'] as const;
export type AlertListStateFilter = (typeof ALERT_STATES)[number];

export const ALERT_SORT_VALUES = [
  '-count',
  '+count',
  '-createdAt',
  '+createdAt',
  '-updatedAt',
  '+updatedAt',
  '-severity',
  '+severity',
  '-state',
  '+state',
] as const;

export interface AlertListUrlState {
  state: AlertListStateFilter;
  processDefinitionId: string | null;
  from: string | null;
  to: string | null;
  timezone: string | null;
  page: number;
  size: number;
  sort: string;
  alertId: string | null;
}

export const DEFAULT_ALERT_LIST_URL_STATE: AlertListUrlState = {
  state: 'FIRING',
  processDefinitionId: null,
  from: null,
  to: null,
  timezone: null,
  page: 0,
  size: 25,
  sort: '-count',
  alertId: null,
};

const isEmptyString = (value: string | null | undefined) => !value?.trim();

export const alertListUrlSchema: UrlStateSchema<AlertListUrlState> = {
  state: {
    parse: (value) => {
      if (!value?.trim()) return 'FIRING';
      const parsed = parseEnum(value, ALERT_STATES);
      if (parsed === null) {
        throw new Error('Invalid alert state');
      }
      return parsed;
    },
    serialize: (value) => (value === 'FIRING' ? undefined : serializeEnum(value, ALERT_STATES)),
    isEmpty: (value) => !value || value === 'FIRING',
  },
  processDefinitionId: {
    parse: parseString,
    serialize: serializeString,
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
  page: {
    parse: (value) => parseNumber(value, 0, { min: 0 }),
    serialize: (value) => serializeNumber(value === 0 ? null : value, { min: 0 }),
    isEmpty: (value) => value === 0,
  },
  size: {
    parse: (value) => parseNumber(value, 25, { min: 1, max: 100 }),
    serialize: (value) => serializeNumber(value === 25 ? null : value, { min: 1, max: 100 }),
    isEmpty: (value) => value === 25,
  },
  sort: {
    parse: (value) => parseEnum(value, ALERT_SORT_VALUES, '-count') ?? '-count',
    serialize: (value) => (value === '-count' ? undefined : value),
    isEmpty: (value) => value === '-count',
  },
  alertId: {
    parse: parseString,
    serialize: serializeString,
    isEmpty: isEmptyString,
  },
};

export function buildAlertListSearchParams(
  patch: Partial<AlertListUrlState> = {},
): URLSearchParams {
  return buildSearchParams(
    { ...DEFAULT_ALERT_LIST_URL_STATE, ...patch },
    alertListUrlSchema,
    DEFAULT_ALERT_LIST_URL_STATE,
  );
}

export function buildAlertListPath(patch: Partial<AlertListUrlState> = {}): string {
  const params = buildAlertListSearchParams(patch);
  params.set('view', 'alerts');
  const query = params.toString();
  return `/dashboard?${query}`;
}
