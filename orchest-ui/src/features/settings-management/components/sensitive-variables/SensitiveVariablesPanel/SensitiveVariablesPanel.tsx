import type { SensitiveVariable } from '@/api/domains/sensitive-variables';
import { useAuth, UserRoles } from '@/shared/auth';
import { Button } from '@/design-system/components/ui/button';
import { ConfirmationDialog } from '@/shared/components/ConfirmationDialog/ConfirmationDialog';
import { DataTable } from '@/shared/components/DataTable/DataTable';
import { SmartText } from '@/shared/components/SmartText/SmartText';
import { Badge } from '@/design-system/components/ui/badge/badge';
import { BADGE_STYLES } from '@/shared/constants';
import { Combobox } from '@/design-system/components/ui/combobox';
import { Trash2, Pencil, ToggleLeft, ToggleRight, EyeOff, Eye } from 'lucide-react';
import React, { useCallback, useMemo, useState } from 'react';
import { useSensitiveVariables } from '../../../hooks/useSensitiveVariables';
import SensitiveVariableFormModal from '../SensitiveVariableFormModal/SensitiveVariableFormModal';
import styles from './SensitiveVariablesPanel.module.css';

const SensitiveVariablesPanel: React.FC = () => {
  const { hasRole } = useAuth();
  const isAdmin = useMemo(() => hasRole(UserRoles.ADMIN), [hasRole]);

  const {
    selectedProcessId,
    uniqueProcessIds,
    variables,
    currentConfig,
    loadingVariables,
    saving,
    handleProcessChange,
    addVariable,
    updateVariable,
    deleteVariable,
    toggleEnabled,
  } = useSensitiveVariables();

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingVariable, setEditingVariable] = useState<SensitiveVariable | null>(null);
  const [editingIndex, setEditingIndex] = useState<number | null>(null);
  const [deleteTargetIndex, setDeleteTargetIndex] = useState<number | null>(null);

  const handleAdd = useCallback(() => {
    setEditingVariable(null);
    setEditingIndex(null);
    setIsFormOpen(true);
  }, []);

  const handleEdit = useCallback((variable: SensitiveVariable, index: number) => {
    setEditingVariable(variable);
    setEditingIndex(index);
    setIsFormOpen(true);
  }, []);

  const handleFormClose = useCallback(() => {
    setIsFormOpen(false);
    setEditingVariable(null);
    setEditingIndex(null);
  }, []);

  const handleFormSave = useCallback(async (variable: SensitiveVariable) => {
    if (editingIndex !== null) {
      await updateVariable(editingIndex, variable);
    } else {
      await addVariable(variable);
    }
    handleFormClose();
  }, [editingIndex, updateVariable, addVariable, handleFormClose]);

  const handleDelete = useCallback(async () => {
    if (deleteTargetIndex === null) return;
    await deleteVariable(deleteTargetIndex);
    setDeleteTargetIndex(null);
  }, [deleteTargetIndex, deleteVariable]);

  const handleProcessSelect = useCallback((value: string) => {
    if (value) handleProcessChange(value);
  }, [handleProcessChange]);

  const processOptions = useMemo(() => {
    return uniqueProcessIds.map((id) => ({ label: id, value: id }));
  }, [uniqueProcessIds]);

  const indexedVariables = useMemo(() => {
    return variables.map((v, i) => ({ ...v, _index: i }));
  }, [variables]);

  const columns = useMemo(() => {
    const cols = [
      {
        key: 'name',
        header: 'Variable Name',
        render: (row: SensitiveVariable & { _index: number }) => (
          <SmartText text={row.name} maxWidth="100%" />
        ),
      },
      {
        key: 'type',
        header: 'Type',
        render: (row: SensitiveVariable & { _index: number }) => {
          const isHide = row.type === 'HIDE';
          const badgeStyle = isHide ? BADGE_STYLES.ERROR : BADGE_STYLES.SUCCESS;
          return (
            <Badge variant={badgeStyle.variant} icon={isHide ? <EyeOff /> : <Eye />} className={badgeStyle.className}>
              {row.type}
            </Badge>
          );
        },
        align: 'center' as const,
      },
      {
        key: 'value',
        header: 'Value',
        render: (row: SensitiveVariable & { _index: number }) => {
          if (row.type === 'SHOW') {
            return <span className={styles.showLabel}>Visible in instances</span>;
          }
          return (
            <span className={styles.hiddenValue}>
              [**hidden**, enable it from OrchestSetting to see the value]
            </span>
          );
        },
      },
    ];

    if (isAdmin) {
      cols.push({
        key: 'actions',
        header: '',
        render: (row: SensitiveVariable & { _index: number }) => (
          <div className={styles.actionsCell}>
            <button
              className={styles.actionButton}
              onClick={(e) => { e.stopPropagation(); handleEdit(row, row._index); }}
              type="button"
              title="Edit variable"
            >
              <Pencil size={14} />
            </button>
            <button
              className={`${styles.actionButton} ${styles.deleteButton}`}
              onClick={(e) => { e.stopPropagation(); setDeleteTargetIndex(row._index); }}
              type="button"
              title="Delete variable"
            >
              <Trash2 size={14} />
            </button>
          </div>
        ),
        align: 'center' as const,
      });
    }

    return cols;
  }, [isAdmin, handleEdit]);

  const deleteTargetName = deleteTargetIndex !== null ? variables[deleteTargetIndex]?.name : '';

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>Sensitive Variables</h2>
        <div className={styles.headerActions}>
          {isAdmin && selectedProcessId && currentConfig && (
            <Button
              variant={currentConfig.enabled ? 'outline' : 'secondary'}
              size="sm"
              onClick={toggleEnabled}
              disabled={saving}
            >
              {currentConfig.enabled ? <ToggleRight size={16} /> : <ToggleLeft size={16} />}
              {currentConfig.enabled ? 'Enabled' : 'Disabled'}
            </Button>
          )}
          {isAdmin && (
            <Button
              variant="primary"
              size="sm"
              onClick={handleAdd}
              disabled={!selectedProcessId}
            >
              + Add Variable
            </Button>
          )}
        </div>
      </div>

      <div className={styles.processSelector}>
        <span className={styles.processSelectorLabel}>Select Process</span>
        <Combobox
          items={processOptions}
          value={selectedProcessId || undefined}
          onSelect={handleProcessSelect}
          placeholder="Choose a process..."
          searchPlaceholder="Type to search processes..."
          emptyMessage="No processes found."
        />
      </div>

      {selectedProcessId && (
        <div className={styles.tableContainer}>
          <DataTable<SensitiveVariable & { _index: number }>
            data={indexedVariables}
            columns={columns}
            keyExtractor={(row) => `${row._index}-${row.name}`}
            loading={loadingVariables}
            loadingText="Loading sensitive variables..."
            emptyText="No sensitive variables configured for this process."
            showCard={true}
            stickyHeader={false}
          />
        </div>
      )}

      {!selectedProcessId && (
        <div className={styles.emptyState}>
          <p>Select a process to view its sensitive variable configuration.</p>
        </div>
      )}

      <SensitiveVariableFormModal
        isOpen={isFormOpen}
        onClose={handleFormClose}
        onSave={handleFormSave}
        editingVariable={editingVariable}
        editingIndex={editingIndex}
        processDefinitionId={selectedProcessId}
        saving={saving}
      />

      <ConfirmationDialog
        open={deleteTargetIndex !== null}
        onOpenChange={(open) => { if (!open) setDeleteTargetIndex(null); }}
        title="Delete Sensitive Variable"
        description={`Are you sure you want to delete the variable "${deleteTargetName}"? This action cannot be undone.`}
        onConfirm={handleDelete}
        confirmText="Delete"
        confirmVariant="primary"
      />
    </div>
  );
};

export default React.memo(SensitiveVariablesPanel);