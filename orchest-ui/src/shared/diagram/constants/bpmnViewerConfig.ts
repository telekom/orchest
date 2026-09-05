import TemplateIconRendererModule from "@bpmn-io/element-templates-icons-renderer";
import OrchestBpmnThemeModule from "@/shared/diagram/bpmn-theme";
import ZeebeBpmnModdle from "zeebe-bpmn-moddle/resources/zeebe.json";

export const BPMN_VIEWER_DEFAULTS = {
  additionalModules: [OrchestBpmnThemeModule, TemplateIconRendererModule],
  moddleExtensions: { zeebe: ZeebeBpmnModdle },
} as const;
