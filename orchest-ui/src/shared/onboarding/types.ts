export interface TourStep {
  target: string
  title: string
  description: string
  placement?: 'top' | 'bottom' | 'left' | 'right'
  /** Selector to click when this step activates (e.g. to open a dropdown). */
  clickOnEnter?: string
}

export interface TourConfig {
  id: string
  steps: TourStep[]
  route?: string
  routePattern?: RegExp
}
