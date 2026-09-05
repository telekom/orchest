import { beforeEach, describe, expect, it, vi } from 'vitest';
import { DEFAULT_ALERT_LIST_URL_STATE } from '@/shared/url-state/configs/alertListUrlState';
import { useAlertsData } from './useAlertsData';

const mocks = vi.hoisted(() => ({
  toastError: vi.fn(),
  useApiMutation: vi.fn((_mutationFn, options) => options),
  useApiQuery: vi.fn(() => ({})),
}));

vi.mock('@/design-system/components/ui/sonner', () => ({
  toast: { error: mocks.toastError },
}));

vi.mock('@/shared/hooks/useApiQuery', () => ({
  useApiMutation: mocks.useApiMutation,
  useApiQuery: mocks.useApiQuery,
}));

describe('useAlertsData mutation errors', () => {
  beforeEach(() => {
    mocks.toastError.mockClear();
    mocks.useApiMutation.mockClear();
    mocks.useApiQuery.mockClear();
  });

  it('shows API messages and useful fallback messages', () => {
    useAlertsData(DEFAULT_ALERT_LIST_URL_STATE);

    const acknowledgeOptions = mocks.useApiMutation.mock.calls[0][1];
    const silenceOptions = mocks.useApiMutation.mock.calls[1][1];

    acknowledgeOptions.onError({
      response: { data: { message: 'Alert transition is not allowed' } },
    });
    silenceOptions.onError(new Error('Network error'));

    expect(mocks.toastError).toHaveBeenNthCalledWith(1, 'Alert transition is not allowed');
    expect(mocks.toastError).toHaveBeenNthCalledWith(2, 'Failed to silence alert.');
  });
});
