import type { TourConfig } from '@/shared/onboarding/types'

export const executedPathsTour: TourConfig = {
  id: 'executed-paths',
  routePattern: /^\/processes\/[^/]+$/,
  steps: [
    {
      target: '[data-tour="bpmn-diagram"]',
      title: 'Process Diagram',
      description: 'This is the live BPMN diagram showing your process execution. Executed paths are highlighted with animated edges.',
      placement: 'bottom',
    },
    {
      target: '[data-tour="executed-path-toggle"]',
      title: 'View Options',
      description: 'Click this menu to toggle display options: focus on executed paths, expand the full journey for child processes, or enable hover zoom for large diagrams.',
      placement: 'right',
      clickOnEnter: '[data-tour="executed-path-toggle"] button',
    },
    {
      target: '.bpmn-controls',
      title: 'Diagram Controls',
      description: 'Use these controls to zoom in/out, reset the view, or export the diagram as an image.',
      placement: 'top',
    },
  ],
}
