import { StandardModal } from '@/shared/components';
import { isValidEmail } from '@/shared/utils';
import { Button } from '@/design-system/components/ui/button';
import { Input } from '@/design-system/components/ui/input';
import React, { forwardRef, useImperativeHandle, useMemo, useState } from 'react';
import { TASK_MESSAGES } from '../../constants/messages';
import { isTerminalTaskState } from '../../constants/taskState';
import { useAssignTask, useClaimTask, useUnclaimTask } from '../../hooks';
import styles from './TaskActionsToolbar.module.css';

export interface TaskActionsToolbarProps {
  taskId: string;
  assignee?: string | null;
  claimedBy?: string | null;
  currentUserEmail?: string;
  /** Task state — claim/unclaim hidden when completed or cancelled */
  state?: string;
  onActionComplete?: () => void;
}

export interface TaskActionsToolbarRef {
  openAssignDialog: () => void;
}

export const TaskActionsToolbar = forwardRef<TaskActionsToolbarRef, TaskActionsToolbarProps>(({
  taskId,
  claimedBy,
  currentUserEmail,
  state,
  onActionComplete,
}, ref) => {
  const claimTask = useClaimTask();
  const unclaimTask = useUnclaimTask();
  const assignTask = useAssignTask();

  const [assignDialogOpen, setAssignDialogOpen] = useState(false);
  const [assigneeEmail, setAssigneeEmail] = useState('');

  const isEmailInvalid = useMemo(() => {
    const trimmed = assigneeEmail.trim();
    return trimmed.length > 0 && !isValidEmail(trimmed);
  }, [assigneeEmail]);

  const canAssign = useMemo(() => {
    const trimmed = assigneeEmail.trim();
    return trimmed.length > 0 && isValidEmail(trimmed) && !assignTask.isPending;
  }, [assigneeEmail, assignTask.isPending]);

  const isTerminal = !!state && isTerminalTaskState(state);
  const normalizedClaimedBy = claimedBy?.toLowerCase().trim() || '';
  const normalizedCurrentUser = currentUserEmail?.toLowerCase().trim() || '';
  const isClaimed = !!normalizedClaimedBy && normalizedClaimedBy === normalizedCurrentUser;

  const handleClaim = async () => {
    try {
      await claimTask.mutateAsync(taskId);
      await onActionComplete?.();
    } catch {
      // Error handled by mutation hook
    }
  };

  const handleUnclaim = async () => {
    try {
      await unclaimTask.mutateAsync(taskId);
      await onActionComplete?.();
    } catch {
      // Error handled by mutation hook
    }
  };

  const handleAssignDialogOpen = () => {
    setAssigneeEmail('');
    setAssignDialogOpen(true);
  };

  const handleAssignConfirm = async () => {
    if (!canAssign) return;

    try {
      await assignTask.mutateAsync({ taskId, assignee: assigneeEmail.trim() });
      setAssignDialogOpen(false);
      await onActionComplete?.();
    } catch {
      // Error handled by mutation hook
    }
  };

  useImperativeHandle(ref, () => ({
    openAssignDialog: handleAssignDialogOpen,
  }));

  if (isTerminal) {
    return null;
  }

  return (
    <>
      <div className={styles.toolbar}>
        {!isClaimed ? (
          <Button
            onClick={handleClaim}
            disabled={claimTask.isPending}
            variant="primary"
            size="sm"
            label={claimTask.isPending ? 'Claiming...' : 'Claim Task'}
          />
        ) : (
          <Button
            onClick={handleUnclaim}
            disabled={unclaimTask.isPending}
            variant="ghost"
            size="sm"
            label={unclaimTask.isPending ? 'Releasing...' : 'Unclaim Task'}
          />
        )}
      </div>

      <StandardModal
        isOpen={assignDialogOpen}
        onClose={() => setAssignDialogOpen(false)}
        title="Assign Task"
        size="md"
        confirmText="Assign"
        cancelText="Cancel"
        onConfirm={handleAssignConfirm}
        confirmDisabled={!canAssign}
        confirmLoading={assignTask.isPending}
      >
        <div className={styles.assignForm}>
          <Input
            size="sm"
            labelText="Assignee Email"
            placeholderText="user@example.com"
            value={assigneeEmail}
            mode={isEmailInvalid ? 'error' : 'standard'}
            supportMessageProps={isEmailInvalid ? { helperText: 'Please enter a valid email address', mode: 'error' } : undefined}
            inputProps={{
              id: "assignee-email",
              type: "email",
              onChange: (e: React.ChangeEvent<HTMLInputElement>) => {
                setAssigneeEmail(e.target.value);
              },
              onKeyDown: (e: React.KeyboardEvent<HTMLInputElement>) => {
                if (e.key === 'Enter' && canAssign) {
                  e.preventDefault();
                  handleAssignConfirm();
                }
              }
            }}
          />
          <p className={styles.assignHint}>
            {TASK_MESSAGES.ASSIGN.ASSIGN_HINT}
          </p>
        </div>
      </StandardModal>
    </>
  );
});

TaskActionsToolbar.displayName = 'TaskActionsToolbar';
