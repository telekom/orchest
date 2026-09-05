import {
    TableCell,
    TableRow,
} from "@/design-system/components/ui/table";
import { ProcessVariable } from "@/features/process-management/types/processInstance";
import { ActionIconButton } from "@/shared/components/ActionIconButton/ActionIconButton";
import { VariableAction } from "@/shared/enums";
import { useModalState } from "@/shared/hooks";
import {
    Braces,
    Check,
    ChevronDown,
    ChevronUp,
    Copy,
    Hash,
    Layers,
    Network,
    ToggleLeft,
    Type,
} from "lucide-react";
import { SpinnerLoader } from "@/shared/components/Loader/Loader";
import React, { lazy, Suspense } from "react";
import { DeleteVariableModal } from "../DeleteVariableModal/DeleteVariableModal";
import { EditVariableModal } from "../EditVariableModal/EditVariableModal";
import { ViewVariableModal } from "../ViewVariableModal/ViewVariableModal";
import styles from './ProcessVariableRow.module.css';

const JsonEditor = lazy(() => import("../JsonEditor/JsonEditor"));

export interface ProcessVariableRowProps {
  variable: ProcessVariable;
  isExpanded: boolean;
  onToggleExpand: (name: string) => void;
  onCopyToClipboard: (value: string, name: string) => void;
  onDeleteVariable: (variable: ProcessVariable) => void;
  readOnly?: boolean;
  variableOperation: (params: {
    action: VariableAction;
    variable: { name: string; value: unknown; type?: string; scope?: string };
  }) => void;
  copiedItems: Record<string, boolean>;
}

const TYPE_CONFIG: Record<string, { icon: React.ElementType; color: string; bg: string; label: string }> = {
  string:  { icon: Type,       color: '#d946ef', bg: 'rgba(217, 70, 239, 0.12)', label: 'String' },
  number:  { icon: Hash,       color: '#f97316', bg: 'rgba(249, 115, 22, 0.12)', label: 'Number' },
  boolean: { icon: ToggleLeft, color: '#14b8a6', bg: 'rgba(20, 184, 166, 0.12)', label: 'Boolean' },
  object:  { icon: Braces,     color: '#6366f1', bg: 'rgba(99, 102, 241, 0.12)', label: 'Object' },
};

const TypeIcon: React.FC<{ type: string }> = ({ type }) => {
  const config = TYPE_CONFIG[type] || TYPE_CONFIG.string;
  const Icon = config.icon;
  return (
    <span className={styles.typePill} style={{ color: config.color, backgroundColor: config.bg }} title={config.label}>
      <Icon size={13} strokeWidth={2.2} />
    </span>
  );
};

const SCOPE_CONFIG: Record<string, { icon: React.ElementType; className: string; label: string }> = {
  global: { icon: Network, className: styles.scopeGlobal, label: 'Global' },
  local:  { icon: Layers,  className: styles.scopeLocal,  label: 'Local' },
};

const ScopeIcon: React.FC<{ scope: string }> = ({ scope }) => {
  const config = SCOPE_CONFIG[scope] || SCOPE_CONFIG.global;
  const Icon = config.icon;
  return (
    <span className={`${styles.scopePill} ${config.className}`} title={config.label}>
      <Icon size={13} strokeWidth={2.2} />
    </span>
  );
};

function truncateObjectPreview(value: string): string {
  try {
    const parsed = JSON.parse(value);
    const keys = Object.keys(parsed);
    if (keys.length === 0) return '{ }';
    const first = keys[0];
    const v = parsed[first];
    const preview = typeof v === 'string' ? `"${v.slice(0, 16)}${v.length > 16 ? '…' : ''}"` : JSON.stringify(v);
    const suffix = keys.length > 1 ? `, …${keys.length - 1} more` : '';
    return `{ ${first}: ${preview}${suffix} }`;
  } catch {
    return '{…}';
  }
}

const ProcessVariableRow = React.memo<ProcessVariableRowProps>(
  ({
    variable,
    isExpanded,
    onToggleExpand,
    onCopyToClipboard,
    onDeleteVariable,
    readOnly = false,
    variableOperation,
    copiedItems,
  }) => {
    const isJsonType = variable.type === "object";
    const isCopied = copiedItems[variable.name];

    const modals = useModalState({
      modalNames: ['edit', 'view', 'delete'],
    });

    const handleRowClick = () => onToggleExpand(variable.name);

    const handleEditClick = (e: React.MouseEvent) => {
      e.stopPropagation();
      modals.open('edit');
    };

    const handleViewClick = (e: React.MouseEvent) => {
      e.stopPropagation();
      modals.open('view');
    };

    const handleDeleteClick = (e: React.MouseEvent) => {
      e.stopPropagation();
      modals.open('delete');
    };

    const handleConfirmDelete = () => onDeleteVariable(variable);

    const handleCopyClick = (e: React.MouseEvent) => {
      e.stopPropagation();
      onCopyToClipboard(variable.value, variable.name);
    };

    return (
      <React.Fragment>
        <TableRow
          className={styles.tableRow}
          onClick={handleRowClick}
        >
          <TableCell className={styles.nameCell}>
            <div className={styles.nameCellContent}>
              {isExpanded ? (
                <ChevronUp className={styles.chevronIcon} />
              ) : (
                <ChevronDown className={styles.chevronIcon} />
              )}
              <span className={styles.nameText}>{variable.name}</span>
            </div>
          </TableCell>
          <TableCell className={styles.typeCell}>
            <TypeIcon type={variable.type} />
          </TableCell>
          <TableCell className={styles.scopeCell}>
            <ScopeIcon scope={variable.scope} />
          </TableCell>
          <TableCell className={styles.valueCell}>
            <div className={styles.valueCellContent}>
              <span className={styles.valueText}>
                {isJsonType ? truncateObjectPreview(variable.value) : variable.value}
              </span>
              <button
                type="button"
                className={styles.copyButton}
                onClick={handleCopyClick}
                title="Copy value to clipboard"
              >
                {isCopied ? <Check size={12} /> : <Copy size={12} />}
              </button>
            </div>
          </TableCell>
          <TableCell className={styles.actionsCell}>
            <div className={styles.actionButtons}>
              {!readOnly && (
                <>
                  <ActionIconButton icon="edit" title="Edit variable" onClick={handleEditClick} />
                  <ActionIconButton
                    icon="delete"
                    title="Delete variable"
                    onClick={handleDeleteClick}
                    variant="danger"
                  />
                </>
              )}
              {readOnly && (
                <ActionIconButton icon="visibility-on" title="View variable details" onClick={handleViewClick} />
              )}
            </div>
          </TableCell>
        </TableRow>

        {isExpanded && (
          <TableRow className={styles.expandedRow}>
            <TableCell colSpan={5} className={styles.expandedCell}>
              {isJsonType ? (
                <div className={styles.jsonContainer}>
                  <Suspense fallback={
                    <div className={styles.jsonLoader}>
                      <SpinnerLoader size="md" />
                    </div>
                  }>
                    <JsonEditor
                      value={variable.value}
                      readOnly={true}
                      mode="tree"
                      height="100%"
                    />
                  </Suspense>
                </div>
              ) : (
                <div className={styles.valueDisplay}>
                  {variable.value}
                </div>
              )}
            </TableCell>
          </TableRow>
        )}

        {!readOnly && (
          <EditVariableModal
            variable={variable}
            isOpen={modals.isOpen('edit')}
            onClose={() => modals.close('edit')}
            variableOperation={variableOperation}
          />
        )}

        {readOnly && (
          <ViewVariableModal
            variable={variable}
            isOpen={modals.isOpen('view')}
            onClose={() => modals.close('view')}
          />
        )}

        {!readOnly && (
          <DeleteVariableModal
            variable={variable}
            isOpen={modals.isOpen('delete')}
            onClose={() => modals.close('delete')}
            onConfirm={handleConfirmDelete}
          />
        )}
      </React.Fragment>
    );
  }
);

ProcessVariableRow.displayName = "ProcessVariableRow";

export default ProcessVariableRow;
