import { format } from "date-fns";

function formatDateBase(
  dateString: string | null | undefined,
  formatter: (date: Date) => string,
  fallback: string
): string {
  if (!dateString) return fallback;
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return fallback;
    return formatter(date);
  } catch {
    return fallback;
  }
}

export function formatDate(dateString: string | null | undefined, fallback: string = "-"): string {
  return formatDateBase(dateString, (date) => format(date, "MMM d, HH:mm"), fallback);
}

export function formatDateDetailed(
  dateString: string | null | undefined,
  fallback: string = "-"
): string {
  return formatDateBase(dateString, (date) => format(date, "MMM d, yyyy HH:mm:ss"), fallback);
}

export function formatDateFull(
  dateString: string | null | undefined,
  fallback: string = "No date set"
): string {
  return formatDateBase(dateString, (date) => format(date, "EEEE, MMMM d, yyyy 'at' HH:mm:ss"), fallback);
}

export function formatDateISO(
  dateString: string | null | undefined,
  fallback: string = "-"
): string {
  return formatDateBase(dateString, (date) => format(date, "yyyy-MM-dd"), fallback);
}

export function formatTime(
  dateString: string | null | undefined,
  fallback: string = "-"
): string {
  return formatDateBase(dateString, (date) => format(date, "HH:mm:ss"), fallback);
}

export function formatRelativeTime(dateString: string | null | undefined): string {
  if (!dateString) return "Unknown time";

  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return "Invalid date";

    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffSecs = Math.floor(diffMs / 1000);
    const diffMins = Math.floor(diffSecs / 60);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffSecs < 60) return "just now";
    if (diffMins < 60) return `${diffMins} minute${diffMins !== 1 ? "s" : ""} ago`;
    if (diffHours < 24) return `${diffHours} hour${diffHours !== 1 ? "s" : ""} ago`;
    if (diffDays < 7) return `${diffDays} day${diffDays !== 1 ? "s" : ""} ago`;

    return formatDate(dateString);
  } catch {
    return "Invalid date";
  }
}

export function isValidDate(dateString: string | null | undefined): boolean {
  if (!dateString) return false;
  try {
    const date = new Date(dateString);
    return !isNaN(date.getTime());
  } catch {
    return false;
  }
}
