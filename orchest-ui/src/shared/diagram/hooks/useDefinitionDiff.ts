import { processDefinitionService, decisionDefinitionService } from "@/api/domains";
import { useApiQuery } from "@/shared/hooks";
import { useCallback } from "react";

interface UseDefinitionDiffOptions {
  type: "bpmn" | "dmn";
  selectedDefinition: string;
  enabled?: boolean;
}

export function useDefinitionDiff({
  type,
  selectedDefinition,
  enabled = true,
}: UseDefinitionDiffOptions) {
  const fetchLatestVersionXml = useCallback(async () => {
    if (!selectedDefinition) return null;

    try {
      const service = type === "bpmn" ? processDefinitionService : decisionDefinitionService;

      const response = type === "bpmn"
        ? await service.getProcessDefinitions({ size: 1000 })
        : await service.getDecisionDefinitions({ size: 1000 });

      const definitions = response?.content || [];

      const versions = definitions
        .filter(d => d.definitionId === selectedDefinition)
        .map(d => parseInt(d.version?.toString() || "0", 10))
        .filter(v => v > 0)
        .sort((a, b) => b - a);

      const latestVersion = versions[0];
      if (!latestVersion) return null;

      const xmlResponse = type === "bpmn"
        ? await processDefinitionService.getProcessDefinition(selectedDefinition, latestVersion)
        : await decisionDefinitionService.getDecisionDefinitionByVersion(selectedDefinition, latestVersion);

      return xmlResponse?.resourceXML;
    } catch {
      return null;
    }
  }, [type, selectedDefinition]);

  const { data: originalXml, isLoading, error } = useApiQuery(
    ["definition-diff", type, selectedDefinition],
    fetchLatestVersionXml,
    {
      enabled: enabled && !!selectedDefinition,
      retry: 1,
    }
  );

  return {
    originalXml,
    isLoading,
    error,
    hasOriginal: !!originalXml,
  };
}
