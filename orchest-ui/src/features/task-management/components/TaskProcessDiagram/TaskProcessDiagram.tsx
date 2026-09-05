import React, { lazy, Suspense } from 'react';
import { useProcessDiagram } from '@/features/process-management/hooks/useProcessDiagram';
import { SpinnerLoader } from '@/shared/components/Loader/Loader';
import GenericDiagramViewer from '@/shared/diagram/components/GenericDiagramViewer/GenericDiagramViewer';
import styles from './TaskProcessDiagram.module.css';

const BpmnViewer = lazy(
  () => import('@/features/process-management/components/BpmnViewer/BpmnViewer')
);

export interface TaskProcessDiagramProps {
  /** Process definition ID */
  processDefinitionId: string;
  /** Activity ID to highlight on the diagram */
  activityId: string;
  /** Task name for display */
  taskName?: string;
}

/**
 * TaskProcessDiagram - Displays BPMN diagram with highlighted task activity
 *
 * Features:
 * - Fetches BPMN XML for process definition
 * - Highlights the current task's activity on the diagram
 * - Shows loading and error states
 * - Lazy loads the BPMN viewer for performance
 *
 * @example
 * ```tsx
 * <TaskProcessDiagram
 *   processDefinitionId="process_1"
 *   activityId="UserTask_1"
 *   taskName="Approve Request"
 * />
 * ```
 */
export const TaskProcessDiagram: React.FC<TaskProcessDiagramProps> = ({
  processDefinitionId,
  activityId,
  taskName,
}) => {
  const { diagramXml, isLoading } = useProcessDiagram(processDefinitionId);

  return (
    <GenericDiagramViewer
      id={processDefinitionId}
      diagramXml={diagramXml}
      isLoading={isLoading}
      height="min(60vh, 700px)"
      minHeight="400px"
      loadingText={`Loading process diagram for ${taskName || 'task'}...`}
      emptyStateConfig={{
        noSelection: {
          title: 'No process diagram available',
          description: 'Unable to load the process diagram for this task',
        },
        noData: {
          title: 'BPMN diagram not available',
          description: (id) => `No BPMN diagram found for process: ${id}`,
        },
      }}
      viewerClassName={`${styles.viewerContainer} bpmn-container task-process-diagram`}
    >
      <Suspense
        fallback={
          <div className={styles.loadingContainer}>
            <SpinnerLoader size="lg" />
          </div>
        }
      >
        <BpmnViewer
          xml={diagramXml}
          state="ACTIVE"
          processInstanceId={null}
          processDefinitionId={processDefinitionId}
          sequenceExecutions={{}}
          showInstanceActions={false}
          selectedNodeId={activityId}
          className="task-diagram-viewer-wrapper"
        />
      </Suspense>
    </GenericDiagramViewer>
  );
};

export default React.memo(TaskProcessDiagram);
