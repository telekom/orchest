import React from 'react';
import styles from './OrchLogo.module.css';

interface OrchLogoProps {
  size?: number;
  className?: string;
}

export const OrchLogo: React.FC<OrchLogoProps> = ({ size = 28, className }) => (
  <svg
    width={size}
    height={size}
    viewBox="0 0 96 96"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    role="img"
    aria-label="OrchesT"
    className={`${styles.logo} ${className ?? ''}`}
  >
    <rect width="96" height="96" rx="22" className={styles.bg} />

    <path
      className={styles.flowPath}
      d="M26 24 H46 V38 H66 V24 H82 V52 H66 V72 H46 V58 H26 V74"
    />

    <rect className={styles.node} x="18" y="16" width="16" height="16" rx="4" />
    <rect className={styles.node} x="58" y="16" width="16" height="16" rx="4" />
    <rect className={styles.node} x="58" y="64" width="16" height="16" rx="4" />
    <rect className={styles.node} x="18" y="66" width="16" height="16" rx="4" />

    <rect className={styles.flowDot} width="6" height="6" rx="1">
      <animateMotion
        dur="2s"
        repeatCount="indefinite"
        path="M26 24 H46 V38 H66 V24 H82 V52 H66 V72 H46 V58 H26 V74"
      />
    </rect>
  </svg>
);