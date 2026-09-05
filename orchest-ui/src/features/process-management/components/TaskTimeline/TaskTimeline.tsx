import { ScrollArea } from "@/design-system/components/ui/scroll-area";
import { StatusBadge } from "@/shared/components";
import { ProcessStatus, EMPTY_STATE_MESSAGES } from "@/shared/constants";
import { TIMELINE_CONFIG } from "../../constants/timelineConfig";
import { getNodeTypeIcon } from "../../constants/nodeTypeIcons";
import React, { useCallback } from "react";
import clsx from "clsx";
import styles from "./TaskTimeline.module.css";

interface Task {
  taskId: string;
  taskName: string;
  status: string;
  nodeType?: string;
  timestamp: Array<{ timestamp: string; state: string }>;
}

export interface TaskTimelineProps {
  tasks: Task[];
  isLoading: boolean;
}

const TaskTimelineItem = React.memo<{
  task: Task;
  index: number;
}>(({ task, index }) => {
  const formatTimestamp = useCallback((timestamp: string) => {
    return timestamp ? new Date(timestamp).toLocaleString() : "-";
  }, []);

  const getDotClass = () => {
    switch (task.status) {
      case ProcessStatus.COMPLETED:
        return styles.dotCompleted;
      case ProcessStatus.INCIDENT:
        return styles.dotIncident;
      case ProcessStatus.HOLD:
        return styles.dotHold;
      default:
        return styles.dotDefault;
    }
  };

  const nodeTypeIcon = getNodeTypeIcon(task.nodeType);

  return (
    <div
      className={styles.timelineItem}
      style={{ animationDelay: `${index * TIMELINE_CONFIG.ANIMATION_DELAY_PER_ITEM}ms` }}
    >
      <div className={clsx(styles.timelineDot, getDotClass())}></div>

      <div className={styles.content}>
        {task.taskName && (
          <h4 className={styles.taskName}>
            {nodeTypeIcon && (
              <img
                src={nodeTypeIcon}
                alt={task.nodeType ?? ""}
                className={styles.nodeTypeIcon}
                aria-hidden="true"
              />
            )}
            <span>{task.taskName}</span>
          </h4>
        )}
        {task?.timestamp?.length && (
          <div className={styles.timestamps}>
            {task?.timestamp?.map((statusTime, statusIndex) => (
              <div className={styles.timestampRow} key={statusIndex}>
                <StatusBadge status={statusTime.state || ""} />
                <span className={styles.timestampText}>{formatTimestamp(statusTime.timestamp)}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
});

TaskTimelineItem.displayName = 'TaskTimelineItem';

const TaskTimeline: React.FC<TaskTimelineProps> = ({ tasks, isLoading }) => {
  if (isLoading) {
    return (
      <ScrollArea className={styles.scrollArea}>
        <div className={styles.loadingContainer}>
          {Array.from({ length: TIMELINE_CONFIG.LOADING_ITEMS_COUNT }, (_, i) => i + 1).map((item) => (
            <div key={item} className={styles.loadingItem}>
              <div className={styles.loadingDot}></div>
              <div className={styles.loadingContent}>
                <div className={styles.loadingTitle}></div>
                <div className={clsx(styles.loadingLine, styles.loadingLineShort)}></div>
                <div className={clsx(styles.loadingLine, styles.loadingLineLong)}></div>
              </div>
            </div>
          ))}
        </div>
      </ScrollArea>
    );
  }

  if (tasks.length === 0) {
    return (
      <ScrollArea className={styles.scrollArea}>
        <div className={styles.emptyContainer}>
          <p className={styles.emptyText}>
            {EMPTY_STATE_MESSAGES.NO_HISTORY}
          </p>
        </div>
      </ScrollArea>
    );
  }

  return (
    <ScrollArea className={styles.scrollArea}>
      <div className={styles.timelineContainer}>
        <div className={styles.timelineWrapper}>
          <div className={styles.verticalLine}></div>

          <div className={styles.timelineList}>
            {tasks.map((task, index) => (
              <TaskTimelineItem
                key={task.taskId}
                task={task}
                index={index}
              />
            ))}
          </div>
        </div>
      </div>
    </ScrollArea>
  );
};

export default React.memo(TaskTimeline);
