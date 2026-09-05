export const DIFF_STYLES = `
.diff-added .djs-visual > :nth-child(1) {
  stroke: #52c41a !important;
  stroke-width: 3px !important;
  fill: #f6ffed !important;
}
.dark .diff-added .djs-visual > :nth-child(1) {
  fill: rgba(34, 197, 94, 0.15) !important;
}

.diff-removed .djs-visual > :nth-child(1) {
  stroke: #ff4d4f !important;
  stroke-width: 3px !important;
  fill: #fff1f0 !important;
}
.dark .diff-removed .djs-visual > :nth-child(1) {
  fill: rgba(239, 68, 68, 0.15) !important;
}

.diff-changed .djs-visual > :nth-child(1) {
  stroke: #fa8c16 !important;
  stroke-width: 3px !important;
  fill: #fff7e6 !important;
}
.dark .diff-changed .djs-visual > :nth-child(1) {
  fill: rgba(249, 115, 22, 0.15) !important;
}

.diff-layout-changed .djs-visual > :nth-child(1) {
  stroke: #1890ff !important;
  stroke-width: 2px !important;
  stroke-dasharray: 5, 5 !important;
}
.dark .diff-layout-changed .djs-visual > :nth-child(1) {
  stroke: #60a5fa !important;
}

.diff-added .djs-outline, .diff-removed .djs-outline, .diff-changed .djs-outline {
  stroke-width: 2px !important;
  fill: none !important;
}

.diff-added .djs-outline { stroke: #52c41a !important; }
.diff-removed .djs-outline { stroke: #ff4d4f !important; }
.diff-changed .djs-outline { stroke: #fa8c16 !important; }
.diff-layout-changed .djs-outline { stroke: #1890ff !important; stroke-dasharray: 5, 5 !important; fill: none !important; }
`;
