import { describe, expect, it } from 'vitest';
import {
  getAlertSeverityBadgeVariant,
  getAlertStateBadgeVariant,
  getAlertVersion,
} from './alertBadges';

describe('alertBadges', () => {
  it('maps alert states to badge variants', () => {
    expect(getAlertStateBadgeVariant('FIRING')).toBe('destructive');
    expect(getAlertStateBadgeVariant('ACKNOWLEDGED')).toBe('warning');
    expect(getAlertStateBadgeVariant('SILENCED')).toBe('secondary');
    expect(getAlertStateBadgeVariant('RESOLVED')).toBe('success');
  });

  it('maps severities to badge variants', () => {
    expect(getAlertSeverityBadgeVariant('critical')).toBe('destructive');
    expect(getAlertSeverityBadgeVariant('HIGH')).toBe('incident');
    expect(getAlertSeverityBadgeVariant('warning')).toBe('warning');
    expect(getAlertSeverityBadgeVariant('info')).toBe('info');
    expect(getAlertSeverityBadgeVariant(undefined)).toBe('secondary');
  });

  it('reads version from metadata', () => {
    expect(getAlertVersion({ version: '3' })).toBe('3');
    expect(getAlertVersion({})).toBe('—');
  });
});
