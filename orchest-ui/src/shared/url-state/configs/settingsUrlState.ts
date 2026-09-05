import { ConfigStatus, FilterValue } from '@/shared/constants/status';
import { buildSearchParams } from '../buildSearchParams';
import { parseEnum, parseNumber, parseString, serializeEnum, serializeNumber, serializeString } from '../serializers/primitives';
import type { UrlStateSchema } from '../types';

export const SETTINGS_STATUS_VALUES = [FilterValue.ALL, ConfigStatus.ENABLED, ConfigStatus.DISABLED] as const;

export interface SettingsUrlState {
  searchTerm: string;
  statusFilter: string;
  page: number;
  pageSize: number;
}

export const DEFAULT_SETTINGS_URL_STATE: SettingsUrlState = {
  searchTerm: '',
  statusFilter: FilterValue.ALL,
  page: 0,
  pageSize: 10,
};

export const settingsUrlSchema: UrlStateSchema<SettingsUrlState> = {
  searchTerm: {
    parse: (value) => parseString(value) ?? '',
    serialize: (value) => serializeString(value || null),
    isEmpty: (value) => !value,
  },
  statusFilter: {
    parse: (value) => {
      if (value === FilterValue.TOTAL || value === FilterValue.ALL) return FilterValue.ALL;
      return parseEnum(value, SETTINGS_STATUS_VALUES, FilterValue.ALL) ?? FilterValue.ALL;
    },
    serialize: (value) => serializeEnum(value, SETTINGS_STATUS_VALUES),
    isEmpty: (value) => value === FilterValue.ALL,
  },
  page: {
    parse: (value) => parseNumber(value, 0, { min: 0 }),
    serialize: (value) => serializeNumber(value, { min: 0 }),
    isEmpty: (value) => value === 0,
  },
  pageSize: {
    parse: (value) => parseNumber(value, 10, { min: 1, max: 100 }),
    serialize: (value) => serializeNumber(value, { min: 1, max: 100 }),
    isEmpty: (value) => value === 10,
  },
};

export function buildSettingsSearchParams(patch: Partial<SettingsUrlState> = {}): URLSearchParams {
  return buildSearchParams(
    { ...DEFAULT_SETTINGS_URL_STATE, ...patch },
    settingsUrlSchema,
    DEFAULT_SETTINGS_URL_STATE,
  );
}

export function buildSettingsPath(patch: Partial<SettingsUrlState> = {}): string {
  const params = buildSettingsSearchParams(patch);
  const query = params.toString();
  return query ? `/settings?${query}` : '/settings';
}
