import React, { useEffect, useState } from 'react';
import styles from './AfterHoursBanner.module.css';
import { environment } from '@/shared/constants/environment';

const isAfterHours = (date: Date): boolean => {
  const hour = date.getHours();
  return hour >= 19 || hour < 8;
};

export const AfterHoursBanner: React.FC = () => {
  const [visible, setVisible] = useState(() => isAfterHours(new Date()));

  useEffect(() => {
    if (!environment.enableQuoteBanner) {
      return;
    }

    const interval = setInterval(() => {
      setVisible(isAfterHours(new Date()));
    }, 60_000);

    return () => clearInterval(interval);
  }, []);

  if (!environment.enableQuoteBanner || !visible) {
    return null;
  }

  return (
    <div className={styles.banner} role="status" aria-live="polite">
      <span className={styles.message}><i>"Work is a never-ending process. It can never be completed. Interest of a client is important, so is your family"</i> - Dr APJ Abdul Kalam</span>
    </div>
  );
};

export default AfterHoursBanner;
