import { useCallback, useState } from 'react';

export type ModalStateMap = Record<string, boolean>;

export interface UseModalStateConfig {
  modalNames: string[];
  initialState?: Partial<ModalStateMap>;
}

/**
 * Hook for managing multiple modal open/close states
 */
export function useModalState(config: UseModalStateConfig) {
  const { modalNames, initialState = {} } = config;

  const [modalStates, setModalStates] = useState<ModalStateMap>(() => {
    const initial: ModalStateMap = {};
    modalNames.forEach((name) => {
      initial[name] = initialState[name] ?? false;
    });
    return initial;
  });

  const open = useCallback((modalName: string) => {
    setModalStates((prev) => ({ ...prev, [modalName]: true }));
  }, []);

  const close = useCallback((modalName: string) => {
    setModalStates((prev) => ({ ...prev, [modalName]: false }));
  }, []);

  const toggle = useCallback((modalName: string) => {
    setModalStates((prev) => ({ ...prev, [modalName]: !prev[modalName] }));
  }, []);

  const isOpen = useCallback(
    (modalName: string): boolean => {
      return modalStates[modalName] ?? false;
    },
    [modalStates]
  );

  const closeAll = useCallback(() => {
    setModalStates((prev) => {
      const newState: ModalStateMap = {};
      Object.keys(prev).forEach((key) => {
        newState[key] = false;
      });
      return newState;
    });
  }, []);

  const openAll = useCallback(() => {
    setModalStates((prev) => {
      const newState: ModalStateMap = {};
      Object.keys(prev).forEach((key) => {
        newState[key] = true;
      });
      return newState;
    });
  }, []);

  const getAll = useCallback((): ModalStateMap => {
    return { ...modalStates };
  }, [modalStates]);

  const isAnyOpen = useCallback((): boolean => {
    return Object.values(modalStates).some((state) => state);
  }, [modalStates]);

  return {
    open,
    close,
    toggle,
    isOpen,
    closeAll,
    openAll,
    getAll,
    isAnyOpen,
  };
}
