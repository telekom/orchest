import { ApprovalState, NodeType, ProcessInstanceState, VariableAction } from '@/shared/enums';
import { PagedResponse, ResponseDTO } from './types';

export interface SequenceExecution {
  nodeId: string;
  nodeName: string;
  nodeType: NodeType;
  sourceNodeId?: string;
  sequenceFlowIds?: string[];
  state: ProcessInstanceState;
  stateChanges: StateChange[];
  metaData?: {
    decisionInstanceIds?: string[];
    decisionInstanceId?: string[];
    childProcessInstanceId?: string;
    childProcessInstanceIds?: string[];
    userTaskId?: string | string[];
    userTaskIds?: string | string[];
    [key: string]: unknown;
  };
}

export interface RateLimit {
  id?: string;
  enabled: boolean;
  processId: string;
  windowDuration: number;
  allowedSize: number;
  switchToNewCamunda: boolean;
  [key: string]: unknown;
}

export type RateLimitResponse = ResponseDTO<RateLimit>;
export type RateLimitsResponse = RateLimit[];

export interface VariablesRequest {
  action: VariableAction;
  processInstanceId: string;
  variables: Record<string, unknown>;
}

export interface VariablesResponse {
  variables: Record<string, unknown>;
}

export type VariablesAPIResponse = ResponseDTO<VariablesResponse>;

export interface StateChange {
  state: ProcessInstanceState;
  timestamp: string;
}

/**
 * ProcessInstanceDTO - API Layer Data Transfer Object
 *
 * Represents the exact structure returned by the backend Orchest API.
 * This type should NOT be used directly in UI components.
 *
 * For UI consumption, use the ProcessInstance type from @/shared/types,
 * which is converted from ProcessInstanceDTO via typeMapping utilities.
 *
 * @see ProcessInstance in @/shared/types for UI layer representation
 * @see mapDomainToContextProcessInstance in @/shared/utils/typeMapping for conversion
 */
export interface ProcessInstanceDTO {
  processDefinitionId: string;
  processInstanceId: string;
  parentProcessInstanceId: string;
  variables: Record<string, unknown>;
  bpmnXML: string;
  version: number;
  incidentMessage?: string;
  state: ProcessInstanceState;
  sequenceExecutions: Record<string, SequenceExecution>;
  createdAt: string;
  completedAt?: string;
  lastActivityAt: string;
  activeElements?: string[];
}

export interface ProcessInstanceStats {
  total: number;
  completed: number;
  hold: number;
  failed: number;
  incidents: number;
  active: number;
  started: number;
  cancelled: number;
}

/** Single `{ state, count }` bucket returned by consolidated `/orchest/stats`. */
export interface OrchestStatItem {
  state: string;
  count: number;
}

/**
 * Consolidated orchestrator dashboard stats (Swagger: StatsDTO).
 * Nested maps deserialize as nested JSON objects; version keys are strings.
 */
export interface OrchestStatsDTO {
  totalProcessStats?: OrchestStatItem[] | null;
  totalDecisionStats?: OrchestStatItem[] | null;
  /** processDefinitionId → version key → buckets */
  processStats?: Record<string, Record<string, OrchestStatItem[]>> | null;
  /** decisionId → version key → buckets */
  decisionStats?: Record<string, Record<string, OrchestStatItem[]>> | null;
  /** ISO-8601 offset datetime from server */
  lastUpdatedAt?: string | null;
}

export interface RetryProcessEvent {
  processInstanceId: string;
  activityId: string;
  previousActivityId: string;
}

export interface ProcessInvocationRequest {
  processDefinitionId: string;
  processInstanceId?: string;
  version?: number;
  variables?: Record<string, unknown>;
  parentProcessInstanceId?: string;
  nodeInformation?: NodeInformation;
}

export interface ProcessInvocationResponse {
  processInstanceId: string;
  processId: string;
  version: number;
}

export interface UpdateInstanceRequest {
  processInstanceId: string;
  fromNodeId: string;
  toNodeId: string;
}

export interface CancelInstanceRequest {
  instances: CancelInstance[];
}

export interface CancelInstance {
  instanceId: string;
  processDefinitionId: string;
}

export interface BatchRetryRequest {
  processInstanceIds: string[];
}

export interface BatchCancelRequest {
  processInstanceIds: string[];
}

export interface CompensateInstanceRequest {
  processInstanceId: string;
  processDefinitionId: string;
  version?: number;
  variables?: Record<string, unknown>;
}

export interface CompensateInstanceResponse {
  processInstanceId?: string;
  processDefinitionId?: string;
  version?: number;
  compensatedInstanceId: string;
}

export interface BatchCompensateInstanceRequest {
  processInstanceIds: string[];
  processDefinitionId: string;
  version?: number;
  variables?: Record<string, unknown>;
}

export type ProcessInstanceResponse = ResponseDTO<ProcessInstanceDTO>;
export type ProcessInstancesResponse = PagedResponse<ProcessInstanceDTO>;
export type ProcessInstanceStatsResponse = ProcessInstanceStats;
export type ProcessInvocationAPIResponse = ResponseDTO<ProcessInvocationResponse>;

export interface ResourceDefinitionDTO {
  definitionId: string;
  resourceXML: string;
  version: number;
  createdAt: string;
}

export interface ResourceDeploymentRequest {
  partitionCount?: number;
  resourceUTF8XML: string;
  bypassWorkerValidation?: boolean;
  compensateFlow?: boolean;
}

export interface ResourceDeploymentResponse {
  processId: string;
  version: number;
  status: string;
}

export type ProcessDefinitionResponse = ResponseDTO<ResourceDefinitionDTO>;
export type ProcessDefinitionsResponse = PagedResponse<ResourceDefinitionDTO>;
export type ResourceDefinitionListResponse = ResponseDTO<ResourceDefinitionDTO[]>;
export type ResourceDeploymentAPIResponse = ResponseDTO<ResourceDeploymentResponse>;

export interface DecisionDefinitionDTO {
  definitionId: string;
  resourceXML: string;
  version: number;
  createdAt: string;
  name?: string;
  key?: string;
  /**
   * Optional metadata returned by the backend. Contains domain-specific
   * information such as the actual decision IDs embedded in the DMN file.
   * The evaluate endpoint requires one of these IDs rather than the
   * definitionId, so callers should prefer `resourceMetadata.decisionIds[0]`
   * when present.
   */
  resourceMetadata?: {
    decisionIds?: string[];
    [key: string]: unknown;
  };
}

export type DecisionDefinitionResponse = ResponseDTO<DecisionDefinitionDTO>;
export type DecisionDefinitionsResponse = PagedResponse<DecisionDefinitionDTO>;

// ─── Evaluate Decision ────────────────────────────────────────────────────────

export interface EvaluateDecisionRequest {
  /**
   * The decision ID to evaluate. This comes from the resource metadata
   * returned by `/decisionDefinitions/{id}/{version}` endpoint.
   */
  decisionId: string;
  version: number;
  inputVariables: Record<string, unknown>;
}

export interface MatchedRule {
  id: string;
  inputEntries: string[];
  outputEntries: string[];
  description: string;
  ruleNumber: number;
}

export interface EvaluateDecisionResult {
  matchedRules: MatchedRule[];
  inputVariables: Record<string, unknown>;
  outputVariables: Record<string, unknown>;
}

export type EvaluateDecisionResponse = ResponseDTO<EvaluateDecisionResult>;

export interface DecisionInstanceDTO {
  decisionId: string;
  decisionInstanceId: string;
  processInstanceId: string;
  inputVariables: Record<string, unknown>;
  outputVariables: Record<string, unknown>;
  resourceUTF8XML: string;
  version: number;
  state: string;
  executedRuleId: string;
  executedAt: string;
  matchedRuleId?: string;
  matchedRuleIds?: string[];
}

export type DecisionInstanceResponse = ResponseDTO<DecisionInstanceDTO>;
export type DecisionInstancesResponse = PagedResponse<DecisionInstanceDTO>;

export interface NodeInformation {
  outgoingSequenceIds: string[];
  incomingSequenceIds: string[];
  multiInstance: boolean;
  boundaryEventIds: string[];
  parentScopeId?: string;
  scopeId: string;
  name: string;
  id: string;
  nodeType: NodeType;
  impl: string;
}

export interface BaseNodeInformation extends NodeInformation {
  type: NodeType;
}

interface MultiInstanceFields {
  outputData?: Record<string, unknown>;
  inputCollectionExpression?: string;
  inputElement?: string;
  outputCollection?: string;
  outputElementExpression?: string;
  retries?: number;
}

export interface ServiceTask extends NodeInformation, MultiInstanceFields {
  type: string;
}

export interface ExclusiveGateway extends NodeInformation {
  type: NodeType;
  defaultFlowId?: string;
  outgoingFlowNodeExpression?: Record<string, string>;
  converging: boolean;
}

export interface InclusiveGateway extends NodeInformation {
  type: NodeType;
  defaultFlowId?: string;
  outgoingFlowNodeExpression?: Record<string, string>;
  converging: boolean;
}

export interface ParallelGateway extends NodeInformation {
  type: NodeType;
  converging: boolean;
}

export interface StartEvent extends NodeInformation {
  type: NodeType;
  startEvent: boolean;
}

export interface EndEvent extends NodeInformation {
  type: NodeType;
  terminateEvent: boolean;
  startEvent: boolean;
}

export interface ErrorBoundaryEvent extends NodeInformation {
  type: NodeType;
  errorName?: string;
  errorCode?: string;
  attachedToRef?: string;
  attachedRefId: string;
}

export interface MessageBoundaryEvent extends NodeInformation {
  type: NodeType;
  attachedToRef?: string;
  messageName: string;
  correlationKeyExpression?: string;
  attachedRefId: string;
}

export interface CallActivity extends NodeInformation, MultiInstanceFields {
  type: string;
  processId: string;
  binding: string;
  propagateAllChildVariables: boolean;
}

export interface DmnTask extends NodeInformation, MultiInstanceFields {
  type: string;
  decisionId: string;
  resultVariable: string;
}

export interface SubProcess extends NodeInformation, MultiInstanceFields {
  type: string;
  nodeInformationMap?: Record<string, Record<string, NodeInformation>>;
}

export interface ProcessInstanceQueryParams {
  processDefinitionId?: string;
  version?: number;
  state?: string;
  searchText?: string;
  from?: string;
  to?: string;
  timezone?: string;
  page?: number;
  size?: number;
  /** Sort field with +/- prefix, e.g. "-createdAt" (+ = asc, - = desc). Multiple fields separated by comma. */
  sort?: string;
}

export interface ProcessInstanceScrollParams {
  processDefinitionId?: string;
  version?: number;
  state?: string;
  searchText?: string;
  createdFrom?: string;
  createdTo?: string;
  timezone?: string;
  from?: number;
  to: number;
  sort: string;
}

export interface ProcessInstanceScrollDTO {
  content: ProcessInstanceDTO[];
  from: number;
  to: number;
  hasNext: boolean;
}

export interface ProcessDefinitionQueryParams {
  page?: number;
  size?: number;
}

export interface DecisionDefinitionQueryParams {
  page?: number;
  size?: number;
}

export interface DecisionInstanceQueryParams {
  decisionId?: string;
  version?: number;
  state?: "EVALUATED" | "FAILED" | "UNKNOWN";
  searchText?: string;
  from?: string;
  to?: string;
  timezone?: string;
  page?: number;
  size?: number;
}

/**
 * Query params for `GET /decisionInstances/scroll`. Uses offset-based slicing:
 * `from` is inclusive, `to` is exclusive, and `(to - from)` must be ≤ 500.
 * Date-range filters live under `executedFrom`/`executedTo` to avoid colliding
 * with the offset params.
 */
export interface DecisionInstanceScrollParams {
  decisionId?: string;
  version?: number;
  state?: "EVALUATED" | "FAILED" | "UNKNOWN";
  searchText?: string;
  executedFrom?: string;
  executedTo?: string;
  timezone?: string;
  from?: number;
  to: number;
  /** Sort field with +/- prefix, e.g. "-executedAt". */
  sort: string;
}

export interface DecisionInstanceScrollDTO {
  content: DecisionInstanceDTO[];
  from: number;
  to: number;
  hasNext: boolean;
}

export interface ResourceDeploymentRequestPayload {
  partitionCount?: number;
  resourceUTF8XML: string;
  bypassWorkerValidation?: boolean;
  approvers?: string[];
  compensateFlow?: boolean;
}

export interface AuditLog {
  approvalState: ApprovalState;
  approver: string;
  createTime: string;
}

export interface DeploymentApprovalDTO {
  id: string;
  reviewers: string[];
  requestedBy: string;
  state: ApprovalState;
  resourceType: string;
  resourceDeploymentRequest: ResourceDeploymentRequestPayload;
  nextVersion: string;
  auditLog: AuditLog;
  createdAt: string;
  approvalTime: string;
}

export interface RequestApprovalRequest {
  deploymentRequest: ResourceDeploymentRequestPayload;
  type: string;
  requestedBy: string;
}

export interface SubmitApprovalRequest {
  deploymentRequestId: string;
  approver: string;
  state: ApprovalState;
}

export type DeploymentApprovalsResponse = ResponseDTO<DeploymentApprovalDTO[]>;
export type DeploymentApprovalResponse = ResponseDTO<DeploymentApprovalDTO>;

export interface ProcessOwnershipDTO {
  definitionId: string;
  definitionName: string;
  diagramType: 'bpmn' | 'dmn';
  owner: string;
  deputies: string[];
}

export interface AddDeputyRequest {
  definitionId: string;
  deputyEmail: string;
}

export interface RemoveDeputyRequest {
  definitionId: string;
  deputyEmail: string;
}

export type ProcessOwnershipsResponse = ResponseDTO<ProcessOwnershipDTO[]>;
export type ProcessOwnershipResponse = ResponseDTO<ProcessOwnershipDTO>;

export interface ProcessDefinitionDTO {
  id: string;
  processId: string;
  approvers: string[];
  createdAt: string;
  lastModifiedAt: string;
}

export type ProcessDefinitionListResponse = ResponseDTO<ProcessDefinitionDTO[]>;

/**
 * User Task Types
 */

export interface UserTaskDTO {
  taskId: string;
  processInstanceId: string;
  processDefinitionId: string;
  activityId: string;
  taskName: string;
  state: string;
  assignee?: string;
  candidateUsers?: string[];
  candidateGroups?: string[];
  claimedBy?: string;
  dueDate?: string;
  followUpDate?: string;
  formKey?: string;
  variables?: Record<string, unknown>;
  createdAt: string;
  claimedAt?: string;
  completedAt?: string;
  priority?: number;
}

export interface UserTaskQueryParams {
  state?: string;
  assignee?: string;
  candidateGroup?: string;
  processDefinitionId?: string;
  processInstanceId?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export interface CompleteTaskRequest {
  variables?: Record<string, unknown>;
}

export interface AssignTaskRequest {
  assignee: string;
}

export type UserTaskResponse = ResponseDTO<UserTaskDTO>;
export type UserTasksResponse = PagedResponse<UserTaskDTO>;
