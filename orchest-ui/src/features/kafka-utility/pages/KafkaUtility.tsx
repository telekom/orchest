import { kafkaService } from '@/api/domains/kafka/kafkaService';
import type { ClusterInfo, ProcessHealth, ResourceStatus, WorkerInfo } from '@/api/domains/kafka/kafkaService';
import { Button } from '@/design-system/components/ui/button';
import AppLayout from '@/shared/layouts/AppLayout';
import { useQuery } from '@tanstack/react-query';
import {
  Activity,
  AlertCircle,
  CheckCircle2,
  CircleX,
  Database,
  Hash,
  Info,
  Layers,
  List,
  Radio,
  Server,
  ShieldCheck,
  Users,
  XCircle,
} from 'lucide-react';
import React, { useState } from 'react';
import styles from './KafkaUtility.module.css';

type Tab = 'cluster' | 'health' | 'worker';

// ── Bool pill ────────────────────────────────────────────────────────────────

const BoolPill: React.FC<{ value: boolean; trueLabel?: string; falseLabel?: string }> = ({
  value,
  trueLabel = 'Present',
  falseLabel = 'Absent',
}) => (
  <span className={`${styles.boolPill} ${value ? styles.boolPillTrue : styles.boolPillFalse}`}>
    {value ? <CheckCircle2 size={10} /> : <XCircle size={10} />}
    {value ? trueLabel : falseLabel}
  </span>
);

// ── Bool row ─────────────────────────────────────────────────────────────────

const BoolRow: React.FC<{ label: string; value: boolean }> = ({ label, value }) => (
  <div className={styles.boolRow}>
    <span className={styles.boolLabel}>{label}</span>
    <BoolPill value={value} />
  </div>
);

// ── Loading / error helpers ───────────────────────────────────────────────────

const Loading: React.FC = () => (
  <div className={styles.loadingRow}>
    <div className={styles.spinner} />
    <span>Fetching data…</span>
  </div>
);

const ErrorBanner: React.FC<{ message?: string; onRetry?: () => void }> = ({ message, onRetry }) => (
  <div className={styles.errorBanner}>
    <AlertCircle className={styles.errorIcon} />
    <span>{message ?? 'Failed to load data. Please try again.'}</span>
    {onRetry && (
      <Button variant="ghost" size="sm" label="Retry" onClick={onRetry} style={{ marginLeft: 'auto' }} />
    )}
  </div>
);

// ── Cluster Info tab ──────────────────────────────────────────────────────────

const ClusterInfoTab: React.FC = () => {
  const { data, isLoading, error, refetch } = useQuery<{ data: ClusterInfo }>({
    queryKey: ['kafka', 'cluster-info'],
    queryFn: () => kafkaService.getClusterInfo(),
  });

  if (isLoading) return <Loading />;
  if (error || !data?.data) return <ErrorBanner onRetry={() => void refetch()} />;

  const { topics, consumerGroups } = data.data;

  return (
    <div className={styles.clusterGrid}>
      <div className={styles.listCard}>
        <div className={styles.listCardHeader}>
          <Radio className={styles.listCardHeaderIcon} />
          <span className={styles.listCardTitle}>Topics</span>
          <span className={styles.listCardCount}>{topics.length}</span>
        </div>
        <div className={styles.listCardBody}>
          {topics.length === 0 ? (
            <div className={styles.empty}>
              <Database className={styles.emptyIcon} />
              <span className={styles.emptyText}>No topics found</span>
            </div>
          ) : (
            topics.map((t) => (
              <div key={t} className={styles.listItem}>
                <div className={styles.listItemDot} />
                {t}
              </div>
            ))
          )}
        </div>
      </div>

      <div className={styles.listCard}>
        <div className={styles.listCardHeader}>
          <Users className={styles.listCardHeaderIcon} />
          <span className={styles.listCardTitle}>Consumer Groups</span>
          <span className={styles.listCardCount}>{consumerGroups.length}</span>
        </div>
        <div className={styles.listCardBody}>
          {consumerGroups.length === 0 ? (
            <div className={styles.empty}>
              <Users className={styles.emptyIcon} />
              <span className={styles.emptyText}>No consumer groups found</span>
            </div>
          ) : (
            consumerGroups.map((g) => (
              <div key={g} className={styles.listItem}>
                <div className={styles.listItemDot} />
                {g}
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

// ── Process Health tab ────────────────────────────────────────────────────────

const ResourceExistsIcon: React.FC<{ exists: boolean }> = ({ exists }) =>
  exists ? (
    <CheckCircle2 size={16} className={styles.resourceExistsTrue} />
  ) : (
    <CircleX size={16} className={styles.resourceExistsFalse} />
  );

const ResourceRow: React.FC<{ label: string; rs: ResourceStatus | undefined }> = ({ label, rs }) => (
  <div className={styles.resourceRow}>
    <ResourceExistsIcon exists={rs?.exists ?? false} />
    <div className={styles.resourceInfo}>
      <span className={styles.resourceLabel}>{label}</span>
      {rs?.name && <span className={styles.resourceId}>{rs.name}</span>}
    </div>
    <BoolPill value={rs?.exists ?? false} trueLabel="Exists" falseLabel="Missing" />
  </div>
);

const ClientConsumerGroupsRow: React.FC<{ groups: string[] }> = ({ groups }) => {
  const hasGroups = groups.length > 0;
  return (
    <div className={styles.resourceRow}>
      <ResourceExistsIcon exists={hasGroups} />
      <div className={styles.resourceInfo}>
        <span className={styles.resourceLabel}>Consumer Groups</span>
        {hasGroups ? (
          <span className={styles.resourceId}>{groups.join(', ')}</span>
        ) : (
          <span className={styles.resourceId}>None</span>
        )}
      </div>
      <BoolPill value={hasGroups} trueLabel={`${groups.length} found`} falseLabel="Missing" />
    </div>
  );
};

const ProcessHealthCard: React.FC<{ ph: ProcessHealth }> = ({ ph }) => (
  <div className={styles.processHealthRow}>
    <div className={styles.processHealthRowHeader}>
      <span className={styles.processId}>{ph.processDefinitionId}</span>
      <span className={`${styles.healthBadge} ${ph.healthy ? styles.healthBadgeHealthy : styles.healthBadgeUnhealthy}`}>
        <span className={`${styles.healthDot} ${ph.healthy ? styles.healthDotGreen : styles.healthDotRed}`} />
        {ph.healthy ? 'Healthy' : 'Unhealthy'}
      </span>
    </div>
    <div className={styles.resourceSections}>
      <div className={styles.resourceSection}>
        <span className={styles.resourceSectionTitle}>Client</span>
        <ResourceRow label="Topic" rs={ph.clientWorkerEventTopic} />
        <ClientConsumerGroupsRow groups={ph.clientConsumerGroups ?? []} />
      </div>
      <div className={styles.resourceSectionDivider} />
      <div className={styles.resourceSection}>
        <span className={styles.resourceSectionTitle}>Server</span>
        <ResourceRow label="Topic" rs={ph.serverWorkerEventTopic} />
        <ResourceRow label="Consumer Group" rs={ph.serverWorkerEventConsumerGroup} />
      </div>
    </div>
  </div>
);

const ProcessHealthTab: React.FC = () => {
  const { data, isLoading, error, refetch } = useQuery<{ data: ProcessHealth[] }>({
    queryKey: ['kafka', 'process-health'],
    queryFn: () => kafkaService.getProcessHealth(),
  });

  if (isLoading) return <Loading />;
  if (error || !data?.data) return <ErrorBanner onRetry={() => void refetch()} />;

  const list = data.data;
  const healthyCount = list.filter((p) => p.healthy).length;

  if (list.length === 0) {
    return (
      <div className={styles.empty}>
        <Activity className={styles.emptyIcon} />
        <span className={styles.emptyText}>No process health data available</span>
      </div>
    );
  }

  return (
    <>
      <div className={styles.grid2} style={{ marginBottom: 'var(--spacing-4)' }}>
        <div className={styles.card}>
          <div className={styles.cardHeader}>
            <ShieldCheck className={styles.cardIcon} />
            <span className={styles.cardTitle}>Healthy Processes</span>
          </div>
          <div style={{ fontSize: 'var(--text-h2)', fontWeight: 'var(--font-weight-bold)', color: 'hsl(142 65% 45%)' }}>
            {healthyCount} / {list.length}
          </div>
        </div>
        <div className={styles.card}>
          <div className={styles.cardHeader}>
            <AlertCircle className={styles.cardIcon} />
            <span className={styles.cardTitle}>Unhealthy Processes</span>
          </div>
          <div style={{ fontSize: 'var(--text-h2)', fontWeight: 'var(--font-weight-bold)', color: 'hsl(0 72% 50%)' }}>
            {list.length - healthyCount} / {list.length}
          </div>
        </div>
      </div>
      <div className={styles.processHealthList}>
        {list.map((ph) => (
          <ProcessHealthCard key={ph.processDefinitionId} ph={ph} />
        ))}
      </div>
    </>
  );
};

// ── Worker Info tab ───────────────────────────────────────────────────────────

const TopicInfoCard: React.FC<{ title: string; icon: React.ReactNode; info: WorkerInfo['serverWorkerEvent'] }> = ({
  title,
  icon,
  info,
}) => (
  <div className={styles.card}>
    <div className={styles.cardHeader}>
      {icon}
      <span className={styles.cardTitle}>{title}</span>
    </div>
    <p className={styles.cardSubtitle} style={{ marginBottom: 'var(--spacing-3)' }}>
      <strong>Topic:</strong> {info.topic}
    </p>
    <p className={styles.cardSubtitle} style={{ marginBottom: 'var(--spacing-3)' }}>
      <strong>Consumer Group:</strong> {info.consumerGroup}
    </p>
    <BoolRow label="Topic Exists" value={info.topicExists} />
    <BoolRow label="Consumer Group Exists" value={info.consumerGroupExists} />
  </div>
);

const ClientTopicInfoCard: React.FC<{
  icon: React.ReactNode;
  info: WorkerInfo['clientWorkerEvent'];
  consumerGroups: string[];
  consumerStatus: string;
}> = ({ icon, info, consumerGroups, consumerStatus }) => {
  const hasGroups = consumerGroups.length > 0;
  return (
    <div className={styles.card}>
      <div className={styles.cardHeader}>
        {icon}
        <span className={styles.cardTitle}>Client Worker Event</span>
      </div>
      <p className={styles.cardSubtitle} style={{ marginBottom: 'var(--spacing-3)' }}>
        <strong>Topic:</strong> {info.topic}
      </p>
      <BoolRow label="Topic Exists" value={info.topicExists} />
      <div className={styles.boolRow}>
        <span className={styles.boolLabel}>Consumer Groups</span>
        <BoolPill value={hasGroups} trueLabel={`${consumerGroups.length} found`} falseLabel="Missing" />
      </div>
      {hasGroups && (
        <div className={styles.consumerGroupList}>
          {consumerGroups.map((g) => (
            <span key={g} className={styles.consumerGroupTag}>{g}</span>
          ))}
        </div>
      )}
      {consumerStatus && (
        <p className={styles.cardSubtitle} style={{ marginTop: 'var(--spacing-2)' }}>
          <strong>Status:</strong> {consumerStatus}
        </p>
      )}
    </div>
  );
};

const WorkerInfoTab: React.FC = () => {
  const [processId, setProcessId] = useState('');
  const [submittedId, setSubmittedId] = useState('');

  const { data, isLoading, error, refetch } = useQuery<{ data: WorkerInfo }>({
    queryKey: ['kafka', 'worker-info', submittedId],
    queryFn: () => kafkaService.getWorkerInfo(submittedId),
    enabled: !!submittedId,
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (processId.trim()) setSubmittedId(processId.trim());
  };

  return (
    <>
      <form onSubmit={handleSubmit} className={styles.workerInputRow}>
        <div className={styles.inputGroup}>
          <label className={styles.inputLabel} htmlFor="processDefId">
            Process Definition ID
          </label>
          <input
            id="processDefId"
            className={styles.input}
            type="text"
            placeholder="e.g. my-process"
            value={processId}
            onChange={(e) => setProcessId(e.target.value)}
          />
        </div>
        <Button type="submit" variant="primary" size="sm" label="Inspect" disabled={!processId.trim()} />
      </form>

      {!submittedId && (
        <div className={styles.empty}>
          <Hash className={styles.emptyIcon} />
          <span className={styles.emptyText}>Enter a Process Definition ID to inspect its worker topics</span>
        </div>
      )}

      {submittedId && isLoading && <Loading />}
      {submittedId && error && <ErrorBanner onRetry={() => void refetch()} />}

      {data?.data && (
        <>
          <div className={styles.card} style={{ marginBottom: 'var(--spacing-4)' }}>
            <div className={styles.cardHeader}>
              <Info className={styles.cardIcon} />
              <span className={styles.cardTitle}>Process Definition</span>
            </div>
            <p className={styles.cardSubtitle}>{data.data.processDefinitionId}</p>
          </div>
          <div className={styles.grid2}>
            <ClientTopicInfoCard
              icon={<Server className={styles.cardIcon} />}
              info={data.data.clientWorkerEvent}
              consumerGroups={data.data.clientConsumerGroups ?? []}
              consumerStatus={data.data.clientConsumerStatus ?? ''}
            />
            <TopicInfoCard
              title="Server Worker Event"
              icon={<Layers className={styles.cardIcon} />}
              info={data.data.serverWorkerEvent}
            />
          </div>
        </>
      )}
    </>
  );
};

// ── Page ──────────────────────────────────────────────────────────────────────

const TABS: { id: Tab; label: string; icon: React.ReactNode }[] = [
  { id: 'cluster', label: 'Cluster Info', icon: <Database className={styles.tabIcon} /> },
  { id: 'health', label: 'Process Health', icon: <Activity className={styles.tabIcon} /> },
  { id: 'worker', label: 'Worker Info', icon: <List className={styles.tabIcon} /> },
];

const KafkaUtility: React.FC = () => {
  const [activeTab, setActiveTab] = useState<Tab>('cluster');

  return (
    <AppLayout>
      <div className={styles.page}>
        <div className={styles.header}>
          <div className={styles.titleRow}>
            <Radio className={styles.titleIcon} />
            <div>
              <h1 className={styles.title}>Kafka Utility</h1>
              <p className={styles.subtitle}>Inspect Kafka cluster, process health and worker topics</p>
            </div>
          </div>
          <Button
            variant="outline"
            size="sm"
            label="Refresh"
            buttonIcon="refresh"
            onClick={() => window.location.reload()}
          />
        </div>

        <div className={styles.tabList}>
          {TABS.map((t) => (
            <button
              key={t.id}
              type="button"
              className={`${styles.tab} ${activeTab === t.id ? styles.tabActive : ''}`}
              onClick={() => setActiveTab(t.id)}
            >
              {t.icon}
              {t.label}
            </button>
          ))}
        </div>

        <div className={styles.content}>
          {activeTab === 'cluster' && <ClusterInfoTab />}
          {activeTab === 'health' && <ProcessHealthTab />}
          {activeTab === 'worker' && <WorkerInfoTab />}
        </div>
      </div>
    </AppLayout>
  );
};

export default React.memo(KafkaUtility);
