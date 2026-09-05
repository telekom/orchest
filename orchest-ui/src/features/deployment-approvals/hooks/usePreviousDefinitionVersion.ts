import {
  decisionDefinitionService,
  processDefinitionService,
} from '@/api/domains';
import { useApiQuery } from '@/shared/hooks/useApiQuery';

interface DefinitionInfo {
  definitionId: string;
  diagramType: 'bpmn' | 'dmn' | 'unknown';
}

interface UseDefinitionVersionParams {
  definitionInfo: DefinitionInfo;
  approvalId: string;
  enabled?: boolean;
}

/**
 * Fetches the latest deployed version XML for a given definition.
 * Used for comparing with pending approval changes.
 */
export const usePreviousDefinitionVersion = ({
  definitionInfo,
  approvalId,
  enabled = true,
}: UseDefinitionVersionParams) => {
  return useApiQuery(
    ['previous-version', approvalId, definitionInfo.definitionId],
    async () => {
      const { definitionId, diagramType } = definitionInfo;

      if (diagramType === 'bpmn') {
        return fetchLatestProcessXml(definitionId);
      }

      if (diagramType === 'dmn') {
        return fetchLatestDecisionXml(definitionId);
      }

      return null;
    },
    {
      enabled: enabled && definitionInfo.diagramType !== 'unknown',
      staleTime: 60000, // 1 minute - definitions don't change frequently
    }
  );
};

// Extract version fetching logic
const getLatestVersion = (
  definitions: Array<{ definitionId?: string; version?: number }>,
  targetDefinitionId: string
): number | null => {
  const versions = definitions
    .filter((d) => d.definitionId === targetDefinitionId)
    .map((d) => d.version as number)
    .sort((a, b) => b - a);

  return versions.length > 0 ? versions[0] : null;
};

const fetchLatestProcessXml = async (
  definitionId: string
): Promise<string | null> => {
  const response = await processDefinitionService.getProcessDefinitions({
    size: 1000,
  });

  const latestVersion = getLatestVersion(response.content, definitionId);
  if (!latestVersion) return null;

  const definition = await processDefinitionService.getProcessDefinition(
    definitionId,
    latestVersion
  );

  return definition.resourceXML || null;
};

const fetchLatestDecisionXml = async (
  definitionId: string
): Promise<string | null> => {
  const response = await decisionDefinitionService.getDecisionDefinitions({
    size: 100,
  });

  const latestVersion = getLatestVersion(response.content, definitionId);
  if (!latestVersion) return null;

  const definition =
    await decisionDefinitionService.getDecisionDefinitionByVersion(
      definitionId,
      latestVersion
    );

  return definition.resourceXML || null;
};
