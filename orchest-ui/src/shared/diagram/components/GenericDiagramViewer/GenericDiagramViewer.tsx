import { EmptyState } from "@/shared/components/EmptyState/EmptyState";
import { SpinnerLoader } from "@/shared/components/Loader/Loader";
import { LAYOUT } from "@/shared/constants";
import React from "react";
import styles from "./GenericDiagramViewer.module.css";

interface EmptyStateConfig {
  noSelection: { title: string; description: string };
  noData: { title: string; description: (id: string) => string };
}

interface GenericDiagramViewerProps {
  id: string | null;
  diagramXml: string;
  isLoading?: boolean;
  height?: string;
  minHeight?: string;
  emptyStateConfig: EmptyStateConfig;
  loadingText?: string;
  children: React.ReactNode;
  containerClassName?: string;
  viewerClassName?: string;
}

const GenericDiagramViewer: React.FC<GenericDiagramViewerProps> = ({
  id,
  diagramXml,
  isLoading = false,
  height = LAYOUT.DEFAULT_DIAGRAM_HEIGHT,
  minHeight = LAYOUT.MIN_DIAGRAM_HEIGHT,
  emptyStateConfig,
  loadingText = "Loading diagram...",
  children,
  containerClassName = "",
  viewerClassName = "",
}) => {
  // INLINE STYLE: Dynamic height/minHeight props from parent - cannot use CSS classes
  const sizeStyles = { height, minHeight };

  if (!id) {
    return (
      <div className={`${styles.container} ${containerClassName}`} style={sizeStyles}>
        <EmptyState
          title={emptyStateConfig.noSelection.title}
          description={emptyStateConfig.noSelection.description}
          variant="dark-text"
        />
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className={`${styles.container} ${containerClassName}`} style={sizeStyles}>
        <div className={styles.loadingContainer}>
          <SpinnerLoader text={loadingText} size="lg" variant="dark" />
        </div>
      </div>
    );
  }

  if (diagramXml) {
    return (
      <div className={`${styles.container} ${containerClassName}`} style={sizeStyles}>
        <div className={styles.viewerWrapper}>
          <div className={`${styles.viewer} ${viewerClassName}`}>{children}</div>
        </div>
      </div>
    );
  }

  return (
    <div className={`${styles.container} ${containerClassName}`} style={sizeStyles}>
      <EmptyState
        title={emptyStateConfig.noData.title}
        description={emptyStateConfig.noData.description(id)}
        variant="dark-text"
      />
    </div>
  );
};

export default React.memo(GenericDiagramViewer);
