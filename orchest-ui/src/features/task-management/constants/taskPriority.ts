/**
 * Task priority labels
 * Maps numeric priority values (0-100) to human-readable labels
 */
export enum TaskPriority {
  LOW = 'Low',
  MEDIUM = 'Medium',
  HIGH = 'High',
  CRITICAL = 'Critical',
}

/**
 * Priority ranges for task classification
 * Based on Camunda's priority model (0-100)
 */
export const PRIORITY_RANGES = {
  LOW: { min: 0, max: 25, label: TaskPriority.LOW },
  MEDIUM: { min: 26, max: 50, label: TaskPriority.MEDIUM },
  HIGH: { min: 51, max: 75, label: TaskPriority.HIGH },
  CRITICAL: { min: 76, max: 100, label: TaskPriority.CRITICAL },
} as const;

/**
 * Get priority label from numeric value
 *
 * @param priority - Numeric priority value (0-100)
 * @returns Priority label (Low, Medium, High, Critical)
 *
 * @example
 * ```typescript
 * getPriorityLabel(10)  // 'Low'
 * getPriorityLabel(60)  // 'High'
 * getPriorityLabel(90)  // 'Critical'
 * ```
 */
export const getPriorityLabel = (priority: number): TaskPriority => {
  if (priority <= PRIORITY_RANGES.LOW.max) return TaskPriority.LOW;
  if (priority <= PRIORITY_RANGES.MEDIUM.max) return TaskPriority.MEDIUM;
  if (priority <= PRIORITY_RANGES.HIGH.max) return TaskPriority.HIGH;
  return TaskPriority.CRITICAL;
};

/**
 * Get priority variant for styling (used for badge colors)
 *
 * @param priority - Numeric priority value (0-100)
 * @returns Priority variant name
 */
export const getPriorityVariant = (priority: number): string => {
  const label = getPriorityLabel(priority);
  return label.toLowerCase();
};

/**
 * Check if a priority value is valid
 *
 * @param priority - Numeric priority value
 * @returns True if priority is between 0-100
 */
export const isValidPriority = (priority: number): boolean => {
  return priority >= 0 && priority <= 100;
};
