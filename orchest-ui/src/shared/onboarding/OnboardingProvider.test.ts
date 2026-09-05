import { describe, it, expect, beforeEach } from 'vitest'

const TOUR_PREFIX = 'orchest_tour_done_'

function isTourCompleted(id: string): boolean {
  return localStorage.getItem(`${TOUR_PREFIX}${id}`) === '1'
}

function markTourCompleted(id: string): void {
  localStorage.setItem(`${TOUR_PREFIX}${id}`, '1')
}

describe('tour persistence', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('reports uncompleted for unseen tours', () => {
    expect(isTourCompleted('new-tour')).toBe(false)
  })

  it('persists completion', () => {
    markTourCompleted('my-tour')
    expect(isTourCompleted('my-tour')).toBe(true)
  })

  it('isolates tour ids', () => {
    markTourCompleted('tour-a')
    expect(isTourCompleted('tour-b')).toBe(false)
  })
})
