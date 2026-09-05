import { useMemo } from 'react';
import { useDropdownOptions } from './useDropdownOptions';

interface Definition {
  definitionId: string;
  version: number;
}

interface UseVersionCalculationOptions {
  definitions: readonly Definition[] | undefined;
  selectedId: string | null | undefined;
}

interface UseVersionCalculationResult {
  versionData: string[];
  versionOptions: Array<{ value: string; label: string }>;
  calculateLatestVersion: (definitionId: string | null) => string | null;
}

/**
 * Custom hook to handle version calculation logic for filter sidebars.
 *
 * This hook:
 * - Calculates available versions for the currently selected definition
 * - Provides version dropdown options
 * - Provides a function to calculate latest version for a new selection
 *
 * @param definitions - Array of definitions (process or decision)
 * @param selectedId - Currently selected definition ID
 * @returns Version data, options, and latest version calculator
 */
export function useVersionCalculation({
  definitions,
  selectedId,
}: UseVersionCalculationOptions): UseVersionCalculationResult {
  // Memoized version data for currently selected definition
  const versionData = useMemo(() => {
    if (!selectedId || !definitions) return [];

    return definitions
      .filter((d) => d.definitionId === selectedId)
      .map((d) => String(d.version))
      .sort((a, b) => Number(b) - Number(a));
  }, [selectedId, definitions]);

  // Version dropdown options
  const versionOptions = useDropdownOptions({
    data: versionData,
    transform: (versions) => versions.map((v) => ({ value: v, label: `Version ${v}` })),
    includeAllOption: true,
    allOption: { value: 'all', label: 'All Versions' },
  });

  /**
   * Calculate the latest version for a given definition ID.
   *
   * IMPORTANT: This function calculates versions for the NEW selection synchronously,
   * which is crucial to avoid stale closure issues. Do NOT use versionData here
   * as it is memoized based on the OLD selectedId.
   *
   * @param definitionId - The definition ID to calculate latest version for
   * @returns Latest version string or null if no versions available
   */
  const calculateLatestVersion = (definitionId: string | null): string | null => {
    if (!definitionId || !definitions) return null;

    const versions = definitions
      .filter((d) => d.definitionId === definitionId)
      .map((d) => String(d.version))
      .sort((a, b) => Number(b) - Number(a));

    return versions.length > 0 ? versions[0] : null;
  };

  return {
    versionData,
    versionOptions,
    calculateLatestVersion,
  };
}
