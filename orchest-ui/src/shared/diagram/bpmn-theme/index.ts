import OrchestBpmnRenderer from "./OrchestBpmnRenderer";

/**
 * diagram-js module: futuristic Orchest BPMN shape rendering (Phase 1).
 */
export default {
  __init__: ["orchestBpmnRenderer"],
  orchestBpmnRenderer: ["type", OrchestBpmnRenderer],
};

export { ORCHEST_BPMN_THEME } from "./theme";
export {
  PHASE1_ELEMENT_TYPES,
  TYPE_ICON_MAP,
  ICON_PATHS,
  ICON_DEFS,
} from "./icons/iconPaths";
export {
  shouldOrchestRender,
  hasModelerTemplateIcon,
  resolveIconKey,
  getAccentColor,
} from "./elementStyle";
