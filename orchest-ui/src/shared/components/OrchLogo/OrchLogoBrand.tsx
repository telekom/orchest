import React from 'react';
import styles from './OrchLogoBrand.module.css';

interface OrchLogoBrandProps {
  collapsed?: boolean;
  className?: string;
}

export const OrchLogoBrand: React.FC<OrchLogoBrandProps> = ({
  collapsed = false,
  className,
}) => {
  if (collapsed) {
    return (
      <svg
        width={28}
        height={28}
        viewBox="0 0 96 96"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className={`${styles.logoIcon} ${className ?? ''}`}
      >
        <rect width="96" height="96" rx="22" className={styles.bg} />
        <path
          className={styles.flowPath}
          d="M26 24 H46 V38 H66 V24 H82 V52 H66 V72 H46 V58 H26 V74"
        />
        <rect className={styles.nodeRect} x="18" y="16" width="16" height="16" rx="4" />
        <rect className={styles.nodeRect} x="58" y="16" width="16" height="16" rx="4" />
        <rect className={styles.nodeRect} x="58" y="64" width="16" height="16" rx="4" />
        <rect className={styles.nodeRect} x="18" y="66" width="16" height="16" rx="4" />
        <rect className={styles.flowDot} width="6" height="6" rx="1">
          <animateMotion
            dur="2s"
            repeatCount="indefinite"
            path="M26 24 H46 V38 H66 V24 H82 V52 H66 V72 H46 V58 H26 V74"
          />
        </rect>
      </svg>
    );
  }

  return (
    <svg
      height={45}
      viewBox="0 0 500 140"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`${styles.logoFull} ${className ?? ''}`}
    >
      <defs>
        <linearGradient id="orchGradFull" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#E20074" />
          <stop offset="100%" stopColor="#8B0052" />
        </linearGradient>
      </defs>

      {/* Step-flow icon */}
      <g transform="translate(20, 22)">
        <rect width="96" height="96" rx="22" className={styles.bg} />
        <path
          className={styles.flowPath}
          d="M26 24 H46 V38 H66 V24 H82 V52 H66 V72 H46 V58 H26 V74"
        />
        <rect className={styles.nodeRect} x="18" y="16" width="16" height="16" rx="4" />
        <rect className={styles.nodeRect} x="58" y="16" width="16" height="16" rx="4" />
        <rect className={styles.nodeRect} x="58" y="64" width="16" height="16" rx="4" />
        <rect className={styles.nodeRect} x="18" y="66" width="16" height="16" rx="4" />
        <rect className={styles.flowDot} width="6" height="6" rx="1">
          <animateMotion
            dur="2s"
            repeatCount="indefinite"
            path="M26 24 H46 V38 H66 V24 H82 V52 H66 V72 H46 V58 H26 V74"
          />
        </rect>
      </g>

      {/* Text: Orches + Telekom Magenta T */}
      <g className={styles.textGlow}>
        <text
          x="145"
          y="80"
          fontSize="60"
          fontWeight="700"
          letterSpacing="0.5"
          className={styles.brandText}
        >
          Orches
        </text>
        <g transform="translate(388, 36) scale(0.48)" aria-hidden>
          <g transform="matrix(.2857 0 0 .2857 71.408 28.262)" fill="#E20074">
            <path d="m-33.599 218.73v-22.192h-15.256c-26.315 0-38.393-15.643-38.393-38.665v-232.6h4.5246c49.283 0 80.582 32.707 80.582 80.797v4.3092h18.745v-107.3h-264.58v107.3h18.745v-4.3092c0-48.09 31.298-80.797 80.582-80.797h4.5246v232.6c0 23.022-12.078 38.665-38.393 38.665h-15.256v22.192z" />
            <path d="m16.603 111.43h-62.914v-63.129h62.914z" />
            <path d="m-185.07 111.43h-62.914v-63.129h62.914z" />
          </g>
        </g>
      </g>

      {/* Subtitle */}
      <text
        x="148"
        y="112"
        fontFamily="Inter, Arial, sans-serif"
        fontSize="22"
        fontWeight="500"
        letterSpacing="2"
        className={styles.subtitleText}
      >
        ORCHESTRATION PLATFORM
      </text>
    </svg>
  );
};
