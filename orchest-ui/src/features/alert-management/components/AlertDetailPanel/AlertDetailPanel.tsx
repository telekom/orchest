import type { AlertResponse } from '@/api/domains/alerts';
import { Badge } from '@/design-system/components/ui/badge/badge';
import { Button } from '@/design-system/components/ui/button';
import commonStyles from '@/shared/styles/common.module.css';
import { X } from 'lucide-react';
import { useState, type ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { getAvailableAlertActions, type AlertAction } from '../../utils/alertActions';
import {
  getAlertSeverityBadgeConfig,
  getAlertStateBadgeConfig,
  getAlertVersion,
} from '../../utils/alertBadges';
import styles from './AlertDetailPanel.module.css';

type ActorActionInput = {
  id: string;
  actor: string;
};

type ActionResult = void | Promise<unknown>;

export interface AlertDetailPanelProps {
  alert?: AlertResponse;
  actorEmail: string;
  isLoading?: boolean;
  isError?: boolean;
  isPending: boolean;
  onAcknowledge: (input: ActorActionInput) => ActionResult;
  onSilence: (input: ActorActionInput) => ActionResult;
  onUnmute: (id: string) => ActionResult;
  onResolve: (id: string) => ActionResult;
  onClose: () => void;
}

const ACTION_LABELS: Record<AlertAction, string> = {
  acknowledge: 'Acknowledge',
  silence: 'Silence',
  unmute: 'Unmute',
  resolve: 'Resolve',
};

const ACTION_VARIANTS: Record<
  AlertAction,
  'primary' | 'warning' | 'secondary' | 'destructive'
> = {
  acknowledge: 'primary',
  silence: 'warning',
  unmute: 'secondary',
  resolve: 'destructive',
};

const BODY_PREVIEW_CHARS = 280;

const HIDDEN_METADATA_KEYS = new Set([
  'processDefinitionId',
  'processInstanceId',
  'version',
]);

const formatDate = (value?: string) => (value ? new Date(value).toLocaleString() : '—');

const formatRecipients = (recipients?: string[]) =>
  recipients?.length ? recipients.join(', ') : '—';

export function AlertDetailPanel({
  alert,
  actorEmail,
  isLoading = false,
  isError = false,
  isPending,
  onAcknowledge,
  onSilence,
  onUnmute,
  onResolve,
  onClose,
}: Readonly<AlertDetailPanelProps>) {
  if (isLoading) {
    return (
      <aside className={styles.panel} aria-label="Alert details">
        <output className={styles.message}>Loading alert…</output>
      </aside>
    );
  }

  if (isError || !alert) {
    return (
      <aside className={styles.panel} aria-label="Alert details">
        <div className={styles.message} role="alert">
          <p>Alert not found</p>
          <Button type="button" variant="secondary" size="sm" onClick={onClose}>
            Close
          </Button>
        </div>
      </aside>
    );
  }

  const actions = getAvailableAlertActions(alert.state);
  const metadataEntries = Object.entries(alert.metadata ?? {});
  const processInstanceId = alert.metadata?.processInstanceId?.trim() || undefined;

  const stateCfg = getAlertStateBadgeConfig(alert.state);
  const StateIcon = stateCfg.icon;
  const severityCfg = alert.severity ? getAlertSeverityBadgeConfig(alert.severity) : null;
  const SeverityIcon = severityCfg?.icon;

  const runAction = async (action: AlertAction) => {
    try {
      switch (action) {
        case 'acknowledge':
          await onAcknowledge({ id: alert.id, actor: actorEmail });
          break;
        case 'silence':
          await onSilence({ id: alert.id, actor: actorEmail });
          break;
        case 'unmute':
          await onUnmute(alert.id);
          break;
        case 'resolve':
          await onResolve(alert.id);
          break;
      }
    } catch {
      // Mutation callbacks own user-facing error handling.
    }
  };

  return (
    <aside className={styles.panel} aria-label="Alert details">
      <header className={styles.header}>
        <div className={styles.summary}>
          <Badge variant={stateCfg.variant} icon={<StateIcon />}>{alert.state}</Badge>
          {severityCfg && SeverityIcon ? (
            <Badge variant={severityCfg.variant} icon={<SeverityIcon />}>{alert.severity}</Badge>
          ) : (
            <Badge variant="secondary">No severity</Badge>
          )}
          <span className={styles.count}>
            {alert.count} {alert.count === 1 ? 'occurrence' : 'occurrences'}
          </span>
        </div>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          onClick={onClose}
          aria-label="Close alert details"
        >
          <X size={18} aria-hidden="true" />
        </Button>
      </header>

      <div className={styles.content}>
        <section className={styles.section} aria-labelledby="alert-content-heading">
          <h2 id="alert-content-heading" className={styles.subject}>
            {alert.subject ?? 'Untitled alert'}
          </h2>
          <AlertBody text={alert.body ?? 'No alert body provided.'} />
        </section>

        <section className={styles.section} aria-labelledby="alert-metadata-heading">
          <h3 id="alert-metadata-heading" className={styles.sectionTitle}>
            Metadata
          </h3>
          <dl className={styles.definitionList}>
            <Detail label="Source" value={alert.source} />
            <Detail label="Alert key" value={alert.alertKey} />
            <Detail label="Fingerprint" value={alert.fingerprint} />
            <Detail
              label="Process"
              value={alert.metadata?.processDefinitionId}
            />
            <Detail
              label="Process instance"
              value={
                processInstanceId ? (
                  <Link
                    to={`/processes/${encodeURIComponent(processInstanceId)}`}
                    className={commonStyles.link}
                  >
                    {processInstanceId}
                  </Link>
                ) : undefined
              }
            />
            <Detail label="Version" value={getAlertVersion(alert.metadata)} />
            <Detail
              label="Resend interval"
              value={
                alert.resendIntervalMs === undefined ? undefined : `${alert.resendIntervalMs} ms`
              }
            />
            {metadataEntries
              .filter(([key]) => !HIDDEN_METADATA_KEYS.has(key))
              .map(([key, value]) => (
                <Detail key={key} label={key} value={value} />
              ))}
          </dl>
        </section>

        <section className={styles.section} aria-labelledby="alert-recipients-heading">
          <h3 id="alert-recipients-heading" className={styles.sectionTitle}>
            Recipients
          </h3>
          <dl className={styles.definitionList}>
            <Detail label="To" value={formatRecipients(alert.recipients?.to)} />
            <Detail label="Cc" value={formatRecipients(alert.recipients?.cc)} />
            <Detail label="Bcc" value={formatRecipients(alert.recipients?.bcc)} />
          </dl>
        </section>

        <section className={`${styles.section} ${styles.lastSection}`} aria-labelledby="alert-lifecycle-heading">
          <h3 id="alert-lifecycle-heading" className={styles.sectionTitle}>
            Lifecycle
          </h3>
          <dl className={styles.definitionList}>
            <Detail label="Created" value={formatDate(alert.createdAt)} />
            <Detail label="Updated" value={formatDate(alert.updatedAt)} />
            <Detail label="Last triggered" value={formatDate(alert.lastTriggeredAt)} />
            <Detail label="Acknowledged" value={formatDate(alert.acknowledgedAt)} />
            <Detail label="Acknowledged by" value={alert.acknowledgedBy} />
            <Detail label="Silenced" value={formatDate(alert.silencedAt)} />
            <Detail label="Silenced by" value={alert.silencedBy} />
            <Detail label="Resolved" value={formatDate(alert.resolvedAt)} />
          </dl>
        </section>
      </div>

      {actions.length > 0 && (
        <footer className={styles.footer}>
          {actions.map((action) => (
            <Button
              key={action}
              type="button"
              variant={ACTION_VARIANTS[action]}
              size="sm"
              disabled={isPending}
              onClick={() => void runAction(action)}
            >
              {ACTION_LABELS[action]}
            </Button>
          ))}
        </footer>
      )}
    </aside>
  );
}

function AlertBody({ text }: Readonly<{ text: string }>) {
  const [expanded, setExpanded] = useState(false);
  const needsTruncate = text.length > BODY_PREVIEW_CHARS;
  const visibleText =
    !needsTruncate || expanded
      ? text
      : `${text.slice(0, BODY_PREVIEW_CHARS).trimEnd()}…`;

  return (
    <div className={styles.bodyBlock}>
      <p className={styles.body}>{visibleText}</p>
      {needsTruncate && (
        <button
          type="button"
          className={styles.loadMore}
          aria-expanded={expanded}
          onClick={() => setExpanded((current) => !current)}
        >
          {expanded ? 'Show less' : 'Load more'}
        </button>
      )}
    </div>
  );
}

function Detail({ label, value }: Readonly<{ label: string; value?: ReactNode }>) {
  return (
    <div className={styles.detail}>
      <dt>{label}</dt>
      <dd>{value || '—'}</dd>
    </div>
  );
}
