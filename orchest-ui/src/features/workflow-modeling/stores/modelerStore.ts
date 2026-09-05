import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';
import { ModelerType, ModelerTypes } from '../constants';

interface ModelerStoreState {
  /** Active modeler tab (BPMN / DMN / Form) - ONLY this is persisted */
  activeModelerType: ModelerType;
  setActiveModelerType: (type: ModelerType) => void;
}

export const useModelerStore = create<ModelerStoreState>()(
  devtools(
    persist(
      (set) => ({
        activeModelerType: ModelerTypes.BPMN,
        setActiveModelerType: (type) =>
          set({ activeModelerType: type }, false, 'setActiveModelerType'),
      }),
      {
        name: 'orchest-modeler-store',
        storage: {
          getItem: (name) => {
            const str = sessionStorage.getItem(name);
            return str ? JSON.parse(str) : null;
          },
          setItem: (name, value) => {
            sessionStorage.setItem(name, JSON.stringify(value));
          },
          removeItem: (name) => {
            sessionStorage.removeItem(name);
          },
        },
      }
    ),
    { name: 'ModelerStore' }
  )
);

// Selector
export const useActiveModelerType = () =>
  useModelerStore((state) => state.activeModelerType);
