export const createDiagramErrorContext = (engine: string): string => {
  return `diagram-viewer-import-${engine}`;
};

export const ERROR_SEVERITY = {
  HIGH: 'high',
  MEDIUM: 'medium',
  LOW: 'low',
} as const;
