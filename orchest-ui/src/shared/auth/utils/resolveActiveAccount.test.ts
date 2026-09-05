import { describe, expect, it, vi } from 'vitest';
import { resolveActiveAccount } from './resolveActiveAccount';

describe('resolveActiveAccount', () => {
  it('returns the existing active account without calling setActiveAccount', () => {
    const active = { homeAccountId: 'active' };
    const setActiveAccount = vi.fn();

    const result = resolveActiveAccount(
      () => active,
      () => [{ homeAccountId: 'other' }],
      setActiveAccount
    );

    expect(result).toBe(active);
    expect(setActiveAccount).not.toHaveBeenCalled();
  });

  it('restores the first cached account when no active account is set', () => {
    const cached = { homeAccountId: 'cached' };
    const setActiveAccount = vi.fn();

    const result = resolveActiveAccount(
      () => null,
      () => [cached, { homeAccountId: 'second' }],
      setActiveAccount
    );

    expect(result).toBe(cached);
    expect(setActiveAccount).toHaveBeenCalledWith(cached);
  });

  it('returns null when there is no active or cached account', () => {
    const setActiveAccount = vi.fn();

    const result = resolveActiveAccount(() => null, () => [], setActiveAccount);

    expect(result).toBeNull();
    expect(setActiveAccount).not.toHaveBeenCalled();
  });
});
