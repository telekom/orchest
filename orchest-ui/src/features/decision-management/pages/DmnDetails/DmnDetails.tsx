import { decisionInstanceService } from "@/api/domains";
import { Badge } from "@/design-system/components/ui/badge/badge";
import { useRoles } from '@/shared/auth';
import { BackButton, Column, DataTable, ErrorBanner, FullPageLoader, InstanceSummary, JsonDisplay, ResizableDetailsLayout, StatusBadge, type SummaryField } from "@/shared/components";
import { getBadgeStyleForVariableType } from "@/shared/utils/badgeUtils";
import { LOADING_MESSAGES } from "@/shared/constants";
import { queryKeys } from "@/shared/constants/queryKeys";
import { useApiQuery } from "@/shared/hooks";
import { useCopyToClipboard } from "@/shared/hooks/useCopyToClipboard";
import commonStyles from '@/shared/styles/common.module.css';
import { mapDomainToContextDecisionInstance } from "@/shared/utils/typeMapping";
import { transformVariablesToArray } from "@/shared/utils/variableUtils";
import { format } from "date-fns";
import { ArrowDownCircle, ArrowUpCircle, LucideIcon } from "lucide-react";
import React, { lazy, Suspense, useCallback, useMemo } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { DECISION_TRANSLATIONS } from "../../constants/translations";
import { useProcessInstanceLink } from "../../hooks";
import additionalStyles from "./DmnDetails.additional.module.css";
import styles from "./DmnDetails.module.css";

// Lazy load heavy DMN component
const DmnViewer = lazy(() => import("@/shared/diagram/components/DmnViewer/DmnViewer"));

interface DmnVariable {
  name: string;
  value: string;
  type: string;
  scope: string;
  [key: string]: unknown;
}

const VARIABLE_COLUMNS: Column<DmnVariable>[] = [
  {
    key: "name",
    header: "Name",
    render: (row: DmnVariable) => (
      <span className={additionalStyles.variableName}>{row.name}</span>
    ),
  },
  {
    key: "value",
    header: "Value",
    render: (row: DmnVariable) => (
      <div className={additionalStyles.valueContainer}>
        <JsonDisplay
          value={row.value}
          type={row.type}
          codeBlock={row.type === "object"}
        />
      </div>
    ),
  },
  {
    key: "type",
    header: "Type",
    render: (row: DmnVariable) => {
      const typeBadgeStyle = getBadgeStyleForVariableType(row.type);
      const Icon = typeBadgeStyle.icon;
      return (
        <Badge
          variant={typeBadgeStyle.variant}
          icon={Icon ? <Icon /> : undefined}
          className={typeBadgeStyle.className}
        >
          <span className={additionalStyles.truncate}>{row.type}</span>
        </Badge>
      );
    },
  },
];

interface VariableSectionProps {
  title: string;
  icon: LucideIcon;
  data: DmnVariable[];
  keyPrefix: string;
  emptyText: string;
}

const VariableSection: React.FC<VariableSectionProps> = React.memo(({ title, icon: Icon, data, keyPrefix, emptyText }) => (
  <div className={`${commonStyles.card} ${styles.variableSection}`}>
    <div className={styles.variableSectionHeader}>
      <Icon className={styles.variableSectionIcon} />
      <h3 className={styles.variableSectionTitle}>{title}</h3>
    </div>
    <div className={styles.variableSectionContent}>
      <DataTable
        data={data}
        columns={VARIABLE_COLUMNS}
        keyExtractor={(_row, index) => `${keyPrefix}-${index}`}
        emptyText={emptyText}
        stickyHeader={true}
      />
    </div>
  </div>
));

const DmnDetails: React.FC = () => {
  const { canView } = useRoles();
  const { id: decisionInstanceId } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { handleProcessInstanceClick } = useProcessInstanceLink();

  const { copyToClipboard, copiedItems } = useCopyToClipboard();

  const { data: instanceData, isLoading: loading, error } = useApiQuery(
    queryKeys.decisionInstances.detail(decisionInstanceId || ''),
    async () => {
      if (!decisionInstanceId) throw new Error("No decision instance ID provided");
      return await decisionInstanceService.getDecisionInstance(decisionInstanceId);
    },
    {
      enabled: !!decisionInstanceId,
      showErrorToast: false,
      retryOnError: false,
    }
  );

  const instance = useMemo(() =>
    instanceData ? mapDomainToContextDecisionInstance(instanceData) : null,
    [instanceData]
  );

  const summaryFields: SummaryField[] = useMemo(() => {
    if (!instance) return [];

    const baseFields: SummaryField[] = [
      {
        label: "Status",
        value: <StatusBadge status={instance.state || ""} />,
      },
      {
        label: "ID",
        value: instance.decisionInstanceId,
        copyValue: instance.decisionInstanceId,
        copyId: "id",
      },
      {
        label: "Name",
        value: instance.decisionId,
        copyValue: instance.decisionId,
        copyId: "name",
      },
    ];

    if (instance.processInstanceId) {
      baseFields.push({
        label: "Process ID",
        value: (
          <a
            href={`/processes/${instance.processInstanceId}`}
            className={additionalStyles.processLink}
            onClick={(e) => handleProcessInstanceClick(e, instance.processInstanceId)}
          >
            {instance.processInstanceId}
          </a>
        ),
        copyValue: instance.processInstanceId,
        copyId: "processId",
      });
    }

    baseFields.push(
      {
        label: "Executed",
        value: instance.executedAt
          ? format(new Date(instance.executedAt), "MMM d, yyyy HH:mm:ss")
          : "-",
      },
      {
        label: "Version",
        value: String(instance.version),
      }
    );

    return baseFields;
  }, [instance, handleProcessInstanceClick]);

  const handleBack = useCallback(() => {
    if (window.history.length > 1) {
      navigate(-1);
    } else {
      navigate('/decisions');
    }
  }, [navigate]);

  // Loading state
  if (loading) {
    return <FullPageLoader text={DECISION_TRANSLATIONS.LOADING_DECISION_DETAILS} />;
  }

  // Error state
  if (error || !instance) {
    return <ErrorBanner message={DECISION_TRANSLATIONS.ERROR_LOADING_DECISION} />;
  }

  // Transform variables
  const inputVariables = transformVariablesToArray(instanceData.inputVariables) as DmnVariable[];
  const outputVariables = transformVariablesToArray(instanceData.outputVariables) as DmnVariable[];

  return (
    <div className={commonStyles.pageContainer}>
      <InstanceSummary
        fields={summaryFields}
        copiedItems={copiedItems}
        onCopyToClipboard={copyToClipboard}
        leftSlot={<BackButton onClick={handleBack} ariaLabel={DECISION_TRANSLATIONS.ARIA_BACK_BUTTON} />}
      />

      <div className={commonStyles.mainContent}>
        <ResizableDetailsLayout
          storageKey="dmn-details"
          defaultDiagramSize={60}
          minDiagramSize={10}
          maxDiagramSize={90}
          diagramSlot={
            <Suspense fallback={<FullPageLoader text={LOADING_MESSAGES.LOADING_DIAGRAM} />}>
              <DmnViewer
                matchedRuleId={instance.matchedRuleId}
                xml={instanceData.resourceUTF8XML || ""}
              />
            </Suspense>
          }
          contentSlot={
            canView ? (
              <div className={styles.variablesContainer}>
                <div className={styles.variablesGrid}>
                  <VariableSection
                    title={DECISION_TRANSLATIONS.INPUT_VARIABLES}
                    icon={ArrowDownCircle}
                    data={inputVariables}
                    keyPrefix="input"
                    emptyText={DECISION_TRANSLATIONS.EMPTY_INPUT_VARIABLES}
                  />
                  <VariableSection
                    title={DECISION_TRANSLATIONS.OUTPUT_VARIABLES}
                    icon={ArrowUpCircle}
                    data={outputVariables}
                    keyPrefix="output"
                    emptyText={DECISION_TRANSLATIONS.EMPTY_OUTPUT_VARIABLES}
                  />
                </div>
              </div>
            ) : (
              <div className={`${styles.noVariableData} ${commonStyles.textMuted}`}>
                {DECISION_TRANSLATIONS.NO_VARIABLE_DATA}
              </div>
            )
          }
        />
      </div>
    </div>
  );
};

export default React.memo(DmnDetails);