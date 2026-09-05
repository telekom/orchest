import { Button } from '@/design-system/components/ui/button';
import clsx from 'clsx';
import { Download, Trash2 } from 'lucide-react';
import React, { ReactNode } from 'react';
import styles from '../../FilterSidebar.module.css';

interface FilterSidebarActionsProps {
  hasActiveFilters: boolean;
  onClear: () => void;
  onExport?: () => void;
  isExporting?: boolean;
  canExport?: boolean;
  leftSlot?: ReactNode;
}

export const FilterSidebarActions: React.FC<FilterSidebarActionsProps> = ({
  hasActiveFilters,
  onClear,
  onExport,
  isExporting = false,
  canExport = true,
  leftSlot,
}) => {
  return (
    <div className={clsx(styles.actions, (leftSlot || onExport) && styles.actionsWithSlot)}>
      <div className={styles.actionsLeftSlot}>
        {leftSlot}
        {onExport && (
          <Button
            variant="ghost"
            size="icon"
            onClick={onExport}
            disabled={isExporting || !canExport}
            className={clsx(styles.exportButton)}
            aria-label={isExporting ? "Exporting..." : "Export CSV"}
            title={isExporting ? "Exporting..." : "Export CSV"}
          >
            <Download size={16} />
          </Button>
        )}
      </div>
      <Button
        variant="ghost"
        size="icon"
        onClick={onClear}
        disabled={!hasActiveFilters}
        className={clsx(styles.clearButton)}
        aria-label="Clear all filters"
        title="Clear all filters"
      >
        <Trash2 size={16} />
      </Button>
    </div>
  );
};
