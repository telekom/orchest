import { processInstanceService } from "@/api";
import { incidentService } from "@/api/domains/incidents";
import { SequenceExecution } from "@/api/types/orchest-api";
import { Combobox } from "@/design-system/components/ui/combobox";
import { Input } from "@/design-system/components/ui/input";
import { Textarea } from "@/design-system/components/ui/textarea";
import { toast } from "@/design-system/components/ui/sonner";
import { useInstanceActions } from "@/features/process-management/hooks";
import { useProcessDefinitions } from "@/features/process-management/hooks/useProcessDefinitions";
import {
  COMPENSATION_FLOW_VERSION,
  getCompensationProcessDefinitionIds,
  parseCompensateVariables,
} from "@/features/process-management/utils/compensateVariables";
import { UserRoles } from "@/shared/auth";
import { ActionIconButton } from "@/shared/components/ActionIconButton/ActionIconButton";
import { ConfirmationDialog } from "@/shared/components/ConfirmationDialog/ConfirmationDialog";
import { StandardModal } from "@/shared/components/StandardModal/StandardModal";
import { TIMING } from "@/shared/constants";
import { ProcessInstanceState } from "@/shared/enums";
import { createUniqueOptions, useRoleBasedAction } from "@/shared/hooks";
import { closeDialogWithDelay, logger } from "@/shared/utils";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router";

interface CancelInstanceButtonProps {
  processInstanceId: string;
  processDefinitionId: string;
  onSuccess?: () => void;
  className?: string;
}

interface RetryInstanceButtonProps {
  processInstanceId: string;
  sequenceExecutions?: Record<string, SequenceExecution>;
  processInstanceState?: string;
  onSuccess?: () => void;
  className?: string;
  loadProcessDetails?: boolean;
}

const RETRYABLE_STATES: readonly ProcessInstanceState[] = [
  ProcessInstanceState.INCIDENT,
  ProcessInstanceState.FAILED,
];

export const CancelInstanceButton = ({
  processInstanceId,
  processDefinitionId,
  onSuccess,
  className,
}: CancelInstanceButtonProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const { canPerformAction } = useRoleBasedAction(UserRoles.ADMIN);
  const { cancelInstance } = useInstanceActions();

  const handleCancelInstance = async (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    e.stopPropagation();

    await closeDialogWithDelay(setIsOpen);

    cancelInstance.mutate(
      {
        instances: [{ instanceId: processInstanceId, processDefinitionId }],
      },
      {
        onSuccess: () => {
          onSuccess?.();
        },
      }
    );
  };

  if (!canPerformAction) return null;

  return (
    <ConfirmationDialog
      open={isOpen}
      onOpenChange={setIsOpen}
      title="Cancel Process Instance"
      description="This action cannot be undone. This will permanently stop/cancel the current instance."
      onConfirm={handleCancelInstance}
      confirmText="Cancel Instance"
      cancelText="Go Back"
      confirmVariant="primary"
      affectedIds={[processInstanceId]}
      trigger={
        <ActionIconButton
          icon="circle-close"
          onClick={() => setIsOpen(true)}
          title="Cancel Instance"
          className={className}
          variant="danger"
        />
      }
    />
  );
};

export const RetryInstanceButton = ({
  processInstanceId,
  sequenceExecutions: initialSequenceExecutions,
  processInstanceState,
  onSuccess,
  className,
  loadProcessDetails = false,
}: RetryInstanceButtonProps) => {
  const navigate = useNavigate();
  const { canPerformAction } = useRoleBasedAction(UserRoles.ADMIN);
  const { retryInstance } = useInstanceActions();
  const [isOpen, setIsOpen] = useState(false);
  const [sequenceExecutions, setSequenceExecutions] = useState(initialSequenceExecutions);
  const [currentProcessState, setCurrentProcessState] = useState(processInstanceState);

  const loadDetails = async () => {
    if (!loadProcessDetails) {
      return {
        success: !!sequenceExecutions && Object.keys(sequenceExecutions).length > 0,
        state: currentProcessState,
        executions: sequenceExecutions,
      };
    }

    try {
      const processDetails = await processInstanceService.getProcessInstance(processInstanceId);
      if (!processDetails?.sequenceExecutions) {
        toast.error("Failed to load process details");
        return { success: false };
      }

      setSequenceExecutions(processDetails.sequenceExecutions);
      setCurrentProcessState(processDetails.state);
      return {
        success: true,
        state: processDetails.state,
        executions: processDetails.sequenceExecutions,
      };
    } catch (error) {
      toast.error(`Error loading process details: ${error instanceof Error ? error.message : "Unknown error"}`);
      return { success: false };
    }
  };

  const handleTriggerClick = async () => {
    if (!loadProcessDetails) {
      setIsOpen(true);
      return;
    }

    const details = await loadDetails();
    if (details.success) {
      setIsOpen(true);
    }
  };

  const retryJobs = async (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    e.stopPropagation();

    const executions = sequenceExecutions;

    if (!executions || Object.keys(executions).length === 0) {
      await closeDialogWithDelay(setIsOpen);
      toast.error("No sequence execution data available");
      return;
    }

    // Walk executions from last to first, find the first one whose
    // current state is INCIDENT or FAILED — retry only that one.
    const executionList = Object.values(executions);
    let executionToRetry: SequenceExecution | null = null;

    for (let i = executionList.length - 1; i >= 0; i--) {
      const state = executionList[i].state?.toUpperCase() as ProcessInstanceState;
      if (RETRYABLE_STATES.includes(state)) {
        executionToRetry = executionList[i];
        break;
      }
    }

    if (!executionToRetry) {
      executionToRetry = executionList[executionList.length - 1];
    }

    if (!executionToRetry.nodeId) {
      await closeDialogWithDelay(setIsOpen);
      toast.error("Missing node ID for the retryable activity");
      return;
    }

    await closeDialogWithDelay(setIsOpen);

    try {
      await new Promise<void>((resolve, reject) => {
        retryInstance.mutate(
          {
            processInstanceId,
            activityId: executionToRetry.nodeId,
            previousActivityId: executionToRetry.sourceNodeId || "",
          },
          {
            onSuccess: () => resolve(),
            onError: (error) => reject(error),
          }
        );
      });

      if (onSuccess) {
        onSuccess();
      } else {
        setTimeout(() => navigate(0), TIMING.QUICK_REDIRECT_DELAY);
      }
    } catch (error) {
      logger.error("Retry operation failed:", error);
    }
  };

  if (!canPerformAction) return null;

  return (
    <ConfirmationDialog
      open={isOpen}
      onOpenChange={setIsOpen}
      title="Retry Process Instance"
      description="This action cannot be undone. This will retry the instance from the failed activity."
      onConfirm={retryJobs}
      confirmText="Retry"
      cancelText="Cancel"
      confirmVariant="primary"
      cancelVariant="outline"
      affectedIds={[processInstanceId]}
      trigger={
        <ActionIconButton
          icon="refresh"
          onClick={handleTriggerClick}
          title="Retry Instance"
          className={className}
          variant="success"
        />
      }
    />
  );
};

interface RaiseIncidentButtonProps {
  processInstanceId: string;
  onSuccess?: () => void;
  className?: string;
}

export const RaiseIncidentButton = ({
  processInstanceId,
  onSuccess,
  className,
}: RaiseIncidentButtonProps) => {
  const { canPerformAction } = useRoleBasedAction(UserRoles.ADMIN);
  const [isOpen, setIsOpen] = useState(false);
  const [incidentMessage, setIncidentMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleRaise = async () => {
    if (!incidentMessage.trim()) return;
    setIsSubmitting(true);
    try {
      await incidentService.raise({ processInstanceId, incidentMessage: incidentMessage.trim() });
      toast.success('Incident raised successfully');
      setIsOpen(false);
      setIncidentMessage('');
      onSuccess?.();
    } catch (error) {
      toast.error(`Failed to raise incident: ${error instanceof Error ? error.message : 'Unknown error'}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!canPerformAction) return null;

  return (
    <>
      <ActionIconButton
        icon="alert-triangle"
        onClick={() => setIsOpen(true)}
        title="Raise Incident"
        className={className}
        variant="warning"
      />
      <StandardModal
        isOpen={isOpen}
        onClose={() => { setIsOpen(false); setIncidentMessage(''); }}
        title="Raise Incident"
        size="md"
        onConfirm={handleRaise}
        confirmText={isSubmitting ? 'Raising...' : 'Raise Incident'}
        confirmLoading={isSubmitting}
        confirmDisabled={!incidentMessage.trim()}
        confirmVariant="destructive"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          <span style={{ fontSize: '0.8rem', color: 'hsl(var(--muted-foreground))' }}>
            This will mark the process instance as having an incident.
          </span>
          <Input
            type="text"
            placeholder="Describe the incident..."
            value={incidentMessage}
            onChange={(e) => setIncidentMessage(e.target.value)}
            autoFocus
          />
        </div>
      </StandardModal>
    </>
  );
};

const DEFAULT_VARIABLES_JSON = '{}';

interface CompensateInstanceButtonProps {
  processInstanceId: string;
  onSuccess?: () => void;
  className?: string;
}

export const CompensateInstanceButton = ({
  processInstanceId,
  onSuccess,
  className,
}: CompensateInstanceButtonProps) => {
  const navigate = useNavigate();
  const { canPerformAction } = useRoleBasedAction(UserRoles.ADMIN);
  const { compensateInstance } = useInstanceActions();
  const { processDefinitions, isLoading: definitionsLoading } = useProcessDefinitions();

  const [isOpen, setIsOpen] = useState(false);
  const [selectedProcessId, setSelectedProcessId] = useState('');
  const [variablesJson, setVariablesJson] = useState(DEFAULT_VARIABLES_JSON);
  const [variablesError, setVariablesError] = useState<string | null>(null);

  const processOptions = useMemo(() => {
    const compensationIds = getCompensationProcessDefinitionIds(processDefinitions);
    return createUniqueOptions(processDefinitions ?? [], 'definitionId')
      .filter((opt) => compensationIds.has(opt.value))
      .map((opt) => ({
        label: opt.label,
        value: opt.value,
      }));
  }, [processDefinitions]);

  const resetForm = () => {
    setSelectedProcessId('');
    setVariablesJson(DEFAULT_VARIABLES_JSON);
    setVariablesError(null);
  };

  const handleClose = () => {
    setIsOpen(false);
    resetForm();
  };

  const handleProcessSelect = (value: string) => {
    setSelectedProcessId(value);
  };

  const handleCompensate = () => {
    if (!selectedProcessId) return;

    const parsedVariables = parseCompensateVariables(variablesJson);
    if (!parsedVariables.ok) {
      setVariablesError(parsedVariables.error);
      return;
    }
    setVariablesError(null);

    const request: {
      processInstanceId: string;
      processDefinitionId: string;
      version: number;
      variables?: Record<string, unknown>;
    } = {
      processInstanceId,
      processDefinitionId: selectedProcessId,
      version: COMPENSATION_FLOW_VERSION,
    };

    if (parsedVariables.variables) {
      request.variables = parsedVariables.variables;
    }

    compensateInstance.mutate(request, {
      onSuccess: (response) => {
        const compensatedId = response?.compensatedInstanceId;
        toast.success('Process compensated successfully', {
          action: compensatedId
            ? {
                label: 'View instance',
                onClick: () => navigate(`/processes/${compensatedId}`),
              }
            : undefined,
        });
        handleClose();
        onSuccess?.();
      },
    });
  };

  if (!canPerformAction) return null;

  return (
    <>
      <ActionIconButton
        icon="rewind"
        onClick={() => setIsOpen(true)}
        title="Compensate"
        className={className}
        variant="info"
      />
      <StandardModal
        isOpen={isOpen}
        onClose={handleClose}
        title="Compensate Process Instance"
        description="Select a process definition to start as compensation for this incident instance."
        size="md"
        onConfirm={handleCompensate}
        confirmText={compensateInstance.isPending ? 'Compensating...' : 'Compensate'}
        confirmLoading={compensateInstance.isPending}
        confirmDisabled={!selectedProcessId || compensateInstance.isPending}
        confirmVariant="primary"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          <Combobox
            items={processOptions}
            value={selectedProcessId || undefined}
            onSelect={handleProcessSelect}
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
              value={variablesJson}
              onChange={(e) => {
                setVariablesJson(e.target.value);
                if (variablesError) setVariablesError(null);
              }}
              placeholder='{ "key": "value" }'
              rows={5}
              style={{ fontFamily: 'ui-monospace, SFMono-Regular, Menlo, monospace', fontSize: '0.8rem' }}
            />
            {variablesError && (
              <span style={{ fontSize: '0.75rem', color: 'hsl(var(--destructive))' }}>
                {variablesError}
              </span>
            )}
          </div>
        </div>
      </StandardModal>
    </>
  );
};
