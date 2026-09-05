import andGatewayIcon from "@/assets/bpmn/and-gateway.svg";
import businessRuleTaskIcon from "@/assets/bpmn/business-rule-task.svg";
import callActivityIcon from "@/assets/bpmn/call-activity.svg";
import eventGatewayIcon from "@/assets/bpmn/event-gateway.svg";
import manualTaskIcon from "@/assets/bpmn/manual-task.svg";
import orGatewayIcon from "@/assets/bpmn/or-gateway.svg";
import receiveTaskIcon from "@/assets/bpmn/receive-task.svg";
import scriptTaskIcon from "@/assets/bpmn/script-task.svg";
import sendTaskIcon from "@/assets/bpmn/send-task.svg";
import serviceTaskIcon from "@/assets/bpmn/service-task.svg";
import subprocessIcon from "@/assets/bpmn/subprocess.svg";
import undefinedTaskIcon from "@/assets/bpmn/undefined-task.svg";
import userTaskIcon from "@/assets/bpmn/user-task.svg";
import xorGatewayIcon from "@/assets/bpmn/xor-gateway.svg";

/**
 * Maps a process node type (as returned in the `nodeType` attribute of a
 * sequence execution) to the BPMN icon that visually represents it.
 *
 * Node types without a dedicated BPMN symbol (e.g. events) intentionally fall
 * back to the timeline status dot instead of a misleading icon.
 */
const NODE_TYPE_ICON_MAP: Record<string, string> = {
  TASK: undefinedTaskIcon,
  USER_TASK: userTaskIcon,
  SERVICE_TASK: serviceTaskIcon,
  RECEIVE_TASK: receiveTaskIcon,
  SEND_TASK: sendTaskIcon,
  BUSINESS_RULE_TASK: businessRuleTaskIcon,
  SCRIPT_TASK: scriptTaskIcon,
  MANUAL_TASK: manualTaskIcon,
  SUB_PROCESS: subprocessIcon,
  CALL_ACTIVITY: callActivityIcon,
  EXCLUSIVE_GATEWAY: xorGatewayIcon,
  PARALLEL_GATEWAY: andGatewayIcon,
  INCLUSIVE_GATEWAY: orGatewayIcon,
  EVENT_BASED_GATEWAY: eventGatewayIcon,
};

/**
 * Returns the icon URL for a given node type, or `undefined` when no icon is
 * available for that type.
 */
export const getNodeTypeIcon = (nodeType?: string): string | undefined =>
  nodeType ? NODE_TYPE_ICON_MAP[nodeType] : undefined;
