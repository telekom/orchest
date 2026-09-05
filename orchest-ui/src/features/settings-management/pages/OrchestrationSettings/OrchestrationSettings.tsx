import { rateLimitsService, type RateLimit } from "@/api/domains";
import { Button } from "@/design-system/components/ui/button";
import { useAuth, UserRoles } from '@/shared/auth';
import { ConfigStatus, FilterValue } from "@/shared/constants/status";
import { useApiMutation, useApiQuery } from "@/shared/hooks";
import { useDebouncedUrlFilter } from "@/shared/url-state";
import AppLayout from "@/shared/layouts/AppLayout";
import React, { useEffect, useMemo, useState } from "react";
import SettingsNavSidebar, { type SettingsTab } from "../../components/SettingsNavSidebar/SettingsNavSidebar";
import SettingsFilterSidebar from "../../components/SettingsFilterSidebar";
import RateLimitFormModal from "../../components/orchestration/RateLimitFormModal/RateLimitFormModal";
import RateLimitTable from "../../components/orchestration/RateLimitTable/RateLimitTable";
import EnvVariablesPanel from "../../components/env-variables/EnvVariablesPanel/EnvVariablesPanel";
import SensitiveVariablesPanel from "../../components/sensitive-variables/SensitiveVariablesPanel/SensitiveVariablesPanel";
import ProcessStateManagementPanel from "../../components/process-state/ProcessStateManagementPanel/ProcessStateManagementPanel";
import AlertMailerPanel from "../../components/alert-mailer/AlertMailerPanel";
import { useRateLimitForm } from "../../hooks/useRateLimitForm";
import { useSettingsUrlState } from "../../hooks/useSettingsUrlState";
import { validateForm } from "../../utils/rateLimitUtils";
import styles from './OrchestrationSettings.module.css';

const OrchestrationSettings: React.FC = () => {
  const { hasRole } = useAuth();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState<SettingsTab>('process');

  const isAdmin = useMemo(() => hasRole(UserRoles.ADMIN), [hasRole]);

  const {
    filters,
    updateFilter,
    clearFilters,
    hasActiveFilters,
    handlePageChangeWithTotal,
    handlePageSizeChange,
    setFilters,
  } = useSettingsUrlState();

  const { localValue: searchTerm, onChange: onSearchChange, clear: clearSearch } = useDebouncedUrlFilter({
    filters,
    setFilters: (patch) => updateFilter('searchTerm', patch.searchTerm as string),
    key: 'searchTerm',
    delayMs: 400,
  });

  const { formData, isOpen, openForEdit, openForNew, updateField, close } = useRateLimitForm();

  const { data: configs = [], isLoading: loading } = useApiQuery(
    ['rateLimits'],
    () => rateLimitsService.getRateLimits(),
    { showErrorToast: true, staleTime: 30000 }
  );

  const { mutateAsync: saveRateLimit, isPending: saving } = useApiMutation(
    async (data: RateLimit) => {
      const errors = validateForm(data);
      if (errors.length > 0) throw new Error(errors[0]);

      const isNewConfig = !configs.find((config) => config.id === data.id);

      await rateLimitsService.upsertRateLimit({
        id: data.id?.startsWith("temp-") ? undefined : data.id,
        enabled: data.enabled,
        processId: data.processId,
        windowDuration: data.windowDuration,
        allowedSize: data.allowedSize,
        switchToNewCamunda: data.switchToNewCamunda ?? false,
      });

      return isNewConfig;
    },
    {
      invalidateQueries: [['rateLimits']],
      showSuccessToast: true,
      successMessage: 'Orchestration settings saved successfully',
      onSuccess: () => close(),
    }
  );

  const filteredConfigs = useMemo(() => {
    const term = filters.searchTerm.toLowerCase();
    return configs.filter((config) => {
      const matchesSearch = term === "" ||
        config.processId.toLowerCase().includes(term) ||
        (config.id?.toLowerCase().includes(term) ?? false);

      const matchesStatus = filters.statusFilter === FilterValue.ALL ||
        (filters.statusFilter === ConfigStatus.ENABLED && config.enabled) ||
        (filters.statusFilter === ConfigStatus.DISABLED && !config.enabled);

      return matchesSearch && matchesStatus;
    });
  }, [configs, filters.searchTerm, filters.statusFilter]);

  const totalElements = filteredConfigs.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / filters.pageSize));
  const safePage = Math.min(filters.page, Math.max(totalPages - 1, 0));

  const paginatedData = useMemo(() => {
    const start = safePage * filters.pageSize;
    return filteredConfigs.slice(start, start + filters.pageSize);
  }, [filteredConfigs, safePage, filters.pageSize]);

  useEffect(() => {
    if (totalElements > 0 && filters.page >= totalPages) {
      setFilters({ page: totalPages - 1 }, 'replace');
    }
  }, [filters.page, totalElements, totalPages, setFilters]);

  const handleSave = async () => {
    if (!formData) return;
    await saveRateLimit(formData);
  };

  const handleClearFilters = () => {
    clearSearch();
    clearFilters();
  };

  const renderContent = () => {
    switch (activeTab) {
      case 'process':
        return (
          <>
            <div className={styles.filterBar}>
              <SettingsFilterSidebar
                isCollapsed={false}
                onToggleCollapse={() => {}}
                searchTerm={searchTerm}
                statusFilter={filters.statusFilter}
                onSearchChange={onSearchChange}
                onStatusFilterChange={(value) => updateFilter("statusFilter", value)}
                onClearFilters={handleClearFilters}
                hasActiveFilters={hasActiveFilters}
                inline
              />
            </div>
            {isAdmin && (
              <div className={styles.headerAction}>
                <Button
                  variant="primary"
                  size="sm"
                  onClick={openForNew}
                >
                  + Add Setting
                </Button>
              </div>
            )}
            <div className={styles.tableSection}>
              <RateLimitTable
                data={paginatedData || []}
                loading={loading}
                canEdit={isAdmin}
                onEdit={openForEdit}
                pagination={{
                  currentPage: safePage,
                  totalPages,
                  totalElements,
                  pageSize: filters.pageSize,
                  onPageChange: (direction) => handlePageChangeWithTotal(direction, totalPages),
                  onPageSizeChange: handlePageSizeChange,
                }}
              />
            </div>
          </>
        );
      case 'env-variables':
        return <EnvVariablesPanel variableType="PLAIN_TEXT" title="Environment Variables" />;
      case 'sensitive-variables':
        return <SensitiveVariablesPanel />;
      case 'process-state':
        return <ProcessStateManagementPanel />;
      case 'alert-mailer':
        return <AlertMailerPanel />;
    }
  };

  return (
    <AppLayout
      sidebar={
        <SettingsNavSidebar
          isCollapsed={sidebarCollapsed}
          onToggleCollapse={() => setSidebarCollapsed(!sidebarCollapsed)}
          activeTab={activeTab}
          onTabChange={setActiveTab}
        />
      }
      sidebarCollapsed={sidebarCollapsed}
    >
      <div className={styles.container}>
        {renderContent()}
      </div>

      <RateLimitFormModal
        isOpen={isOpen}
        formData={formData}
        configs={configs}
        saving={saving}
        onClose={close}
        onSave={handleSave}
        onInputChange={updateField}
      />
    </AppLayout>
  );
};

export default OrchestrationSettings;
