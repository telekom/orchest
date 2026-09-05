import { Input } from '@/design-system/components/ui/input';
import { Search } from 'lucide-react';
import React from 'react';
import styles from '../../FilterSidebar.module.css';

interface FilterSidebarSearchProps {
  value: string;
  placeholder: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

export const FilterSidebarSearch: React.FC<FilterSidebarSearchProps> = ({
  value,
  placeholder,
  onChange,
}) => {
  return (
    <div className={styles.searchContainer}>
      <label className={styles.searchLabel}>Search</label>
      <div className={styles.searchInputWrapper}>
        <Input
          placeholder={placeholder}
          value={value}
          onChange={onChange}
          className={styles.searchInput}
        />
        <Search className={styles.searchIcon} />
      </div>
    </div>
  );
};
