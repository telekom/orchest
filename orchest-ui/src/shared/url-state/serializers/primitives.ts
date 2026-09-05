export function serializeString(value: string | null | undefined): string | undefined {
  const trimmed = value?.trim();
  return trimmed ? trimmed : undefined;
}

export function parseString(value: string | null): string | null {
  const trimmed = value?.trim();
  return trimmed ? trimmed : null;
}

export function serializeEnum<T extends string>(
  value: T | null | undefined,
  allowed: readonly T[],
): string | undefined {
  return value && allowed.includes(value) ? value : undefined;
}

export function parseEnum<T extends string>(
  value: string | null,
  allowed: readonly T[],
  fallback: T | null = null,
): T | null {
  if (!value) return fallback;
  return (allowed as readonly string[]).includes(value) ? (value as T) : fallback;
}

export function serializeBoolean(value: boolean | undefined, defaultValue = false): string | undefined {
  if (value === defaultValue) return undefined;
  return value ? 'true' : 'false';
}

export function parseBoolean(value: string | null, fallback = false): boolean {
  if (value === 'true') return true;
  if (value === 'false') return false;
  return fallback;
}

export function serializeNumber(
  value: number | null | undefined,
  { min, max }: { min?: number; max?: number } = {},
): string | undefined {
  if (value == null || Number.isNaN(value)) return undefined;
  if (min != null && value < min) return undefined;
  if (max != null && value > max) return undefined;
  return String(value);
}

export function parseNumber(
  value: string | null,
  fallback: number,
  opts: { min?: number; max?: number } = {},
): number {
  if (!value) return fallback;
  const parsed = Number(value);
  if (Number.isNaN(parsed)) return fallback;
  if (opts.min != null && parsed < opts.min) return fallback;
  if (opts.max != null && parsed > opts.max) return fallback;
  return parsed;
}
