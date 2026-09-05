import { ApprovalState } from '@/shared/enums';

type BadgeVariant = 'default' | 'destructive' | 'secondary';

export const STATE_BADGE_VARIANTS: Record<ApprovalState, BadgeVariant> = {
  [ApprovalState.ACCEPTED]: 'default',
  [ApprovalState.REJECTED]: 'destructive',
  [ApprovalState.REQUESTED]: 'secondary',
};
