import { CircleHelp } from 'lucide-react'
import { useOnboarding } from './OnboardingProvider'

export function ReplayTourButton({ tourId, className }: { tourId: string; className?: string }) {
  const { startTour } = useOnboarding()
  return (
    <button
      onClick={() => startTour(tourId)}
      className={className}
      aria-label="Replay guided tour"
      title="Take a tour"
      style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'inherit', padding: 4 }}
    >
      <CircleHelp size={18} />
    </button>
  )
}
