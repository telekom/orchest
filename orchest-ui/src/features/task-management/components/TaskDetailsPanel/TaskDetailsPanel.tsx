import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/design-system/components/ui/tabs';
import { BackButton, InstanceSummary, StatusBadge, SummaryField } from '@/shared/components';
import { PriorityBadge } from '@/shared/components/PriorityBadge/PriorityBadge';
import { useCopyToClipboard } from '@/shared/hooks/useCopyToClipboard';
import commonStyles from '@/shared/styles/common.module.css';
import { Button } from '@/design-system/components/ui/button';
import { formatDistanceToNow } from 'date-fns';
import { CheckSquare, FileText } from 'lucide-react';
import React, { useMemo, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { TASK_MESSAGES } from '../../constants/messages';
import { isTerminalTaskState } from '../../constants/taskState';
import { useCompleteTask, useUserTask } from '../../hooks';
import { TaskActionsToolbar, TaskActionsToolbarRef } from '../TaskActionsToolbar/TaskActionsToolbar';
import { TaskFormView } from '../TaskFormView/TaskFormView';
import { TaskProcessDiagram } from '../TaskProcessDiagram/TaskProcessDiagram';
import styles from './TaskDetailsPanel.module.css';

export interface TaskDetailsPanelProps {
  taskId: string;
  currentUserEmail?: string;
  onComplete?: () => void;
}

const normalizeEmail = (email?: string | null) => email?.toLowerCase().trim() || '';

export const TaskDetailsPanel: React.FC<TaskDetailsPanelProps> = ({
  taskId,
  currentUserEmail,
  onComplete,
}) => {
  const navigate = useNavigate();
  const { data: task, isLoading, error, refetch } = useUserTask(taskId);
  const completeTask = useCompleteTask();
  const { copyToClipboard, copiedItems } = useCopyToClipboard();
  const [taskVariables, setTaskVariables] = useState<Record<string, unknown>>(task?.variables || {});
  const toolbarRef = useRef<TaskActionsToolbarRef>(null);

  React.useEffect(() => {
    if (task?.variables) {
      setTaskVariables(task.variables);
    }
  }, [task?.taskId, task?.variables]);

  const isFormValid = useMemo(() => {
    if (!taskVariables || Object.keys(taskVariables).length === 0) return true;
    return Object.values(taskVariables).every(value => {
      if (value === null || value === undefined) return false;
      if (typeof value === 'string' && value.trim() === '') return false;
      return true;
    });
  }, [taskVariables]);

  // Summary fields for InstanceSummary
  const summaryFields: SummaryField[] = useMemo(() => {
    if (!task) return [];

    const fields: SummaryField[] = [];

    if (task.state) {
      fields.push({
        label: 'Status',
        value: <StatusBadge status={task.state} />,
      });
    }

    if (task.priority !== undefined && task.priority !== null) {
      fields.push({
        label: 'Priority',
        value: <PriorityBadge priority={task.priority as number} />,
      });
    }

    fields.push(
      { label: 'Task ID', value: task.taskId, copyValue: task.taskId, copyId: 'taskId' },
      {
        label: 'Process Instance',
        value: (
          <Link
            to={`/processes/${task.processInstanceId}`}
            className={commonStyles.link}
          >
            {task.processInstanceId}
          </Link>
        ),
        copyValue: task.processInstanceId,
        copyId: 'processInstanceId',
      },
      {
        label: 'Process',
        value: task.processDefinitionId,
        copyValue: task.processDefinitionId,
        copyId: 'processId',
      }
    );

    if (task.createdAt) {
      fields.push({ label: 'Created', value: formatDistanceToNow(new Date(task.createdAt), { addSuffix: true }) });
    }

    if (task.followUpDate) {
      fields.push({ label: 'Follow-up', value: formatDistanceToNow(new Date(task.followUpDate), { addSuffix: true }) });
    }

    if (task.candidateGroups?.length) {
      fields.push({ label: 'Candidate Groups', value: task.candidateGroups.join(', ') });
    }

    if (task.assignee) {
      fields.push({
        label: 'Assignee',
        value: task.assignee,
        copyValue: task.assignee,
        copyId: 'assignee',
        ...(isTerminalTaskState(task.state)
          ? {}
          : {
              onClick: () => toolbarRef.current?.openAssignDialog(),
              tooltip: 'Click to reassign this task',
            }),
      });
    } else if (task.claimedBy) {
      fields.push({
        label: 'Claimed By',
        value: task.claimedBy,
        copyValue: task.claimedBy,
        copyId: 'claimedBy',
        ...(isTerminalTaskState(task.state)
          ? {}
          : {
              onClick: () => toolbarRef.current?.openAssignDialog(),
              tooltip: 'Click to reassign this task',
            }),
      });
    } else if (!isTerminalTaskState(task.state)) {
      fields.push({
        label: 'Assignment',
        value: 'Unassigned',
        onClick: () => toolbarRef.current?.openAssignDialog(),
        tooltip: 'Click to assign this task to someone',
      });
    } else {
      fields.push({
        label: 'Assignment',
        value: 'Unassigned',
      });
    }

    return fields;
  }, [task]);

  if (isLoading) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>
          <p>{TASK_MESSAGES.LOADING.LOADING_TASK_DETAILS}</p>
        </div>
      </div>
    );
  }

  if (error || !task) {
    return (
      <div className={styles.container}>
        <div className={styles.error}>
          <p>{TASK_MESSAGES.ERROR.LOADING_TASK_DETAILS}</p>
        </div>
      </div>
    );
  }

  const currentEmail = normalizeEmail(currentUserEmail);
  const isTerminal = isTerminalTaskState(task.state);
  const isAssignedToCurrentUser =
    !isTerminal &&
    (normalizeEmail(task.claimedBy) === currentEmail ||
      normalizeEmail(task.assignee) === currentEmail);

  const handleBack = () => {
    if (window.history.length > 1) {
      navigate(-1);
    } else {
      navigate('/tasks');
    }
  };

  return (
    <div className={commonStyles.pageContainerMediumGap}>
      <InstanceSummary
        fields={summaryFields}
        copiedItems={copiedItems}
        onCopyToClipboard={copyToClipboard}
        leftSlot={<BackButton onClick={handleBack} ariaLabel="Back to task list" />}
      />

      <TaskActionsToolbar
        ref={toolbarRef}
        taskId={task.taskId}
        assignee={task.assignee}
        claimedBy={task.claimedBy}
        currentUserEmail={currentUserEmail}
        state={task.state}
        onActionComplete={refetch}
      />

      <div className={commonStyles.mainContent}>
        <div className={commonStyles.tabsWrapper}>
          <Tabs defaultValue="form" className={commonStyles.tabsContainer}>
        <TabsList className={`${commonStyles.tabsList} ${commonStyles.tabsListTwoCol}`}>
          <TabsTrigger
            value="form"
            className={commonStyles.tabTrigger}
            aria-label="Complete task form"
          >
            <CheckSquare className={commonStyles.tabIcon} aria-hidden="true" />
            Form
          </TabsTrigger>
          <TabsTrigger
            value="diagram"
            className={commonStyles.tabTrigger}
            aria-label="Process diagram"
          >
            <FileText className={commonStyles.tabIcon} aria-hidden="true" />
            Diagram
          </TabsTrigger>
        </TabsList>

        <TabsContent value="form" className={`${commonStyles.tabContent} ${commonStyles.tabContentPadding}`}>
          <div className={styles.formContainer}>
            <TaskFormView
              variables={taskVariables}
              onChange={setTaskVariables}
              readOnly={!isAssignedToCurrentUser || isTerminal}
            />

            {!isTerminal && (
              <div className={styles.formFooter}>
                <Button
                  onClick={async () => {
                    try {
                      await completeTask.mutateAsync({
                        taskId: task.taskId,
                        variables: taskVariables,
                      });
                      onComplete?.();
                    } catch {
                      // Error handled by mutation hook with toast
                    }
                  }}
                  disabled={!isAssignedToCurrentUser || completeTask.isPending || !isFormValid}
                  variant="primary"
                  size="sm"
                  label={completeTask.isPending ? 'Completing...' : 'Complete Task'}
                />
              </div>
            )}
          </div>
        </TabsContent>

        <TabsContent value="diagram" className={`${commonStyles.tabContent} ${commonStyles.tabContentPadding}`}>
          <TaskProcessDiagram
            processDefinitionId={task.processDefinitionId}
            activityId={task.activityId}
            taskName={task.taskName}
          />
        </TabsContent>
          </Tabs>
        </div>
      </div>
    </div>
  );
};

export default TaskDetailsPanel;
