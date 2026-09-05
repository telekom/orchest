import { BADGE_STYLES, VARIABLE_TYPE_STYLES, VARIABLE_SCOPE_STYLES, type BadgeStyle } from '@/shared/constants/badgeStyles';
import { ProcessStatus } from '@/shared/constants';

const STATUS_BADGE_MAP: Record<string, BadgeStyle> = {
  [ProcessStatus.RUNNING]: BADGE_STYLES.RUNNING,
  [ProcessStatus.ACTIVE]: BADGE_STYLES.RUNNING,
  [ProcessStatus.STARTED]: BADGE_STYLES.RUNNING,
  [ProcessStatus.COMPLETED]: BADGE_STYLES.COMPLETED,
  [ProcessStatus.HOLD]: BADGE_STYLES.HOLD,
  [ProcessStatus.INCIDENT]: BADGE_STYLES.INCIDENT,
  [ProcessStatus.FAILED]: BADGE_STYLES.ERROR,
  [ProcessStatus.CANCELLED]: BADGE_STYLES.CANCELLED,
  'TRIGGERED': BADGE_STYLES.RUNNING,
  'EXECUTED': BADGE_STYLES.SUCCESS,
} as const;

export function getBadgeStyleForStatus(status: string): BadgeStyle {
  const upperStatus = (status || '').toUpperCase();
  return STATUS_BADGE_MAP[upperStatus] || BADGE_STYLES.INFO;
}

export function getBadgeStyleForVariableType(type: string): BadgeStyle {
  const upperType = (type || '').toUpperCase();

  switch (upperType) {
    case 'STRING':
      return VARIABLE_TYPE_STYLES.STRING;
    case 'NUMBER':
      return VARIABLE_TYPE_STYLES.NUMBER;
    case 'BOOLEAN':
      return VARIABLE_TYPE_STYLES.BOOLEAN;
    case 'OBJECT':
      return VARIABLE_TYPE_STYLES.OBJECT;
    default:
      return BADGE_STYLES.NEUTRAL;
  }
}

export function getBadgeStyleForVariableScope(scope: string): BadgeStyle {
  const upperScope = (scope || '').toUpperCase();

  switch (upperScope) {
    case 'LOCAL':
      return VARIABLE_SCOPE_STYLES.LOCAL;
    case 'GLOBAL':
      return VARIABLE_SCOPE_STYLES.GLOBAL;
    default:
      return BADGE_STYLES.NEUTRAL;
  }
}

export function getBadgeStyleForPriority(priority: number): BadgeStyle {
  if (priority <= 25) {
    return BADGE_STYLES.NEUTRAL;
  } else if (priority <= 50) {
    return BADGE_STYLES.WARNING;
  } else if (priority <= 75) {
    return BADGE_STYLES.INCIDENT;
  } else {
    return BADGE_STYLES.CRITICAL;
  }
}
