import type { EnvVariableType, ProcessEnvVariable } from '@/api/domains/process-env-variables';
import { useAuth, UserRoles } from '@/shared/auth';
import { Button } from '@/design-system/components/ui/button';
import { ActionIconButton } from '@/shared/components/ActionIconButton/ActionIconButton';
import { ConfirmationDialog } from '@/shared/components/ConfirmationDialog/ConfirmationDialog';
import { DataTable } from '@/shared/components/DataTable/DataTable';
import { SmartText } from '@/shared/components/SmartText/SmartText';
import { Badge } from '@/design-system/components/ui/badge/badge';
import { BADGE_STYLES } from '@/shared/constants';
import { Combobox } from '@/design-system/components/ui/combobox';
import { Eye, EyeOff, Trash2, Pencil, ShieldCheck, FileText } from 'lucide-react';
import React, { useCallback, useMemo, useState } from 'react';
import { useEnvVariables } from '../../../hooks/useEnvVariables';
import EnvVariableFormModal from '../EnvVariableFormModal/EnvVariableFormModal';
import styles from './EnvVariablesPanel.module.css';

interface EnvVariablesPanelProps {
  variableType: EnvVariableType;
  title: string;
}

const EnvVariablesPanel: React.FC<EnvVariablesPanelProps> = ({ variableType, title }) => {
  const { hasRole } = useAuth();
  const isAdmin = useMemo(() => hasRole(UserRoles.ADMIN), [hasRole]);

  const {
    selectedProcessId,
    uniqueProcessIds,
    filteredVariables,
    loadingVariables,
    adding,
    updating,
    handleProcessChange,
    addVariable,
    updateVariable,
    deleteVariable,
  } = useEnvVariables(variableType);

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingVariable, setEditingVariable] = useState<ProcessEnvVariable | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<ProcessEnvVariable | null>(null);
  const [revealedIds, setRevealedIds] = useState<Set<string>>(new Set());

  const isSecret = variableType === 'SECRET';

  const handleAdd = useCallback(() => {
    setEditingVariable(null);
    setIsFormOpen(true);
  }, []);

  const handleEdit = useCallback((variable: ProcessEnvVariable) => {
    setEditingVariable(variable);
    setIsFormOpen(true);
  }, []);

  const handleFormClose = useCallback(() => {
    setIsFormOpen(false);
    setEditingVariable(null);
  }, []);

  const handleFormSave = useCallback(async (variable: ProcessEnvVariable) => {
    if (editingVariable) {
      await updateVariable(variable);
    } else {
      await addVariable(variable);
    }
    handleFormClose();
  }, [editingVariable, updateVariable, addVariable, handleFormClose]);

  const handleDelete = useCallback(async () => {
    if (!deleteTarget) return;
    await deleteVariable(deleteTarget);
    setDeleteTarget(null);
  }, [deleteTarget, deleteVariable]);

  const toggleReveal = useCallback((id: string) => {
    setRevealedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  }, []);

  const handleProcessSelect = useCallback((value: string) => {
    
    if (value) handleProcessChange(value);
  }, [handleProcessChange]);

  const processOptions = useMemo(() => {
    return uniqueProcessIds.map((id) => ({ label: id, value: id }));
  }, [uniqueProcessIds]);

  const columns = useMemo(() => {
    const cols = [
      {
        key: 'name',
        header: 'Variable Name',
        render: (row: ProcessEnvVariable) => (
          <SmartText text={row.name} maxWidth="100%" />
        ),
      },
      {
        key: 'value',
        header: 'Value',
        render: (row: ProcessEnvVariable) => {
          const revealed = revealedIds.has(row.id ?? row.name);
          const displayValue = isSecret && !revealed ? '••••••••' : row.value;
          return (
            <div className={styles.valueCell}>
              <SmartText text={displayValue} maxWidth="200px" />
              {isSecret && (
                <button
                  className={styles.revealButton}
                  onClick={(e) => {
                    e.stopPropagation();
                    toggleReveal(row.id ?? row.name);
                  }}
                  type="button"
                  title={revealed ? 'Hide value' : 'Reveal value'}
                >
                  {revealed ? <EyeOff size={14} /> : <Eye size={14} />}
                </button>
              )}
            </div>
          );
        },
      },
      {
        key: 'type',
        header: 'Type',
        render: (row: ProcessEnvVariable) => {
          const isSecret = row.type === 'SECRET';
          const badgeStyle = isSecret ? BADGE_STYLES.ERROR : BADGE_STYLES.SUCCESS;
          return (
            <Badge variant={badgeStyle.variant} icon={isSecret ? <ShieldCheck /> : <FileText />} className={badgeStyle.className}>
              {row.type}
            </Badge>
          );
        },
        align: 'center' as const,
      },
    ];

    if (isAdmin) {
      cols.push({
        key: 'actions',
        header: '',
        render: (row: ProcessEnvVariable) => (
          <div className={styles.actionsCell}>
            <button
              className={styles.actionButton}
              onClick={(e) => { e.stopPropagation(); handleEdit(row); }}
              type="button"
              title="Edit variable"
            >
              <Pencil size={14} />
            </button>
            <button
              className={`${styles.actionButton} ${styles.deleteButton}`}
              onClick={(e) => { e.stopPropagation(); setDeleteTarget(row); }}
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
  }, [isAdmin, isSecret, revealedIds, handleEdit, toggleReveal]);

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>{title}</h2>
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
          <DataTable<ProcessEnvVariable>
            data={filteredVariables}
            columns={columns}
            keyExtractor={(row) => row.id ?? `${row.processDefinitionId}-${row.name}`}
            loading={loadingVariables}
            loadingText="Loading variables..."
            emptyText={`No ${variableType === 'SECRET' ? 'sensitive' : 'environment'} variables found for this process.`}
            showCard={true}
            stickyHeader={false}
          />
        </div>
      )}

      {!selectedProcessId && (
        <div className={styles.emptyState}>
          <p>Select a process to view its {variableType === 'SECRET' ? 'sensitive' : 'environment'} variables.</p>
        </div>
      )}

      <EnvVariableFormModal
        isOpen={isFormOpen}
        onClose={handleFormClose}
        onSave={handleFormSave}
        editingVariable={editingVariable}
        processDefinitionId={selectedProcessId}
        variableType={variableType}
        saving={adding || updating}
      />

      <ConfirmationDialog
        open={!!deleteTarget}
        onOpenChange={(open) => { if (!open) setDeleteTarget(null); }}
        title="Delete Variable"
        description={`Are you sure you want to delete the variable "${deleteTarget?.name}"? This action cannot be undone.`}
        onConfirm={handleDelete}
        confirmText="Delete"
        confirmVariant="primary"
      />
    </div>
  );
};

export default React.memo(EnvVariablesPanel);
