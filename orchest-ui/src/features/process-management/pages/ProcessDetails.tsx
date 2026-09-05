import { ProcessInstanceState } from "@/api/domains";
import {
    Tabs,
    TabsContent,
    TabsList,
    TabsTrigger,
} from "@/design-system/components/ui/tabs";
import { DIAGRAM_CONFIG } from "@/features/process-management/constants";
import { PROCESS_TRANSLATIONS } from "@/features/process-management/constants/translations";
import { useRoles } from '@/shared/auth';
import { BackButton, ErrorBanner, FullPageLoader, ResizableDetailsLayout } from '@/shared/components';
import { LOADING_MESSAGES } from "@/shared/constants";
import { useCopyToClipboard } from '@/shared/hooks/useCopyToClipboard';
import commonStyles from '@/shared/styles/common.module.css';
import {
    Code,
    History,
    CheckSquare,
    Sparkles,
} from "lucide-react";
import React, { lazy, Suspense, useCallback, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import IncidentError from "../components/IncidentError/IncidentError";
import ProcessInstanceSummary from "../components/ProcessInstanceSummary/ProcessInstanceSummary";
import summaryStyles from "../components/ProcessInstanceSummary/ProcessInstanceSummary.module.css";
import ProcessVariables from "../components/ProcessVariables/ProcessVariables";
import InstanceHistory from "../components/InstanceHistory/InstanceHistory";
import ProcessUserTasks from "../components/ProcessUserTasks/ProcessUserTasks";
import { useProcessInstanceQuery } from "../hooks/useProcessInstanceQuery";
import { hasUserTasksInSequenceExecutions, extractAllUserTaskIds } from "@/shared/diagram/utils/childInstanceExtractor";

// Lazy load heavy BPMN component
const DiagramViewer = lazy(() => import("../components/DiagramViewer/DiagramViewer"));

const ProcessDetails: React.FC = () => {
  const { isAdmin, canView, isSensitive } = useRoles();
  const { id: processId } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const {
    instance,
    diagramXml,
    variables,
    variableContext,
    loading,
    error,
    fetchInstance,
  } = useProcessInstanceQuery(processId);

  const { copyToClipboard, copiedItems } = useCopyToClipboard();

  const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
  const handleSelectNode = useCallback((nodeId: string) => {
    setSelectedNodeId((prev) => (prev === nodeId ? null : nodeId));
  }, []);

  const handleBack = useCallback(() => {
    if (window.history.length > 1) {
      navigate(-1);
    } else {
      navigate('/processes');
    }
  }, [navigate]);

  // Loading state
  if (loading) {
    return <FullPageLoader text={LOADING_MESSAGES.PROCESS_DETAILS} />;
  }

  // Error state
  if (error || !instance) {
    return <ErrorBanner message={PROCESS_TRANSLATIONS.ERROR_LOADING_PROCESS} />;
  }

  // Determine default tab and styling
  const defaultTab = isSensitive && !canView ? "history" : "variables";
  const userTaskIds = extractAllUserTaskIds(instance.sequenceExecutions);
  const showUserTasksTab = isAdmin && (
    userTaskIds.length > 0 || hasUserTasksInSequenceExecutions(instance.sequenceExecutions)
  );

  // Tab layout logic:
  // - Admins with user tasks: variables, history, tasks
  // - Viewers / admins without user tasks: variables, history
  // - Sensitive users: history only
  let tabsListClass = commonStyles.tabsList;
  if (showUserTasksTab) {
    tabsListClass += ` ${commonStyles.tabsListThreeCol}`;
  } else if (canView) {
    tabsListClass += ` ${commonStyles.tabsListTwoCol}`;
  } else {
    tabsListClass += ` ${commonStyles.tabsListOneCol}`;
  }

  const hasIncident = instance.status === ProcessInstanceState.INCIDENT && instance.incidentMessage;
  const canModifyInstance = instance.status === ProcessInstanceState.INCIDENT || instance.status === ProcessInstanceState.HOLD || instance.status === ProcessInstanceState.RUNNING;
  const rawCompensatedId = variableContext.compensatedInstanceId;
  const compensatedInstanceId =
    instance.status === ProcessInstanceState.CANCELLED &&
    (typeof rawCompensatedId === "string" || typeof rawCompensatedId === "number") &&
    String(rawCompensatedId).length > 0
      ? String(rawCompensatedId)
      : undefined;

  return (
    <div className={commonStyles.pageContainerMediumGap}>
      <ProcessInstanceSummary
        instance={instance}
        copiedItems={copiedItems}
        onCopyToClipboard={copyToClipboard}
        leftSlot={<BackButton onClick={handleBack} ariaLabel={PROCESS_TRANSLATIONS.ARIA_BACK_BUTTON} />}
        rightSlot={hasIncident ? (
          <button
            className={summaryStyles.analyseBtn}
            onClick={() => navigate('/ai-chat', { state: { chatMessage: `Analyse the processInstance: ${processId}`, chatType: 'INCIDENT_ANALYSIS' } })}
          >
            <Sparkles size={14} />
            Analyse Incident
            <span className={summaryStyles.betaTag}>BETA</span>
          </button>
        ) : undefined}
        compensatedInstanceId={compensatedInstanceId}
      />

      <div className={commonStyles.mainContent}>
        <ResizableDetailsLayout
          {...DIAGRAM_CONFIG}
          diagramSlot={
            <Suspense fallback={<FullPageLoader text={LOADING_MESSAGES.DIAGRAM} />}>
              <DiagramViewer
                diagramLoading={loading}
                diagramXml={diagramXml}
                instance={instance}
                showInstanceActions={isAdmin && canModifyInstance}
                onInstanceModified={fetchInstance}
                selectedNodeId={selectedNodeId}
                variableContext={variableContext}
              />
            </Suspense>
          }
          contentSlot={
            <div className={commonStyles.tabsWrapper}>
              {hasIncident && (
                <IncidentError
                  incidentMessage={instance.incidentMessage!}
                  copiedItems={copiedItems}
                  onCopyToClipboard={copyToClipboard}
                />
              )}

              <Tabs
                defaultValue={defaultTab}
                className={commonStyles.tabsContainer}
              >
                <TabsList className={tabsListClass}>
                  {canView && (
                    <TabsTrigger
                      value="variables"
                      className={commonStyles.tabTrigger}
                      aria-label={PROCESS_TRANSLATIONS.ARIA_TAB_VARIABLES}
                    >
                      <Code className={commonStyles.tabIcon} aria-hidden="true" />
                      {PROCESS_TRANSLATIONS.TAB_PROCESS_VARIABLES}
                    </TabsTrigger>
                  )}
                  <TabsTrigger
                    value="history"
                    className={commonStyles.tabTrigger}
                    aria-label={PROCESS_TRANSLATIONS.ARIA_TAB_HISTORY}
                  >
                    <History className={commonStyles.tabIcon} aria-hidden="true" />
                    {PROCESS_TRANSLATIONS.TAB_TASK_HISTORY}
                  </TabsTrigger>
                  {showUserTasksTab && (
                    <TabsTrigger
                      value="tasks"
                      className={commonStyles.tabTrigger}
                      aria-label={PROCESS_TRANSLATIONS.ARIA_TAB_TASKS}
                    >
                      <CheckSquare className={commonStyles.tabIcon} aria-hidden="true" />
                      {PROCESS_TRANSLATIONS.TAB_USER_TASKS}
                    </TabsTrigger>
                  )}
                </TabsList>

                {canView && (
                  <TabsContent
                    value="variables"
                    className={`${commonStyles.tabContent} ${commonStyles.tabContentPadding}`}
                  >
                    <ProcessVariables
                      variables={variables}
                      instanceId={processId!}
                      isLoading={loading}
                      fetchInstance={fetchInstance}
                      readOnly={!isAdmin}
                    />
                  </TabsContent>
                )}

                <TabsContent value="history" className={commonStyles.tabContent}>
                  <InstanceHistory
                    sequenceExecutions={instance.sequenceExecutions}
                    bpmnXML={diagramXml}
                    processName={instance.processName}
                    status={instance.status}
                    startDate={instance.startDate}
                    endDate={instance.endDate}
                    isLoading={loading}
                    selectedNodeId={selectedNodeId}
                    onSelectNode={handleSelectNode}
                  />
                </TabsContent>

                {showUserTasksTab && (
                  <TabsContent value="tasks" className={`${commonStyles.tabContent} ${commonStyles.tabContentPadding}`}>
                    <ProcessUserTasks
                      processInstanceId={processId!}
                      userTaskIds={userTaskIds}
                      enabled={showUserTasksTab}
                    />
                  </TabsContent>
                )}
              </Tabs>
            </div>
          }
        />
      </div>
    </div>
  );
};

export default React.memo(ProcessDetails);
