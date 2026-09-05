import { Button } from '@/design-system/components/ui/button';
import { PanelLeftClose } from 'lucide-react';
import React from 'react';
import styles from '../../FilterSidebar.module.css';

interface FilterSidebarHeaderProps {
  title: string;
  onToggle: () => void;
  headerIcon?: string;
}

export const FilterSidebarHeader: React.FC<FilterSidebarHeaderProps> = ({
  title,
  onToggle,
}) => {
  return (
    <div className={styles.header}>
      <div className={styles.headerTitle}>
        <h2 className={styles.headerText}>{title}</h2>
      </div>
      <Button
        variant="ghost"
        size="icon"
        onClick={onToggle}
        aria-label="Collapse filters"
        className={styles.toggleButton}
      >
        <PanelLeftClose size={16} />
      </Button>
    </div>
  );
};
