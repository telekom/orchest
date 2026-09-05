import { useCallback, useRef } from "react";
import { toast } from "@/design-system/components/ui/sonner";
import { globalErrorHandler } from "@/shared/error/globalErrorHandler";
import { DiagramTypeValue } from "@/shared/enums/DiagramType";
import { DiagramViewer } from "./useDiagramViewer";

interface UseFileImportConfig {
  modelerRef: React.RefObject<DiagramViewer | null>;
  onImportSuccess?: () => void;
  onImportError?: (error: Error) => void;
  onImportStart?: () => void;
  fileType: DiagramTypeValue;
}

export const useFileImport = ({
  modelerRef,
  onImportSuccess,
  onImportError,
  onImportStart,
  fileType,
}: UseFileImportConfig) => {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const importXML = useCallback(
    async (xml: string) => {
      if (!modelerRef.current) {
        toast.error("Modeler not initialized");
        return;
      }

      try {
        onImportStart?.();
        await modelerRef.current.importXML(xml);
        toast.success("Diagram imported successfully");
        onImportSuccess?.();
      } catch (err) {
        const error = err instanceof Error ? err : new Error(String(err));
        globalErrorHandler.handleError(error, 'runtime', {
          context: `diagram-import-${fileType}`,
          severity: 'medium'
        });
        toast.error("Failed to import diagram");
        onImportError?.(error);
      }
    },
    [modelerRef, onImportSuccess, onImportError, onImportStart, fileType]
  );

  const handleFileSelect = useCallback(
    (event: React.ChangeEvent<HTMLInputElement>) => {
      const file = event.target.files?.[0];
      if (!file) return;

      const reader = new FileReader();
      reader.onload = (e) => {
        const xml = e.target?.result as string;
        importXML(xml);

        if (fileInputRef.current) {
          fileInputRef.current.value = "";
        }
      };
      reader.onerror = () => {
        toast.error("Failed to read file");
        onImportError?.(new Error("Failed to read file"));
      };
      reader.readAsText(file);
    },
    [importXML, onImportError]
  );

  const handleDragOver = useCallback((event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    event.dataTransfer.dropEffect = "copy";
    event.currentTarget.classList.add("!border-primary");
  }, []);

  const handleDragLeave = useCallback((event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    event.currentTarget.classList.remove("!border-primary");
  }, []);

  const handleDrop = useCallback(
    (event: React.DragEvent<HTMLDivElement>) => {
      event.preventDefault();
      event.currentTarget.classList.remove("!border-primary");

      const file = event.dataTransfer.files[0];
      if (!file) return;

      const reader = new FileReader();
      reader.onload = (e) => {
        const xml = e.target?.result as string;
        importXML(xml);
      };
      reader.onerror = () => {
        toast.error("Failed to read file");
        onImportError?.(new Error("Failed to read file"));
      };
      reader.readAsText(file);
    },
    [importXML, onImportError]
  );

  return {
    fileInputRef,
    handleFileSelect,
    handleDragOver,
    handleDragLeave,
    handleDrop,
  };
};
