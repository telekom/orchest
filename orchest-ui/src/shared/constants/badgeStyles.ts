import type { LucideIcon } from 'lucide-react';
import {
  AlertTriangle,
  CheckCircle2,
  XCircle,
  Clock,
  Play,
  Send,
  Eye,
  Info,
  Flame,
  Globe,
  Lock,
  Hash,
  ToggleLeft,
  Braces,
  Ban,
  Hourglass,
  TimerOff,
  ShieldAlert,
  PauseCircle,
  CircleDot,
} from 'lucide-react';

export interface BadgeStyle {
  icon?: LucideIcon;
  /** @deprecated kept for back-compat, use icon */
  dotColor: string;
  className?: string;
  variant: "default" | "secondary" | "destructive" | "outline" | "success" | "warning" | "incident" | "info" | "purple" | "indigo" | "pink";
}

const BASE_BADGE_CLASS = "max-w-full";

export const BADGE_STYLES = {
  SUCCESS: {
    icon: CheckCircle2,
    dotColor: '#16a34a',
    className: BASE_BADGE_CLASS,
    variant: "success" as const,
  },
  RUNNING: {
    icon: Play,
    dotColor: '#16a34a',
    className: BASE_BADGE_CLASS,
    variant: "success" as const,
  },
  COMPLETED: {
    icon: CheckCircle2,
    dotColor: '#2563eb',
    className: BASE_BADGE_CLASS,
    variant: "info" as const,
  },
  WARNING: {
    icon: AlertTriangle,
    dotColor: '#eab308',
    className: BASE_BADGE_CLASS,
    variant: "warning" as const,
  },
  HOLD: {
    icon: PauseCircle,
    dotColor: '#eab308',
    className: BASE_BADGE_CLASS,
    variant: "warning" as const,
  },
  INCIDENT: {
    icon: Flame,
    dotColor: '#f97316',
    className: BASE_BADGE_CLASS,
    variant: "incident" as const,
  },
  ERROR: {
    icon: XCircle,
    dotColor: '#dc2626',
    className: BASE_BADGE_CLASS,
    variant: "destructive" as const,
  },
  CRITICAL: {
    icon: ShieldAlert,
    dotColor: '#dc2626',
    className: BASE_BADGE_CLASS,
    variant: "destructive" as const,
  },
  NEUTRAL: {
    icon: CircleDot,
    dotColor: '#6b7280',
    className: BASE_BADGE_CLASS,
    variant: "outline" as const,
  },
  CANCELLED: {
    icon: Ban,
    dotColor: '#6b7280',
    className: BASE_BADGE_CLASS,
    variant: "outline" as const,
  },
  INFO: {
    icon: Info,
    dotColor: '#2563eb',
    className: BASE_BADGE_CLASS,
    variant: "info" as const,
  },
  IN_PROGRESS: {
    icon: Play,
    dotColor: '#2563eb',
    className: BASE_BADGE_CLASS,
    variant: "info" as const,
  },
  SUBMITTED: {
    icon: Send,
    dotColor: '#a855f7',
    className: BASE_BADGE_CLASS,
    variant: "purple" as const,
  },
  IN_REVIEW: {
    icon: Eye,
    dotColor: '#eab308',
    className: BASE_BADGE_CLASS,
    variant: "warning" as const,
  },
  PENDING: {
    icon: Hourglass,
    dotColor: '#f97316',
    className: BASE_BADGE_CLASS,
    variant: "incident" as const,
  },
  EXPIRED: {
    icon: TimerOff,
    dotColor: '#6b7280',
    className: BASE_BADGE_CLASS,
    variant: "secondary" as const,
  },
} as const;

export const VARIABLE_TYPE_STYLES = {
  STRING: {
    icon: Hash,
    dotColor: '#2563eb',
    className: BASE_BADGE_CLASS,
    variant: "info" as const,
  },
  NUMBER: {
    icon: Hash,
    dotColor: '#16a34a',
    className: BASE_BADGE_CLASS,
    variant: "success" as const,
  },
  BOOLEAN: {
    icon: ToggleLeft,
    dotColor: '#f97316',
    className: BASE_BADGE_CLASS,
    variant: "warning" as const,
  },
  OBJECT: {
    icon: Braces,
    dotColor: '#a855f7',
    className: BASE_BADGE_CLASS,
    variant: "purple" as const,
  },
} as const;

export const VARIABLE_SCOPE_STYLES = {
  LOCAL: {
    icon: Lock,
    dotColor: '#6366f1',
    className: BASE_BADGE_CLASS,
    variant: "indigo" as const,
  },
  GLOBAL: {
    icon: Globe,
    dotColor: '#ec4899',
    className: BASE_BADGE_CLASS,
    variant: "pink" as const,
  },
} as const;
