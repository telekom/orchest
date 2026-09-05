import { ProcessInstance } from "@/shared/types";
import { InstanceSummary, StatusBadge, SummaryField } from "@/shared/components";
import { Activity, Calendar, CheckCircle2, ExternalLink, GitBranch, Tag, Workflow } from "lucide-react";
import { Link } from "react-router-dom";
import React, { useMemo } from "react";
import styles from "./ProcessInstanceSummary.module.css";

interface ProcessInstanceSummaryProps {
  instance: ProcessInstance;
  copiedItems: Record<string, boolean>;
  onCopyToClipboard: (text: string, itemId: string) => void;
  leftSlot?: React.ReactNode;
  rightSlot?: React.ReactNode;
  compensatedInstanceId?: string;
}

const ProcessInstanceSummary: React.FC<ProcessInstanceSummaryProps> = ({
  instance,
  copiedItems,
  onCopyToClipboard,
  leftSlot,
  rightSlot,
  compensatedInstanceId,
}) => {
  const fields: SummaryField[] = useMemo(() => {
    const baseFields: SummaryField[] = [
      {
        label: "Status",
        icon: <Activity size={12} />,
        value: <StatusBadge status={instance.status || ""} />,
      },
      {
        label: "ID",
        icon: <Workflow size={12} />,
        value: instance.processId,
        copyValue: instance.processId,
        copyId: "id",
      },
      {
        label: "Name",
        icon: <Tag size={12} />,
        value: instance.processName,
        copyValue: instance.processName,
        copyId: "name",
      },
      {
        label: "Started",
        icon: <Calendar size={12} />,
        value: instance.startDate
          ? new Date(instance.startDate).toLocaleString()
          : "-",
      },
      {
        label: "Version",
        icon: <GitBranch size={12} />,
        value: String(instance.processVersion),
      },
    ];

    if (instance.endDate) {
      baseFields.push({
        label: "Completed",
        icon: <CheckCircle2 size={12} />,
        value: new Date(instance.endDate).toLocaleString(),
      });
    }

    if (instance.parentProcessId) {
      baseFields.push({
        label: "Parent",
        icon: <ExternalLink size={12} />,
        value: (
          <Link
            to={`/processes/${instance.parentProcessId}`}
            className={styles.parentLink}
          >
            Instance
          </Link>
        ),
      });
    }

    if (compensatedInstanceId) {
      baseFields.push({
        label: "Compensated",
        icon: <ExternalLink size={12} />,
        value: (
          <Link
            to={`/processes/${compensatedInstanceId}`}
            className={styles.parentLink}
          >
            Instance
          </Link>
        ),
      });
    }

    return baseFields;
  }, [instance, compensatedInstanceId]);

  return (
    <InstanceSummary
      fields={fields}
      copiedItems={copiedItems}
      onCopyToClipboard={onCopyToClipboard}
      leftSlot={leftSlot}
      rightSlot={rightSlot}
    />
  );
};

export default React.memo(ProcessInstanceSummary);
