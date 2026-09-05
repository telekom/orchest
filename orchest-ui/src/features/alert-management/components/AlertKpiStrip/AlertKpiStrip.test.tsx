import { act } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AlertKpiStrip } from './AlertKpiStrip';

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

const counts = {
  FIRING: 12,
  SILENCED: 3,
} as const;

describe('AlertKpiStrip', () => {
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

  it('renders firing and silenced counts and selects a state', () => {
    const onStateClick = vi.fn();

    act(() => {
      root.render(
        <AlertKpiStrip
          counts={counts}
          selectedState="FIRING"
          onStateClick={onStateClick}
        />,
      );
    });

    expect(container.textContent).toContain('Firing');
    expect(container.textContent).toContain('Silenced');
    expect(container.textContent).toContain('12');
    expect(container.textContent).toContain('3');
    expect(container.textContent).toContain('15');
    expect(container.textContent).not.toContain('ACKNOWLEDGED');
    expect(container.textContent).not.toContain('RESOLVED');

    act(() => {
      container.querySelector<HTMLButtonElement>('[aria-label^="SILENCED:"]')!.click();
    });

    expect(onStateClick).toHaveBeenCalledWith('SILENCED');
  });

  it('marks the active state as pressed', () => {
    act(() => {
      root.render(
        <AlertKpiStrip
          counts={counts}
          selectedState="SILENCED"
          onStateClick={vi.fn()}
        />,
      );
    });

    const activeCard = container.querySelector<HTMLButtonElement>(
      '[aria-label^="SILENCED:"]',
    )!;
    expect(activeCard.getAttribute('aria-pressed')).toBe('true');
  });

  it('disables cards and hides counts while loading', () => {
    act(() => {
      root.render(
        <AlertKpiStrip
          counts={counts}
          selectedState="FIRING"
          isLoading
          onStateClick={vi.fn()}
        />,
      );
    });

    expect(container.querySelectorAll('button:disabled')).toHaveLength(2);
    expect(container.textContent).not.toContain('12');
  });
});
