import { healthService } from '@/api/domains/health/healthService';
import { environment } from '@/shared/constants/environment';
import { useQuery } from '@tanstack/react-query';
import clsx from 'clsx';
import React from 'react';
import styles from './AppFooter.module.css';

const POLL_MS = 10_000;
const API_DOCS_URL = 'https://doc-orchest.oneapp.dev.hal.telekom.de/';

function formatVersion(appVersion: string | null | undefined): string {
  if (!appVersion) return 'UNKNOWN';
  return appVersion.startsWith('v') ? appVersion.slice(1) : appVersion;
}

export const AppFooter: React.FC = () => {
  const configured = Boolean(environment.engineActuatorUrl);

  const healthQuery = useQuery({
    queryKey: ['engine-health'],
    queryFn: () => healthService.getHealth(),
    enabled: configured,
    staleTime: POLL_MS,
    refetchInterval: POLL_MS,
    retry: false,
  });

  const envQuery = useQuery({
    queryKey: ['engine-env'],
    queryFn: () => healthService.getEnv(),
    enabled: configured,
    staleTime: POLL_MS,
    refetchInterval: POLL_MS,
    retry: false,
  });

  const isOnline = configured && healthQuery.isSuccess && healthQuery.data.status === 'UP';
  const versionLabel = formatVersion(envQuery.data?.appVersion);

  return (
    <footer className={styles.footer} aria-label="Application status">
      <div className={styles.row}>
        <span
          className={clsx(styles.status, isOnline ? styles.statusOnline : styles.statusOffline)}
          role="status"
        >
          <span className={styles.dot} aria-hidden="true" />
          {isOnline ? 'Engine Online' : 'Engine Offline'}
        </span>
        <span className={styles.separator} aria-hidden="true">
          ·
        </span>
        <span className={styles.version}>{versionLabel}</span>
        <span className={styles.separator} aria-hidden="true">
          ·
        </span>
        <a
          className={styles.docsLink}
          href={API_DOCS_URL}
          target="_blank"
          rel="noopener noreferrer"
        >
          API Docs
        </a>
      </div>
    </footer>
  );
};
