import { describe, expect, it } from 'vitest';
import {
  DEFAULT_ALERT_LIST_URL_STATE,
  alertListUrlSchema,
  buildAlertListPath,
  buildAlertListSearchParams,
} from './alertListUrlState';
import { parseSearchParams } from '../parseSearchParams';

describe('alertListUrlState', () => {
  it('defaults sort to -count and omits default params from the URL', () => {
    expect(DEFAULT_ALERT_LIST_URL_STATE.sort).toBe('-count');
    const params = buildAlertListSearchParams({});
    expect(params.get('sort')).toBeNull();
    expect(params.toString()).toBe('');
  });

  it('builds dashboard paths with the alerts view flag', () => {
    expect(buildAlertListPath()).toBe('/dashboard?view=alerts');
    expect(buildAlertListPath({ state: 'SILENCED' })).toBe(
      '/dashboard?state=SILENCED&view=alerts',
    );
  });

  it('round-trips state, process, dates, timezone, pagination, and alertId', () => {
    const params = buildAlertListSearchParams({
      state: 'SILENCED',
      processDefinitionId: 'order-flow',
      from: '2026-08-01T00:00',
      to: '2026-08-10T23:59',
      timezone: 'Asia/Kolkata',
      page: 2,
      size: 50,
      sort: '+severity',
      alertId: 'alert-9',
    });

    expect(params.get('state')).toBe('SILENCED');

    const { state } = parseSearchParams(params, alertListUrlSchema, DEFAULT_ALERT_LIST_URL_STATE);
    expect(state).toMatchObject({
      state: 'SILENCED',
      processDefinitionId: 'order-flow',
      from: '2026-08-01T00:00',
      to: '2026-08-10T23:59',
      timezone: 'Asia/Kolkata',
      page: 2,
      size: 50,
      sort: '+severity',
      alertId: 'alert-9',
    });
  });

  it('defaults state to FIRING and omits it from the URL', () => {
    expect(DEFAULT_ALERT_LIST_URL_STATE.state).toBe('FIRING');
    const params = buildAlertListSearchParams({ state: 'FIRING' });
    expect(params.get('state')).toBeNull();

    const empty = parseSearchParams(
      new URLSearchParams(),
      alertListUrlSchema,
      DEFAULT_ALERT_LIST_URL_STATE,
    );
    expect(empty.state.state).toBe('FIRING');
  });

  it('rejects invalid alert states', () => {
    const params = new URLSearchParams('state=NOPE');
    const { state, invalidKeys } = parseSearchParams(
      params,
      alertListUrlSchema,
      DEFAULT_ALERT_LIST_URL_STATE,
    );
    expect(state.state).toBe('FIRING');
    expect(invalidKeys).toContain('state');
  });

  it('rejects acknowledged and resolved as list filters', () => {
    const params = new URLSearchParams('state=ACKNOWLEDGED');
    const { state, invalidKeys } = parseSearchParams(
      params,
      alertListUrlSchema,
      DEFAULT_ALERT_LIST_URL_STATE,
    );
    expect(state.state).toBe('FIRING');
    expect(invalidKeys).toContain('state');
  });
});