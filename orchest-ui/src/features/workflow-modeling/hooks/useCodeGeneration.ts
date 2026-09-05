import { aiService } from "@/api/external";
import { logger } from "@/shared/utils/logger";
import { TOAST_MESSAGES } from "@/shared/constants";
import { MutableRefObject, useCallback, useState } from "react";
import { toast } from "@/design-system/components/ui/sonner";
import { MODELER_CONSTANTS } from "../constants/modelerConfig";

interface BpmnModelerInstance {
  saveXML(options: { format: boolean }): Promise<{ xml?: string }>;
}

interface CodeGenerationResponse {
  projectId: string;
  status?: string;
}

interface UseCodeGenerationOptions {
  modelerRef: MutableRefObject<BpmnModelerInstance | null>;
}

interface UseCodeGenerationReturn {
  generateCode: () => Promise<void>;
  isGenerating: boolean;
  generatedProjectId: string;
}

export function useCodeGeneration({
  modelerRef,
}: UseCodeGenerationOptions): UseCodeGenerationReturn {
  const [isGenerating, setIsGenerating] = useState(false);
  const [generatedProjectId, setGeneratedProjectId] = useState("");

  const getProcessName = useCallback((): string | null => {
    const containerGeneral = document.querySelector('[data-group-id="group-general"]');
    const generalInput = containerGeneral?.querySelector(
      ".bio-properties-panel-input"
    ) as HTMLInputElement | null;

    if (!generalInput || !generalInput.value) {
      toast.error(
        "Please provide a process name in the properties panel before generating code."
      );
      return null;
    }

    return generalInput.value;
  }, []);

  const pollForCompletion = useCallback(async (projectId: string): Promise<void> => {
    return new Promise((resolve, reject) => {
      const intervalId = setInterval(async () => {
        try {
          const response = await aiService.checkCodeGenerationStatus(projectId);

          if (response.status === "completed") {
            clearInterval(intervalId);
            await aiService.downloadProject(projectId);
            setIsGenerating(false);
            toast.success(TOAST_MESSAGES.SUCCESS.CODE_GENERATION_COMPLETED);
            resolve();
          } else if (response.status === "failed") {
            clearInterval(intervalId);
            setIsGenerating(false);
            toast.error(TOAST_MESSAGES.ERROR.CODE_GENERATION_FAILED);
            reject(new Error("Code generation failed"));
          }
        } catch (error) {
          logger.error("Error checking code generation status:", error);
        }
      }, MODELER_CONSTANTS.POLLING.CODE_GENERATION_INTERVAL);

      setTimeout(() => {
        clearInterval(intervalId);
        if (isGenerating) {
          setIsGenerating(false);
          toast.error(TOAST_MESSAGES.ERROR.CODE_GENERATION_TIMEOUT);
          reject(new Error("Code generation timeout"));
        }
      }, MODELER_CONSTANTS.POLLING.CODE_GENERATION_TIMEOUT);
    });
  }, [isGenerating]);

  const generateCode = useCallback(async () => {
    if (!modelerRef.current) {
      toast.error(TOAST_MESSAGES.ERROR.MODELER_NOT_INITIALIZED);
      return;
    }

    const processName = getProcessName();
    if (!processName) return;

    setIsGenerating(true);
    toast.success(TOAST_MESSAGES.SUCCESS.CODE_GENERATION_STARTED);

    try {
      const { xml } = await modelerRef.current.saveXML({ format: true });

      if (!xml) {
        toast.error(TOAST_MESSAGES.ERROR.EXPORT_BPMN_XML);
        setIsGenerating(false);
        return;
      }

      const generationResponse: CodeGenerationResponse = await aiService.generateProject(
        processName,
        xml,
        "de.telekom"
      );

      setGeneratedProjectId(generationResponse.projectId);

      await pollForCompletion(generationResponse.projectId);
    } catch (error) {
      logger.error("Code generation error:", error);
      toast.error(TOAST_MESSAGES.ERROR.GENERATING_CODE);
      setIsGenerating(false);
    }
  }, [modelerRef, getProcessName, pollForCompletion]);

  return {
    generateCode,
    isGenerating,
    generatedProjectId,
  };
}
