import { Separator } from '@/design-system/components/ui/separator';
import { FilterValue } from '@/shared/constants/status';
import { Select } from '@/design-system/components/ui/select';
import { SearchInput } from '@/design-system/components/ui/search-input';
import React, { useCallback } from 'react';
import styles from './SettingsFilterSidebar.module.css';

interface SettingsFilterSidebarProps {
  isCollapsed: boolean;
  onToggleCollapse: () => void;
  searchTerm: string;
  statusFilter: string;
  onSearchChange: (value: string) => void;
  onStatusFilterChange: (value: string) => void;
  onClearFilters: () => void;
  hasActiveFilters: boolean;
  onFilterChange?: () => void;
  inline?: boolean;
}

const STATUS_OPTIONS = [
  { label: 'All Status', value: FilterValue.ALL },
  { label: 'Enabled', value: 'enabled' },
  { label: 'Disabled', value: 'disabled' }
];

const SettingsFilterSidebar: React.FC<SettingsFilterSidebarProps> = ({
  searchTerm,
  statusFilter,
  onSearchChange,
  onStatusFilterChange,
  onClearFilters,
  hasActiveFilters,
  onFilterChange,
  inline = false,
}) => {
  const handleSearchChange = useCallback((value: string) => {
    onSearchChange(value);
  }, [onSearchChange]);

  const handleSearchClear = useCallback(() => {
    onSearchChange('');
  }, [onSearchChange]);

  const handleStatusFilter = useCallback((value: string) => {
    
    if (!value) return;

    const newStatus = value === FilterValue.TOTAL || value === FilterValue.ALL ? FilterValue.ALL : value;
    onStatusFilterChange(newStatus);
    onFilterChange?.();
  }, [onStatusFilterChange, onFilterChange]);

  return (
    <div className={inline ? styles.inlineContainer : styles.stackedContainer}>
      <div className={inline ? styles.inlineFilterItem : styles.filterInputWrapper}>
        <SearchInput
          labelText="Search settings..."
          value={searchTerm}
          onChange={handleSearchChange}
          onClear={handleSearchClear}
          size="sm"
          showResultList={false}
        />
      </div>

      {!inline && <Separator />}

      <div className={inline ? styles.inlineFilterItem : styles.filterInputWrapper}>
        <div className={styles.dropdownWrapper}>
          <Select
            value={statusFilter || FilterValue.ALL}
            items={STATUS_OPTIONS}
            onValueChange={handleStatusFilter}
            size="sm"
            placeholder="Filter by status"
            className={styles.dropdown}
          />
        </div>
      </div>

      {hasActiveFilters && (
        <button className={styles.clearButton} onClick={onClearFilters} type="button">
          Clear
        </button>
      )}
    </div>
  );
};

export default React.memo(SettingsFilterSidebar);
