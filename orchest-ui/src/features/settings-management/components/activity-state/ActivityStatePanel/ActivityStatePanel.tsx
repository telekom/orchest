import { processDefinitionService } from '@/api/domains';
import { activityStateService, type ActivityState } from '@/api/domains/activity-state';
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
import styles from './ActivityStatePanel.module.css';

interface ToggleTarget {
  activity: ActivityState;
  newEnabled: boolean;
}

const ActivityStatePanel: React.FC = () => {
  const { hasRole } = useAuth();
  const isAdmin = useMemo(() => hasRole(UserRoles.ADMIN), [hasRole]);

  const [toggleTarget, setToggleTarget] = useState<ToggleTarget | null>(null);
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [selectedProcessId, setSelectedProcessId] = useState<string>('');
  const [version, setVersion] = useState<number>(1);
  const [activityId, setActivityId] = useState<string>('');
  const [description, setDescription] = useState<string>('');
  const [initialEnabled, setInitialEnabled] = useState<boolean>(true);
  const [confirmAdd, setConfirmAdd] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [deleteTarget, setDeleteTarget] = useState<ActivityState | null>(null);

  const { data: activityStates = [], isLoading } = useApiQuery(
    ['activityStates'],
    () => activityStateService.getAll(),
    { showErrorToast: true, staleTime: 30000 }
  );

  const { data: definitionsResponse } = useApiQuery(
    ['processDefinitions', 'activityStateSelector'],
    () => processDefinitionService.getProcessDefinitions({ size: 1000 }),
    { staleTime: 60000 }
  );

  const processOptions = useMemo(() => {
    const content = definitionsResponse?.content ?? definitionsResponse?.data ?? [];
    if (!Array.isArray(content)) return [];
    const ids = content.map((d: Record<string, unknown>) =>
      String(d.definitionId || d.processDefinitionId || d.id || '')
    );
    const unique = [...new Set(ids)].filter(Boolean).sort();
    return unique.map((id) => ({ label: id, value: id }));
  }, [definitionsResponse]);

  const filteredActivityStates = useMemo(() => {
    if (!searchTerm.trim()) return activityStates;
    const term = searchTerm.toLowerCase();
    return activityStates.filter(
      (a) =>
        a.processDefinitionId.toLowerCase().includes(term) ||
        a.activityId.toLowerCase().includes(term)
    );
  }, [activityStates, searchTerm]);

  const { mutateAsync: upsertState, isPending: saving } = useApiMutation(
    (data: ActivityState) => activityStateService.upsert(data),
    {
      invalidateQueries: [['activityStates']],
      showSuccessToast: true,
      successMessage: 'Activity state updated successfully',
    }
  );

  const { mutateAsync: removeState } = useApiMutation(
    (target: { processDefinitionId: string; version: number; activityId: string }) =>
      activityStateService.remove(target.processDefinitionId, target.version, target.activityId),
    {
      invalidateQueries: [['activityStates']],
      showSuccessToast: true,
      successMessage: 'Activity state removed successfully',
    }
  );

  const handleToggleClick = useCallback((activity: ActivityState) => {
    setToggleTarget({ activity, newEnabled: !activity.enabled });
  }, []);

  const handleConfirmToggle = useCallback(async () => {
    if (!toggleTarget) return;
    await upsertState({
      ...toggleTarget.activity,
      enabled: toggleTarget.newEnabled,
    });
    setToggleTarget(null);
  }, [toggleTarget, upsertState]);

  const handleOpenAdd = useCallback(() => {
    setSelectedProcessId('');
    setVersion(1);
    setActivityId('');
    setDescription('');
    setInitialEnabled(true);
    setConfirmAdd(false);
    setIsAddOpen(true);
  }, []);

  const handleAddSubmit = useCallback(() => {
    if (!selectedProcessId || !activityId) return;
    if (!initialEnabled) {
      setConfirmAdd(true);
    } else {
      void doAdd();
    }
  }, [selectedProcessId, activityId, initialEnabled]);

  const doAdd = useCallback(async () => {
    await upsertState({
      processDefinitionId: selectedProcessId,
      version,
      activityId,
      description: description || undefined,
      enabled: initialEnabled,
    });
    setIsAddOpen(false);
    setConfirmAdd(false);
    setSelectedProcessId('');
    setVersion(1);
    setActivityId('');
    setDescription('');
    setInitialEnabled(true);
  }, [selectedProcessId, version, activityId, description, initialEnabled, upsertState]);

  const handleConfirmDisabledAdd = useCallback(async () => {
    await doAdd();
  }, [doAdd]);

  const handleDeleteClick = useCallback((activity: ActivityState) => {
    setDeleteTarget(activity);
  }, []);

  const handleConfirmDelete = useCallback(async () => {
    if (!deleteTarget) return;
    await removeState({
      processDefinitionId: deleteTarget.processDefinitionId,
      version: deleteTarget.version,
      activityId: deleteTarget.activityId,
    });
    setDeleteTarget(null);
  }, [deleteTarget, removeState]);

  const handleSearchChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
  }, []);

  const columns = useMemo(
    () => [
      {
        key: 'processDefinitionId',
        header: 'Process ID',
        render: (row: ActivityState) => (
          <SmartText text={row.processDefinitionId} maxWidth="100%" />
        ),
      },
      {
        key: 'version',
        header: 'Version',
        render: (row: ActivityState) => <span>{row.version}</span>,
        align: 'center' as const,
      },
      {
        key: 'activityId',
        header: 'Activity ID',
        render: (row: ActivityState) => (
          <SmartText text={row.activityId} maxWidth="100%" />
        ),
      },
      {
        key: 'description',
        header: 'Description',
        render: (row: ActivityState) => (
          <span className={styles.description}>{row.description || '—'}</span>
        ),
      },
      {
        key: 'enabled',
        header: 'State',
        render: (row: ActivityState) => (
          <span
            className={`${styles.statusBadge} ${row.enabled ? styles.statusActive : styles.statusDisabled}`}
          >
            <span className={styles.statusDot} />
            {row.enabled ? 'Active' : 'Disabled'}
          </span>
        ),
        align: 'center' as const,
      },
      ...(isAdmin
        ? [
            {
              key: 'actions',
              header: 'Action',
              render: (row: ActivityState) => (
                <div className={styles.actionsCell}>
                  <button
                    type="button"
                    className={`${styles.toggleButton} ${row.enabled ? styles.toggleDisable : styles.toggleEnable}`}
                    onClick={() => handleToggleClick(row)}
                    disabled={saving}
                  >
                    {row.enabled ? 'Disable' : 'Enable'}
                  </button>
                  <button
                    type="button"
                    className={styles.deleteButton}
                    onClick={() => handleDeleteClick(row)}
                    title="Remove activity state"
                  >
                    <Trash2 size={14} />
                  </button>
                </div>
              ),
              align: 'center' as const,
            },
          ]
        : []),
    ],
    [isAdmin, handleToggleClick, handleDeleteClick, saving]
  );

  const isDisablingAction = toggleTarget && !toggleTarget.newEnabled;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div className={styles.headerTop}>
          <div>
            <h2 className={styles.title}>Activity State</h2>
            <p className={styles.subtitle}>
              Control which activities are active or disabled within a process.
              {!isAdmin && (
                <span className={styles.readonlyHint}>
                  {' '}
                  (Read-only — admin access required to modify)
                </span>
              )}
            </p>
          </div>
          {isAdmin && (
            <Button variant="primary" size="sm" onClick={handleOpenAdd}>
              + Add Activity State
            </Button>
          )}
        </div>
      </div>

      <div className={styles.searchBar}>
        <div className={styles.searchInputWrap}>
          <Search size={14} className={styles.searchIcon} />
          <Input
            placeholder="Search by process or activity ID..."
            value={searchTerm}
            onChange={handleSearchChange}
            type="text"
          />
        </div>
        <span className={styles.searchCount}>
          {filteredActivityStates.length} of {activityStates.length} activities
        </span>
      </div>

      <div className={styles.tableContainer}>
        <DataTable<ActivityState>
          data={filteredActivityStates}
          columns={columns}
          keyExtractor={(row) => row.id ?? `${row.processDefinitionId}-${row.version}-${row.activityId}`}
          loading={isLoading}
          loadingText="Loading activity states..."
          emptyText={
            searchTerm
              ? 'No activities match your search.'
              : 'No activity states configured.'
          }
          showCard={true}
          stickyHeader={false}
        />
      </div>

      <StandardModal
        isOpen={isAddOpen}
        onClose={() => setIsAddOpen(false)}
        title="Add Activity State"
        size="md"
        onConfirm={handleAddSubmit}
        confirmText="Add"
        confirmLoading={saving}
        confirmDisabled={!selectedProcessId || !activityId}
      >
        <div className={styles.addFormContainer}>
          <div className={styles.addFieldGroup}>
            <span className={styles.addFieldLabel}>Process Definition ID</span>
            <Combobox
              items={processOptions}
              value={selectedProcessId || undefined}
              onSelect={(value: string) => setSelectedProcessId(value)}
              placeholder="Select a process..."
              searchPlaceholder="Type to search processes..."
              emptyMessage="No processes found."
            />
          </div>

          <div className={styles.addFieldGroup}>
            <span className={styles.addFieldLabel}>Version</span>
            <Input
              type="number"
              min={1}
              value={version}
              onChange={(e) => setVersion(Number(e.target.value) || 1)}
              placeholder="1"
            />
          </div>

          <div className={styles.addFieldGroup}>
            <span className={styles.addFieldLabel}>Activity ID</span>
            <Input
              type="text"
              value={activityId}
              onChange={(e) => setActivityId(e.target.value)}
              placeholder="e.g. Activity_SendEmail"
            />
          </div>

          <div className={styles.addFieldGroup}>
            <span className={styles.addFieldLabel}>Description</span>
            <Input
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Optional description..."
            />
            <span className={styles.addHint}>
              Brief description of what this activity does.
            </span>
          </div>

          <div className={styles.addFieldGroup}>
            <span className={styles.addFieldLabel}>Initial State</span>
            <div className={styles.stateToggleGroup}>
              <button
                type="button"
                className={`${styles.stateOption} ${initialEnabled ? styles.stateOptionActiveSelected : ''}`}
                onClick={() => setInitialEnabled(true)}
              >
                <span className={`${styles.statusDot} ${styles.dotActive}`} />
                Active
              </button>
              <button
                type="button"
                className={`${styles.stateOption} ${!initialEnabled ? styles.stateOptionDisabledSelected : ''}`}
                onClick={() => setInitialEnabled(false)}
              >
                <span className={`${styles.statusDot} ${styles.dotDisabled}`} />
                Disabled
              </button>
            </div>
            {!initialEnabled && (
              <span className={styles.addWarning}>
                Adding as disabled means this activity will be skipped during process execution.
              </span>
            )}
          </div>
        </div>
      </StandardModal>

      <ConfirmationDialog
        open={confirmAdd}
        onOpenChange={(open) => {
          if (!open) setConfirmAdd(false);
        }}
        title="Add as Disabled"
        description={`You are adding activity "${activityId}" with state DISABLED. This activity will be skipped during process execution until manually enabled. Are you sure?`}
        onConfirm={handleConfirmDisabledAdd}
        confirmText="Yes, Add as Disabled"
        confirmVariant="destructive"
      />

      <ConfirmationDialog
        open={!!toggleTarget}
        onOpenChange={(open) => {
          if (!open) setToggleTarget(null);
        }}
        title={isDisablingAction ? 'Disable Activity' : 'Enable Activity'}
        description={
          isDisablingAction
            ? `Are you sure you want to disable activity "${toggleTarget?.activity.activityId}" in process "${toggleTarget?.activity.processDefinitionId}" (v${toggleTarget?.activity.version})? This activity will be skipped during execution.`
            : `Are you sure you want to enable activity "${toggleTarget?.activity.activityId}" in process "${toggleTarget?.activity.processDefinitionId}" (v${toggleTarget?.activity.version})? This will resume normal execution of this activity.`
        }
        onConfirm={handleConfirmToggle}
        confirmText={isDisablingAction ? 'Yes, Disable' : 'Yes, Enable'}
        confirmVariant={isDisablingAction ? 'destructive' : 'success'}
      />

      <ConfirmationDialog
        open={!!deleteTarget}
        onOpenChange={(open) => {
          if (!open) setDeleteTarget(null);
        }}
        title="Delete Activity State"
        description={`Are you sure you want to remove activity "${deleteTarget?.activityId}" from process "${deleteTarget?.processDefinitionId}" (v${deleteTarget?.version})? This action cannot be undone.`}
        onConfirm={handleConfirmDelete}
        confirmText="Yes, Delete"
        confirmVariant="destructive"
      />
    </div>
  );
};

export default React.memo(ActivityStatePanel);