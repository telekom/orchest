import AppLayout from '@/shared/layouts/AppLayout';
import {
  buildDecisionListPath,
  buildProcessListPath,
} from '@/shared/url-state';
import {
  buildOrchestStatsApiQuery,
  type ReportingRangeSlice,
} from '@/features/process-management/utils/dashboardReportingRange';
import clsx from 'clsx';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ProcessStateHeatmap } from '../../components/ProcessStateHeatmap/ProcessStateHeatmap';
import { UsageCalendarHeatmap } from '../../components/UsageCalendarHeatmap/UsageCalendarHeatmap';
import { UsageCharts } from '../../components/UsageCharts/UsageCharts';
import { UsageKpiStrip } from '../../components/UsageKpiStrip/UsageKpiStrip';
import { UsageToolbar } from '../../components/UsageToolbar/UsageToolbar';
import { useUsageCalendarSeries } from '../../hooks/useUsageCalendarSeries';
import { useUsageStats } from '../../hooks/useUsageStats';
import { buildCalendarBuckets } from '../../utils/calendarBuckets';
import type { UsageKind } from '../../utils/usageEntityMappers';
import {
  dateToHm,
  dateToYmd,
  loadUsageReportingRangeFromSession,
  parseYmdToLocalDate,
  saveUsageReportingRangeToSession,
} from '../../utils/usageReportingRange';
import styles from './UsagePage.module.css';

const UsagePage: React.FC = () => {
  const navigate = useNavigate();
  const [manuallyRefreshing, setManuallyRefreshing] = useState(false);
  const [kind, setKind] = useState<UsageKind>('bpmn');

  const [reportingRange, setReportingRange] = useState<ReportingRangeSlice>(
    () => loadUsageReportingRangeFromSession(),
  );

  const [draftFrom, setDraftFrom] = useState<Date | null>(() => {
    const s = loadUsageReportingRangeFromSession();
    if (!s.fromDate) return null;
    return parseYmdToLocalDate(s.fromDate, s.fromTime ?? '00:00');
  });
  const [draftTo, setDraftTo] = useState<Date | null>(() => {
    const s = loadUsageReportingRangeFromSession();
    if (!s.toDate) return null;
    return parseYmdToLocalDate(s.toDate, s.toTime ?? '23:59');
  });
  const [draftTz, setDraftTz] = useState(
    () => loadUsageReportingRangeFromSession().timeZoneId || 'UTC',
  );

  useEffect(() => {
    saveUsageReportingRangeToSession(reportingRange);
  }, [reportingRange]);

  const handleApplyDateFilter = (
    appliedFrom?: Date | null,
    appliedTo?: Date | null,
  ) => {
    const nextFrom = appliedFrom !== undefined ? appliedFrom : draftFrom;
    const nextTo = appliedTo !== undefined ? appliedTo : draftTo;
    if (appliedFrom !== undefined) setDraftFrom(appliedFrom);
    if (appliedTo !== undefined) setDraftTo(appliedTo);

    const fromDate = nextFrom ? dateToYmd(nextFrom) : null;
    const fromTime = nextFrom ? dateToHm(nextFrom) : null;
    const toDate = nextTo ? dateToYmd(nextTo) : null;
    const toTime = nextTo ? dateToHm(nextTo) : null;
    setReportingRange({
      fromDate,
      fromTime,
      toDate,
      toTime,
      timeZoneId: draftTz,
    });
  };

  const handleClearDateFilter = () => {
    setDraftFrom(null);
    setDraftTo(null);
    setReportingRange({
      fromDate: null,
      fromTime: null,
      toDate: null,
      toTime: null,
      timeZoneId: draftTz || 'UTC',
    });
  };

  const statsQuerySlice = useMemo(
    () => buildOrchestStatsApiQuery(reportingRange),
    [reportingRange],
  );
  const isRangeFiltered = statsQuerySlice != null;

  const {
    data: usageData,
    isLoading,
    refetch,
  } = useUsageStats({
    statsQuery: statsQuerySlice,
    staleTime: isRangeFiltered ? Infinity : 0,
    refetchInterval: isRangeFiltered ? false : 15_000,
    refetchOnWindowFocus: !isRangeFiltered,
  });

  const calendarBuckets = useMemo(
    () => (isRangeFiltered ? buildCalendarBuckets(reportingRange) : []),
    [isRangeFiltered, reportingRange],
  );

  const {
    points: calendarPoints,
    maxTotal: calendarMax,
    isLoading: calendarLoading,
    loadedCount,
    totalBuckets,
  } = useUsageCalendarSeries(calendarBuckets, kind);

  const total =
    kind === 'bpmn'
      ? (usageData?.process?.total ?? 0)
      : (usageData?.decision?.total ?? 0);

  const entities = useMemo(() => {
    if (kind === 'bpmn') {
      return (usageData?.process?.byProcess ?? []).map((row) => ({
        id: row.processDefinitionId,
        total: row.total,
      }));
    }
    return (usageData?.decision?.byDecision ?? []).map((row) => ({
      id: row.decisionId,
      total: row.total,
    }));
  }, [kind, usageData?.process?.byProcess, usageData?.decision?.byDecision]);

  const hasEverLoaded = useRef(false);
  useEffect(() => {
    if (!isLoading && usageData !== undefined) {
      hasEverLoaded.current = true;
    }
  }, [isLoading, usageData]);

  const isInitialLoading = !hasEverLoaded.current && isLoading;

  const lastUpdatedLabel = useMemo(() => {
    if (!usageData?.lastUpdatedMillis) return null;
    try {
      return `Updated ${new Date(usageData.lastUpdatedMillis).toLocaleString()}`;
    } catch {
      return null;
    }
  }, [usageData?.lastUpdatedMillis]);

  const goToList = (entityId?: string) => {
    if (kind === 'bpmn') {
      if (!entityId) {
        navigate('/processes');
        return;
      }
      navigate(
        buildProcessListPath({
          status: null,
          process: entityId,
          version: null,
          searchText: '',
          from: null,
          to: null,
        }),
      );
      return;
    }

    if (!entityId) {
      navigate('/decisions');
      return;
    }
    navigate(
      buildDecisionListPath({
        status: null,
        decisionId: entityId,
        version: null,
        searchText: '',
        from: null,
        to: null,
      }),
    );
  };

  const handleBucketDrillDown = (fromDate: string, toDate: string) => {
    const from = parseYmdToLocalDate(fromDate, '00:00');
    const to = parseYmdToLocalDate(toDate, '23:59');
    setDraftFrom(from);
    setDraftTo(to);
    setReportingRange({
      fromDate,
      fromTime: '00:00',
      toDate,
      toTime: '23:59',
      timeZoneId: reportingRange.timeZoneId || 'UTC',
    });
  };

  const handleRefresh = async () => {
    setManuallyRefreshing(true);
    try {
      await refetch();
    } finally {
      setManuallyRefreshing(false);
    }
  };

  return (
    <AppLayout>
      <div className={styles.page}>
        <header className={styles.header}>
          <div className={styles.headerLeft}>
            <span className={styles.eyebrow}>
              <span className={styles.eyebrowDot} />
              OrchesT · Analytics
            </span>
            <h1 className={styles.title}>Usage</h1>
            <p className={styles.subtitle}>
              Heatmaps and charts of{' '}
              {kind === 'bpmn' ? 'process' : 'decision'} executions for the
              selected period.
            </p>
          </div>
          <UsageToolbar
            draftFrom={draftFrom}
            draftTo={draftTo}
            timezone={draftTz}
            isRangeFiltered={isRangeFiltered}
            isRefreshing={manuallyRefreshing}
            lastUpdatedLabel={lastUpdatedLabel}
            onDraftChange={(f, t) => {
              setDraftFrom(f);
              setDraftTo(t);
            }}
            onTimezoneChange={setDraftTz}
            onApply={handleApplyDateFilter}
            onClear={handleClearDateFilter}
            onRefresh={handleRefresh}
          />
        </header>

        <div
          className={styles.kindSegment}
          role="tablist"
          aria-label="Usage type"
        >
          <button
            type="button"
            role="tab"
            aria-selected={kind === 'bpmn'}
            className={clsx(
              styles.kindSegmentBtn,
              kind === 'bpmn' && styles.kindSegmentBtnActive,
            )}
            onClick={() => setKind('bpmn')}
          >
            BPMN
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={kind === 'dmn'}
            className={clsx(
              styles.kindSegmentBtn,
              kind === 'dmn' && styles.kindSegmentBtnActive,
            )}
            onClick={() => setKind('dmn')}
          >
            DMN
          </button>
        </div>

        <UsageKpiStrip
          total={total}
          label="Total executions"
          isLoading={isInitialLoading}
          onSelectTotal={() => goToList()}
        />

        <div className={styles.midGrid}>
          <ProcessStateHeatmap
            kind={kind}
            entities={entities}
            isLoading={isInitialLoading}
            onRowClick={(id) => goToList(id)}
          />
          <UsageCharts
            kind={kind}
            entities={entities}
            isLoading={isInitialLoading}
            onEntityClick={(id) => goToList(id)}
          />
        </div>

        <UsageCalendarHeatmap
          enabled={isRangeFiltered && calendarBuckets.length > 0}
          points={calendarPoints}
          maxTotal={calendarMax}
          isLoading={calendarLoading}
          loadedCount={loadedCount}
          totalBuckets={totalBuckets}
          onBucketClick={handleBucketDrillDown}
        />
      </div>
    </AppLayout>
  );
};

export default UsagePage;
