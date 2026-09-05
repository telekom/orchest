import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'

export type ThemeMode = 'light' | 'dark' | 'system'

interface UIState {
  sidebarCollapsed: boolean
  setSidebarCollapsed: (collapsed: boolean) => void
  toggleSidebar: () => void

  filterSidebarCollapsed: boolean
  toggleFilterSidebar: () => void

  activeInstanceId: string | null
  setActiveInstanceId: (id: string | null) => void

  modals: {
    processModification: boolean
    variableEdit: boolean
    confirmAction: boolean
    diffModal: boolean
    uploadDialog: boolean
    newDiagramDialog: boolean
    deleteApproverModal: boolean
    evaluateDecisionModal: boolean
  }
  openModal: (modal: keyof UIState['modals']) => void
  closeModal: (modal: keyof UIState['modals']) => void

  globalLoading: boolean
  setGlobalLoading: (loading: boolean) => void

  theme: ThemeMode
  isDarkMode: boolean
  setTheme: (theme: ThemeMode) => void

  reset: () => void
}

const getSystemTheme = (): boolean => {
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

const computeIsDarkMode = (theme: ThemeMode): boolean => {
  if (theme === 'system') {
    return getSystemTheme()
  }
  return theme === 'dark'
}

const initialState = {
  sidebarCollapsed: true,
  filterSidebarCollapsed: false,
  activeInstanceId: null,
  modals: {
    processModification: false,
    variableEdit: false,
    confirmAction: false,
    diffModal: false,
    uploadDialog: false,
    newDiagramDialog: false,
    deleteApproverModal: false,
    evaluateDecisionModal: false,
  },
  globalLoading: false,
  theme: 'system' as ThemeMode,
  isDarkMode: getSystemTheme(),
}

export const useUIStore = create<UIState>()(
  devtools(
    persist(
      (set) => ({
        ...initialState,

        setSidebarCollapsed: (collapsed) =>
          set({ sidebarCollapsed: collapsed }, false, 'setSidebarCollapsed'),

        toggleSidebar: () =>
          set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed }), false, 'toggleSidebar'),

        toggleFilterSidebar: () =>
          set((state) => ({ filterSidebarCollapsed: !state.filterSidebarCollapsed }), false, 'toggleFilterSidebar'),

        setActiveInstanceId: (id) =>
          set({ activeInstanceId: id }, false, 'setActiveInstanceId'),

        openModal: (modal) =>
          set(
            (state) => ({
              modals: { ...state.modals, [modal]: true },
            }),
            false,
            `openModal:${modal}`
          ),

        closeModal: (modal) =>
          set(
            (state) => ({
              modals: { ...state.modals, [modal]: false },
            }),
            false,
            `closeModal:${modal}`
          ),

        setGlobalLoading: (loading) =>
          set({ globalLoading: loading }, false, 'setGlobalLoading'),

        setTheme: (theme) =>
          set({ theme, isDarkMode: computeIsDarkMode(theme) }, false, 'setTheme'),

        reset: () => set(initialState, false, 'reset'),
      }),
      {
        name: 'orchest-ui-store',
        version: 1,
        migrate: (persistedState, version) => {
          const state = (persistedState ?? {}) as {
            sidebarCollapsed?: boolean
            theme?: ThemeMode
          }
          // v1: nav starts collapsed by default (landing / first visit)
          if (version < 1) {
            return { ...state, sidebarCollapsed: true }
          }
          return state
        },
        partialize: (state) => ({
          sidebarCollapsed: state.sidebarCollapsed,
          theme: state.theme,
        }),
        onRehydrateStorage: () => (state) => {
          if (state) {
            state.isDarkMode = computeIsDarkMode(state.theme)
          }
        },
      }
    ),
    { name: 'UIStore' }
  )
)

// Selectors
export const useSidebarCollapsed = () => useUIStore((state) => state.sidebarCollapsed)
export const useFilterSidebarCollapsed = () => useUIStore((state) => state.filterSidebarCollapsed)
export const useActiveInstanceId = () => useUIStore((state) => state.activeInstanceId)
export const useModals = () => useUIStore((state) => state.modals)
export const useGlobalLoading = () => useUIStore((state) => state.globalLoading)
export const useTheme = () => useUIStore((state) => state.theme)
export const useIsDarkMode = () => useUIStore((state) => state.isDarkMode)

// Setup system theme listener and DOM class toggling
if (typeof window !== 'undefined') {
  const darkModeQuery = window.matchMedia('(prefers-color-scheme: dark)')

  // Initial DOM class setup
  const updateDOMClass = () => {
    const isDarkMode = useUIStore.getState().isDarkMode
    if (isDarkMode) {
      document.documentElement.classList.add('dark')
      document.documentElement.setAttribute('data-mode', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      document.documentElement.setAttribute('data-mode', 'light')
    }
  }

  // Update DOM class on theme change
  useUIStore.subscribe((state) => {
    if (state.isDarkMode) {
      document.documentElement.classList.add('dark')
      document.documentElement.setAttribute('data-mode', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      document.documentElement.setAttribute('data-mode', 'light')
    }
  })

  // Listen for system theme changes
  darkModeQuery.addEventListener('change', (e) => {
    const currentTheme = useUIStore.getState().theme
    if (currentTheme === 'system') {
      useUIStore.setState({ isDarkMode: e.matches })
    }
  })

  // Initial setup
  updateDOMClass()
}
