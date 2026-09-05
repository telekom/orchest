import { act } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { ProcessFilters } from '@/shared/types/listFilters';

vi.hoisted(() => {
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: (query: string) => ({
      matches: false,
      media: query,
      onchange: null,
      addListener: () => undefined,
      removeListener: () => undefined,
      addEventListener: () => undefined,
      removeEventListener: () => undefined,
      dispatchEvent: () => false,
    }),
  });
});

vi.mock('@/shared/auth', () => ({
  useAuth: () => ({ hasRole: () => false }),
  UserRoles: { ADMIN: 'ADMIN' },
}));

vi.mock('../../hooks/useProcessDefinitions', () => ({
  useProcessDefinitions: () => ({ processDefinitions: [], isLoading: false }),
}));

import ProcessFilterBar from './ProcessFilterBar';

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

const activeDateFilters: ProcessFilters = {
  process: null,
  version: null,
  searchText: '',
  status: null,
  from: '2026-08-01T00:00:00',
  to: '2026-08-10T23:59:00',
  timezone: 'local',
};

describe('ProcessFilterBar clear all', () => {
  let container: HTMLDivElement;
  let root: Root;

  beforeEach(() => {
    container = document.createElement('div');
    document.body.appendChild(container);
    root = createRoot(container);
  });

  afterEach(() => {
    act(() => {
      root.unmount();
    });
    container.remove();
  });

  it('resets the calendar trigger to no selection after Clear all', () => {
    const clearProcessFilters = vi.fn();

    act(() => {
      root.render(
        <ProcessFilterBar
          processFilters={activeDateFilters}
          setProcessFilters={vi.fn()}
          clearProcessFilters={clearProcessFilters}
          hasActiveProcessFilters
        />,
      );
    });

    expect(container.textContent).toContain('Aug 1');
    expect(container.textContent).toContain('Aug 10');

    const clearAllButton = Array.from(container.querySelectorAll('button')).find((button) =>
      button.textContent?.includes('Clear all'),
    );
    expect(clearAllButton).toBeTruthy();

    act(() => {
      clearAllButton!.click();
    });

    expect(clearProcessFilters).toHaveBeenCalled();

    // Simulate parent clearing URL-backed filters after clearProcessFilters.
    act(() => {
      root.render(
        <ProcessFilterBar
          processFilters={{
            ...activeDateFilters,
            from: null,
            to: null,
            timezone: null,
          }}
          setProcessFilters={vi.fn()}
          clearProcessFilters={clearProcessFilters}
          hasActiveProcessFilters={false}
        />,
      );
    });

    expect(container.textContent).toContain('Select date range');
    expect(container.textContent).not.toMatch(/Aug 1/);
    expect(container.textContent).not.toMatch(/Aug 10/);
  });

  it('resets the calendar when URL date filters are cleared externally', () => {
    act(() => {
      root.render(
        <ProcessFilterBar
          processFilters={activeDateFilters}
          setProcessFilters={vi.fn()}
          clearProcessFilters={vi.fn()}
          hasActiveProcessFilters
        />,
      );
    });

    expect(container.textContent).toMatch(/Aug 1/);

    act(() => {
      root.render(
        <ProcessFilterBar
          processFilters={{
            ...activeDateFilters,
            from: null,
            to: null,
            timezone: null,
          }}
          setProcessFilters={vi.fn()}
          clearProcessFilters={vi.fn()}
          hasActiveProcessFilters={false}
        />,
      );
    });

    expect(container.textContent).toContain('Select date range');
    expect(container.textContent).not.toMatch(/Aug 1/);
    expect(container.textContent).not.toMatch(/Aug 10/);
  });
});
