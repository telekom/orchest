import { DecisionPreviewCard } from '@/features/decision-management/components/DecisionPreviewCard/DecisionPreviewCard';
import { getDecisionColumns } from '@/features/decision-management/constants/decisionTableColumns';
import { DECISION_TRANSLATIONS } from '@/features/decision-management/constants/translations';
import { useDecisionStats, useDecisionXML, useInfiniteDecisionInstances, useProcessInstanceLink } from '@/features/decision-management/hooks';
import { useDecisionListUrlState } from '@/features/decision-management/hooks/useDecisionListUrlState';
import { resolveDecisionStatsTotal } from '@/features/process-management/utils/projectListStats';
import { useRoles } from '@/shared/auth';
import { DataTable, ResizableDetailsLayout } from '@/shared/components';
import { ActionIconButton } from "@/shared/components/ActionIconButton/ActionIconButton";
import type { InfiniteScrollConfig } from '@/shared/components/DataTable/types';
import AppLayout from '@/shared/layouts/AppLayout';
import commonStyles from '@/shared/styles/common.module.css';
import { DecisionInstance } from '@/shared/types';
import { Button } from "@/design-system/components/ui/button";
import { AlertCircle } from "lucide-react";
import clsx from "clsx";
import React, { useCallback, useMemo } from 'react';
import DecisionDiagramViewer from '../../components/DecisionDiagramViewer';
import DecisionFilterBar from '../../components/DecisionFilterBar/DecisionFilterBar';
import DecisionStatsGrid from '../../components/DecisionStatsGrid/DecisionStatsGrid';
import styles from './DmnList.module.css';

const INFINITE_SCROLL_PAGE_SIZE = 25;

const DmnList: React.FC = () => {
  const {
    decisionFilters,
    setFilters: setDecisionFilters,
    clearFilters: clearDecisionFilters,
    hasActiveFilters: hasActiveDecisionFilters,
  } = useDecisionListUrlState();

  const handleSetDecisionFilters = useCallback(
    (patch: Partial<typeof decisionFilters>, mode?: 'push' | 'replace') => {
      setDecisionFilters(
        {
          ...patch,
          ...(patch.searchText !== undefined && {
            searchText: patch.searchText ?? '',
          }),
        },
        mode,
      );
    },
    [setDecisionFilters],
  );
  const { handleProcessInstanceClick } = useProcessInstanceLink();
  const { canView } = useRoles();

  const filters = {
    decisionId: decisionFilters.decisionId || null,
    version: decisionFilters.version || null,
    status: decisionFilters.status || null,
    searchText: decisionFilters.searchText || null,
    from: decisionFilters.from || null,
    to: decisionFilters.to || null,
    timezone: decisionFilters.timezone || null,
  };

  const { stats, isLoading: loadingStats } = useDecisionStats({
    filters: {
      decisionId: filters.decisionId,
      version: filters.version,
      from: filters.from,
      to: filters.to,
      timezone: filters.timezone,
    },
  });

  const {
    instances,
    isLoading: loading,
    isFetchingNextPage,
    hasNextPage,
    totalLoaded,
    loadMore,
    error: instancesError,
    refetch: refetchInstances,
  } = useInfiniteDecisionInstances({
    filters,
    pageSize: INFINITE_SCROLL_PAGE_SIZE,
  });

  const displayTotal = useMemo(
    () => resolveDecisionStatsTotal(stats, decisionFilters.status) || totalLoaded,
    [stats, decisionFilters.status, totalLoaded],
  );

  const infiniteScrollConfig: InfiniteScrollConfig = {
    hasNextPage,
    isFetchingNextPage,
    onLoadMore: loadMore,
    totalElements: displayTotal,
    loadedElements: totalLoaded,
    loadedLabel: 'instances loaded',
  };

  const {
    dmnXml,
    isLoading: dmnLoading,
    error: diagramError,
    refetch: refetchDiagram,
  } = useDecisionXML({
    decisionId: filters.decisionId,
    version: filters.version,
  });

  const columns = useMemo(
    () => getDecisionColumns({ canView, handleProcessInstanceClick }),
    [canView, handleProcessInstanceClick]
  );

  const renderActions = (row: DecisionInstance) => (
    <div
      className={styles.actionsContainer}
      onClick={(e: React.MouseEvent) => e.stopPropagation()}
    >
      <DecisionPreviewCard decisionInstanceId={row.decisionInstanceId}>
        <ActionIconButton
          icon="visibility-on"
          to={`/decisions/${row.decisionInstanceId}`}
          title={DECISION_TRANSLATIONS.ARIA_VIEW_DECISION}
        />
      </DecisionPreviewCard>
    </div>
  );

  const getRowHref = (row: DecisionInstance) => `/decisions/${row.decisionInstanceId}`;

  const handleStatusClick = (status: string) => {
    setDecisionFilters({ status }, 'push');
  };

  const showDiagram = filters.decisionId && filters.version;

  return (
    <AppLayout>
      <div className={commonStyles.pageContainerMediumGap}>
        <DecisionStatsGrid
          stats={stats}
          isLoading={loadingStats}
          selectedStatus={decisionFilters.status ?? null}
          onStatusClick={handleStatusClick}
          onClearFilters={clearDecisionFilters}
          hasActiveFilters={hasActiveDecisionFilters}
        />

        <DecisionFilterBar
          decisionFilters={decisionFilters}
          setDecisionFilters={handleSetDecisionFilters}
          clearDecisionFilters={clearDecisionFilters}
          hasActiveDecisionFilters={hasActiveDecisionFilters}
        />

        {showDiagram ? (
          <div className={commonStyles.mainContent}>
            <ResizableDetailsLayout
              storageKey="dmn-list"
              defaultDiagramSize={30}
              minDiagramSize={10}
              maxDiagramSize={90}
              diagramSlot={
                diagramError && !dmnLoading ? (
                  <div className={commonStyles.errorBanner}>
                    <AlertCircle className={commonStyles.errorIcon} />
                    <span className={commonStyles.errorText}>
                      {DECISION_TRANSLATIONS.ERROR_LOADING_DIAGRAM}
                    </span>
                    <Button
                      variant="outline"
                      size="sm"
                      label={DECISION_TRANSLATIONS.RETRY}
                      onClick={refetchDiagram}
                      className={clsx(commonStyles.retryButton)}
                      aria-label={DECISION_TRANSLATIONS.ARIA_RETRY_DIAGRAM}
                    />
                  </div>
                ) : (
                  <DecisionDiagramViewer
                    decisionId={filters.decisionId}
                    version={filters.version}
                    diagramXml={dmnXml}
                    isLoading={dmnLoading}
                    height="100%"
                    minHeight="200px"
                  />
                )
              }
              contentSlot={
                <>
                  {instancesError && !loading && (
                    <div className={commonStyles.errorBanner}>
                      <AlertCircle className={commonStyles.errorIcon} />
                      <span className={commonStyles.errorText}>
                        {DECISION_TRANSLATIONS.ERROR_LOADING_INSTANCES}
                      </span>
                      <Button
                        variant="outline"
                        size="sm"
                        label={DECISION_TRANSLATIONS.RETRY}
                        onClick={refetchInstances}
                        className={clsx(commonStyles.retryButton)}
                        aria-label={DECISION_TRANSLATIONS.ARIA_RETRY_BUTTON}
                      />
                    </div>
                  )}
                  <DataTable<DecisionInstance>
                    data={instances}
                    columns={columns}
                    keyExtractor={(row) => row.decisionInstanceId}
                    loading={loading}
                    loadingText={DECISION_TRANSLATIONS.LOADING_DECISION_LIST}
                    emptyText={DECISION_TRANSLATIONS.EMPTY_DECISIONS}
                    getRowHref={getRowHref}
                    showCard={true}
                    stickyHeader={true}
                    maxHeight="100%"
                    renderActions={renderActions}
                    actionsHeader=""
                    infiniteScroll={infiniteScrollConfig}
                  />
                </>
              }
            />
          </div>
        ) : (
          <>
            {instancesError && !loading && (
              <div className={commonStyles.errorBanner}>
                <AlertCircle className={commonStyles.errorIcon} />
                <span className={commonStyles.errorText}>
                  {DECISION_TRANSLATIONS.ERROR_LOADING_INSTANCES}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  label={DECISION_TRANSLATIONS.RETRY}
                  onClick={refetchInstances}
                  className={clsx(commonStyles.retryButton)}
                  aria-label={DECISION_TRANSLATIONS.ARIA_RETRY_BUTTON}
                />
              </div>
            )}
            <div className={commonStyles.tableContainerFull}>
              <DataTable<DecisionInstance>
                data={instances}
                columns={columns}
                keyExtractor={(row) => row.decisionInstanceId}
                loading={loading}
                loadingText={DECISION_TRANSLATIONS.LOADING_DECISION_LIST}
                emptyText={DECISION_TRANSLATIONS.EMPTY_DECISIONS}
                getRowHref={getRowHref}
                showCard={true}
                stickyHeader={true}
                maxHeight="100%"
                renderActions={renderActions}
                actionsHeader=""
                infiniteScroll={infiniteScrollConfig}
              />
            </div>
          </>
        )}
      </div>
    </AppLayout>
  );
};

export default React.memo(DmnList);
