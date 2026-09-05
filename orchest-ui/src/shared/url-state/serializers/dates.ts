/**
 * Dates stored as YYYY-MM-DD or YYYY-MM-DDTHH:mm to support time selection.
 */
const DATE_REGEX = /^\d{4}-\d{2}-\d{2}(T\d{2}:\d{2})?$/;

export function serializeLocalDate(value: string | null | undefined): string | undefined {
  if (!value?.trim()) return undefined;
  if (DATE_REGEX.test(value)) return value;
  return undefined;
}

export function parseLocalDateParam(value: string | null): string | null {
  if (!value?.trim()) return null;
  if (DATE_REGEX.test(value)) return value;
  return null;
}
