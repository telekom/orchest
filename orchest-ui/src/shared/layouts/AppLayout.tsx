import clsx from 'clsx';
import React from 'react';
import { AppFooter } from '@/design-system/layouts/AppFooter/AppFooter';
import { AppSidebar } from '@/design-system/layouts/AppSidebar/AppSidebar';
import { TopBar } from '@/design-system/layouts/TopBar/TopBar';
import { AfterHoursBanner } from '@/shared/components/AfterHoursBanner/AfterHoursBanner';
import { useUIStore } from '@/shared/stores/uiStore';
import styles from './AppLayout.module.css';

interface AppLayoutProps {
  children: React.ReactNode;
  sidebar?: React.ReactNode;
  sidebarCollapsed?: boolean;
  className?: string;
}

const AppLayout: React.FC<AppLayoutProps> = ({
  children,
  sidebar,
  sidebarCollapsed = false,
  className
}) => {
  const navCollapsed = useUIStore((state) => state.sidebarCollapsed);

  return (
    <div className={styles.layout}>
      <AppSidebar />
      <AfterHoursBanner />

      <div className={clsx(styles.mainContainer, navCollapsed ? styles.mainContainerCollapsed : styles.mainContainerExpanded)}>
        <TopBar />

        <div className={styles.contentArea}>
          {sidebar}

          <main
            className={clsx(
              styles.mainContent,
              sidebar && (sidebarCollapsed ? styles.mainContentWithCollapsedSidebar : styles.mainContentWithSidebar),
              className
            )}
          >
            <div className={clsx(
              styles.contentWrapper,
              sidebar ? styles.contentWrapperWithSidebar : styles.contentWrapperNoSidebar
            )}>
              <div className={styles.pageBody}>{children}</div>
              <AppFooter />
            </div>
          </main>
        </div>
      </div>
    </div>
  );
};

export default AppLayout;
