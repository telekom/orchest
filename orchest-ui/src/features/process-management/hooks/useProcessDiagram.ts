import { processDefinitionService, processInstanceService } from "@/api/domains";
import { toast } from "@/design-system/components/ui/sonner";
import { useApiQuery } from "@/shared/hooks";

export interface ProcessDiagramResult {
  diagramXml: string;
  isLoading: boolean;
  error?: Error;
  refetch: () => void;
}

export function useProcessDiagram(
  processDefinitionId: string | null,
  version: string | null = null
): ProcessDiagramResult {

  const {
    data: diagramXml = "",
    isLoading,
    error,
    refetch,
  } = useApiQuery(
    ['process-diagram', processDefinitionId, version],
    async () => {
      if (!processDefinitionId) return "";

      const parsedVersion = version ? parseInt(version, 10) : undefined;

      if (parsedVersion !== undefined && !Number.isNaN(parsedVersion)) {
        const definition = await processDefinitionService.getProcessDefinition(
          processDefinitionId,
          parsedVersion
        );

        if (definition?.resourceXML) {
          return definition.resourceXML;
        }

        toast.info(`BPMN diagram not available for process version ${version}`);
        return "";
      }

      const response = await processInstanceService.getProcessInstances({
        processDefinitionId,
        page: 0,
        size: 1,
      });

      if (response && response.content && response.content.length > 0) {
        const latestInstance = response.content[0];

        const instanceResponse =
          await processInstanceService.getProcessInstance(
            latestInstance.processInstanceId
          );

        if (instanceResponse && instanceResponse.bpmnXML) {
          return instanceResponse.bpmnXML;
        } else {
          toast.info("BPMN diagram not available for this process");
          return "";
        }
      } else {
        toast.info(
          version
            ? `No instances found for process version ${version}`
            : "No instances found for this process"
        );
        return "";
      }
    },
    {
      enabled: !!processDefinitionId,
      showErrorToast: false,
    }
  );

  return {
    diagramXml,
    isLoading,
    error: error as Error | undefined,
    refetch,
  };
}
