
import { DecisionInstanceDTO, ProcessInstanceDTO } from "@/api/domains";
import { ProcessInstance as ContextProcessInstance, DecisionInstance as ContextDecisionInstance } from "@/shared/types";

export function mapDomainToContextProcessInstance(domain: ProcessInstanceDTO): ContextProcessInstance {
  return {
    processId: domain.processInstanceId,
    parentProcessId: domain.parentProcessInstanceId ?? "",
    processName: domain.processDefinitionId,
    processVersion: domain.version?.toString() || "1",
    sequenceExecutions: domain.sequenceExecutions ?? {},
    status: domain.state,
    startDate: domain.createdAt,
    endDate: domain.completedAt ?? null,
    incidentMessage: domain.incidentMessage,
    lastActivityAt: domain.lastActivityAt,
    activeElements: domain.activeElements,
  };
}

export function mapDomainToContextDecisionInstance(
  domain: DecisionInstanceDTO
): ContextDecisionInstance {
  return {
    decisionId: domain.decisionId,
    decisionInstanceId: domain.decisionInstanceId,
    processInstanceId: domain.processInstanceId || "",
    inputVariables: domain.inputVariables || {},
    outputVariables: domain.outputVariables || {},
    version: domain.version?.toString() || "1",
    state: domain.state || "UNKNOWN",
    resourceUTF8XML: domain.resourceUTF8XML || "",
    executedAt: domain.executedAt,
    matchedRuleId: domain.matchedRuleIds || domain.matchedRuleId || undefined,
  };
}

export function mapDomainToContextProcessInstances(
  domains: ProcessInstanceDTO[]
): ContextProcessInstance[] {
  return domains.map(mapDomainToContextProcessInstance);
}

export function mapDomainToContextDecisionInstances(
  domains: DecisionInstanceDTO[]
): ContextDecisionInstance[] {
  return domains.map(mapDomainToContextDecisionInstance);
}
