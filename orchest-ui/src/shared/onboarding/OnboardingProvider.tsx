import { createContext, useCallback, useContext, useEffect, useRef, useState, useSyncExternalStore, type ReactNode } from 'react'
import { tours } from './tours'
import { TourOverlay } from './TourOverlay'
import type { TourConfig } from './types'

// Patch pushState/replaceState to emit events so useSyncExternalStore picks up SPA navigations
const PATHNAME_EVENT = 'orchest:pathname'

if (typeof window !== 'undefined') {
  const origPush = history.pushState.bind(history)
  const origReplace = history.replaceState.bind(history)
  history.pushState = (...args) => { origPush(...args); window.dispatchEvent(new Event(PATHNAME_EVENT)) }
  history.replaceState = (...args) => { origReplace(...args); window.dispatchEvent(new Event(PATHNAME_EVENT)) }
}

function subscribeToPathname(cb: () => void) {
  window.addEventListener('popstate', cb)
  window.addEventListener(PATHNAME_EVENT, cb)
  return () => {
    window.removeEventListener('popstate', cb)
    window.removeEventListener(PATHNAME_EVENT, cb)
  }
}

function getPathname() {
  return window.location.pathname
}

const TOUR_PREFIX = 'orchest_tour_done_'

function isTourCompleted(id: string): boolean {
  return localStorage.getItem(`${TOUR_PREFIX}${id}`) === '1'
}

function markTourCompleted(id: string): void {
  localStorage.setItem(`${TOUR_PREFIX}${id}`, '1')
}

interface OnboardingContextValue {
  startTour: (id: string) => void
  activeTourId: string | null
}

const OnboardingContext = createContext<OnboardingContextValue>({
  startTour: () => {},
  activeTourId: null,
})

export function useOnboarding() {
  return useContext(OnboardingContext)
}

export function OnboardingProvider({ children }: { children: ReactNode }) {
  const [activeTour, setActiveTour] = useState<TourConfig | null>(null)
  const [stepIndex, setStepIndex] = useState(0)
  const pathname = useSyncExternalStore(subscribeToPathname, getPathname)
  const autoStartedRef = useRef<Set<string>>(new Set())

  // Auto-start tour on route match — only once per tour per session
  useEffect(() => {
    if (activeTour) return

    const match = tours.find(
      (t) => {
        const routeMatch = t.routePattern ? t.routePattern.test(pathname) : t.route ? pathname.startsWith(t.route) : false
        return routeMatch && !isTourCompleted(t.id) && !autoStartedRef.current.has(t.id)
      }
    )
    if (match) {
      autoStartedRef.current.add(match.id)
      const timer = setTimeout(() => {
        // Double-check localStorage in case it was marked while waiting
        if (!isTourCompleted(match.id)) {
          setActiveTour(match)
          setStepIndex(0)
        }
      }, 800)
      return () => clearTimeout(timer)
    }
  }, [pathname, activeTour])

  const startTour = useCallback((id: string) => {
    const tour = tours.find((t) => t.id === id)
    if (tour) {
      setActiveTour(tour)
      setStepIndex(0)
    }
  }, [])

  const next = useCallback(() => {
    if (!activeTour) return
    if (stepIndex < activeTour.steps.length - 1) {
      setStepIndex((i) => i + 1)
    } else {
      markTourCompleted(activeTour.id)
      setActiveTour(null)
    }
  }, [activeTour, stepIndex])

  const prev = useCallback(() => {
    setStepIndex((i) => Math.max(0, i - 1))
  }, [])

  const skip = useCallback(() => {
    if (activeTour) {
      markTourCompleted(activeTour.id)
      setActiveTour(null)
    }
  }, [activeTour])

  return (
    <OnboardingContext.Provider value={{ startTour, activeTourId: activeTour?.id ?? null }}>
      {children}
      {activeTour && (
        <TourOverlay
          tour={activeTour}
          stepIndex={stepIndex}
          onNext={next}
          onPrev={prev}
          onSkip={skip}
        />
      )}
    </OnboardingContext.Provider>
  )
}
