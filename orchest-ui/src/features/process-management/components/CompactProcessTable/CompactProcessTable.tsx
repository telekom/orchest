import { processInstanceService } from "@/api/domains";
import { incidentService } from "@/api/domains/incidents";
import { Combobox } from "@/design-system/components/ui/combobox";
import { Input } from "@/design-system/components/ui/input";
import { Textarea } from "@/design-system/components/ui/textarea";
import { toast } from "@/design-system/components/ui/sonner";
import { useProcessDefinitions } from "@/features/process-management/hooks/useProcessDefinitions";
import {
  COMPENSATION_FLOW_VERSION,
  getCompensationProcessDefinitionIds,
  parseCompensateVariables,
} from "@/features/process-management/utils/compensateVariables";
import { InstancePreviewCard } from "../InstancePreviewCard/InstancePreviewCard";
import { DataTable, DateCell, InstanceStatusIndicator, type Column, type PaginationConfig, type RowSelectionConfig } from "@/shared/components";
import { ActionIconButton } from "@/shared/components/ActionIconButton/ActionIconButton";
import { ConfirmationDialog } from "@/shared/components/ConfirmationDialog/ConfirmationDialog";
import { StandardModal } from "@/shared/components/StandardModal/StandardModal";
import type { InfiniteScrollConfig, SortConfig } from "@/shared/components/DataTable/types";
import { SmartText } from "@/shared/components/SmartText/SmartText";
import { PAGINATION } from "@/shared/constants";
import { isTerminalProcessStatus } from "@/shared/constants/status";
import { ProcessInstanceState } from "@/shared/enums";
import { createUniqueOptions } from "@/shared/hooks";
import commonStyles from "@/shared/styles/common.module.css";
import { ProcessInstance } from "@/shared/types";
import { logger } from "@/shared/utils/logger";
import React, { useCallback, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CancelInstanceButton, RaiseIncidentButton, RetryInstanceButton } from "../ui/InstanceActionButtons";
import styles from "./CompactProcessTable.module.css";

interface CompactProcessTableProps {
  instances: ProcessInstance[];
  totalElements: number;
  totalPages?: number;
  loading: boolean;
  isPaginationLoading?: boolean;
  pageNumber?: number;
  handlePageChange?: (direction: 'next' | 'prev' | number) => void;
  handlePageSizeChange?: (val: number) => void;
  pageSize?: number;
  showActions?: boolean;
  onTableRefresh?: () => void;
  selectedProcessId?: string;
  sortConfig?: SortConfig | SortConfig[];
  onSortChange?: (sort: SortConfig[]) => void;
  /**
   * When provided, the table renders the infinite-scroll footer instead of the
   * paginated controls. Mutually exclusive with the `handlePageChange` /
   * `handlePageSizeChange` props.
   */
  infiniteScroll?: InfiniteScrollConfig;
}

const RETRYABLE_STATUSES = [
  ProcessInstanceState.INCIDENT,
  ProcessInstanceState.HOLD,
  ProcessInstanceState.FAILED,
] as const;

const isRetryableStatus = (status: string): boolean =>
  RETRYABLE_STATUSES.some(s => s.toLowerCase() === status?.toLowerCase());

const DEFAULT_VARIABLES_JSON = '{}';

const CompactProcessTable = ({
  instances,
  loading,
  isPaginationLoading = false,
  pageNumber = 0,
  totalElements,
  totalPages = 0,
  handlePageChange,
  handlePageSizeChange,
  pageSize = PAGINATION.ITEMS_PER_PAGE,
  showActions = false,
  onTableRefresh,
  selectedProcessId,
  sortConfig,
  onSortChange,
  infiniteScroll,
}: CompactProcessTableProps) => {
  const navigate = useNavigate();
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);
  const [isBatchRetrying, setIsBatchRetrying] = useState(false);
  const [isBatchCanceling, setIsBatchCanceling] = useState(false);
  const [isBatchRaisingIncident, setIsBatchRaisingIncident] = useState(false);
  const [isBatchCompensating, setIsBatchCompensating] = useState(false);
  const [showBatchRetryConfirm, setShowBatchRetryConfirm] = useState(false);
  const [showBatchCancelConfirm, setShowBatchCancelConfirm] = useState(false);
  const [showBatchRaiseIncident, setShowBatchRaiseIncident] = useState(false);
  const [showBatchCompensate, setShowBatchCompensate] = useState(false);
  const [batchIncidentMessage, setBatchIncidentMessage] = useState('');
  const [batchCompensateProcessId, setBatchCompensateProcessId] = useState('');
  const [batchCompensateVariablesJson, setBatchCompensateVariablesJson] = useState(DEFAULT_VARIABLES_JSON);
  const [batchCompensateVariablesError, setBatchCompensateVariablesError] = useState<string | null>(null);

  const { processDefinitions, isLoading: definitionsLoading } = useProcessDefinitions();

  const selectedInstances = useMemo(() =>
    instances.filter(inst => selectedRowKeys.includes(inst.processId)),
    [instances, selectedRowKeys]
  );

  const retryableInstances = useMemo(() =>
    selectedInstances.filter(inst => isRetryableStatus(inst.status)),
    [selectedInstances]
  );

  const cancelableInstances = useMemo(() =>
    selectedInstances.filter(inst => !isTerminalProcessStatus(inst.status)),
    [selectedInstances]
  );

  const incidentableInstances = useMemo(() =>
    selectedInstances.filter(inst => !isTerminalProcessStatus(inst.status) && inst.status?.toLowerCase() !== 'incident'),
    [selectedInstances]
  );

  const compensatableInstances = useMemo(() =>
    selectedInstances.filter(inst => inst.status?.toLowerCase() === 'incident'),
    [selectedInstances]
  );

  const compensateProcessOptions = useMemo(() => {
    const compensationIds = getCompensationProcessDefinitionIds(processDefinitions);
    return createUniqueOptions(processDefinitions ?? [], 'definitionId')
      .filter((opt) => compensationIds.has(opt.value))
      .map((opt) => ({
        label: opt.label,
        value: opt.value,
      }));
  }, [processDefinitions]);

  const handleParentProcessClick = useCallback(async (e: React.MouseEvent, parentProcessId: string) => {
    e.stopPropagation();

    // Let the browser handle new-tab / modified clicks on the real href
    if (e.metaKey || e.ctrlKey || e.shiftKey || e.altKey || e.button !== 0) {
      return;
    }

    e.preventDefault();

    try {
      await processInstanceService.getProcessInstance(parentProcessId);
      navigate(`/processes/${parentProcessId}`);
    } catch (error) {
      logger.error('Parent process not found:', error);
      toast.error(`Process instance "${parentProcessId}" not found`);
    }
  }, [navigate]);

  const handleBatchOperation = useCallback(async (
    operation: 'retry' | 'cancel',
    instanceIds: string[],
    setLoading: (loading: boolean) => void,
    setDialog: (open: boolean) => void
  ) => {
    if (instanceIds.length === 0) {
      toast.error(`No ${operation === 'retry' ? 'retryable' : 'cancelable'} instances selected`);
      return;
    }

    setDialog(false);
    setLoading(true);

    try {
      if (operation === 'retry') {
        await processInstanceService.retryBatch({ processInstanceIds: instanceIds });
      } else {
        await processInstanceService.cancelBatch({ processInstanceIds: instanceIds });
      }
      toast.success(`Successfully ${operation === 'retry' ? 'retried' : 'canceled'} ${instanceIds.length} instance(s)`);
      setSelectedRowKeys([]);
      onTableRefresh?.();
    } catch (error) {
      logger.error(`Failed to ${operation} instances:`, error);
      toast.error(`Failed to ${operation} instances. Please try again.`);
    } finally {
      setLoading(false);
    }
  }, [onTableRefresh]);

  const handleConfirmBatchRetry = useCallback(async () => {
    await handleBatchOperation(
      'retry',
      retryableInstances.map(inst => inst.processId),
      setIsBatchRetrying,
      setShowBatchRetryConfirm
    );
  }, [retryableInstances, handleBatchOperation]);

  const handleConfirmBatchCancel = useCallback(async () => {
    await handleBatchOperation(
      'cancel',
      cancelableInstances.map(inst => inst.processId),
      setIsBatchCanceling,
      setShowBatchCancelConfirm
    );
  }, [cancelableInstances, handleBatchOperation]);

  const handleConfirmBatchRaiseIncident = useCallback(async () => {
    if (!batchIncidentMessage.trim()) return;

    setIsBatchRaisingIncident(true);
    setShowBatchRaiseIncident(false);

    try {
      await incidentService.raiseBatch({
        processInstanceIds: incidentableInstances.map(inst => inst.processId),
        incidentMessage: batchIncidentMessage.trim(),
      });
      toast.success(`Incident raised on ${incidentableInstances.length} instance(s)`);
      setSelectedRowKeys([]);
      setBatchIncidentMessage('');
      onTableRefresh?.();
    } catch (error) {
      logger.error('Failed to raise incidents:', error);
      toast.error('Failed to raise incidents. Please try again.');
    } finally {
      setIsBatchRaisingIncident(false);
    }
  }, [incidentableInstances, batchIncidentMessage, onTableRefresh]);

  const resetBatchCompensateForm = useCallback(() => {
    setBatchCompensateProcessId('');
    setBatchCompensateVariablesJson(DEFAULT_VARIABLES_JSON);
    setBatchCompensateVariablesError(null);
  }, []);

  const handleCloseBatchCompensate = useCallback(() => {
    setShowBatchCompensate(false);
    resetBatchCompensateForm();
  }, [resetBatchCompensateForm]);

  const handleConfirmBatchCompensate = useCallback(async () => {
    if (!batchCompensateProcessId || compensatableInstances.length === 0) return;

    const parsedVariables = parseCompensateVariables(batchCompensateVariablesJson);
    if (!parsedVariables.ok) {
      setBatchCompensateVariablesError(parsedVariables.error);
      return;
    }
    setBatchCompensateVariablesError(null);

    setIsBatchCompensating(true);

    const request: {
      processInstanceIds: string[];
      processDefinitionId: string;
      version: number;
      variables?: Record<string, unknown>;
    } = {
      processInstanceIds: compensatableInstances.map(inst => inst.processId),
      processDefinitionId: batchCompensateProcessId,
      version: COMPENSATION_FLOW_VERSION,
    };

    if (parsedVariables.variables) {
      request.variables = parsedVariables.variables;
    }

    try {
      await processInstanceService.compensateBatch(request);
      toast.success(`Successfully compensated ${compensatableInstances.length} instance(s)`);
      setSelectedRowKeys([]);
      handleCloseBatchCompensate();
      onTableRefresh?.();
    } catch (error) {
      logger.error('Failed to compensate instances:', error);
      toast.error('Failed to compensate instances. Please try again.');
    } finally {
      setIsBatchCompensating(false);
    }
  }, [
    batchCompensateProcessId,
    batchCompensateVariablesJson,
    compensatableInstances,
    handleCloseBatchCompensate,
    onTableRefresh,
  ]);
  const columns: Column<ProcessInstance>[] = useMemo(() => [
    {
      key: "processId",
      header: "Instance ID",
      render: (row) => (
        <span className={styles.idCell}>
          <InstanceStatusIndicator status={row.status} size="sm" />
          <SmartText text={row.processId} className={styles.textBodySm} maxWidth="100%" showTooltip />
        </span>
      ),
      className: `text-table-cell ${styles.columnTextPrimary}`,
      sortable: true,
      sortKey: "processInstanceId",
    },
    {
      key: "processName",
      header: "Process Name",
      render: (row) => <SmartText text={row.processName} className={styles.textBodySm} maxWidth="100%" showTooltip />,
      className: `text-table-cell ${styles.columnTextPrimary}`,
      sortable: true,
      sortKey: "processDefinitionId",
    },
    {
      key: "version",
      header: "Version",
      render: (row) => <SmartText text={row.processVersion} className={commonStyles.textPrimary} maxWidth="100%" showTooltip />,
      className: `text-table-cell ${commonStyles.textPrimary}`,
      align: "center",
      sortable: true,
      sortKey: "version",
    },
    {
      key: "parentProcessId",
      header: "Parent ID",
      render: (row) => row.parentProcessId ? (
        <a
          href={`/processes/${row.parentProcessId}`}
          className={commonStyles.link}
          onClick={(e) => handleParentProcessClick(e, row.parentProcessId)}
        >
          <SmartText text={row.parentProcessId} maxWidth="100%" showTooltip />
        </a>
      ) : (
        <span className={styles.emptyText}>-</span>
      ),
      className: `text-table-cell ${styles.columnTextSecondary}`,
      hidden: styles.hiddenXl,
    },
    {
      key: "startDate",
      header: "Started",
      render: (row) => <DateCell date={row.startDate} label="Start Date" format="compact" />,
      className: styles.columnTextMuted,
      hidden: styles.hiddenLg,
      sortable: true,
      sortKey: "createdAt",
    },
    {
      key: "endDate",
      header: "Ended",
      render: (row) => <DateCell date={row.endDate} label="End Date" format="compact" />,
      className: styles.columnTextMuted,
      hidden: styles.hiddenLg,
      sortable: true,
      sortKey: "completedAt",
    },
  ], [handleParentProcessClick]);

  const renderActions = showActions ? (row: ProcessInstance) => (
    <div className={styles.actions} onClick={(e) => e.stopPropagation()}>
      {isRetryableStatus(row.status) && (
        <RetryInstanceButton
          processInstanceId={row.processId}
          sequenceExecutions={row.sequenceExecutions}
          processInstanceState={row.status}
          onSuccess={onTableRefresh}
          loadProcessDetails
        />
      )}

      {!isTerminalProcessStatus(row.status) && (
        <CancelInstanceButton
          processInstanceId={row.processId}
          processDefinitionId={row.processName}
          onSuccess={onTableRefresh}
        />
      )}

      {!isTerminalProcessStatus(row.status) && row.status?.toLowerCase() !== 'incident' && (
        <RaiseIncidentButton
          processInstanceId={row.processId}
          onSuccess={onTableRefresh}
        />
      )}

      <InstancePreviewCard processInstanceId={row.processId}>
        <ActionIconButton
          icon="visibility-on"
          to={`/processes/${row.processId}`}
          title="View Instance Details"
        />
      </InstancePreviewCard>
    </div>
  ) : undefined;

  const getRowHref = useCallback(
    (row: ProcessInstance) => `/processes/${row.processId}`,
    []
  );

  const paginationConfig: PaginationConfig | undefined =
    !infiniteScroll && handlePageChange && handlePageSizeChange
      ? {
          currentPage: pageNumber,
          totalPages,
          totalElements,
          pageSize,
          pageSizeOptions: PAGINATION.PAGE_SIZE_OPTIONS,
          onPageChange: handlePageChange,
          onPageSizeChange: handlePageSizeChange,
        }
      : undefined;

  const isBatchBusy = isBatchRetrying || isBatchCanceling || isBatchRaisingIncident || isBatchCompensating;

  const headerActions = selectedRowKeys.length > 0 ? (
    <div className={styles.batchActionsHeader}>
      <ActionIconButton
        icon="refresh"
        onClick={() => retryableInstances.length > 0 && setShowBatchRetryConfirm(true)}
        title={`Retry ${retryableInstances.length} instance${retryableInstances.length === 1 ? '' : 's'}`}
        disabled={isBatchBusy || retryableInstances.length === 0}
        variant="success"
      />
      <ActionIconButton
        icon="rewind"
        onClick={() => compensatableInstances.length > 0 && setShowBatchCompensate(true)}
        title={`Compensate ${compensatableInstances.length} instance${compensatableInstances.length === 1 ? '' : 's'}`}
        disabled={isBatchBusy || compensatableInstances.length === 0}
        variant="info"
      />
      <ActionIconButton
        icon="alert-triangle"
        onClick={() => incidentableInstances.length > 0 && setShowBatchRaiseIncident(true)}
        title={`Raise incident on ${incidentableInstances.length} instance${incidentableInstances.length === 1 ? '' : 's'}`}
        disabled={isBatchBusy || incidentableInstances.length === 0}
        variant="warning"
      />
      <span className={styles.selectedCount}>{selectedRowKeys.length}</span>
      <ActionIconButton
        icon="circle-close"
        onClick={() => cancelableInstances.length > 0 && setShowBatchCancelConfirm(true)}
        title={`Cancel ${cancelableInstances.length} instance${cancelableInstances.length === 1 ? '' : 's'}`}
        disabled={isBatchBusy || cancelableInstances.length === 0}
        variant="danger"
      />
    </div>
  ) : undefined;

  const rowSelectionConfig: RowSelectionConfig<ProcessInstance> = {
    selectedRowKeys,
    onSelectionChange: setSelectedRowKeys,
    headerActions,
  };

  return (
    <>
      <DataTable
        data={instances}
        columns={columns}
        keyExtractor={(row) => row.processId}
        loading={loading}
        isPaginationLoading={isPaginationLoading}
        loadingText="Loading..."
        emptyText="No process instances found with the current filters."
        getRowHref={getRowHref}
        selectedRowKey={selectedProcessId}
        stickyHeader
        maxHeight="100%"
        showCard
        pagination={paginationConfig}
        infiniteScroll={infiniteScroll}
        sortConfig={sortConfig}
        onSortChange={onSortChange}
        rowSelection={rowSelectionConfig}
        renderActions={renderActions}
        actionsHeader="Action"
      />

      <ConfirmationDialog
        open={showBatchRetryConfirm}
        onOpenChange={setShowBatchRetryConfirm}
        onConfirm={handleConfirmBatchRetry}
        title="Retry Selected Instances"
        description={`Are you sure you want to retry ${retryableInstances.length} process instance${retryableInstances.length === 1 ? '' : 's'}? This action cannot be undone.`}
        confirmText="Retry"
        cancelText="Cancel"
        affectedIds={retryableInstances.map(inst => inst.processId)}
      />

      <ConfirmationDialog
        open={showBatchCancelConfirm}
        onOpenChange={setShowBatchCancelConfirm}
        onConfirm={handleConfirmBatchCancel}
        title="Cancel Selected Instances"
        description={`Are you sure you want to cancel ${cancelableInstances.length} process instance${cancelableInstances.length === 1 ? '' : 's'}? This action cannot be undone.`}
        confirmText="Cancel Instances"
        cancelText="Go Back"
        confirmVariant="primary"
        affectedIds={cancelableInstances.map(inst => inst.processId)}
      />

      <StandardModal
        isOpen={showBatchRaiseIncident}
        onClose={() => { setShowBatchRaiseIncident(false); setBatchIncidentMessage(''); }}
        title={`Raise Incident on ${incidentableInstances.length} Instance${incidentableInstances.length === 1 ? '' : 's'}`}
        size="md"
        onConfirm={handleConfirmBatchRaiseIncident}
        confirmText={isBatchRaisingIncident ? 'Raising...' : 'Raise Incident'}
        confirmLoading={isBatchRaisingIncident}
        confirmDisabled={!batchIncidentMessage.trim()}
        confirmVariant="destructive"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          <span style={{ fontSize: '0.8rem', color: 'hsl(var(--muted-foreground))' }}>
            This will mark {incidentableInstances.length} process instance{incidentableInstances.length === 1 ? '' : 's'} as having an incident.
          </span>
          <Input
            type="text"
            placeholder="Describe the incident..."
            value={batchIncidentMessage}
            onChange={(e) => setBatchIncidentMessage(e.target.value)}
            autoFocus
          />
        </div>
      </StandardModal>

      <StandardModal
        isOpen={showBatchCompensate}
        onClose={handleCloseBatchCompensate}
        title={`Compensate ${compensatableInstances.length} Instance${compensatableInstances.length === 1 ? '' : 's'}`}
        description="Select a process definition to start as compensation for the selected incident instances."
        size="md"
        onConfirm={handleConfirmBatchCompensate}
        confirmText={isBatchCompensating ? 'Compensating...' : 'Compensate'}
        confirmLoading={isBatchCompensating}
        confirmDisabled={!batchCompensateProcessId || isBatchCompensating}
        confirmVariant="primary"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          <Combobox
            items={compensateProcessOptions}
            value={batchCompensateProcessId || undefined}
            onSelect={setBatchCompensateProcessId}
            placeholder="Select process definition"
            disabled={definitionsLoading}
            loading={definitionsLoading}
            emptyMessage="No compensation processes found"
            filterOptions
          />
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
            <span style={{ fontSize: '0.8rem', color: 'hsl(var(--muted-foreground))' }}>
              Variables (optional JSON object)
            </span>
            <Textarea
              value={batchCompensateVariablesJson}
              onChange={(e) => {
                setBatchCompensateVariablesJson(e.target.value);
                if (batchCompensateVariablesError) setBatchCompensateVariablesError(null);
              }}
              placeholder='{ "key": "value" }'
              rows={5}
              style={{ fontFamily: 'ui-monospace, SFMono-Regular, Menlo, monospace', fontSize: '0.8rem' }}
            />
            {batchCompensateVariablesError && (
              <span style={{ fontSize: '0.75rem', color: 'hsl(var(--destructive))' }}>
                {batchCompensateVariablesError}
              </span>
            )}
          </div>
        </div>
      </StandardModal>
    </>
  );
};

export default CompactProcessTable;
