import { describe, expect, it, vi } from 'vitest';
import { createInfoCard } from './bpmnDomHelpers';
import type { ElementDetailSection } from './extractBpmnElementDetails';
import styles from './bpmnDomHelpers.module.css';
import {
  formatFeelEvaluationResult,
  normalizeFeelExpression,
} from './feelMappingEval';

const sampleDetails: ElementDetailSection[] = [
  {
    title: 'Task definition',
    fields: [{ label: 'Type', value: 'order-enricher' }],
  },
  {
    title: 'Input mappings',
    fields: [{ label: 'orderId', value: '=order.id' }],
  },
];

describe('feelMappingEval helpers', () => {
  it('strips leading = from Zeebe FEEL expressions', () => {
    expect(normalizeFeelExpression('=order.id')).toBe('order.id');
    expect(normalizeFeelExpression('  = foo  ')).toBe('foo');
    expect(normalizeFeelExpression('order.id')).toBe('order.id');
  });

  it('formats evaluation DTOs', () => {
    expect(formatFeelEvaluationResult({ success: true, result: 42 })).toBe('42');
    expect(formatFeelEvaluationResult({ success: false, error: 'boom' })).toBe('boom');
  });
});

describe('createInfoCard details expand', () => {
  it('renders input mappings as a target/source table', () => {
    const card = createInfoCard('Task_1', 'My Task', 'COMPLETED', {
      details: sampleDetails,
    });
    const panel = card.querySelector(`.${styles.detailsPanel}`) as HTMLDivElement;
    panel.hidden = false;

    const table = panel.querySelector(`.${styles.mappingTable}`);
    expect(table).toBeTruthy();
    expect(table?.textContent).toContain('Target');
    expect(table?.textContent).toContain('Source');
    expect(table?.querySelector(`.${styles.mappingTarget}`)?.textContent).toBe('orderId');
    expect(table?.querySelector(`.${styles.mappingSource}`)?.textContent).toBe('=order.id');
  });

  it('does not render a details button when details are empty', () => {
    const card = createInfoCard('Task_1', 'My Task', 'COMPLETED', { details: [] });
    const buttons = card.querySelectorAll('button');
    expect(buttons).toHaveLength(0);
    expect(card.querySelector(`.${styles.detailsPanel}`)).toBeNull();
  });

  it('renders a details button when details exist and toggles expand', () => {
    const card = createInfoCard('Task_1', 'My Task', 'COMPLETED', {
      details: sampleDetails,
    });

    const buttons = card.querySelectorAll('button');
    expect(buttons).toHaveLength(1);

    const detailsBtn = buttons[0];
    const panel = card.querySelector(`.${styles.detailsPanel}`) as HTMLDivElement;
    expect(panel).toBeTruthy();
    expect(panel.hidden).toBe(true);
    expect(card.classList.contains(styles.infoCardExpanded)).toBe(false);

    detailsBtn.click();
    expect(panel.hidden).toBe(false);
    expect(card.classList.contains(styles.infoCardExpanded)).toBe(true);
    expect(detailsBtn.title).toBe('Hide details');
    expect(detailsBtn.classList.contains(styles.actionButtonActive)).toBe(true);

    detailsBtn.click();
    expect(panel.hidden).toBe(true);
    expect(card.classList.contains(styles.infoCardExpanded)).toBe(false);
    expect(detailsBtn.title).toBe('Show details');
  });

  it('keeps Move Instance and Details buttons together', () => {
    const onMove = () => undefined;
    const card = createInfoCard('Task_1', undefined, 'ACTIVE', {
      onSettingsClick: onMove,
      details: sampleDetails,
    });
    expect(card.querySelectorAll('button')).toHaveLength(2);
  });

  it('does not render mapping tables or section titles when mappings are empty', () => {
    const card = createInfoCard('Task_1', 'My Task', 'COMPLETED', {
      details: [
        {
          title: 'Task definition',
          fields: [{ label: 'Type', value: 'worker' }],
        },
        { title: 'Input mappings', fields: [] },
        { title: 'Output mappings', fields: [] },
      ],
    });
    const panel = card.querySelector(`.${styles.detailsPanel}`) as HTMLDivElement;
    panel.hidden = false;

    expect(panel.querySelector(`.${styles.mappingTable}`)).toBeNull();
    expect(panel.textContent).not.toContain('Input mappings');
    expect(panel.textContent).not.toContain('Output mappings');
    expect(panel.textContent).toContain('Task definition');
  });

  it('evaluates a mapping source on click and toggles the result', async () => {
    const onEvaluateExpression = vi.fn().mockResolvedValue({
      ok: true,
      text: '"abc-123"',
    });

    const card = createInfoCard('Task_1', 'My Task', 'COMPLETED', {
      details: sampleDetails,
      onEvaluateExpression,
    });
    const panel = card.querySelector(`.${styles.detailsPanel}`) as HTMLDivElement;
    panel.hidden = false;

    const source = panel.querySelector(`.${styles.mappingSourceClickable}`) as HTMLElement;
    expect(source).toBeTruthy();

    source.click();
    await vi.waitFor(() => {
      const result = panel.querySelector(`.${styles.mappingEvalResult}`) as HTMLDivElement;
      expect(result.hidden).toBe(false);
      expect(result.textContent).toBe('"abc-123"');
    });
    expect(onEvaluateExpression).toHaveBeenCalledWith('=order.id');

    source.click();
    const result = panel.querySelector(`.${styles.mappingEvalResult}`) as HTMLDivElement;
    expect(result.hidden).toBe(true);
  });
});
