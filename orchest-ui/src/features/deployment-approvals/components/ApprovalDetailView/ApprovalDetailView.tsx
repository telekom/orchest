import { Badge } from '@/design-system/components/ui/badge/badge';
import BpmnDiffViewer from '@/shared/diagram/components/BpmnDiffViewer/BpmnDiffViewer';
import DmnViewer from '@/shared/diagram/components/DmnViewer/DmnViewer';
import { ApprovalState } from '@/shared/enums';
import { Button } from "@/design-system/components/ui/button";
import {
    Calendar,
    FileCode,
    User,
} from 'lucide-react';
import React from 'react';
import { usePreviousDefinitionVersion } from '../../hooks/usePreviousDefinitionVersion';
import type { EnrichedApproval } from '../../hooks/useEnrichedApprovals';
import styles from './ApprovalDetailView.module.css';

interface ApprovalDetailViewProps {
  approval: EnrichedApproval;
  onBack: () => void;
  onApprove: () => void;
  onDecline: () => void;
  isPending: boolean;
}

const formatDate = (dateString: string) =>
  new Date(dateString).toLocaleString();

export const ApprovalDetailView: React.FC<ApprovalDetailViewProps> = ({
  approval,
  onBack,
  onApprove,
  onDecline,
  isPending,
}) => {
  const { data: previousXml } = usePreviousDefinitionVersion({
    definitionInfo: approval.definitionInfo,
    approvalId: approval.id,
  });

  const { definitionInfo } = approval;
  const newXml = approval.resourceDeploymentRequest.resourceUTF8XML;
  const oldXml = previousXml || '';

  return (
    <div className={styles.container}>
      <ApprovalMetadata approval={approval} onBack={onBack} />
      <DiagramViewer
        diagramType={definitionInfo.diagramType}
        oldXml={oldXml}
        newXml={newXml}
      />
      {approval.state === ApprovalState.REQUESTED && (
        <ApprovalActions
          onApprove={onApprove}
          onDecline={onDecline}
          isPending={isPending}
        />
      )}
    </div>
  );
};

// Sub-components for better organization
interface ApprovalMetadataProps {
  approval: EnrichedApproval;
  onBack: () => void;
}

const ApprovalMetadata: React.FC<ApprovalMetadataProps> = ({ approval, onBack }) => (
  <div className={styles.metadata}>
    <Button
      variant="ghost"
      size="sm"
      onClick={onBack}
      buttonIcon="arrow-left-type-standard"
      aria-label="Back to list"
    />
    <Badge variant="outline" className={styles.metadataBadge}>
      {approval.resourceType}
    </Badge>
    <div className={styles.metadataItem}>
      <User className={styles.metadataIcon} />
      <span>{approval.requestedBy}</span>
    </div>
    <span className={styles.versionBadge}>
      v{approval.nextVersion}
    </span>
    <div className={styles.metadataItem}>
      <Calendar className={styles.metadataIcon} />
      <span>{formatDate(approval.createdAt)}</span>
    </div>
  </div>
);

interface DiagramViewerProps {
  diagramType: 'bpmn' | 'dmn' | 'unknown';
  oldXml: string;
  newXml: string;
}

const DiagramViewer: React.FC<DiagramViewerProps> = ({
  diagramType,
  oldXml,
  newXml,
}) => (
  <div className={styles.diagramContainer}>
    {diagramType === 'bpmn' ? (
      <BpmnDiffViewer oldXml={oldXml} newXml={newXml} height="100%" />
    ) : diagramType === 'dmn' ? (
      <div className={styles.dmnViewerContainer}>
        <DmnViewer xml={newXml} showControls />
      </div>
    ) : (
      <UnknownDiagramPlaceholder />
    )}
  </div>
);

const UnknownDiagramPlaceholder: React.FC = () => (
  <div className={styles.placeholderContainer}>
    <FileCode className={styles.placeholderIcon} />
    <h3 className={styles.placeholderTitle}>
      Unknown Diagram Type
    </h3>
    <p className={styles.placeholderText}>
      Unable to display this diagram type.
    </p>
  </div>
);

interface ApprovalActionsProps {
  onApprove: () => void;
  onDecline: () => void;
  isPending: boolean;
}

const ApprovalActions: React.FC<ApprovalActionsProps> = ({
  onApprove,
  onDecline,
  isPending,
}) => (
  <div className={styles.actionsContainer}>
    <Button
      variant="ghost"
      size="sm"
      onClick={onDecline}
      disabled={isPending}
      label="Decline"
      buttonIcon="close-type-standard"
    />
    <Button
      variant="primary"
      size="sm"
      onClick={onApprove}
      disabled={isPending}
      label="Approve"
      buttonIcon="checkmark-type-standard"
    />
  </div>
);
