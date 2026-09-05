import type { AlertResponse } from '@/api/domains/alerts';
import { act } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AlertTable, type AlertTableProps } from './AlertTable';

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

const alerts: AlertResponse[] = [
  {
    id: 'alert-1',
    severity: 'CRITICAL',
    state: 'FIRING',
    subject: 'Payment retries exhausted',
    metadata: {
      processDefinitionId: 'payment-process',
      processInstanceId: 'pi-12345',
      version: '4',
    },
    count: 7,
    lastTriggeredAt: '2026-08-11T10:30:00.000Z',
    updatedAt: '2026-08-11T10:35:00.000Z',
  },
];

describe('AlertTable', () => {
  let container: HTMLDivElement;
  let root: Root;

  beforeEach(() => {
    container = document.createElement('div');
    document.body.appendChild(container);
    root = createRoot(container);
  });

  afterEach(() => {
    act(() => root.unmount());
    container.remove();
  });

  const renderTable = (props: Partial<AlertTableProps> = {}) => {
    act(() => {
      root.render(
        <MemoryRouter>
          <AlertTable
            alerts={alerts}
            page={0}
            pageSize={25}
            totalElements={1}
            totalPages={1}
            sort="-count"
            selectedAlertId="alert-1"
            onSelect={vi.fn()}
            onSort={vi.fn()}
            onPageChange={vi.fn()}
            {...props}
          />
        </MemoryRouter>,
      );
    });
  };

  it('renders alert fields and selects the clicked row', () => {
    const onSelect = vi.fn();
    renderTable({ onSelect });

    expect(container.textContent).toContain('Payment retries exhausted');
    expect(container.textContent).toContain('payment-process');
    expect(container.textContent).toContain('pi-12345');
    expect(container.textContent).toContain('4');
    expect(container.textContent).toContain('CRITICAL');
    expect(container.textContent).toContain('FIRING');
    expect(container.querySelectorAll('[class*="badge"]').length).toBeGreaterThanOrEqual(2);
    expect(container.querySelector<HTMLTableRowElement>('tbody tr')!.className).toContain(
      'rowSelected',
    );
    expect(container.querySelector('a[href="/processes/pi-12345"]')).toBeTruthy();

    act(() => container.querySelector<HTMLTableRowElement>('tbody tr')!.click());

    expect(onSelect).toHaveBeenCalledWith('alert-1');
  });

  it('emits supported sort tokens and page numbers', () => {
    const onSort = vi.fn();
    const onPageChange = vi.fn();

    renderTable({
      page: 1,
      totalElements: 75,
      totalPages: 3,
      selectedAlertId: null,
      onSort,
      onPageChange,
    });

    act(() => container.querySelector<HTMLElement>('th[aria-sort="descending"]')!.click());
    expect(onSort).toHaveBeenCalledWith('+count');

    act(() => container.querySelector<HTMLButtonElement>('[aria-label="Next page"]')!.click());
    expect(onPageChange).toHaveBeenCalledWith(2);
  });

  it('renders loading, empty, and error messages from props', () => {
    const render = (props: { isLoading?: boolean; error?: string; emptyText?: string }) => {
      act(() => {
        root.render(
          <MemoryRouter>
            <AlertTable
              alerts={[]}
              page={0}
              pageSize={25}
              totalElements={0}
              totalPages={0}
              sort="-count"
              onSelect={vi.fn()}
              onSort={vi.fn()}
              onPageChange={vi.fn()}
              {...props}
            />
          </MemoryRouter>,
        );
      });
    };

    render({ isLoading: true });
    expect(container.textContent).toContain('Loading alerts');

    render({ emptyText: 'No matching alerts' });
    expect(container.textContent).toContain('No matching alerts');

    render({ error: 'Alerts could not be loaded' });
    expect(container.textContent).toContain('Alerts could not be loaded');
  });
});
