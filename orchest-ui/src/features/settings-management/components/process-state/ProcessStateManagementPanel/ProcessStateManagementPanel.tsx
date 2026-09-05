import { processDefinitionService } from '@/api/domains';
import { processStateService, type ProcessState } from '@/api/domains/process-state';
import { activityStateService, type ActivityState } from '@/api/domains/activity-state';
import { Button } from '@/design-system/components/ui/button';
import { Combobox } from '@/design-system/components/ui/combobox';
import { Input } from '@/design-system/components/ui/input';
import { useAuth, UserRoles } from '@/shared/auth';
import { ConfirmationDialog } from '@/shared/components/ConfirmationDialog/ConfirmationDialog';
import { DataTable, type SortConfig } from '@/shared/components/DataTable/DataTable';
import { SmartText } from '@/shared/components/SmartText/SmartText';
import { useApiMutation, useApiQuery } from '@/shared/hooks';
import { Trash2, GitBranchPlus } from 'lucide-react';
import React, { useCallback, useMemo, useState } from 'react';
import { ActivityPickerModal, type ActivityPickerResult } from './ActivityPickerModal';
import styles from './ProcessStateManagementPanel.module.css';

type SubTab = 'process' | 'activity';

const ProcessStateManagementPanel: React.FC = () => {
  const { hasRole } = useAuth();
  const isAdmin = useMemo(() => hasRole(UserRoles.ADMIN), [hasRole]);

  const [activeSubTab, setActiveSubTab] = useState<SubTab>('process');
  const [selectedProcessId, setSelectedProcessId] = useState<string>('');

  // Process State
  const [processToggleTarget, setProcessToggleTarget] = useState<{ process: ProcessState; newStatus: boolean } | null>(null);
  const [isAddProcessOpen, setIsAddProcessOpen] = useState(false);
  const [addProcessInitialStatus, setAddProcessInitialStatus] = useState(true);
  const [confirmAddProcess, setConfirmAddProcess] = useState(false);
  const [deleteProcessTarget, setDeleteProcessTarget] = useState<ProcessState | null>(null);

  // Activity State
  const [activityToggleTarget, setActivityToggleTarget] = useState<{ activity: ActivityState; newEnabled: boolean } | null>(null);
  const [activityVersion] = useState<number>(1);
  const [deleteActivityTarget, setDeleteActivityTarget] = useState<ActivityState | null>(null);
  const [isDiagramPickerOpen, setIsDiagramPickerOpen] = useState(false);

  // Sort state
  const [processSortConfig, setProcessSortConfig] = useState<SortConfig[]>([]);
  const [activitySortConfig, setActivitySortConfig] = useState<SortConfig[]>([]);

  // Data queries
  const { data: definitionsResponse } = useApiQuery(
    ['processDefinitions', 'stateManagement'],
    () => processDefinitionService.getProcessDefinitions({ size: 1000 }),
    { staleTime: 60000 }
  );

  const { data: allProcessStates = [], isLoading: loadingProcessStates } = useApiQuery(
    ['processStates'],
    () => processStateService.getAll(),
    { showErrorToast: true, staleTime: 30000 }
  );

  const { data: allActivityStates = [], isLoading: loadingActivityStates } = useApiQuery(
    ['activityStates'],
    () => activityStateService.getAll(),
    { showErrorToast: true, staleTime: 30000 }
  );

  const processOptions = useMemo(() => {
    const content = definitionsResponse?.content ?? definitionsResponse?.data ?? [];
    if (!Array.isArray(content)) return [];
    const ids = content.map((d: Record<string, unknown>) =>
      String(d.definitionId || d.processDefinitionId || d.id || '')
    );
    return [...new Set(ids)].filter(Boolean).sort().map((id) => ({ label: id, value: id }));
  }, [definitionsResponse]);

  const filteredProcessStates = useMemo(() => {
    if (!selectedProcessId) return [];
    const filtered = allProcessStates.filter((ps) => ps.processId === selectedProcessId);
    if (processSortConfig.length === 0) return filtered;
    return [...filtered].sort((a, b) => {
      for (const sort of processSortConfig) {
        let cmp = 0;
        if (sort.field === 'status') {
          cmp = Number(a.status) - Number(b.status);
        }
        if (cmp !== 0) return sort.direction === 'asc' ? cmp : -cmp;
      }
      return 0;
    });
  }, [allProcessStates, selectedProcessId, processSortConfig]);

  const filteredActivityStates = useMemo(() => {
    if (!selectedProcessId) return [];
    const filtered = allActivityStates.filter((a) => a.processDefinitionId === selectedProcessId);
    if (activitySortConfig.length === 0) return filtered;
    return [...filtered].sort((a, b) => {
      for (const sort of activitySortConfig) {
        let cmp = 0;
        if (sort.field === 'version') {
          cmp = a.version - b.version;
        } else if (sort.field === 'enabled') {
          cmp = Number(a.enabled) - Number(b.enabled);
        }
        if (cmp !== 0) return sort.direction === 'asc' ? cmp : -cmp;
      }
      return 0;
    });
  }, [allActivityStates, selectedProcessId, activitySortConfig]);

  // Process State mutations
  const { mutateAsync: upsertProcessState, isPending: savingProcess } = useApiMutation(
    (data: ProcessState) => processStateService.upsert(data),
    { invalidateQueries: [['processStates']], showSuccessToast: true, successMessage: 'Process state updated' }
  );

  const { mutateAsync: addProcessState, isPending: addingProcess } = useApiMutation(
    (data: ProcessState) => processStateService.add(data),
    { invalidateQueries: [['processStates']], showSuccessToast: true, successMessage: 'Process state added' }
  );

  const { mutateAsync: removeProcessState } = useApiMutation(
    (processId: string) => processStateService.remove(processId),
    { invalidateQueries: [['processStates']], showSuccessToast: true, successMessage: 'Process state removed' }
  );

  // Activity State mutations
  const { mutateAsync: upsertActivityState, isPending: savingActivity } = useApiMutation(
    (data: ActivityState) => activityStateService.upsert(data),
    { invalidateQueries: [['activityStates']], showSuccessToast: true, successMessage: 'Activity state updated' }
  );

  const { mutateAsync: removeActivityState } = useApiMutation(
    (target: { processDefinitionId: string; version: number; activityId: string }) =>
      activityStateService.remove(target.processDefinitionId, target.version, target.activityId),
    { invalidateQueries: [['activityStates']], showSuccessToast: true, successMessage: 'Activity state removed' }
  );

  // Process State handlers
  const handleProcessToggleClick = useCallback((process: ProcessState) => {
    setProcessToggleTarget({ process, newStatus: !process.status });
  }, []);

  const handleConfirmProcessToggle = useCallback(async () => {
    if (!processToggleTarget) return;
    await upsertProcessState({ ...processToggleTarget.process, status: processToggleTarget.newStatus });
    setProcessToggleTarget(null);
  }, [processToggleTarget, upsertProcessState]);

  const handleOpenAddProcess = useCallback(() => {
    setAddProcessInitialStatus(true);
    setConfirmAddProcess(false);
    setIsAddProcessOpen(true);
  }, []);

  const handleAddProcessSubmit = useCallback(() => {
    if (!selectedProcessId) return;
    if (!addProcessInitialStatus) {
      setConfirmAddProcess(true);
    } else {
      void doAddProcess();
    }
  }, [selectedProcessId, addProcessInitialStatus]);

  const doAddProcess = useCallback(async () => {
    await addProcessState({ processId: selectedProcessId, status: addProcessInitialStatus });
    setIsAddProcessOpen(false);
    setConfirmAddProcess(false);
  }, [selectedProcessId, addProcessInitialStatus, addProcessState]);

  const handleConfirmDeleteProcess = useCallback(async () => {
    if (!deleteProcessTarget) return;
    await removeProcessState(deleteProcessTarget.processId);
    setDeleteProcessTarget(null);
  }, [deleteProcessTarget, removeProcessState]);

  // Activity State handlers
  const handleActivityToggleClick = useCallback((activity: ActivityState) => {
    setActivityToggleTarget({ activity, newEnabled: !activity.enabled });
  }, []);

  const handleConfirmActivityToggle = useCallback(async () => {
    if (!activityToggleTarget) return;
    await upsertActivityState({ ...activityToggleTarget.activity, enabled: activityToggleTarget.newEnabled });
    setActivityToggleTarget(null);
  }, [activityToggleTarget, upsertActivityState]);

  const handleConfirmDeleteActivity = useCallback(async () => {
    if (!deleteActivityTarget) return;
    await removeActivityState({
      processDefinitionId: deleteActivityTarget.processDefinitionId,
      version: deleteActivityTarget.version,
      activityId: deleteActivityTarget.activityId,
    });
    setDeleteActivityTarget(null);
  }, [deleteActivityTarget, removeActivityState]);

  const handleDiagramPickerSubmit = useCallback(async (result: ActivityPickerResult) => {
    for (const actId of result.activityIds) {
      await upsertActivityState({
        processDefinitionId: selectedProcessId,
        version: result.version,
        activityId: actId,
        enabled: result.enabled,
      });
    }
    setIsDiagramPickerOpen(false);
  }, [selectedProcessId, upsertActivityState]);

  // Columns
  const processColumns = useMemo(() => [
    {
      key: 'processId',
      header: 'Process ID',
      render: (row: ProcessState) => <SmartText text={row.processId} maxWidth="100%" />,
    },
    {
      key: 'status',
      header: 'State',
      sortable: true,
      render: (row: ProcessState) => (
        <span className={`${styles.statusBadge} ${row.status ? styles.statusActive : styles.statusDisabled}`}>
          <span className={styles.statusDot} />
          {row.status ? 'Active' : 'Disabled'}
        </span>
      ),
      align: 'center' as const,
    },
    ...(isAdmin ? [{
      key: 'actions',
      header: 'Action',
      render: (row: ProcessState) => (
        <div className={styles.actionsCell}>
          <button
            type="button"
            className={`${styles.toggleButton} ${row.status ? styles.toggleDisable : styles.toggleEnable}`}
            onClick={() => handleProcessToggleClick(row)}
            disabled={savingProcess}
          >
            {row.status ? 'Disable' : 'Enable'}
          </button>
          <button type="button" className={styles.deleteButton} onClick={() => setDeleteProcessTarget(row)} title="Remove">
            <Trash2 size={14} />
          </button>
        </div>
      ),
      align: 'center' as const,
    }] : []),
  ], [isAdmin, handleProcessToggleClick, savingProcess]);

  const activityColumns = useMemo(() => [
    {
      key: 'activityId',
      header: 'Activity ID',
      render: (row: ActivityState) => <SmartText text={row.activityId} maxWidth="100%" />,
    },
    {
      key: 'version',
      header: 'Version',
      sortable: true,
      render: (row: ActivityState) => <span>{row.version}</span>,
      align: 'center' as const,
    },
    {
      key: 'description',
      header: 'Description',
      render: (row: ActivityState) => <span className={styles.descriptionText}>{row.description || '—'}</span>,
    },
    {
      key: 'enabled',
      header: 'State',
      sortable: true,
      render: (row: ActivityState) => (
        <span className={`${styles.statusBadge} ${row.enabled ? styles.statusActive : styles.statusDisabled}`}>
          <span className={styles.statusDot} />
          {row.enabled ? 'Active' : 'Disabled'}
        </span>
      ),
      align: 'center' as const,
    },
    ...(isAdmin ? [{
      key: 'actions',
      header: 'Action',
      render: (row: ActivityState) => (
        <div className={styles.actionsCell}>
          <button
            type="button"
            className={`${styles.toggleButton} ${row.enabled ? styles.toggleDisable : styles.toggleEnable}`}
            onClick={() => handleActivityToggleClick(row)}
            disabled={savingActivity}
          >
            {row.enabled ? 'Disable' : 'Enable'}
          </button>
          <button type="button" className={styles.deleteButton} onClick={() => setDeleteActivityTarget(row)} title="Remove">
            <Trash2 size={14} />
          </button>
        </div>
      ),
      align: 'center' as const,
    }] : []),
  ], [isAdmin, handleActivityToggleClick, savingActivity]);

  const processIsDisabling = processToggleTarget && !processToggleTarget.newStatus;
  const activityIsDisabling = activityToggleTarget && !activityToggleTarget.newEnabled;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>Process Management</h2>
        {isAdmin && selectedProcessId && (
          <div className={styles.headerActions}>
            {activeSubTab === 'activity' && (
              <Button
                variant="primary"
                size="sm"
                onClick={() => setIsDiagramPickerOpen(true)}
              >
                <GitBranchPlus size={14} />
                Pick from Diagram
              </Button>
            )}
            {activeSubTab === 'process' && (
              <Button
                variant="primary"
                size="sm"
                onClick={handleOpenAddProcess}
              >
                + Add Process State
              </Button>
            )}
          </div>
        )}
      </div>

      <div className={styles.processSelector}>
        <span className={styles.processSelectorLabel}>Select Process</span>
        <Combobox
          items={processOptions}
          value={selectedProcessId || undefined}
          onSelect={(value: string) => setSelectedProcessId(value)}
          placeholder="Choose a process..."
          searchPlaceholder="Type to search processes..."
          emptyMessage="No processes found."
        />
      </div>

      {selectedProcessId && (
        <>
          <div className={styles.tabBar}>
            <button
              type="button"
              className={`${styles.tab} ${activeSubTab === 'process' ? styles.tabActive : ''}`}
              onClick={() => setActiveSubTab('process')}
            >
              Process State
            </button>
            <button
              type="button"
              className={`${styles.tab} ${activeSubTab === 'activity' ? styles.tabActive : ''}`}
              onClick={() => setActiveSubTab('activity')}
            >
              Activity State
            </button>
          </div>

          <div className={styles.tableContainer}>
            {activeSubTab === 'process' ? (
              <DataTable<ProcessState>
                data={filteredProcessStates}
                columns={processColumns}
                keyExtractor={(row) => row.id ?? row.processId}
                loading={loadingProcessStates}
                loadingText="Loading process states..."
                emptyText="No process state configured for this process."
                showCard={true}
                stickyHeader={false}
                sortConfig={processSortConfig}
                onSortChange={setProcessSortConfig}
              />
            ) : (
              <DataTable<ActivityState>
                data={filteredActivityStates}
                columns={activityColumns}
                keyExtractor={(row) => row.id ?? `${row.processDefinitionId}-${row.version}-${row.activityId}`}
                loading={loadingActivityStates}
                loadingText="Loading activity states..."
                emptyText="No activity states configured for this process."
                showCard={true}
                stickyHeader={false}
                sortConfig={activitySortConfig}
                onSortChange={setActivitySortConfig}
              />
            )}
          </div>
        </>
      )}

      {!selectedProcessId && (
        <div className={styles.emptyState}>
          <p>Select a process to manage its state and activities.</p>
        </div>
      )}

      {/* Add Process State Modal */}
      {isAddProcessOpen && (
        <div className={styles.overlay} onClick={() => setIsAddProcessOpen(false)}>
          <div className={styles.modal} onClick={e => e.stopPropagation()}>
            <h2 className={styles.modalTitle}>Add Process State</h2>
            <div className={styles.addFormContainer}>
              <div className={styles.addFieldGroup}>
                <span className={styles.addFieldLabel}>Process Definition ID</span>
                <Input type="text" value={selectedProcessId} disabled />
              </div>
              <div className={styles.addFieldGroup}>
                <span className={styles.addFieldLabel}>Initial State</span>
                <div className={styles.stateToggleGroup}>
                  <button
                    type="button"
                    className={`${styles.stateOption} ${addProcessInitialStatus ? styles.stateOptionActiveSelected : ''}`}
                    onClick={() => setAddProcessInitialStatus(true)}
                  >
                    <span className={`${styles.statusDot} ${styles.dotActive}`} />
                    Active
                  </button>
                  <button
                    type="button"
                    className={`${styles.stateOption} ${!addProcessInitialStatus ? styles.stateOptionDisabledSelected : ''}`}
                    onClick={() => setAddProcessInitialStatus(false)}
                  >
                    <span className={`${styles.statusDot} ${styles.dotDisabled}`} />
                    Disabled
                  </button>
                </div>
                {!addProcessInitialStatus && (
                  <span className={styles.addWarning}>
                    Adding as disabled means this process will NOT accept new instances immediately.
                  </span>
                )}
              </div>
              <div className={styles.modalActions}>
                <Button variant="outline" size="sm" onClick={() => setIsAddProcessOpen(false)}>Cancel</Button>
                <Button
                  variant="primary"
                  size="sm"
                  onClick={handleAddProcessSubmit}
                  disabled={addingProcess || !selectedProcessId}
                >
                  {addingProcess ? 'Adding...' : 'Add'}
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}


      {/* Process State Confirmations */}
      <ConfirmationDialog
        open={confirmAddProcess}
        onOpenChange={(open) => { if (!open) setConfirmAddProcess(false); }}
        title="Add as Disabled"
        description={`You are adding "${selectedProcessId}" with state DISABLED. No new instances will be processed until manually enabled. Are you sure?`}
        onConfirm={doAddProcess}
        confirmText="Yes, Add as Disabled"
        confirmVariant="destructive"
      />

      <ConfirmationDialog
        open={!!processToggleTarget}
        onOpenChange={(open) => { if (!open) setProcessToggleTarget(null); }}
        title={processIsDisabling ? 'Disable Process' : 'Enable Process'}
        description={
          processIsDisabling
            ? `Are you sure you want to disable "${processToggleTarget?.process.processId}"? No new instances will be processed.`
            : `Are you sure you want to enable "${processToggleTarget?.process.processId}"? Processing will resume.`
        }
        onConfirm={handleConfirmProcessToggle}
        confirmText={processIsDisabling ? 'Yes, Disable' : 'Yes, Enable'}
        confirmVariant={processIsDisabling ? 'destructive' : 'success'}
      />

      <ConfirmationDialog
        open={!!deleteProcessTarget}
        onOpenChange={(open) => { if (!open) setDeleteProcessTarget(null); }}
        title="Delete Process State"
        description={`Remove "${deleteProcessTarget?.processId}" from process state settings? This cannot be undone.`}
        onConfirm={handleConfirmDeleteProcess}
        confirmText="Yes, Delete"
        confirmVariant="destructive"
      />

      {/* Activity State Confirmations */}
      <ConfirmationDialog
        open={!!activityToggleTarget}
        onOpenChange={(open) => { if (!open) setActivityToggleTarget(null); }}
        title={activityIsDisabling ? 'Disable Activity' : 'Enable Activity'}
        description={
          activityIsDisabling
            ? `Disable activity "${activityToggleTarget?.activity.activityId}" (v${activityToggleTarget?.activity.version})? It will be skipped during execution.`
            : `Enable activity "${activityToggleTarget?.activity.activityId}" (v${activityToggleTarget?.activity.version})? It will resume normal execution.`
        }
        onConfirm={handleConfirmActivityToggle}
        confirmText={activityIsDisabling ? 'Yes, Disable' : 'Yes, Enable'}
        confirmVariant={activityIsDisabling ? 'destructive' : 'success'}
      />

      <ConfirmationDialog
        open={!!deleteActivityTarget}
        onOpenChange={(open) => { if (!open) setDeleteActivityTarget(null); }}
        title="Delete Activity State"
        description={`Remove activity "${deleteActivityTarget?.activityId}" (v${deleteActivityTarget?.version})? This cannot be undone.`}
        onConfirm={handleConfirmDeleteActivity}
        confirmText="Yes, Delete"
        confirmVariant="destructive"
      />

      {/* BPMN Diagram Activity Picker — conditionally rendered to force fresh mount */}
      {isDiagramPickerOpen && (
        <ActivityPickerModal
          isOpen={isDiagramPickerOpen}
          onClose={() => setIsDiagramPickerOpen(false)}
          processDefinitionId={selectedProcessId}
          version={activityVersion}
          onSubmit={handleDiagramPickerSubmit}
          submitting={savingActivity}
        />
      )}
    </div>
  );
};

export default React.memo(ProcessStateManagementPanel);