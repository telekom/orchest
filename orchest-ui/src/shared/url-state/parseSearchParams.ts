import type { UrlFieldDef, UrlStateSchema } from './types';

export interface ParseResult<T> {
  state: T;
  invalidKeys: string[];
}

export function parseSearchParams<T extends Record<string, unknown>>(
  searchParams: URLSearchParams,
  schema: UrlStateSchema<T>,
  defaults: T,
): ParseResult<T> {
  const state = { ...defaults };
  const invalidKeys: string[] = [];

  for (const key of Object.keys(schema) as (keyof T)[]) {
    const def = schema[key] as UrlFieldDef<unknown>;
    const raw = searchParams.get(String(key));
    if (raw == null) continue;

    try {
      const parsed = def.parse(raw);
      if (!def.isEmpty?.(parsed)) {
        state[key] = parsed as T[keyof T];
      }
    } catch {
      invalidKeys.push(String(key));
    }
  }

  return { state, invalidKeys };
}
