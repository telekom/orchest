import React from 'react';
import { ProcessInstanceState } from '@/shared/enums';
import styles from './InstanceStatusIndicator.module.css';
import clsx from 'clsx';

interface InstanceStatusIndicatorProps {
  status: string;
  size?: 'sm' | 'md';
  showTooltip?: boolean;
}

const STATUS_CONFIG: Record<string, { type: 'dot' | 'icon'; color: string; label: string; animate?: boolean }> = {
  [ProcessInstanceState.RUNNING]: { type: 'dot', color: 'green', label: 'Running', animate: true },
  [ProcessInstanceState.ACTIVE]: { type: 'dot', color: 'green', label: 'Active', animate: true },
  [ProcessInstanceState.STARTED]: { type: 'dot', color: 'green', label: 'Started', animate: true },
  [ProcessInstanceState.TRIGGERED]: { type: 'dot', color: 'green', label: 'Triggered', animate: true },
  [ProcessInstanceState.COMPLETED]: { type: 'icon', color: 'grey', label: 'Completed' },
  [ProcessInstanceState.INCIDENT]: { type: 'icon', color: 'red', label: 'Incident' },
  [ProcessInstanceState.FAILED]: { type: 'icon', color: 'red', label: 'Failed' },
  [ProcessInstanceState.HOLD]: { type: 'icon', color: 'amber', label: 'On Hold' },
  [ProcessInstanceState.PENDING]: { type: 'icon', color: 'blue', label: 'Pending' },
  [ProcessInstanceState.CANCELLED]: { type: 'icon', color: 'grey', label: 'Cancelled' },
  [ProcessInstanceState.TERMINATED]: { type: 'icon', color: 'grey', label: 'Terminated' },
};

function getConfig(status: string) {
  const upper = (status || '').toUpperCase();
  return STATUS_CONFIG[upper] || { type: 'dot', color: 'grey', label: status, animate: false };
}

const CompletedIcon: React.FC<{ className?: string }> = ({ className }) => (
  <svg className={className} viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
    <circle cx="8" cy="8" r="7" stroke="currentColor" strokeWidth="1.5" />
    <path d="M5 8.2l2 2 4-4.4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

const IncidentIcon: React.FC<{ className?: string }> = ({ className }) => (
  <svg className={className} viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M8 1.5L14.5 13.5H1.5L8 1.5Z" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round" />
    <path d="M8 6v3" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" />
    <circle cx="8" cy="11.5" r="0.7" fill="currentColor" />
  </svg>
);

const FailedIcon: React.FC<{ className?: string }> = ({ className }) => (
  <svg className={className} viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
    <circle cx="8" cy="8" r="7" stroke="currentColor" strokeWidth="1.5" />
    <path d="M5.5 5.5l5 5M10.5 5.5l-5 5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
  </svg>
);

const HoldIcon: React.FC<{ className?: string }> = ({ className }) => (
  <svg className={className} viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
    <circle cx="8" cy="8" r="7" stroke="currentColor" strokeWidth="1.5" />
    <path d="M6.5 5.5v5M9.5 5.5v5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
  </svg>
);

const PendingIcon: React.FC<{ className?: string }> = ({ className }) => (
  <svg className={className} viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
    <circle cx="8" cy="8" r="7" stroke="currentColor" strokeWidth="1.5" />
    <path d="M8 4.5v4l2.5 1.5" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

const CancelledIcon: React.FC<{ className?: string }> = ({ className }) => (
  <svg className={className} viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
    <circle cx="8" cy="8" r="7" stroke="currentColor" strokeWidth="1.5" />
    <path d="M4.5 11.5l7-7" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
  </svg>
);

function getIconComponent(status: string): React.FC<{ className?: string }> | null {
  const upper = (status || '').toUpperCase();
  switch (upper) {
    case ProcessInstanceState.COMPLETED:
      return CompletedIcon;
    case ProcessInstanceState.INCIDENT:
      return IncidentIcon;
    case ProcessInstanceState.FAILED:
      return FailedIcon;
    case ProcessInstanceState.HOLD:
      return HoldIcon;
    case ProcessInstanceState.PENDING:
      return PendingIcon;
    case ProcessInstanceState.CANCELLED:
    case ProcessInstanceState.TERMINATED:
      return CancelledIcon;
    default:
      return null;
  }
}

export const InstanceStatusIndicator: React.FC<InstanceStatusIndicatorProps> = ({
  status,
  size = 'sm',
  showTooltip = true,
}) => {
  const config = getConfig(status);
  const IconComponent = getIconComponent(status);

  if (config.type === 'dot') {
    return (
      <span
        className={clsx(styles.indicator, styles[size])}
        title={showTooltip ? config.label : undefined}
        aria-label={config.label}
      >
        <span className={clsx(styles.dot, styles[`dot-${config.color}`], { [styles.pulse]: config.animate })} />
      </span>
    );
  }

  if (IconComponent) {
    return (
      <span
        className={clsx(styles.indicator, styles[size])}
        title={showTooltip ? config.label : undefined}
        aria-label={config.label}
      >
        <IconComponent className={clsx(styles.icon, styles[`icon-${config.color}`])} />
      </span>
    );
  }

  return (
    <span
      className={clsx(styles.indicator, styles[size])}
      title={showTooltip ? config.label : undefined}
      aria-label={config.label}
    >
      <span className={clsx(styles.dot, styles['dot-grey'])} />
    </span>
  );
};

export default InstanceStatusIndicator;
