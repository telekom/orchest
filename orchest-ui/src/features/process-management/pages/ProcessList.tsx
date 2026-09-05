import { processInstanceService } from "@/api/domains";
import { ProcessInstanceDTO } from "@/api/types/orchest-api";
import { toast } from "@/design-system/components/ui/sonner";
import { PROCESS_TRANSLATIONS } from "@/features/process-management/constants/translations";
import { useRoles } from "@/shared/auth";
import { ResizableDetailsLayout } from "@/shared/components";
import type { InfiniteScrollConfig, SortConfig } from "@/shared/components/DataTable/types";
import { DEFAULT_PROCESS_LIST_URL_STATE } from "@/shared/url-state";
import { useExportToCSV } from "@/shared/hooks";
import AppLayout from "@/shared/layouts/AppLayout";
import commonStyles from '@/shared/styles/common.module.css';
import { Button } from "@/design-system/components/ui/button";
import clsx from "clsx";
import { AlertCircle } from "lucide-react";
import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import CompactProcessTable from "../components/CompactProcessTable/CompactProcessTable";
import ProcessDiagramViewer from "../components/ProcessDiagramViewer/ProcessDiagramViewer";
import ProcessFilterBar from "../components/ProcessFilterBar/ProcessFilterBar";
import ProcessStatsGrid from "../components/ProcessStatsGrid/ProcessStatsGrid";
import { useInfiniteProcessInstances } from "../hooks/useInfiniteProcessInstances";
import { useProcessListUrlState } from "../hooks/useProcessListUrlState";
import { useProcessDiagram } from "../hooks/useProcessDiagram";
import { useProcessStats } from "../hooks/useProcessStats";
import { ProcessInstance } from "../types/processInstance";
import { resolveProcessStatsTotal } from "../utils/projectListStats";

const CSV_COLUMN_MAPPING = {
  processId: "ID",
  processName: "Process Name",
  processVersion: "Version",
  status: "Status",
  startDate: "Start Date",
  endDate: "End Date",
} as const;

const INFINITE_SCROLL_PAGE_SIZE = 25;
const EXPORT_BATCH_SIZE = 200;

// Pure function - no need for useCallback
const formatCsvValue = (key: string, value: unknown): string => {
  if ((key === "startDate" || key === "endDate") && value) {
    return new Date(value as string).toLocaleString();
  }
  return String(value || "");
};

const ProcessList: React.FC = () => {
  const {
    processFilters,
    setFilters: setProcessFilters,
    clearFilters: clearProcessFilters,
    hasActiveFilters: hasActiveProcessFilters,
    sortConfig,
  } = useProcessListUrlState();
  const { isAdmin } = useRoles();
  const retryButtonRef = useRef<HTMLButtonElement>(null);

  const { stats, isLoading: loadingStats, refetch: refetchStats } = useProcessStats({
    filters: {
      process: processFilters.process,
      version: processFilters.version,
      from: processFilters.from,
      to: processFilters.to,
      timezone: processFilters.timezone,
    },
  });

  const {
    instances,
    isLoading: loadingInstances,
    isFetchingNextPage,
    hasNextPage,
    totalElements,
    totalLoaded,
    loadMore,
    refetch: refetchInstances,
    error: instancesError,
  } = useInfiniteProcessInstances({
    filters: processFilters,
    pageSize: INFINITE_SCROLL_PAGE_SIZE,
    sortConfig,
  });

  const {
    diagramXml,
    isLoading: diagramLoading,
    error: diagramError,
  } = useProcessDiagram(processFilters.process, processFilters.version);

  const { exportToCSV, isExporting } = useExportToCSV();
  const [isFetchingAllPages, setIsFetchingAllPages] = useState(false);

  // Focus retry button on error for accessibility
  useEffect(() => {
    if (instancesError && retryButtonRef.current) {
      retryButtonRef.current.focus();
    }
  }, [instancesError]);

  // Simple handlers - no memoization needed
  const handleTableRefresh = () => {
    refetchInstances();
    refetchStats();
  };

  const handleStatusClick = (status: string) => {
    setProcessFilters(
      {
        status,
        sortBy: DEFAULT_PROCESS_LIST_URL_STATE.sortBy,
        sortOrder: DEFAULT_PROCESS_LIST_URL_STATE.sortOrder,
      },
      'push',
    );
  };

  const handleClearFilters = () => {
    clearProcessFilters('push');
  };

  const handleExportCSV = async () => {
    if (totalElements === 0) {
      toast.warning("No data to export");
      return;
    }

    setIsFetchingAllPages(true);
    toast.info("Fetching all records for export...");

    try {
      const allDtos: ProcessInstanceDTO[] = [];
      let from = 0;
      let hasNext = true;

      while (hasNext) {
        const response = await processInstanceService.scrollProcessInstances({
          processDefinitionId: processFilters.process || undefined,
          version: processFilters.version ? parseInt(processFilters.version) : undefined,
          searchText: processFilters.searchText || undefined,
          state:
            processFilters.status && processFilters.status !== "All"
              ? processFilters.status
              : undefined,
          createdFrom: processFilters.from || undefined,
          createdTo: processFilters.to || undefined,
          timezone: processFilters.timezone || undefined,
          from,
          to: from + EXPORT_BATCH_SIZE,
          sort: sortConfig ? `${sortConfig.direction === 'desc' ? '-' : ''}${sortConfig.field}` : '-createdAt',
        });

        allDtos.push(...(response.content ?? []));
        hasNext = response.hasNext;
        from = response.to;
      }

      const allInstances: ProcessInstance[] = allDtos.map((instance): ProcessInstance => ({
        processId: instance.processInstanceId,
        processName: instance.processDefinitionId || "Unknown Process",
        processVersion: instance.version?.toString() || "1",
        status: (instance.state as ProcessInstance["status"]) || "ACTIVE",
        startDate: instance.createdAt,
        endDate: instance.completedAt ?? null,
        parentProcessId: instance.parentProcessInstanceId ?? "",
        sequenceExecutions: instance.sequenceExecutions || {},
        incidentMessage: instance.incidentMessage ?? undefined,
      }));

      toast.info(`Exporting ${allInstances.length} records...`);

      await exportToCSV({
        data: allInstances,
        filename: "process-instances",
        columnMapping: CSV_COLUMN_MAPPING,
        valueFormatter: formatCsvValue,
      });
    } catch (error) {
      toast.error("Failed to export data. Please try again.");
      console.error("Export error:", error);
    } finally {
      setIsFetchingAllPages(false);
    }
  };

  const handleSortChange = useCallback((sort: SortConfig[]) => {
    const primary = sort[0];
    if (!primary) return;
    setProcessFilters(
      { sortBy: primary.field, sortOrder: primary.direction },
      'push',
    );
  }, [setProcessFilters]);

  const displayTotal = useMemo(
    () => resolveProcessStatsTotal(stats, processFilters.status) || totalElements,
    [stats, processFilters.status, totalElements],
  );

  const infiniteScrollConfig: InfiniteScrollConfig = {
    hasNextPage,
    isFetchingNextPage,
    onLoadMore: loadMore,
    totalElements: displayTotal,
    loadedElements: totalLoaded,
    loadedLabel: 'instances loaded',
  };

  return (
    <AppLayout>
      <div className={commonStyles.pageContainerMediumGap}>
        <ProcessStatsGrid
          stats={stats}
          isLoading={loadingStats}
          selectedStatus={processFilters.status}
          onStatusClick={handleStatusClick}
          onClearFilters={handleClearFilters}
          hasActiveFilters={hasActiveProcessFilters}
        />

        <ProcessFilterBar
          processFilters={processFilters}
          setProcessFilters={setProcessFilters}
          clearProcessFilters={clearProcessFilters}
          hasActiveProcessFilters={hasActiveProcessFilters}
          onExport={handleExportCSV}
          isExporting={isExporting || isFetchingAllPages}
          canExport={totalElements > 0}
        />

        {processFilters.process ? (
          <div className={commonStyles.mainContent}>
            <ResizableDetailsLayout
              storageKey="process-list"
              defaultDiagramSize={30}
              minDiagramSize={10}
              maxDiagramSize={90}
              diagramSlot={
                diagramError && !diagramLoading ? (
                  <div className={commonStyles.errorBanner}>
                    <AlertCircle className={commonStyles.errorIcon} />
                    <span className={commonStyles.errorText}>
                      {PROCESS_TRANSLATIONS.ERROR_LOADING_DIAGRAM}
                    </span>
                  </div>
                ) : (
                  <ProcessDiagramViewer
                    processDefinitionId={processFilters.process}
                    diagramXml={diagramXml}
                    isLoading={diagramLoading}
                    height="100%"
                    minHeight="200px"
                  />
                )
              }
              contentSlot={
                <>
                  {instancesError && !loadingInstances && (
                    <div className={commonStyles.errorBanner}>
                      <AlertCircle className={commonStyles.errorIcon} />
                      <span className={commonStyles.errorText}>
                        {PROCESS_TRANSLATIONS.ERROR_LOADING_INSTANCES}
                      </span>
                      <Button
                        ref={retryButtonRef}
                        variant="ghost"
                        size="sm"
                        onClick={handleTableRefresh}
                        label={PROCESS_TRANSLATIONS.RETRY}
                        className={clsx(commonStyles.retryButton)}
                        aria-label={PROCESS_TRANSLATIONS.ARIA_RETRY_BUTTON}
                      />
                    </div>
                  )}
                  <CompactProcessTable
                    instances={instances}
                    loading={loadingInstances}
                    totalElements={totalElements}
                    showActions={isAdmin}
                    onTableRefresh={handleTableRefresh}
                    selectedProcessId={undefined}
                    sortConfig={sortConfig}
                    onSortChange={handleSortChange}
                    infiniteScroll={infiniteScrollConfig}
                  />
                </>
              }
            />
          </div>
        ) : (
          <>
            {instancesError && !loadingInstances && (
              <div className={commonStyles.errorBanner}>
                <AlertCircle className={commonStyles.errorIcon} />
                <span className={commonStyles.errorText}>
                  {PROCESS_TRANSLATIONS.ERROR_LOADING_INSTANCES}
                </span>
                <Button
                  ref={retryButtonRef}
                  variant="ghost"
                  size="sm"
                  onClick={handleTableRefresh}
                  label={PROCESS_TRANSLATIONS.RETRY}
                  className={clsx(commonStyles.retryButton)}
                  aria-label={PROCESS_TRANSLATIONS.ARIA_RETRY_BUTTON}
                />
              </div>
            )}
            <div className={commonStyles.tableContainerFull}>
              <CompactProcessTable
                instances={instances}
                loading={loadingInstances}
                totalElements={totalElements}
                showActions={isAdmin}
                onTableRefresh={handleTableRefresh}
                selectedProcessId={undefined}
                sortConfig={sortConfig}
                onSortChange={handleSortChange}
                infiniteScroll={infiniteScrollConfig}
              />
            </div>
          </>
        )}
      </div>
    </AppLayout>
  );
};

export default React.memo(ProcessList);
