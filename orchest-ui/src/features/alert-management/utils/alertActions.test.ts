import { describe, expect, it } from 'vitest';
import { getAvailableAlertActions } from './alertActions';

describe('getAvailableAlertActions', () => {
  it('returns ack/silence/resolve for FIRING', () => {
    expect(getAvailableAlertActions('FIRING')).toEqual([
      'acknowledge',
      'silence',
      'resolve',
    ]);
  });

  it('returns silence/resolve for ACKNOWLEDGED', () => {
    expect(getAvailableAlertActions('ACKNOWLEDGED')).toEqual(['silence', 'resolve']);
  });

  it('returns unmute/resolve for SILENCED', () => {
    expect(getAvailableAlertActions('SILENCED')).toEqual(['unmute', 'resolve']);
  });

  it('returns no actions for RESOLVED', () => {
    expect(getAvailableAlertActions('RESOLVED')).toEqual([]);
  });
});
