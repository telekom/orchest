import { OrchLogo } from "@/shared/components/OrchLogo";
import styles from "./DataTableEmptyState.module.css";

interface DataTableEmptyStateProps {
  loading: boolean;
  dataLength: number;
  loadingText: string;
  emptyText: string;
}

export function DataTableEmptyState({
  loading,
  dataLength,
  loadingText,
  emptyText,
}: DataTableEmptyStateProps) {
  if (dataLength > 0 && !loading) return null;
  if (dataLength > 0 && loading) {
    return (
      <div className={styles.loadingContainer}>
        <OrchLogo size={32} />
      </div>
    );
  }

  if (loading) {
    return (
      <div className={styles.loadingContainer}>
        <div className={styles.loadingContent}>
          <OrchLogo size={48} />
          <span className={styles.loadingText}>{loadingText}</span>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.emptyContainer}>
      <div className={styles.emptyText}>
        {emptyText}
      </div>
    </div>
  );
}
