import { processDefinitionService } from '@/api/domains';
import { processStateService, type ProcessState } from '@/api/domains/process-state';
import { Button } from '@/design-system/components/ui/button';
import { Combobox } from '@/design-system/components/ui/combobox';
import { Input } from '@/design-system/components/ui/input';
import { useAuth, UserRoles } from '@/shared/auth';
import { ConfirmationDialog } from '@/shared/components/ConfirmationDialog/ConfirmationDialog';
import { DataTable } from '@/shared/components/DataTable/DataTable';
import { SmartText } from '@/shared/components/SmartText/SmartText';
import { StandardModal } from '@/shared/components/StandardModal/StandardModal';
import { useApiMutation, useApiQuery } from '@/shared/hooks';
import { Search, Trash2 } from 'lucide-react';
import React, { useCallback, useMemo, useState } from 'react';
import styles from './ProcessStatePanel.module.css';

interface ToggleTarget {
  process: ProcessState;
  newStatus: boolean;
}

const ProcessStatePanel: React.FC = () => {
  const { hasRole } = useAuth();
  const isAdmin = useMemo(() => hasRole(UserRoles.ADMIN), [hasRole]);

  const [toggleTarget, setToggleTarget] = useState<ToggleTarget | null>(null);
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [selectedProcessId, setSelectedProcessId] = useState<string>('');
  const [initialStatus, setInitialStatus] = useState<boolean>(true);
  const [confirmAdd, setConfirmAdd] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [deleteTarget, setDeleteTarget] = useState<ProcessState | null>(null);

  const { data: processStates = [], isLoading } = useApiQuery(
    ['processStates'],
    () => processStateService.getAll(),
    { showErrorToast: true, staleTime: 30000 }
  );

  const { data: definitionsResponse } = useApiQuery(
    ['processDefinitions', 'processStateSelector'],
    () => processDefinitionService.getProcessDefinitions({ size: 1000 }),
    { staleTime: 60000 }
  );

  const existingProcessIds = useMemo(() => {
    return new Set(processStates.map((ps) => ps.processId));
  }, [processStates]);

  const processOptions = useMemo(() => {
    const content = definitionsResponse?.content ?? definitionsResponse?.data ?? [];
    if (!Array.isArray(content)) return [];
    const ids = content.map((d: Record<string, unknown>) =>
      String(d.definitionId || d.processDefinitionId || d.id || '')
    );
    const unique = [...new Set(ids)].filter(Boolean).sort();
    return unique
      .filter((id) => !existingProcessIds.has(id))
      .map((id) => ({ label: id, value: id }));
  }, [definitionsResponse, existingProcessIds]);

  const filteredProcessStates = useMemo(() => {
    if (!searchTerm.trim()) return processStates;
    const term = searchTerm.toLowerCase();
    return processStates.filter((ps) => ps.processId.toLowerCase().includes(term));
  }, [processStates, searchTerm]);

  const { mutateAsync: upsertState, isPending: saving } = useApiMutation(
    (data: ProcessState) => processStateService.upsert(data),
    {
      invalidateQueries: [['processStates']],
      showSuccessToast: true,
      successMessage: 'Process state updated successfully',
    }
  );

  const { mutateAsync: addState, isPending: adding } = useApiMutation(
    (data: ProcessState) => processStateService.add(data),
    {
      invalidateQueries: [['processStates']],
      showSuccessToast: true,
      successMessage: 'Process state added successfully',
    }
  );

  const { mutateAsync: removeState } = useApiMutation(
    (processId: string) => processStateService.remove(processId),
    {
      invalidateQueries: [['processStates']],
      showSuccessToast: true,
      successMessage: 'Process state removed successfully',
    }
  );

  const handleToggleClick = useCallback((process: ProcessState) => {
    setToggleTarget({ process, newStatus: !process.status });
  }, []);

  const handleConfirmToggle = useCallback(async () => {
    if (!toggleTarget) return;
    await upsertState({
      ...toggleTarget.process,
      status: toggleTarget.newStatus,
    });
    setToggleTarget(null);
  }, [toggleTarget, upsertState]);

  const handleOpenAdd = useCallback(() => {
    setSelectedProcessId('');
    setInitialStatus(true);
    setConfirmAdd(false);
    setIsAddOpen(true);
  }, []);

  const handleAddSubmit = useCallback(() => {
    if (!selectedProcessId) return;
    if (!initialStatus) {
      setConfirmAdd(true);
    } else {
      void doAdd();
    }
  }, [selectedProcessId, initialStatus]);

  const doAdd = useCallback(async () => {
    await addState({
      processId: selectedProcessId,
      status: initialStatus,
    });
    setIsAddOpen(false);
    setConfirmAdd(false);
    setSelectedProcessId('');
    setInitialStatus(true);
  }, [selectedProcessId, initialStatus, addState]);

  const handleConfirmDisabledAdd = useCallback(async () => {
    await doAdd();
  }, [doAdd]);

  const handleProcessSelect = useCallback((value: string) => {
    setSelectedProcessId(value);
  }, []);

  const handleDeleteClick = useCallback((process: ProcessState) => {
    setDeleteTarget(process);
  }, []);

  const handleConfirmDelete = useCallback(async () => {
    if (!deleteTarget) return;
    await removeState(deleteTarget.processId);
    setDeleteTarget(null);
  }, [deleteTarget, removeState]);

  const handleSearchChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
  }, []);

  const columns = useMemo(() => [
    {
      key: 'processId',
      header: 'Process ID',
      render: (row: ProcessState) => (
        <SmartText text={row.processId} maxWidth="100%" />
      ),
    },
    {
      key: 'status',
      header: 'State',
      render: (row: ProcessState) => (
        <span className={`${styles.statusBadge} ${row.status ? styles.statusActive : styles.statusDisabled}`}>
          <span className={styles.statusDot} />
          {row.status ? 'Active' : 'Disabled'}
        </span>
      ),
      align: 'center' as const,
    },
    {
      key: 'description',
      header: 'Description',
      render: (row: ProcessState) => (
        <span className={styles.description}>
          {row.status
            ? 'Process is running and accepting new instances'
            : 'Process is stopped — no new instances will be processed'}
        </span>
      ),
    },
    ...(isAdmin ? [{
      key: 'actions',
      header: 'Action',
      render: (row: ProcessState) => (
        <div className={styles.actionsCell}>
          <button
            type="button"
            className={`${styles.toggleButton} ${row.status ? styles.toggleDisable : styles.toggleEnable}`}
            onClick={() => handleToggleClick(row)}
            disabled={saving}
          >
            {row.status ? 'Disable' : 'Enable'}
          </button>
          <button
            type="button"
            className={styles.deleteButton}
            onClick={() => handleDeleteClick(row)}
            title="Remove process state"
          >
            <Trash2 size={14} />
          </button>
        </div>
      ),
      align: 'center' as const,
    }] : []),
  ], [isAdmin, handleToggleClick, handleDeleteClick, saving]);

  const isDisablingAction = toggleTarget && !toggleTarget.newStatus;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div className={styles.headerTop}>
          <div>
            <h2 className={styles.title}>Process State</h2>
            <p className={styles.subtitle}>
              Control which processes are active or disabled.
              {!isAdmin && <span className={styles.readonlyHint}> (Read-only — admin access required to modify)</span>}
            </p>
          </div>
          {isAdmin && (
            <Button variant="primary" size="sm" onClick={handleOpenAdd}>
              + Add Process State
            </Button>
          )}
        </div>
      </div>

      <div className={styles.searchBar}>
        <div className={styles.searchInputWrap}>
          <Search size={14} className={styles.searchIcon} />
          <Input
            placeholder="Search by process ID..."
            value={searchTerm}
            onChange={handleSearchChange}
            type="text"
          />
        </div>
        <span className={styles.searchCount}>
          {filteredProcessStates.length} of {processStates.length} processes
        </span>
      </div>

      <div className={styles.tableContainer}>
        <DataTable<ProcessState>
          data={filteredProcessStates}
          columns={columns}
          keyExtractor={(row) => row.id ?? row.processId}
          loading={isLoading}
          loadingText="Loading process states..."
          emptyText={searchTerm ? 'No processes match your search.' : 'No process states configured.'}
          showCard={true}
          stickyHeader={false}
        />
      </div>

      <StandardModal
        isOpen={isAddOpen}
        onClose={() => setIsAddOpen(false)}
        title="Add Process State"
        size="md"
        onConfirm={handleAddSubmit}
        confirmText="Add"
        confirmLoading={adding}
        confirmDisabled={!selectedProcessId}
      >
        <div className={styles.addFormContainer}>
          <div className={styles.addFieldGroup}>
            <span className={styles.addFieldLabel}>Process Definition ID</span>
            <Combobox
              items={processOptions}
              value={selectedProcessId || undefined}
              onSelect={handleProcessSelect}
              placeholder="Select a process..."
              searchPlaceholder="Type to search processes..."
              emptyMessage="No available processes found."
            />
            {existingProcessIds.size > 0 && (
              <span className={styles.addHint}>
                Already configured processes are excluded from this list.
              </span>
            )}
          </div>

          <div className={styles.addFieldGroup}>
            <span className={styles.addFieldLabel}>Initial State</span>
            <div className={styles.stateToggleGroup}>
              <button
                type="button"
                className={`${styles.stateOption} ${initialStatus ? styles.stateOptionActiveSelected : ''}`}
                onClick={() => setInitialStatus(true)}
              >
                <span className={`${styles.statusDot} ${styles.dotActive}`} />
                Active
              </button>
              <button
                type="button"
                className={`${styles.stateOption} ${!initialStatus ? styles.stateOptionDisabledSelected : ''}`}
                onClick={() => setInitialStatus(false)}
              >
                <span className={`${styles.statusDot} ${styles.dotDisabled}`} />
                Disabled
              </button>
            </div>
            {!initialStatus && (
              <span className={styles.addWarning}>
                Adding as disabled means this process will NOT accept new instances immediately.
              </span>
            )}
          </div>
        </div>
      </StandardModal>

      <ConfirmationDialog
        open={confirmAdd}
        onOpenChange={(open) => { if (!open) setConfirmAdd(false); }}
        title="⚠️ Add as Disabled — Danger"
        description={`You are adding "${selectedProcessId}" with state DISABLED. This means the process will NOT process any new instances until manually enabled. Are you sure?`}
        onConfirm={handleConfirmDisabledAdd}
        confirmText="Yes, Add as Disabled"
        confirmVariant="destructive"
      />

      <ConfirmationDialog
        open={!!toggleTarget}
        onOpenChange={(open) => { if (!open) setToggleTarget(null); }}
        title={
          isDisablingAction
            ? '⚠️ Disable Process — Danger'
            : 'Enable Process'
        }
        description={
          isDisablingAction
            ? `Are you sure you want to disable "${toggleTarget?.process.processId}"? Disabling this process will STOP all processing immediately. No new instances will be created or handled until re-enabled.`
            : `Are you sure you want to enable "${toggleTarget?.process.processId}"? This will resume processing and allow new instances to be created.`
        }
        onConfirm={handleConfirmToggle}
        confirmText={isDisablingAction ? 'Yes, Disable Process' : 'Yes, Enable Process'}
        confirmVariant={isDisablingAction ? 'destructive' : 'success'}
      />

      <ConfirmationDialog
        open={!!deleteTarget}
        onOpenChange={(open) => { if (!open) setDeleteTarget(null); }}
        title="Delete Process State"
        description={`Are you sure you want to remove "${deleteTarget?.processId}" from process state settings? This action cannot be undone.`}
        onConfirm={handleConfirmDelete}
        confirmText="Yes, Delete"
        confirmVariant="destructive"
      />
    </div>
  );
};

export default React.memo(ProcessStatePanel);