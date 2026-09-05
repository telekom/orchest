/**
 * Dev Tools action types for role state management
 */
export const DEV_TOOLS_ACTIONS = {
  UPDATE_ROLES: 'UPDATE_ROLES',
} as const;

export type DevToolsActionType = typeof DEV_TOOLS_ACTIONS[keyof typeof DEV_TOOLS_ACTIONS];
