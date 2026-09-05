import { FilterSidebar } from '@/shared/components';
import { Mail, Settings, Variable, ShieldCheck, Power } from 'lucide-react';
import React from 'react';
import styles from './SettingsNavSidebar.module.css';

export type SettingsTab = 'process' | 'env-variables' | 'sensitive-variables' | 'process-state' | 'alert-mailer';

interface SettingsNavSidebarProps {
  isCollapsed: boolean;
  onToggleCollapse: () => void;
  activeTab: SettingsTab;
  onTabChange: (tab: SettingsTab) => void;
}

const NAV_ITEMS: { id: SettingsTab; label: string; icon: React.ReactNode }[] = [
  { id: 'process', label: 'OrchesT Routing', icon: <Settings size={18} /> },
  { id: 'env-variables', label: 'Env Variables', icon: <Variable size={18} /> },
  { id: 'sensitive-variables', label: 'Sensitive Variables', icon: <ShieldCheck size={18} /> },
  { id: 'process-state', label: 'Process Management', icon: <Power size={18} /> },
  { id: 'alert-mailer', label: 'Alert Mailer', icon: <Mail size={18} /> },
];

const SettingsNavSidebar: React.FC<SettingsNavSidebarProps> = ({
  isCollapsed,
  onToggleCollapse,
  activeTab,
  onTabChange,
}) => {
  return (
    <FilterSidebar
      title="Settings"
      isCollapsed={isCollapsed}
      onToggleCollapse={onToggleCollapse}
      hasActiveFilters={false}
      headerIcon="settings"
    >
      <nav className={styles.navList}>
        {NAV_ITEMS.map((item) => (
          <button
            key={item.id}
            className={`${styles.navItem} ${activeTab === item.id ? styles.navItemActive : ''}`}
            onClick={() => onTabChange(item.id)}
            type="button"
          >
            <span className={styles.navIcon}>{item.icon}</span>
            <span className={styles.navLabel}>{item.label}</span>
          </button>
        ))}
      </nav>
    </FilterSidebar>
  );
};

export default React.memo(SettingsNavSidebar);
