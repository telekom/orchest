/**
 * Magenta intensity steps for heatmaps — platform brand (`--color-magenta`).
 * Built with color-mix so we stay on-token without a full magenta scale.
 */
const HEAT_LEVELS = [
  'color-mix(in srgb, var(--color-magenta) 8%, transparent)',
  'color-mix(in srgb, var(--color-magenta) 16%, transparent)',
  'color-mix(in srgb, var(--color-magenta) 28%, transparent)',
  'color-mix(in srgb, var(--color-magenta) 40%, transparent)',
  'color-mix(in srgb, var(--color-magenta) 55%, transparent)',
  'color-mix(in srgb, var(--color-magenta) 70%, transparent)',
  'var(--color-magenta)',
  'var(--color-magenta-hover)',
] as const;

export function heatColorForCount(count: number, max: number): string {
  if (count <= 0 || max <= 0) {
    return 'color-mix(in srgb, var(--color-gray-400) 18%, transparent)';
  }
  const ratio = Math.min(1, count / max);
  const idx = Math.min(
    HEAT_LEVELS.length - 1,
    Math.max(0, Math.ceil(ratio * (HEAT_LEVELS.length - 1))),
  );
  return HEAT_LEVELS[idx];
}

export function heatTextColorForCount(count: number, max: number): string {
  if (count <= 0 || max <= 0) return 'var(--color-gray-500)';
  const ratio = count / max;
  return ratio >= 0.45 ? 'var(--color-gray-50)' : 'var(--color-gray-800)';
}
