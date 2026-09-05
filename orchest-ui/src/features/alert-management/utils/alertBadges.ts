import type { AlertState } from '@/api/domains/alerts';
import type { BadgeProps } from '@/design-system/components/ui/badge/badge';
import type { LucideIcon } from 'lucide-react';
import { Flame, Eye, VolumeX, CheckCircle2, Ban, AlertTriangle, ShieldAlert, Info } from 'lucide-react';

type BadgeVariant = NonNullable<BadgeProps['variant']>;

interface StateBadgeConfig {
  variant: BadgeVariant;
  label: string;
  icon: LucideIcon;
  /** @deprecated */
  dotColor: string;
}

const STATE_BADGE_MAP: Record<AlertState, StateBadgeConfig> = {
  FIRING: { variant: 'destructive', label: 'Firing', icon: Flame, dotColor: 'var(--color-red-500)' },
  ACKNOWLEDGED: { variant: 'info', label: 'Acknowledged', icon: Eye, dotColor: 'var(--color-blue-500)' },
  SILENCED: { variant: 'secondary', label: 'Silenced', icon: VolumeX, dotColor: 'var(--color-gray-500)' },
  RESOLVED: { variant: 'success', label: 'Resolved', icon: CheckCircle2, dotColor: 'var(--color-green-500)' },
  DISABLED: { variant: 'warning', label: 'Disabled', icon: Ban, dotColor: 'var(--color-orange-500)' },
};

export function getAlertStateBadgeConfig(state: AlertState): StateBadgeConfig {
  return STATE_BADGE_MAP[state] ?? STATE_BADGE_MAP.SILENCED;
}

export function getAlertStateBadgeVariant(state: AlertState): BadgeVariant {
  return getAlertStateBadgeConfig(state).variant;
}

interface SeverityBadgeConfig {
  variant: BadgeVariant;
  label: string;
  icon: LucideIcon;
  /** @deprecated */
  dotColor: string;
}

const SEVERITY_LEVELS: { pattern: RegExp; variant: BadgeVariant; icon: LucideIcon; dotColor: string }[] = [
  { pattern: /(critical|fatal|emergency)/, variant: 'destructive', icon: ShieldAlert, dotColor: 'var(--color-red-500)' },
  { pattern: /(high|major|error|severe)/, variant: 'incident', icon: Flame, dotColor: 'var(--color-orange-500)' },
  { pattern: /(medium|warn|warning)/, variant: 'warning', icon: AlertTriangle, dotColor: 'var(--color-yellow-500)' },
  { pattern: /(low|minor|info|informational)/, variant: 'info', icon: Info, dotColor: 'var(--color-blue-500)' },
];

export function getAlertSeverityBadgeConfig(severity?: string | null): SeverityBadgeConfig {
  const raw = severity?.trim() ?? '';
  const normalized = raw.toLowerCase();
  if (!normalized) return { variant: 'secondary', label: '—', icon: Info, dotColor: 'var(--color-gray-400)' };

  for (const level of SEVERITY_LEVELS) {
    if (level.pattern.test(normalized)) {
      return { variant: level.variant, label: capitalize(raw), icon: level.icon, dotColor: level.dotColor };
    }
  }
  return { variant: 'secondary', label: capitalize(raw), icon: Info, dotColor: 'var(--color-gray-400)' };
}

export function getAlertSeverityBadgeVariant(severity?: string | null): BadgeVariant {
  return getAlertSeverityBadgeConfig(severity).variant;
}

export function getAlertVersion(metadata?: Record<string, string>): string {
  return metadata?.version?.trim() || '—';
}

function capitalize(s: string): string {
  return s.charAt(0).toUpperCase() + s.slice(1).toLowerCase();
}
