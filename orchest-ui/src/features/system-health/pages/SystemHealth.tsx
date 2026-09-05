import { healthService } from '@/api/domains/health/healthService';
import type { ActuatorHealthResponse, DiskSpaceDetails, HealthComponent, HealthStatus } from '@/api/domains/health/healthService';
import { Button } from '@/design-system/components/ui/button';
import { Label } from '@/design-system/components/ui/label';
import { Switch } from '@/design-system/components/ui/switch';
import { environment } from '@/shared/constants/environment';
import AppLayout from '@/shared/layouts/AppLayout';
import { useQuery } from '@tanstack/react-query';
import {
  Activity,
  AlertTriangle,
  Database,
  HardDrive,
  Heart,
  Radio,
  ServerCrash,
  ShieldCheck,
} from 'lucide-react';
import React, { useState } from 'react';
import styles from './SystemHealth.module.css';

// ── Helpers ───────────────────────────────────────────────────────────────────

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  return `${(bytes / Math.pow(1024, i)).toFixed(1)} ${units[i]}`;
}

function pillClass(status: HealthStatus): string {
  if (status === 'UP') return styles.pillUp;
  if (status === 'DOWN') return styles.pillDown;
  return styles.pillUnknown;
}

// ── StatusPill ────────────────────────────────────────────────────────────────

const StatusPill: React.FC<{ status: HealthStatus }> = ({ status }) => (
  <span className={`${styles.statusPill} ${pillClass(status)}`}>
    <span className={styles.statusDot} />
    {status}
  </span>
);

// ── Card shell ────────────────────────────────────────────────────────────────

interface CardShellProps {
  icon: React.ReactNode;
  name: string;
  status: HealthStatus;
  compact?: boolean;
  children?: React.ReactNode;
}

const CardShell: React.FC<CardShellProps> = ({ icon, name, status, compact, children }) => (
  <div className={`${styles.card} ${compact ? styles.cardCompact : ''}`}>
    <div className={styles.cardHeader}>
      <div className={styles.cardHeaderLeft}>
        <div className={styles.cardIconWrap}>{icon}</div>
        <p className={styles.cardName}>{name}</p>
      </div>
      <StatusPill status={status} />
    </div>
    {children}
  </div>
);

// ── Liveness / Readiness card ─────────────────────────────────────────────────

const SimpleStateCard: React.FC<{ name: string; icon: React.ReactNode; status: HealthStatus }> = ({
  name,
  icon,
  status,
}) => (
  <CardShell icon={icon} name={name} status={status} compact />
);

// ── Mongo / Kafka card ────────────────────────────────────────────────────────

const VersionCard: React.FC<{
  name: string;
  icon: React.ReactNode;
  component: HealthComponent<{ version?: string }>;
}> = ({ name, icon, component }) => (
  <CardShell icon={icon} name={name} status={component.status} compact>
    {component.details?.version && (
      <span className={styles.compactDetail}>v{component.details.version}</span>
    )}
  </CardShell>
);

// ── Disk space card ───────────────────────────────────────────────────────────

const CIRCLE_R = 52;
const CIRCLE_CIRC = 2 * Math.PI * CIRCLE_R;

const DiskSpaceCard: React.FC<{ component: HealthComponent<DiskSpaceDetails> }> = ({ component }) => {
  const d = component.details;
  if (!d) return <CardShell icon={<HardDrive className={styles.cardIcon} />} name="Disk Space" status={component.status} />;

  const used = d.total - d.free;
  const usedPct = d.total > 0 ? used / d.total : 0;
  const freePct = d.total > 0 ? d.free / d.total : 0;
  const isNearThreshold = d.free <= d.threshold * 2;

  const usedOffset = CIRCLE_CIRC * (1 - usedPct);

  return (
    <CardShell icon={<HardDrive className={styles.cardIcon} />} name="Disk Space" status={component.status}>
      <div className={styles.diskBody}>
        <div className={styles.diskArcWrap}>
          <div className={styles.diskArc}>
            <svg width={140} height={140} viewBox="0 0 140 140" className={styles.diskArcSvg}>
              <circle cx={70} cy={70} r={CIRCLE_R} className={styles.diskArcTrack} />
              {/* used (red) */}
              <circle
                cx={70}
                cy={70}
                r={CIRCLE_R}
                className={styles.diskArcFillUsed}
                strokeDasharray={CIRCLE_CIRC}
                strokeDashoffset={usedOffset}
              />
              {/* free (green) drawn on top as a capped arc at the free portion */}
              <circle
                cx={70}
                cy={70}
                r={CIRCLE_R}
                className={styles.diskArcFillFree}
                strokeDasharray={`${CIRCLE_CIRC * freePct} ${CIRCLE_CIRC}`}
                strokeDashoffset={0}
              />
            </svg>
            <div className={styles.diskArcCenter}>
              <span className={styles.diskArcCenterUsed}>{Math.round(usedPct * 100)}%</span>
              <span className={styles.diskArcCenterLabel}>used</span>
            </div>
          </div>
        </div>

        <div className={styles.diskLegend}>
          <div className={styles.diskLegendRow}>
            <span className={`${styles.diskLegendDot} ${styles.diskLegendDotUsed}`} />
            <span className={styles.diskLegendLabel}>Used</span>
            <span className={styles.diskLegendValue}>{formatBytes(used)}</span>
          </div>
          <div className={styles.diskLegendRow}>
            <span className={`${styles.diskLegendDot} ${styles.diskLegendDotFree}`} />
            <span className={styles.diskLegendLabel}>Free</span>
            <span className={styles.diskLegendValue}>{formatBytes(d.free)}</span>
          </div>
          <div className={styles.diskLegendRow}>
            <span className={`${styles.diskLegendDot} ${styles.diskLegendDotThreshold}`} />
            <span className={styles.diskLegendLabel}>Total</span>
            <span className={styles.diskLegendValue}>{formatBytes(d.total)}</span>
          </div>
        </div>

        {isNearThreshold && (
          <div className={styles.diskThresholdRow}>
            <AlertTriangle className={styles.diskThresholdIcon} />
            <span className={styles.diskThresholdText}>
              Free space ({formatBytes(d.free)}) is near threshold ({formatBytes(d.threshold)})
            </span>
          </div>
        )}
      </div>
    </CardShell>
  );
};

// ── Hero banner ───────────────────────────────────────────────────────────────

const HeroBanner: React.FC<{ status: HealthStatus }> = ({ status }) => {
  const isUp = status === 'UP';
  const isDown = status === 'DOWN';

  const bannerClass = isUp
    ? styles.heroBannerUp
    : isDown
    ? styles.heroBannerDown
    : styles.heroBannerUnknown;

  const iconWrapClass = isUp
    ? styles.heroIconWrapUp
    : isDown
    ? styles.heroIconWrapDown
    : styles.heroIconWrapUnknown;

  const iconClass = isUp
    ? styles.heroIconUp
    : isDown
    ? styles.heroIconDown
    : styles.heroIconUnknown;

  const labelClass = isUp
    ? styles.heroLabelStatusUp
    : isDown
    ? styles.heroLabelStatusDown
    : styles.heroLabelStatusUnknown;

  return (
    <div className={`${styles.heroBanner} ${bannerClass}`}>
      <div className={`${styles.heroIconWrap} ${iconWrapClass}`}>
        {isUp ? (
          <Heart className={`${styles.heroIcon} ${iconClass}`} />
        ) : isDown ? (
          <ServerCrash className={`${styles.heroIcon} ${iconClass}`} />
        ) : (
          <Activity className={`${styles.heroIcon} ${iconClass}`} />
        )}
      </div>
      <div className={styles.heroLabel}>
        <p className={styles.heroLabelTitle}>Overall System Status</p>
        <p className={`${styles.heroLabelStatus} ${labelClass}`}>{status}</p>
      </div>
    </div>
  );
};

// ── Main page ─────────────────────────────────────────────────────────────────

const SystemHealth: React.FC = () => {
  const [autoRefresh, setAutoRefresh] = useState(false);
  const isConfigured = !!environment.engineActuatorUrl;

  const { data, isLoading, error, refetch, dataUpdatedAt } = useQuery<ActuatorHealthResponse>({
    queryKey: ['system-health'],
    queryFn: () => healthService.getHealth(),
    enabled: isConfigured,
    refetchInterval: autoRefresh ? 30_000 : false,
  });

  const lastUpdated = dataUpdatedAt
    ? new Date(dataUpdatedAt).toLocaleTimeString()
    : null;

  const components = data?.components ?? {};

  return (
    <AppLayout>
      <div className={styles.page}>
        {/* Header */}
        <div className={styles.header}>
          <div className={styles.titleRow}>
            <Activity className={styles.titleIcon} />
            <div className={styles.titleText}>
              <h1 className={styles.title}>System Status</h1>
              <p className={styles.subtitle}>Live health of engine components</p>
            </div>
          </div>
          <div className={styles.headerActions}>
            {lastUpdated && (
              <span className={styles.lastUpdated}>Last updated {lastUpdated}</span>
            )}
            <div className={styles.autoRefreshRow}>
              <Switch
                id="auto-refresh"
                checked={autoRefresh}
                onCheckedChange={setAutoRefresh}
              />
              <Label htmlFor="auto-refresh">Auto-refresh</Label>
            </div>
            <Button
              variant="outline"
              size="sm"
              label="Refresh"
              buttonIcon="refresh"
              onClick={() => void refetch()}
              disabled={isLoading || !isConfigured}
            />
          </div>
        </div>

        {/* Body */}
        <div className={styles.body}>
          {/* Config warning */}
          {!isConfigured && (
            <div className={styles.configWarning}>
              <AlertTriangle className={styles.configWarningIcon} />
              <span className={styles.configWarningText}>
                <strong>Actuator URL not configured.</strong> Set{' '}
                <code className={styles.configWarningCode}>VITE_ENGINE_ACTUATOR_URL</code> in your
                environment (e.g.{' '}
                <code className={styles.configWarningCode}>
                  http://localhost:6200/orchest/actuator/health
                </code>
                ) to enable this page.
              </span>
            </div>
          )}

          {/* Loading */}
          {isConfigured && isLoading && (
            <div className={styles.loadingRow}>
              <div className={styles.spinner} />
              <span>Fetching health data…</span>
            </div>
          )}

          {/* Error */}
          {isConfigured && !isLoading && error && (
            <div className={styles.errorCard}>
              <ServerCrash className={styles.errorIcon} />
              <p className={styles.errorTitle}>Unable to reach actuator endpoint</p>
              <p className={styles.errorMsg}>
                {error instanceof Error ? error.message : 'Unknown error occurred'}
              </p>
              <Button variant="outline" size="sm" label="Retry" onClick={() => void refetch()} />
            </div>
          )}

          {/* Data */}
          {data && (
            <>
              <HeroBanner status={data.status} />

              <div style={{ height: 'var(--spacing-4)' }} />

              <div className={styles.cardsGrid}>
                {components.livenessState && (
                  <SimpleStateCard
                    name="Liveness"
                    icon={<Heart className={styles.cardIcon} />}
                    status={components.livenessState.status}
                  />
                )}
                {components.readinessState && (
                  <SimpleStateCard
                    name="Readiness"
                    icon={<ShieldCheck className={styles.cardIcon} />}
                    status={components.readinessState.status}
                  />
                )}
                {components.mongo && (
                  <VersionCard
                    name="MongoDB"
                    icon={<Database className={styles.cardIcon} />}
                    component={components.mongo as HealthComponent<{ version?: string }>}
                  />
                )}
                {components.kafka && (
                  <VersionCard
                    name="Kafka"
                    icon={<Radio className={styles.cardIcon} />}
                    component={components.kafka as HealthComponent<{ version?: string }>}
                  />
                )}
              </div>

              {components.diskSpace && (
                <div className={styles.diskCardWrap}>
                  <DiskSpaceCard
                    component={components.diskSpace as HealthComponent<DiskSpaceDetails>}
                  />
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </AppLayout>
  );
};

export default React.memo(SystemHealth);