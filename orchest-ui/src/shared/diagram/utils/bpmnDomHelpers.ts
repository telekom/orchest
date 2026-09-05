import type { ChildInstanceRef } from '@/shared/diagram/utils/childInstanceExtractor';
import type { ElementDetailSection } from '@/shared/diagram/utils/extractBpmnElementDetails';
import styles from './bpmnDomHelpers.module.css';

/**
 * Creates a labeled field row for the info card
 */
const createFieldRow = (label: string, value: string): HTMLDivElement => {
  const row = document.createElement('div');
  row.className = styles.fieldRow;
  const labelEl = document.createElement('strong');
  labelEl.className = styles.fieldLabel;
  labelEl.textContent = `${label}:`;
  row.appendChild(labelEl);
  row.appendChild(document.createTextNode(` ${value}`));
  return row;
};

/**
 * Creates an action button with icon
 */
const createActionButton = (
  iconSvg: string,
  title: string,
  onClick: () => void,
  className?: string
): HTMLButtonElement => {
  const button = document.createElement('button');
  button.className = className ? `${styles.actionButton} ${className}` : styles.actionButton;
  button.innerHTML = iconSvg;
  button.title = title;
  button.type = 'button';
  button.onclick = (e: MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    onClick();
  };
  return button;
};

const DETAILS_ICON =
  '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"/></svg>';
const DETAILS_ICON_EXPANDED =
  '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>';

const IO_MAPPING_SECTIONS = new Set(['Input mappings', 'Output mappings']);
const EVALUABLE_EXPRESSION_SECTIONS = new Set(['Condition']);
const HEADER_SECTION = 'Headers';

export type EvaluateExpressionResult = {
  ok: boolean;
  text: string;
};

export type EvaluateExpressionFn = (expression: string) => Promise<EvaluateExpressionResult>;

const attachMappingEvaluation = (
  source: HTMLElement,
  expression: string,
  resultEl: HTMLDivElement,
  onEvaluateExpression: EvaluateExpressionFn
) => {
  source.classList.add(styles.mappingSourceClickable);
  source.setAttribute('role', 'button');
  source.tabIndex = 0;
  source.title = `${expression}\nClick to evaluate against process variables`;

  let open = false;
  let loading = false;

  const runEvaluate = async () => {
    if (loading) return;

    if (open) {
      open = false;
      resultEl.hidden = true;
      resultEl.textContent = '';
      resultEl.classList.remove(styles.mappingEvalError);
      return;
    }

    loading = true;
    open = true;
    resultEl.hidden = false;
    resultEl.classList.remove(styles.mappingEvalError);
    resultEl.textContent = 'Evaluating…';

    try {
      const result = await onEvaluateExpression(expression);
      if (!open) return;
      resultEl.textContent = result.text;
      resultEl.classList.toggle(styles.mappingEvalError, !result.ok);
      resultEl.scrollIntoView({ block: 'nearest', inline: 'nearest' });
    } catch (error) {
      if (!open) return;
      resultEl.textContent = error instanceof Error ? error.message : 'Evaluation failed';
      resultEl.classList.add(styles.mappingEvalError);
      resultEl.scrollIntoView({ block: 'nearest', inline: 'nearest' });
    } finally {
      loading = false;
    }
  };

  const onActivate = (e: Event) => {
    e.preventDefault();
    e.stopPropagation();
    void runEvaluate();
  };

  source.addEventListener('click', onActivate);
  source.addEventListener('keydown', (e: KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === ' ') {
      onActivate(e);
    }
  });
};

const createMappingTable = (
  fields: ElementDetailSection['fields'],
  leftHeader: string,
  rightHeader: string,
  onEvaluateExpression?: EvaluateExpressionFn
): HTMLDivElement | null => {
  if (fields.length === 0) return null;

  const table = document.createElement('div');
  table.className = styles.mappingTable;

  const header = document.createElement('div');
  header.className = styles.mappingTableHeader;
  const leftHead = document.createElement('span');
  leftHead.textContent = leftHeader;
  const rightHead = document.createElement('span');
  rightHead.textContent = rightHeader;
  header.appendChild(leftHead);
  header.appendChild(document.createElement('span')); // arrow column
  header.appendChild(rightHead);
  table.appendChild(header);

  fields.forEach((field) => {
    const wrap = document.createElement('div');
    wrap.className = styles.mappingTableRowWrap;

    const row = document.createElement('div');
    row.className = styles.mappingTableRow;

    const target = document.createElement('span');
    target.className = styles.mappingTarget;
    target.textContent = field.label;
    target.title = field.label;

    const arrow = document.createElement('span');
    arrow.className = styles.mappingArrow;
    arrow.textContent = '←';
    arrow.setAttribute('aria-hidden', 'true');

    const source = document.createElement('span');
    source.className = styles.mappingSource;
    source.textContent = field.value || '—';

    row.appendChild(target);
    row.appendChild(arrow);
    row.appendChild(source);
    wrap.appendChild(row);

    if (onEvaluateExpression && field.value.trim()) {
      const resultEl = document.createElement('div');
      resultEl.className = styles.mappingEvalResult;
      resultEl.hidden = true;
      wrap.appendChild(resultEl);
      attachMappingEvaluation(source, field.value, resultEl, onEvaluateExpression);
    } else {
      source.title = field.value || '';
    }

    table.appendChild(wrap);
  });

  return table;
};

const createHeaderRows = (fields: ElementDetailSection['fields']): HTMLDivElement | null => {
  if (fields.length === 0) return null;

  const table = document.createElement('div');
  table.className = styles.mappingTable;

  const header = document.createElement('div');
  header.className = styles.mappingTableHeader;
  const keyHead = document.createElement('span');
  keyHead.textContent = 'Key';
  const valueHead = document.createElement('span');
  valueHead.textContent = 'Value';
  header.appendChild(keyHead);
  header.appendChild(document.createElement('span'));
  header.appendChild(valueHead);
  table.appendChild(header);

  fields.forEach((field) => {
    const row = document.createElement('div');
    row.className = styles.mappingTableRow;

    const key = document.createElement('span');
    key.className = styles.mappingTarget;
    key.textContent = field.label;
    key.title = field.label;

    const sep = document.createElement('span');
    sep.className = styles.mappingArrow;
    sep.textContent = '·';
    sep.setAttribute('aria-hidden', 'true');

    const value = document.createElement('span');
    value.className = styles.mappingSource;
    value.textContent = field.value || '—';
    value.title = field.value || '';

    row.appendChild(key);
    row.appendChild(sep);
    row.appendChild(value);
    table.appendChild(row);
  });

  return table;
};

const createDetailsPanel = (
  details: ElementDetailSection[],
  onEvaluateExpression?: EvaluateExpressionFn
): HTMLDivElement => {
  const panel = document.createElement('div');
  panel.className = styles.detailsPanel;

  details.forEach((section) => {
    const fields = section.fields.filter(
      (f) => f.label.trim().length > 0 || f.value.trim().length > 0
    );
    if (fields.length === 0) return;

    if (IO_MAPPING_SECTIONS.has(section.title)) {
      const table = createMappingTable(fields, 'Target', 'Source', onEvaluateExpression);
      if (!table) return;
      const title = document.createElement('div');
      title.className = styles.detailsSectionTitle;
      title.textContent = section.title;
      panel.appendChild(title);
      panel.appendChild(table);
      return;
    }

    if (EVALUABLE_EXPRESSION_SECTIONS.has(section.title)) {
      const table = createMappingTable(
        fields,
        'Field',
        'Expression',
        onEvaluateExpression
      );
      if (!table) return;
      const title = document.createElement('div');
      title.className = styles.detailsSectionTitle;
      title.textContent = section.title;
      panel.appendChild(title);
      panel.appendChild(table);
      return;
    }

    if (section.title === HEADER_SECTION) {
      const table = createHeaderRows(fields);
      if (!table) return;
      const title = document.createElement('div');
      title.className = styles.detailsSectionTitle;
      title.textContent = section.title;
      panel.appendChild(title);
      panel.appendChild(table);
      return;
    }

    const title = document.createElement('div');
    title.className = styles.detailsSectionTitle;
    title.textContent = section.title;
    panel.appendChild(title);
    fields.forEach((field) => {
      panel.appendChild(createFieldRow(field.label, field.value));
    });
  });

  return panel;
};

export interface CreateInfoCardOptions {
  onSettingsClick?: () => void;
  details?: ElementDetailSection[];
  onEvaluateExpression?: EvaluateExpressionFn;
}

/**
 * Creates an info card overlay for BPMN elements.
 * When `details` is non-empty, shows a Details toggle that expands a wider scrollable panel.
 */
export const createInfoCard = (
  elementId: string,
  elementName: string | undefined,
  status: string,
  onSettingsClickOrOptions?: (() => void) | CreateInfoCardOptions
): HTMLDivElement => {
  const options: CreateInfoCardOptions =
    typeof onSettingsClickOrOptions === 'function'
      ? { onSettingsClick: onSettingsClickOrOptions }
      : onSettingsClickOrOptions ?? {};

  const { onSettingsClick, details = [], onEvaluateExpression } = options;
  const hasDetails = details.length > 0;

  const container = document.createElement('div');
  container.className = styles.infoCard;

  const topRow = document.createElement('div');
  topRow.className = styles.topRow;

  const contentContainer = document.createElement('div');
  contentContainer.className = styles.contentContainer;

  if (elementName) {
    contentContainer.appendChild(createFieldRow('Type', elementName));
  }
  contentContainer.appendChild(createFieldRow('ID', elementId));
  contentContainer.appendChild(createFieldRow('Status', status));

  topRow.appendChild(contentContainer);

  const needsActions = Boolean(onSettingsClick) || hasDetails;
  let detailsButton: HTMLButtonElement | null = null;
  let detailsPanel: HTMLDivElement | null = null;
  let expanded = false;

  if (needsActions) {
    const actionsContainer = document.createElement('div');
    actionsContainer.className = styles.actionsContainer;

    if (onSettingsClick) {
      const moveIcon =
        '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M7 16V4M7 4L3 8M7 4l4 4"/><path d="M17 8v12m0 0l4-4m-4 4l-4-4"/></svg>';
      actionsContainer.appendChild(createActionButton(moveIcon, 'Move Instance', onSettingsClick));
    }

    if (hasDetails) {
      detailsPanel = createDetailsPanel(details, onEvaluateExpression);
      detailsPanel.hidden = true;

      detailsButton = createActionButton(DETAILS_ICON, 'Show details', () => {
        expanded = !expanded;
        container.classList.toggle(styles.infoCardExpanded, expanded);
        if (detailsPanel) detailsPanel.hidden = !expanded;
        if (detailsButton) {
          detailsButton.innerHTML = expanded ? DETAILS_ICON_EXPANDED : DETAILS_ICON;
          detailsButton.title = expanded ? 'Hide details' : 'Show details';
          detailsButton.classList.toggle(styles.actionButtonActive, expanded);
        }
      });
      actionsContainer.appendChild(detailsButton);
    }

    topRow.appendChild(actionsContainer);
  }

  container.appendChild(topRow);
  if (detailsPanel) {
    container.appendChild(detailsPanel);
  }
  return container;
};

const shortInstanceLabel = (instanceId: string): string =>
  instanceId.length > 14 ? `…${instanceId.slice(-10)}` : instanceId;

const linkLabelForItem = (item: ChildInstanceRef, total: number): string => {
  if (total === 1) {
    return item.childType === 'process' ? 'Child instance' : 'Invoked instance';
  }
  const base = item.childType === 'process' ? 'Child instance' : 'Invoked instance';
  return `${base} · ${shortInstanceLabel(item.instanceId)}`;
};

/**
 * Adds one or more invoked-instance links to an existing info card container.
 */
export const addInvokedInstanceLinks = (
  container: HTMLElement,
  items: ChildInstanceRef[],
  onNavigate: (instanceId: string, childType: 'process' | 'decision') => void
): void => {
  if (items.length === 0) return;

  const block = document.createElement('div');
  block.className = styles.invokedLinksBlock;

  if (items.length > 1) {
    const heading = document.createElement('div');
    heading.className = styles.invokedLinksHeading;
    heading.textContent = 'Linked instances';
    block.appendChild(heading);
  }

  items.forEach((item) => {
    const path =
      item.childType === 'process'
        ? `/processes/${item.instanceId}`
        : `/decisions/${item.instanceId}`;
    const link = document.createElement('a');
    link.href = path;
    link.className = styles.invokedLink;
    link.textContent = linkLabelForItem(item, items.length);
    link.onclick = (e: MouseEvent) => {
      e.stopPropagation();
      if (e.metaKey || e.ctrlKey || e.shiftKey || e.altKey || e.button !== 0) {
        return;
      }
      e.preventDefault();
      onNavigate(item.instanceId, item.childType);
    };

    const row = document.createElement('div');
    row.className = styles.invokedLinkRow;
    row.appendChild(link);
    block.appendChild(row);
  });

  const detailsPanel = container.querySelector(`.${styles.detailsPanel}`);
  if (detailsPanel) {
    container.insertBefore(block, detailsPanel);
  } else {
    container.appendChild(block);
  }
};

const shortTaskLabel = (taskId: string): string =>
  taskId.length > 14 ? `…${taskId.slice(-10)}` : taskId;

/**
 * Adds user-task list links to an existing info card (bpmn:UserTask).
 */
export const addUserTaskLinks = (
  container: HTMLElement,
  taskIds: string[],
  onNavigate: (taskId: string) => void
): void => {
  if (taskIds.length === 0) return;

  const block = document.createElement('div');
  block.className = styles.invokedLinksBlock;

  if (taskIds.length > 1) {
    const heading = document.createElement('div');
    heading.className = styles.invokedLinksHeading;
    heading.textContent = 'User tasks';
    block.appendChild(heading);
  }

  taskIds.forEach((taskId) => {
    const link = document.createElement('a');
    link.href = `/tasks/${taskId}`;
    link.className = styles.invokedLink;
    link.textContent =
      taskIds.length === 1
        ? 'Open user task'
        : `Open user task · ${shortTaskLabel(taskId)}`;
    link.onclick = (e: MouseEvent) => {
      e.stopPropagation();
      if (e.metaKey || e.ctrlKey || e.shiftKey || e.altKey || e.button !== 0) {
        return;
      }
      e.preventDefault();
      onNavigate(taskId);
    };

    const row = document.createElement('div');
    row.className = styles.invokedLinkRow;
    row.appendChild(link);
    block.appendChild(row);
  });

  const detailsPanel = container.querySelector(`.${styles.detailsPanel}`);
  if (detailsPanel) {
    container.insertBefore(block, detailsPanel);
  } else {
    container.appendChild(block);
  }
};
