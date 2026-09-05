import type { UrlFieldDef, UrlStateSchema } from './types';

export function buildSearchParams<T extends Record<string, unknown>>(
  state: T,
  schema: UrlStateSchema<T>,
  defaults: T,
): URLSearchParams {
  const params = new URLSearchParams();

  for (const key of Object.keys(schema) as (keyof T)[]) {
    const def = schema[key] as UrlFieldDef<unknown>;
    const value = state[key];
    const defaultValue = defaults[key];

    if (def.isEmpty?.(value) ?? value === defaultValue) continue;

    const serialized = def.serialize(value);
    if (serialized == null || serialized === '') continue;

    params.set(String(key), serialized);
  }

  return params;
}

export function searchParamsToString(params: URLSearchParams): string {
  const entries = Array.from(params.entries()).sort(([a], [b]) => a.localeCompare(b));
  return new URLSearchParams(entries).toString();
}
