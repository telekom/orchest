import { AnimatePresence, motion } from 'framer-motion'
import { useEffect, useLayoutEffect, useRef, useState } from 'react'
import styles from './TourOverlay.module.css'
import type { TourConfig } from './types'

interface Props {
  tour: TourConfig
  stepIndex: number
  onNext: () => void
  onPrev: () => void
  onSkip: () => void
}

interface Rect {
  top: number
  left: number
  width: number
  height: number
}

const PADDING = 8

function getTargetRect(selector: string): Rect | null {
  const el = document.querySelector(selector)
  if (!el) return null
  const r = el.getBoundingClientRect()
  return {
    top: r.top - PADDING,
    left: r.left - PADDING,
    width: r.width + PADDING * 2,
    height: r.height + PADDING * 2,
  }
}

const POPOVER_WIDTH = 320
const GAP = 12
const EDGE = 12

// ponytail: auto-flip placement if preferred side has no room
function computePosition(targetRect: Rect, popoverHeight: number, preferred: string) {
  const vw = window.innerWidth
  const vh = window.innerHeight

  const spaceAbove = targetRect.top - GAP
  const spaceBelow = vh - (targetRect.top + targetRect.height + GAP)
  const spaceLeft = targetRect.left - GAP
  const spaceRight = vw - (targetRect.left + targetRect.width + GAP)

  // Pick side: try preferred, flip if not enough room
  let side = preferred
  if (side === 'bottom' && spaceBelow < popoverHeight && spaceAbove > spaceBelow) side = 'top'
  else if (side === 'top' && spaceAbove < popoverHeight && spaceBelow > spaceAbove) side = 'bottom'
  else if (side === 'left' && spaceLeft < POPOVER_WIDTH && spaceRight > spaceLeft) side = 'right'
  else if (side === 'right' && spaceRight < POPOVER_WIDTH && spaceLeft > spaceRight) side = 'left'

  let top: number
  let left: number

  switch (side) {
    case 'top':
      top = targetRect.top - GAP - popoverHeight
      left = targetRect.left + targetRect.width / 2 - POPOVER_WIDTH / 2
      break
    case 'left':
      top = targetRect.top + targetRect.height / 2 - popoverHeight / 2
      left = targetRect.left - GAP - POPOVER_WIDTH
      break
    case 'right':
      top = targetRect.top + targetRect.height / 2 - popoverHeight / 2
      left = targetRect.left + targetRect.width + GAP
      break
    default:
      top = targetRect.top + targetRect.height + GAP
      left = targetRect.left + targetRect.width / 2 - POPOVER_WIDTH / 2
  }

  // Clamp to viewport
  left = Math.max(EDGE, Math.min(left, vw - POPOVER_WIDTH - EDGE))
  top = Math.max(EDGE, Math.min(top, vh - popoverHeight - EDGE))

  return { top, left }
}

export function TourOverlay({ tour, stepIndex, onNext, onPrev, onSkip }: Props) {
  const step = tour.steps[stepIndex]
  const [rect, setRect] = useState<Rect | null>(null)
  const [popoverHeight, setPopoverHeight] = useState(160)
  const popoverRef = useRef<HTMLDivElement>(null)
  const rafRef = useRef(0)

  const updateRect = () => {
    const r = getTargetRect(step.target)
    setRect(r)
    return r !== null
  }

  // Poll for target element until it appears (handles lazy-loaded content)
  useLayoutEffect(() => {
    if (updateRect()) return
    let attempts = 0
    const interval = setInterval(() => {
      attempts++
      if (updateRect() || attempts > 20) clearInterval(interval)
    }, 200)
    return () => clearInterval(interval)
  }, [step.target])

  // Measure actual popover height after render
  useEffect(() => {
    if (popoverRef.current) {
      setPopoverHeight(popoverRef.current.offsetHeight)
    }
  })

  useEffect(() => {
    const onResize = () => updateRect()
    window.addEventListener('resize', onResize)
    window.addEventListener('scroll', onResize, true)
    return () => {
      window.removeEventListener('resize', onResize)
      window.removeEventListener('scroll', onResize, true)
      cancelAnimationFrame(rafRef.current)
    }
  }, [step.target])

  // Click a trigger element when step activates (e.g. open a dropdown)
  useEffect(() => {
    if (!step.clickOnEnter) return
    const timer = setTimeout(() => {
      const el = document.querySelector(step.clickOnEnter!) as HTMLElement | null
      el?.click()
    }, 300)
    return () => clearTimeout(timer)
  }, [step.clickOnEnter, stepIndex])

  // Scroll target into view if needed
  useEffect(() => {
    const el = document.querySelector(step.target)
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
      const timer = setTimeout(updateRect, 350)
      return () => clearTimeout(timer)
    }
  }, [step.target])

  if (!rect) return null

  const placement = step.placement ?? 'bottom'
  const pos = computePosition(rect, popoverHeight, placement)
  const isFirst = stepIndex === 0
  const isLast = stepIndex === tour.steps.length - 1
  const total = tour.steps.length

  const clipPath = `polygon(
    0% 0%, 0% 100%,
    ${rect.left}px 100%,
    ${rect.left}px ${rect.top}px,
    ${rect.left + rect.width}px ${rect.top}px,
    ${rect.left + rect.width}px ${rect.top + rect.height}px,
    ${rect.left}px ${rect.top + rect.height}px,
    ${rect.left}px 100%,
    100% 100%, 100% 0%
  )`

  return (
    <div className={styles.root}>
      <motion.div
        className={styles.backdrop}
        style={{ clipPath }}
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        transition={{ duration: 0.2 }}
      />

      <AnimatePresence mode="wait">
        <motion.div
          ref={popoverRef}
          key={stepIndex}
          className={styles.popover}
          style={{ top: pos.top, left: pos.left }}
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          exit={{ opacity: 0, scale: 0.95 }}
          transition={{ duration: 0.2 }}
        >
          <div className={styles.header}>
            <span className={styles.title}>{step.title}</span>
            <button className={styles.closeBtn} onClick={onSkip} aria-label="Skip tour">×</button>
          </div>
          <p className={styles.description}>{step.description}</p>
          <div className={styles.footer}>
            <div className={styles.dots}>
              {Array.from({ length: total }, (_, i) => (
                <span key={i} className={i === stepIndex ? styles.dotActive : styles.dot} />
              ))}
            </div>
            <div className={styles.actions}>
              <button className={styles.btnSkip} onClick={onSkip}>Skip</button>
              {!isFirst && (
                <button className={styles.btnSecondary} onClick={onPrev}>Back</button>
              )}
              <button className={styles.btnPrimary} onClick={onNext}>
                {isLast ? 'Done' : 'Next'}
              </button>
            </div>
          </div>
        </motion.div>
      </AnimatePresence>
    </div>
  )
}
