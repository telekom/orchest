import { deploymentApprovalsService } from "@/api/domains";
import { toast } from "@/design-system/components/ui/sonner";
import { useAuth, UserRoles } from "@/shared/auth";
import { buildResourceDeploymentRequest } from "@/shared/diagram/utils/buildResourceDeploymentRequest";
import { useErrorHandling } from "@/shared/hooks/useErrorHandling";
import { MutableRefObject, useCallback } from "react";
import { DiagramViewer } from "./useDiagramViewer";

interface UseModelerActionsOptions {
  modelerRef: MutableRefObject<DiagramViewer | null>;
  diagramType: "bpmn" | "dmn" | "form";
  emptyTemplate: string | object;
  onSave?: (xml: string) => void;
  setSelectedVersion?: (version: string) => void;
}

interface UseModelerActionsReturn {
  handleUndo: () => void;
  handleRedo: () => void;
  handleNew: () => void;
  getCurrentXml: () => Promise<string | null>;
  handleDeploy: (xml: string, options?: { compensateFlow?: boolean }) => Promise<void>;
}

interface CommandStack {
  undo: () => void;
  redo: () => void;
  canUndo?: () => boolean;
  canRedo?: () => boolean;
}

const getCommandStack = (modeler: DiagramViewer, isDmn: boolean): CommandStack | null => {
  if (isDmn) {
    const activeViewer = modeler.getActiveViewer?.();
    return activeViewer?.get?.("commandStack") || null;
  }
  return modeler.get("commandStack");
};

const fixDmnTableTransform = () => {
  setTimeout(() => {
    const tables = document.querySelectorAll(".tjs-table");
    tables.forEach((t) => {
      if (t instanceof HTMLElement) {
        t.style.transform = "translateZ(0)";
      }
    });
  }, 50);
};

export function useModelerActions({
  modelerRef,
  diagramType,
  emptyTemplate,
  onSave,
  setSelectedVersion,
}: UseModelerActionsOptions): UseModelerActionsReturn {
  const diagramTypeLabel = diagramType.toUpperCase();
  const { user, hasRole } = useAuth();
  const { handleError } = useErrorHandling({ context: `modeler-${diagramType}` });

  const handleUndo = useCallback(() => {
    const modeler = modelerRef.current;
    if (!modeler || diagramType === "form") return;

    try {
      const isDmn = diagramType === "dmn";
      const commandStack = getCommandStack(modeler, isDmn);

      if (isDmn && commandStack?.canUndo?.()) {
        const activeElement = document.activeElement;
        const wasEditing =
          !!activeElement &&
          ["input", "textarea"].includes(activeElement.tagName.toLowerCase());

        commandStack.undo();

        if (wasEditing) {
          fixDmnTableTransform();
        }
      } else if (!isDmn && commandStack) {
        commandStack.undo();
      }
    } catch (error) {
      handleError(error, { severity: 'low', action: 'undo' });
    }
  }, [modelerRef, diagramType, handleError]);

  const handleRedo = useCallback(() => {
    const modeler = modelerRef.current;
    if (!modeler || diagramType === "form") return;

    try {
      const isDmn = diagramType === "dmn";
      const commandStack = getCommandStack(modeler, isDmn);

      if (isDmn && commandStack?.canRedo?.()) {
        commandStack.redo();
      } else if (!isDmn && commandStack) {
        commandStack.redo();
      }
    } catch (error) {
      handleError(error, { severity: 'low', action: 'redo' });
    }
  }, [modelerRef, diagramType, handleError]);

  const handleNew = useCallback(async () => {

    const modeler = modelerRef.current;
    if (!modeler) {
      toast.error("Modeler not initialized");
      return;
    }


    try {
      // Handle different diagram types
      if (diagramType === "form") {
        // Form uses importSchema instead of importXML
        if ('importSchema' in modeler && typeof modeler.importSchema === 'function') {
          await modeler.importSchema(emptyTemplate);
          toast.success("New form created");
          setSelectedVersion?.('');
          return;
        }
      } else if ('importXML' in modeler && typeof modeler.importXML === 'function') {
        // BPMN and DMN use importXML
        await modeler.importXML(emptyTemplate as string);

        // For DMN, need to open the first available view after import
        if (diagramType === "dmn" && 'getViews' in modeler && 'open' in modeler) {
          try {
            const views = (modeler as any).getViews();
            if (views && views.length > 0) {
              await (modeler as any).open(views[0]);
            }
          } catch (dmnError) {
          }
        }

        // Zoom to fit the diagram in viewport (only for BPMN, DMN handles it differently)
        if (diagramType === "bpmn") {
          try {
            if ('get' in modeler && typeof modeler.get === 'function') {
              const canvas = modeler.get('canvas');
              if (canvas && typeof canvas.zoom === 'function') {
                canvas.zoom('fit-viewport');
              }
            }
          } catch (zoomError) {
          }
        }

        toast.success("New diagram created");
        setSelectedVersion?.('');
      }
    } catch (err) {
      handleError(err as Error, { severity: 'medium', action: 'new' });
      toast.error("Failed to create new diagram");
    }
  }, [modelerRef, emptyTemplate, diagramType, setSelectedVersion, handleError]);

  const getCurrentXml = useCallback(async (): Promise<string | null> => {
    const modeler = modelerRef.current;
    if (!modeler || !modeler.saveXML) return null;

    try {
      const { xml } = await modeler.saveXML({ format: true });
      return xml;
    } catch (err) {
      handleError(err, { severity: 'medium', action: 'get-xml' });
      toast.error("Failed to get diagram XML");
      return null;
    }
  }, [modelerRef, handleError]);

  const handleDeploy = useCallback(async (xml: string, options?: { compensateFlow?: boolean }) => {
    try {
      if (!hasRole(UserRoles.ADMIN, true)) {
        toast.error("Only administrators can deploy diagrams");
        throw new Error("Insufficient permissions: ADMIN role required for deployment");
      }

      onSave?.(xml);

      if (!user?.email) {
        throw new Error("User email not available");
      }

      toast.success("Requesting deployment approval...");

      const approvalResponse = await deploymentApprovalsService.requestApproval({
        deploymentRequest:
          diagramType === "bpmn"
            ? buildResourceDeploymentRequest(xml, options?.compensateFlow ?? false)
            : {
                resourceUTF8XML: xml,
                partitionCount: 1,
                bypassWorkerValidation: true,
                approvers: [],
              },
        type: diagramTypeLabel,
        requestedBy: user.email,
      });

      toast.success(
        `${diagramTypeLabel} deployment approval requested successfully! Approval ID: ${approvalResponse.id}`
      );
    } catch (deployError) {
      handleError(deployError, { severity: 'high', action: 'deploy' });
      toast.error(`Failed to request ${diagramTypeLabel} deployment approval`);
      throw deployError;
    }
  }, [diagramType, diagramTypeLabel, user, hasRole, onSave, handleError]);

  return {
    handleUndo,
    handleRedo,
    handleNew,
    getCurrentXml,
    handleDeploy,
  };
}
