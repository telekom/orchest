export const OVERRIDE_PREFIX = 'OVERRIDE_';

export const createOverrideRole = (role: string): string => {
  return `${OVERRIDE_PREFIX}${role}`;
};
