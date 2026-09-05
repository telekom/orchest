import { Button } from '@/design-system/components/ui/button';
import { AlertDetailPanel } from '@/features/alert-management/components/AlertDetailPanel/AlertDetailPanel';
import AlertFilterBar from '@/features/alert-management/components/AlertFilterBar/AlertFilterBar';
import { AlertFiringChart } from '@/features/alert-management/components/AlertFiringChart/AlertFiringChart';
import { AlertKpiStrip } from '@/features/alert-management/components/AlertKpiStrip/AlertKpiStrip';
import { AlertStateShare } from '@/features/alert-management/components/AlertStateShare/AlertStateShare';
import { AlertTable } from '@/features/alert-management/components/AlertTable/AlertTable';
import { useAlertListUrlState } from '@/features/alert-management/hooks/useAlertListUrlState';
import { useAlertsData } from '@/features/alert-management/hooks/useAlertsData';
import { useAuth } from '@/shared/auth';
import commonStyles from '@/shared/styles/common.module.css';
import { AlertCircle, ChevronRight } from 'lucide-react';
import React, { useState } from 'react';
import styles from '../../pages/AlertsPage/AlertsPage.module.css';

/**
 * Full Alert Management workspace (no AppLayout) — used on Dashboard tabs
 * and the standalone /alerts redirect target.
 */
export const AlertsWorkspace: React.FC = () => {
  const { user } = useAuth();
  const { filters, setFilters, clearFilters, hasActiveAlertFilters } =
    useAlertListUrlState();
  const {
    listQuery,
    firingStatsQuery,
    stateCounts,
    stateCountsLoading,
    detailQuery,
    acknowledgeMutation,
    silenceMutation,
    unmuteMutation,
    resolveMutation,
  } = useAlertsData(filters);

  const page = listQuery.data;
  const actor = user?.email || 'unknown';
  const isMutationPending =
    acknowledgeMutation.isPending ||
    silenceMutation.isPending ||
    unmuteMutation.isPending ||
    resolveMutation.isPending;

  const [chartsOpen, setChartsOpen] = useState(false);

  const focusState = (state: 'FIRING' | 'SILENCED') =>
    setFilters({ state, page: 0 });

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <span className={styles.eyebrow}>
            <span className={styles.eyebrowDot} />
            Ops · Live radar
          </span>
          <h1 className={styles.title}>Alert Management</h1>
          <p className={styles.subtitle}>
            Track firing and silenced signals — volume, mix, and process hotspots
            in one ops desk.
          </p>
        </div>
        <div className={styles.headerRight}>
          <div className={styles.statePills} role="group" aria-label="Alert focus">
            <button
              type="button"
              className={`${styles.statePill} ${styles.firingPill} ${
                filters.state === 'FIRING' ? styles.statePillActive : ''
              }`}
              aria-pressed={filters.state === 'FIRING'}
              onClick={() => focusState('FIRING')}
            >
              Firing
            </button>
            <button
              type="button"
              className={`${styles.statePill} ${styles.silencedPill} ${
                filters.state === 'SILENCED' ? styles.statePillActive : ''
              }`}
              aria-pressed={filters.state === 'SILENCED'}
              onClick={() => focusState('SILENCED')}
            >
              Silenced
            </button>
          </div>
        </div>
      </header>

      <AlertKpiStrip
        counts={stateCounts}
        selectedState={filters.state}
        isLoading={stateCountsLoading}
        onStateClick={focusState}
      />

      {firingStatsQuery.isError && !firingStatsQuery.isLoading && (
        <ErrorBanner
          message="Unable to load firing alert statistics."
          onRetry={() => void firingStatsQuery.refetch()}
        />
      )}

      <div className={styles.chartsSection}>
        <button
          type="button"
          className={styles.chartsToggle}
          onClick={() => setChartsOpen((v) => !v)}
          aria-expanded={chartsOpen}
        >
          <ChevronRight
            size={16}
            className={`${styles.chartsChevron} ${chartsOpen ? styles.chartsChevronOpen : ''}`}
          />
          <span>Spectrum &amp; Balance</span>
        </button>
        <div className={`${styles.chartsCollapse} ${chartsOpen ? styles.chartsCollapseOpen : ''}`}>
          <div className={styles.charts}>
            <AlertFiringChart
              stats={firingStatsQuery.data}
              isLoading={firingStatsQuery.isLoading}
              onProcessClick={(processDefinitionId) =>
                setFilters({ processDefinitionId, page: 0 })
              }
            />
            <AlertStateShare
              counts={stateCounts}
              selectedState={filters.state}
              isLoading={stateCountsLoading}
              onStateClick={focusState}
            />
          </div>
        </div>
      </div>

      <section className={styles.deck} aria-label="Alert listing">
        <div className={styles.deckHeader}>
          <div>
            <p className={styles.deckKicker}>Inbox</p>
            <h2 className={styles.deckTitle}>
              {filters.state === 'FIRING' ? 'Firing alerts' : 'Silenced alerts'}
            </h2>
          </div>
          <AlertFilterBar
            filters={filters}
            setFilters={setFilters}
            clearFilters={clearFilters}
            hasActiveAlertFilters={hasActiveAlertFilters}
          />
        </div>

        {listQuery.isError && !listQuery.isLoading && (
          <ErrorBanner
            message="Unable to load alerts."
            onRetry={() => void listQuery.refetch()}
          />
        )}

        <div
          className={`${styles.split}${filters.alertId ? ` ${styles.splitWithDetail}` : ''}`}
        >
          <div className={styles.list}>
            <AlertTable
              alerts={page?.content ?? []}
              page={filters.page}
              pageSize={filters.size}
              totalElements={page?.totalElements ?? 0}
              totalPages={page?.totalPages ?? 0}
              sort={filters.sort}
              selectedAlertId={filters.alertId}
              isLoading={listQuery.isLoading}
              onSelect={(alertId) => setFilters({ alertId })}
              onSort={(sort) => setFilters({ sort, page: 0 })}
              onPageChange={(nextPage) => setFilters({ page: nextPage })}
              onPageSizeChange={(size) => setFilters({ size, page: 0 })}
            />
          </div>

          {filters.alertId && (
            <aside className={styles.detail} aria-label="Alert detail">
              <AlertDetailPanel
                alert={detailQuery.data}
                actorEmail={actor}
                isLoading={detailQuery.isLoading}
                isError={detailQuery.isError}
                isPending={isMutationPending}
                onAcknowledge={acknowledgeMutation.mutateAsync}
                onSilence={silenceMutation.mutateAsync}
                onUnmute={unmuteMutation.mutateAsync}
                onResolve={resolveMutation.mutateAsync}
                onClose={() => setFilters({ alertId: null })}
              />
            </aside>
          )}
        </div>
      </section>
    </div>
  );
};

function ErrorBanner({
  message,
  onRetry,
}: Readonly<{ message: string; onRetry: () => void }>) {
  return (
    <div className={commonStyles.errorBanner} role="alert">
      <AlertCircle className={commonStyles.errorIcon} aria-hidden="true" />
      <span className={commonStyles.errorText}>{message}</span>
      <Button
        type="button"
        variant="ghost"
        size="sm"
        className={commonStyles.retryButton}
        onClick={onRetry}
      >
        Retry
      </Button>
    </div>
  );
}

export default AlertsWorkspace;
