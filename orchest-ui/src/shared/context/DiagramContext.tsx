import { StorageUtils } from '@/shared/utils/storageUtils';
import React, { createContext, ReactNode, useContext, useState, useEffect, useRef, useCallback, useMemo } from 'react';

interface DiagramContextType {
  savedDiagrams: Record<string, string>;
  saveDiagram: (id: string, xml: string) => void;
  removeDiagram: (id: string) => void;
  getDiagram: (id: string) => string | undefined;
  clearAllDiagrams: () => void;
}

const DiagramContext = createContext<DiagramContextType | undefined>(undefined);

const DIAGRAM_STORAGE_KEY = 'saved_diagrams';

const loadDiagramsFromStorage = (): Record<string, string> => {
  const stored = StorageUtils.getItem<Record<string, string>>(DIAGRAM_STORAGE_KEY);
  return stored || {};
};

const saveDiagramsToStorage = (diagrams: Record<string, string>): void => {
  StorageUtils.setItem(DIAGRAM_STORAGE_KEY, diagrams);
};

export const DiagramProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [savedDiagrams, setSavedDiagrams] = useState<Record<string, string>>(() =>
    loadDiagramsFromStorage()
  );

  const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const isInitialMount = useRef(true);

  useEffect(() => {
    // Skip storage write on initial mount (data just loaded from storage)
    if (isInitialMount.current) {
      isInitialMount.current = false;
      return;
    }

    // Debounce storage writes to prevent excessive writes during rapid updates
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    debounceTimerRef.current = setTimeout(() => {
      saveDiagramsToStorage(savedDiagrams);
    }, 500); // 500ms debounce

    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, [savedDiagrams]);

  const saveDiagram = useCallback((id: string, xml: string) => {
    setSavedDiagrams((prev) => ({
      ...prev,
      [id]: xml,
    }));
  }, []);

  const removeDiagram = useCallback((id: string) => {
    setSavedDiagrams((prev) => {
      const newDiagrams = { ...prev };
      delete newDiagrams[id];
      return newDiagrams;
    });
  }, []);

  const getDiagram = useCallback((id: string): string | undefined => {
    return savedDiagrams[id];
  }, [savedDiagrams]);

  const clearAllDiagrams = useCallback(() => {
    setSavedDiagrams({});
    StorageUtils.removeItem(DIAGRAM_STORAGE_KEY);
  }, []);

  const contextValue = useMemo(() => ({
    savedDiagrams,
    saveDiagram,
    removeDiagram,
    getDiagram,
    clearAllDiagrams,
  }), [savedDiagrams, saveDiagram, removeDiagram, getDiagram, clearAllDiagrams]);

  return (
    <DiagramContext.Provider value={contextValue}>
      {children}
    </DiagramContext.Provider>
  );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useDiagrams = (): DiagramContextType => {
  const context = useContext(DiagramContext);
  if (context === undefined) {
    throw new Error('useDiagrams must be used within a DiagramProvider');
  }
  return context;
};
