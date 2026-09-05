/* eslint-disable react-refresh/only-export-components */
import { RouteGuard } from '@/shared/auth';
import { PageLoader } from '@/shared/components';
import { RouteErrorBoundary } from '@/shared/components/RouteErrorBoundary/RouteErrorBoundary';
import { RouteErrorElement } from '@/shared/components/RouteErrorElement/RouteErrorElement';
import AppLayout from '@/shared/layouts/AppLayout';
import { isNoLoginMode } from '@/shared/utils/environmentUtils';
import { ComponentType, lazy, Suspense } from 'react';
import { createBrowserRouter, Navigate, RouteObject } from 'react-router-dom';
import { AnimatedOutlet } from './AnimatedOutlet';

const Dashboard = lazy(() => import('@/features/process-management/pages/Dashboard'));
const ProcessList = lazy(() => import('@/features/process-management/pages/ProcessList'));
const ProcessDetails = lazy(() => import('@/features/process-management/pages/ProcessDetails'));
const TasksPage = lazy(() => import('@/features/task-management/pages/TasksPage/TasksPage'));
const ModelerPage = lazy(() => import('@/features/workflow-modeling/pages/ModelerPage'));
const BpmnGenerator = lazy(() => import('@/features/bpmn-generator/pages/BpmnGenerator/BpmnGenerator'));
const DmnList = lazy(() => import('@/features/decision-management/pages/DmnList'));
const DmnDetails = lazy(() => import('@/features/decision-management/pages/DmnDetails'));
const OrchestrationSettings = lazy(() => import('@/features/settings-management/pages/OrchestrationSettings/OrchestrationSettings'));
const DevToolsPage = lazy(() => import('@/features/dev-tools/pages/DevToolsPage'));
const FeelPlayground = lazy(() => import('@/features/feel-playground/pages/FeelPlayground/FeelPlayground'));
const AiChatPage = lazy(() => import('@/features/ai-chat/pages/AiChatPage/AiChatPage'));
const KafkaUtility = lazy(() => import('@/features/kafka-utility/pages/KafkaUtility'));
const SystemHealth = lazy(() => import('@/features/system-health/pages/SystemHealth'));
const AuditTrailPage = lazy(() => import('@/features/audit-trail/pages/AuditTrailPage/AuditTrailPage'));
const AccessTokensPage = lazy(() => import('@/features/access-tokens/pages/AccessTokensPage/AccessTokensPage'));
const UsagePage = lazy(() => import('@/features/usage/pages/UsagePage/UsagePage'));
const ApprovalsPage = lazy(() => import('@/features/deployment-approvals/pages/ApprovalsPage/ApprovalsPage'));
const ApprovalDetailPage = lazy(() => import('@/features/deployment-approvals/pages/ApprovalDetailPage/ApprovalDetailPage'));
const AlertsPage = lazy(() => import('@/features/alert-management/pages/AlertsPage/AlertsPage'));
const NotFound = lazy(() => import('@/design-system/pages/NotFound'));
const LoginPage = lazy(() => import('@/shared/auth/pages/Login/Login'));
const CallbackPage = lazy(() => import('@/shared/auth/pages/Callback'));
const NoRolesPage = lazy(() => import('@/shared/auth/pages/NoRoles/NoRoles'));

const SuspenseWrapper = ({ children }: { children: React.ReactNode }) => (
  <Suspense fallback={<PageLoader />}>{children}</Suspense>
);

const ERROR_ELEMENT = <RouteErrorElement />;

const createRoute = (
  Component: ComponentType,
  routeName: string,
  withLayout = false
): JSX.Element => {
  const content = (
    <RouteErrorBoundary routeName={routeName}>
      <Component />
    </RouteErrorBoundary>
  );

  return (
    <SuspenseWrapper>
      {withLayout ? <AppLayout>{content}</AppLayout> : content}
    </SuspenseWrapper>
  );
};

const createProtectedRoute = (
  path: string,
  Component: ComponentType,
  routeName: string,
  withLayout = false
): RouteObject => ({
  path,
  errorElement: ERROR_ELEMENT,
  element: createRoute(Component, routeName, withLayout),
});

export const createAppRouter = () => {
  return createBrowserRouter([
    {
      path: '/login',
      element: <SuspenseWrapper><LoginPage /></SuspenseWrapper>,
    },
    {
      path: '/callback',
      element: createRoute(CallbackPage, "Callback"),
    },
    {
      path: '/no-roles',
      element: createRoute(NoRolesPage, "NoRoles"),
    },
    {
      path: '/feel-playground',
      element: createRoute(FeelPlayground, "FeelPlayground"),
    },
    ...(isNoLoginMode() || import.meta.env.DEV
      ? [
          {
            path: '/dev',
            element: createRoute(DevToolsPage, "DevTools", true),
          },
        ]
      : []),
    {
      path: '/',
      element: (
        <RouteGuard>
          <AnimatedOutlet />
        </RouteGuard>
      ),
      errorElement: ERROR_ELEMENT,
      children: [
        {
          index: true,
          element: <Navigate to="/dashboard" replace />,
        },
        createProtectedRoute('dashboard', Dashboard, "Dashboard"),
        createProtectedRoute('processes', ProcessList, "ProcessList"),
        createProtectedRoute('processes/:id', ProcessDetails, "ProcessDetails", true),
        createProtectedRoute('decisions', DmnList, "DmnList"),
        createProtectedRoute('decisions/:id', DmnDetails, "DmnDetails", true),
        createProtectedRoute('tasks', TasksPage, "Tasks"),
        createProtectedRoute('tasks/:taskId', TasksPage, "TaskDetails"),
        createProtectedRoute('settings', OrchestrationSettings, "OrchestrationSettings"),
        createProtectedRoute('modeler', ModelerPage, "Modeler", true),
        createProtectedRoute('generator', BpmnGenerator, "BpmnGenerator", true),
        createProtectedRoute('ai-chat', AiChatPage, "AiChat"),
        createProtectedRoute('utility', KafkaUtility, "KafkaUtility"),
        createProtectedRoute('status', SystemHealth, "SystemHealth"),
        createProtectedRoute('audit-trail', AuditTrailPage, "AuditTrail"),
        createProtectedRoute('access-tokens', AccessTokensPage, "AccessTokens"),
        createProtectedRoute('usage', UsagePage, "Usage"),
        createProtectedRoute('approvals', ApprovalsPage, "Approvals"),
        createProtectedRoute('approvals/:id', ApprovalDetailPage, "ApprovalDetail"),
        createProtectedRoute('alerts', AlertsPage, 'Alerts'),
        {
          path: '*',
          element: createRoute(NotFound, "NotFound", true),
        },
      ],
    },
  ]);
};
