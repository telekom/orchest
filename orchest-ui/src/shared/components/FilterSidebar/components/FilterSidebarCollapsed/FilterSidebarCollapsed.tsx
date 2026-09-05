import { Button } from '@/design-system/components/ui/button';
import clsx from 'clsx';
import { Download, Filter, Trash2 } from 'lucide-react';
import React from 'react';
import styles from '../../FilterSidebar.module.css';

interface FilterSidebarCollapsedProps {
  hasActiveFilters: boolean;
  onToggle: () => void;
  onClear?: () => void;
  onExport?: () => void;
  isExporting?: boolean;
  canExport?: boolean;
}

export const FilterSidebarCollapsed: React.FC<FilterSidebarCollapsedProps> = ({
  hasActiveFilters,
  onToggle,
  onClear,
  onExport,
  isExporting = false,
  canExport = true,
}) => {
  return (
    <div className={styles.collapsedContainer}>
      <Button
        variant="ghost"
        size="icon"
        onClick={onToggle}
        className={clsx(styles.collapsedTrigger)}
        aria-label="Expand filters"
        title="Expand filters"
      >
        <Filter size={16} />
      </Button>

      <div className={styles.collapsedActionsContainer}>
        {onExport && (
          <Button
            variant="ghost"
            size="icon"
            onClick={onExport}
            disabled={isExporting || !canExport}
            className={clsx(styles.collapsedExport)}
            aria-label={isExporting ? "Exporting..." : "Export CSV"}
            title={isExporting ? "Exporting..." : "Export CSV"}
          >
            <Download size={16} />
          </Button>
        )}
        {hasActiveFilters && onClear && (
          <Button
            variant="ghost"
            size="icon"
            onClick={(e) => {
              e.stopPropagation();
              onClear();
            }}
            className={styles.collapsedActions}
            aria-label="Clear all filters"
            title="Clear all filters"
          >
            <Trash2 size={16} />
          </Button>
        )}
      </div>
    </div>
  );
};
