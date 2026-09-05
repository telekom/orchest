import { useState, useMemo } from "react";
import { useApiQuery } from "@/shared/hooks";

interface DefinitionDTO {
  definitionId: string;
  version?: number | string;
}

interface UseDefinitionLoaderConfig {
  type: "process" | "decision" | "form";
  fetchService: {
    getDefinitions: (params: { size: number }) => Promise<{ content: DefinitionDTO[] }>;
  } | null;
  enabled?: boolean;
  selectedDefinition?: string;
}

export const useDefinitionLoader = ({
  type,
  fetchService,
  enabled = true,
  selectedDefinition: externalSelectedDefinition
}: UseDefinitionLoaderConfig) => {
  const [selectedDefinition, setSelectedDefinition] = useState<string>("");
  const [selectedVersion, setSelectedVersion] = useState<string>("");

  const activeDefinition = externalSelectedDefinition ?? selectedDefinition;

  const { data: definitionsResponse, isLoading: loadingDefinitions } = useApiQuery(
    [`${type}-definitions`],
    async () => {
      if (!fetchService) return null;
      return await fetchService.getDefinitions({ size: 1000 });
    },
    {
      enabled: enabled && !!fetchService,
      showErrorToast: false,
    }
  );

  const definitions = useMemo(() => {
    if (!definitionsResponse?.content) return [];

    const unique = Array.from(
      new Set(definitionsResponse.content.map((d) => d.definitionId))
    ).map((name: string, index: number) => {
      const definition = definitionsResponse.content.find(
        (d) => d.definitionId === name
      );
      return {
        id: definition?.definitionId || `_generated_id_${index}`,
        name: name || `_generated_name_${index}`,
      };
    });

    return unique;
  }, [definitionsResponse]);

  const versions = useMemo(() => {
    if (!activeDefinition || !definitionsResponse?.content) return [];

    const definitionVersions = definitionsResponse.content
      .filter((d) => d.definitionId === activeDefinition)
      .map((d) => d.version?.toString() || "")
      .sort((a: string, b: string) => parseInt(b) - parseInt(a));

    return definitionVersions;
  }, [activeDefinition, definitionsResponse]);

  return {
    selectedDefinition,
    setSelectedDefinition,
    selectedVersion,
    setSelectedVersion,
    definitions,
    versions,
    loadingDefinitions,
  };
};
