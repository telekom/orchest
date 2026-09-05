import { describe, expect, it, vi } from 'vitest';
import { withTimeout } from './withTimeout';

describe('withTimeout', () => {
  it('resolves when the operation finishes before the timeout', async () => {
    await expect(withTimeout(Promise.resolve('ok'), 100)).resolves.toBe('ok');
  });

  it('rejects when the operation exceeds the timeout', async () => {
    vi.useFakeTimers();

    const slow = new Promise<string>((resolve) => {
      setTimeout(() => resolve('late'), 5_000);
    });

    const result = withTimeout(slow, 100, 'Auth init timed out');
    const assertion = expect(result).rejects.toThrow('Auth init timed out');

    await vi.advanceTimersByTimeAsync(100);
    await assertion;

    vi.useRealTimers();
  });
});
