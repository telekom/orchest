import { act } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AlertFiringChart } from './AlertFiringChart';

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

describe('AlertFiringChart', () => {
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

  it('shows the top ten processes by firing count and handles bar clicks', () => {
    const onProcessClick = vi.fn();
    const stats = Array.from({ length: 12 }, (_, index) => ({
      processDefinitionId: `process-${index + 1}`,
      version: `${index + 1}`,
      totalCount: index + 1,
    }));

    act(() => {
      root.render(
        <AlertFiringChart stats={stats} onProcessClick={onProcessClick} />,
      );
    });

    const bars = container.querySelectorAll<HTMLButtonElement>('[role="listitem"]');
    expect(bars).toHaveLength(10);
    expect(bars[0].textContent).toContain('process-12 v12');
    expect(bars[0].textContent).toContain('01');
    expect(bars[0].className).toContain('rowLeader');
    expect(container.textContent).toContain('#1 share');
    expect(container.textContent).not.toContain('process-1 v1');

    act(() => bars[0].click());

    expect(onProcessClick).toHaveBeenCalledWith('process-12');
  });

  it('shows the empty message when there are no positive firing counts', () => {
    act(() => {
      root.render(
        <AlertFiringChart
          stats={[
            {
              processDefinitionId: 'invoice',
              version: '1',
              totalCount: 0,
            },
          ]}
          onProcessClick={vi.fn()}
        />,
      );
    });

    expect(container.textContent).toContain('No firing alerts.');
  });

  it('shows an accessible loading skeleton', () => {
    act(() => {
      root.render(
        <AlertFiringChart stats={undefined} isLoading onProcessClick={vi.fn()} />,
      );
    });

    expect(container.querySelector('[aria-busy="true"]')).toBeTruthy();
    expect(container.querySelectorAll('[aria-hidden="true"] > *')).toHaveLength(8);
  });
});
