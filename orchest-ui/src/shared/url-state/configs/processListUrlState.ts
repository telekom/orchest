import { FilterValue, ProcessStatus } from '@/shared/constants/status';
import type { SortDirection } from '@/shared/components/DataTable/types';
import { buildSearchParams } from '../buildSearchParams';
import { parseLocalDateParam, serializeLocalDate } from '../serializers/dates';
import { parseEnum, parseString, serializeEnum, serializeString } from '../serializers/primitives';
import type { UrlStateSchema } from '../types';

export interface ProcessListUrlState {
  process: string | null;
  version: string | null;
  searchText: string;
  status: string | null;
  from: string | null;
  to: string | null;
  timezone: string | null;
  sortBy: string;
  sortOrder: SortDirection;
}

export const PROCESS_LIST_STATUS_VALUES = [
  ProcessStatus.RUNNING,
  ProcessStatus.COMPLETED,
  ProcessStatus.FAILED,
  ProcessStatus.HOLD,
  ProcessStatus.INCIDENT,
  ProcessStatus.CANCELLED,
  ProcessStatus.ACTIVE,
  ProcessStatus.STARTED,
  ProcessStatus.TERMINATED,
] as const;

export const PROCESS_LIST_SORT_FIELDS = [
  'processInstanceId',
  'processDefinitionId',
  'version',
  'state',
  'createdAt',
  'completedAt',
] as const;

export const DEFAULT_PROCESS_LIST_URL_STATE: ProcessListUrlState = {
  process: null,
  version: null,
  searchText: '',
  status: null,
  from: null,
  to: null,
  timezone: null,
  sortBy: 'createdAt',
  sortOrder: 'desc',
};

const isEmptyString = (value: string | null | undefined) => !value?.trim();

export const processListUrlSchema: UrlStateSchema<ProcessListUrlState> = {
  process: {
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
      return parseEnum(value, PROCESS_LIST_STATUS_VALUES);
    },
    serialize: (value) => serializeEnum(value, PROCESS_LIST_STATUS_VALUES),
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
  sortBy: {
    parse: (value) => parseEnum(value, PROCESS_LIST_SORT_FIELDS, 'createdAt') ?? 'createdAt',
    serialize: (value) => serializeEnum(value, PROCESS_LIST_SORT_FIELDS),
    isEmpty: (value) => value === 'createdAt',
  },
  sortOrder: {
    parse: (value) => (value === 'asc' ? 'asc' : 'desc'),
    serialize: (value) => (value === 'desc' ? undefined : value),
    isEmpty: (value) => value === 'desc',
  },
};

export function buildProcessListSearchParams(
  patch: Partial<ProcessListUrlState> = {},
): URLSearchParams {
  return buildSearchParams(
    { ...DEFAULT_PROCESS_LIST_URL_STATE, ...patch },
    processListUrlSchema,
    DEFAULT_PROCESS_LIST_URL_STATE,
  );
}

export function buildProcessListPath(patch: Partial<ProcessListUrlState> = {}): string {
  const params = buildProcessListSearchParams(patch);
  const query = params.toString();
  return query ? `/processes?${query}` : '/processes';
}
