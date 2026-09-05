import type { AlertResponse } from '@/api/domains/alerts';
import { act } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AlertDetailPanel, type AlertDetailPanelProps } from './AlertDetailPanel';

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

const longBody = 'The retry limit was reached. '.repeat(20).trim();

const alert: AlertResponse = {
  id: 'alert-1',
  state: 'FIRING',
  severity: 'CRITICAL',
  count: 7,
  subject: 'Payment retries exhausted',
  body: 'The retry limit was reached.',
  source: 'payment-worker',
  alertKey: 'payment-retries',
  fingerprint: 'fingerprint-1',
  metadata: {
    processDefinitionId: 'payment-process',
    processInstanceId: 'pi-12345',
    version: '4',
    region: 'eu-central-1',
  },
  recipients: {
    to: ['operator@example.com'],
    cc: ['team@example.com'],
    bcc: ['audit@example.com'],
  },
  resendIntervalMs: 60_000,
  createdAt: '2026-08-11T10:00:00.000Z',
  updatedAt: '2026-08-11T10:30:00.000Z',
  lastTriggeredAt: '2026-08-11T10:25:00.000Z',
  acknowledgedAt: '2026-08-11T10:10:00.000Z',
  acknowledgedBy: 'first.operator@example.com',
  silencedAt: '2026-08-11T10:15:00.000Z',
  silencedBy: 'second.operator@example.com',
};

const callbacks = {
  onAcknowledge: vi.fn(),
  onSilence: vi.fn(),
  onUnmute: vi.fn(),
  onResolve: vi.fn(),
  onClose: vi.fn(),
};

describe('AlertDetailPanel', () => {
  let container: HTMLDivElement;
  let root: Root;

  beforeEach(() => {
    vi.clearAllMocks();
    container = document.createElement('div');
    document.body.appendChild(container);
    root = createRoot(container);
  });

  afterEach(() => {
    act(() => root.unmount());
    container.remove();
  });

  const renderPanel = (props: Partial<AlertDetailPanelProps> = {}) => {
    act(() => {
      root.render(
        <MemoryRouter>
          <AlertDetailPanel
            alert={alert}
            actorEmail="current.operator@example.com"
            isPending={false}
            {...callbacks}
            {...props}
          />
        </MemoryRouter>,
      );
    });
  };

  it('renders the alert details in stacked sections', () => {
    renderPanel();

    expect(container.textContent).toContain('FIRING');
    expect(container.textContent).toContain('CRITICAL');
    expect(container.textContent).toContain('7 occurrences');
    expect(container.textContent).toContain('Payment retries exhausted');
    expect(container.textContent).toContain('The retry limit was reached.');
    expect(container.textContent).toContain('Process');
    expect(container.textContent).toContain('payment-process');
    expect(container.textContent).toContain('Process instance');
    expect(container.textContent).toContain('pi-12345');
    expect(container.textContent).toContain('Version');
    expect(container.textContent).toContain('4');
    expect(container.textContent).toContain('operator@example.com');
    expect(container.textContent).toContain('first.operator@example.com');

    const instanceLink = container.querySelector<HTMLAnchorElement>(
      'a[href="/processes/pi-12345"]',
    );
    expect(instanceLink).toBeTruthy();
    expect(instanceLink?.textContent).toBe('pi-12345');
  });

  it('truncates long body text with load more / show less', () => {
    renderPanel({ alert: { ...alert, body: longBody } });

    expect(container.textContent).toContain('Load more');
    expect(container.textContent).not.toContain(longBody);

    act(() => {
      Array.from(container.querySelectorAll('button')).forEach((button) => {
        if (button.textContent === 'Load more') button.click();
      });
    });

    expect(container.textContent).toContain(longBody);
    expect(container.textContent).toContain('Show less');
  });

  it('shows state-aware actions and passes the alert identity and actor', () => {
    renderPanel();

    const buttons = Array.from(container.querySelectorAll('button'));
    const button = (label: string) => buttons.find((item) => item.textContent === label)!;

    expect(button('Acknowledge')).toBeTruthy();
    expect(button('Silence')).toBeTruthy();
    expect(button('Resolve')).toBeTruthy();
    expect(button('Unmute')).toBeFalsy();

    act(() => button('Acknowledge').click());
    act(() => button('Silence').click());
    act(() => button('Resolve').click());

    expect(callbacks.onAcknowledge).toHaveBeenCalledWith({
      id: 'alert-1',
      actor: 'current.operator@example.com',
    });
    expect(callbacks.onSilence).toHaveBeenCalledWith({
      id: 'alert-1',
      actor: 'current.operator@example.com',
    });
    expect(callbacks.onResolve).toHaveBeenCalledWith('alert-1');
    expect(callbacks.onClose).not.toHaveBeenCalled();
  });

  it('shows only unmute and resolve for silenced alerts and disables pending actions', () => {
    renderPanel({
      alert: { ...alert, state: 'SILENCED' },
      isPending: true,
    });

    const actionButtons = Array.from(
      container.querySelectorAll<HTMLButtonElement>('footer button'),
    );

    expect(actionButtons.map((button) => button.textContent)).toEqual(['Unmute', 'Resolve']);
    expect(actionButtons.every((button) => button.disabled)).toBe(true);
  });

  it('renders loading and persistent not-found states with a working close action', () => {
    renderPanel({ alert: undefined, isLoading: true });
    expect(container.textContent).toContain('Loading alert');

    renderPanel({ alert: undefined, isLoading: false, isError: true });
    expect(container.textContent).toContain('Alert not found');
    expect(callbacks.onClose).not.toHaveBeenCalled();

    act(() => container.querySelector<HTMLButtonElement>('button')!.click());
    expect(callbacks.onClose).toHaveBeenCalledOnce();
  });
});
