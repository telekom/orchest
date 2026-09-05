import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/design-system/components/ui/dropdown-menu";
import {
    Tooltip,
    TooltipArrow,
    TooltipContent,
    TooltipTrigger,
} from "@/design-system/components/ui/tooltip/tooltip";
import { ExportFormat } from "@/shared/enums";
import { Button } from "@/design-system/components/ui/button";
import clsx from "clsx";
import { Download, Scan, CirclePlus, CircleMinus } from "lucide-react";
import { ReactNode } from "react";
import styles from "./DiagramControlBar.module.css";

interface DiagramControlBarProps {
  onZoomReset: () => void;
  onZoomIn?: () => void;
  onZoomOut?: () => void;
  onExport: (format: ExportFormat) => void;
  exportFormats?: Array<{ value: string; label: string }>;
  additionalButtons?: ReactNode;
  className?: string;
  showZoomButton?: boolean;
}

export const DiagramControlBar = ({
  onZoomReset,
  onZoomIn,
  onZoomOut,
  onExport,
  exportFormats = [
    { value: "svg", label: "Export as SVG" },
    { value: "png", label: "Export as PNG" },
  ],
  additionalButtons,
  className = "",
  showZoomButton = true,
}: DiagramControlBarProps) => {
  return (
    <div className={`${styles.container} ${className}`}>
      {additionalButtons}

      {showZoomButton && onZoomOut && (
        <Tooltip>
          <TooltipTrigger asChild>
            <div>
              <Button
                variant="ghost"
                size="icon"
                onClick={onZoomOut}
                aria-label="Zoom out"
                className={clsx(styles.controlButton, styles.zoomButton)}
              >
                <CircleMinus size={15} strokeWidth={2.25} className={styles.zoomIcon} />
              </Button>
            </div>
          </TooltipTrigger>
          <TooltipContent side="top" className={styles.tooltipContent}>
            Zoom Out
            <TooltipArrow className={styles.tooltipArrow} />
          </TooltipContent>
        </Tooltip>
      )}

      {showZoomButton && onZoomIn && (
        <Tooltip>
          <TooltipTrigger asChild>
            <div>
              <Button
                variant="ghost"
                size="icon"
                onClick={onZoomIn}
                aria-label="Zoom in"
                className={clsx(styles.controlButton, styles.zoomButton)}
              >
                <CirclePlus size={15} strokeWidth={2.25} className={styles.zoomIcon} />
              </Button>
            </div>
          </TooltipTrigger>
          <TooltipContent side="top" className={styles.tooltipContent}>
            Zoom In
            <TooltipArrow className={styles.tooltipArrow} />
          </TooltipContent>
        </Tooltip>
      )}

      {showZoomButton && (
        <Tooltip>
          <TooltipTrigger asChild>
            <div>
              <Button
                variant="ghost"
                size="icon"
                onClick={onZoomReset}
                aria-label="Reset zoom"
                className={clsx(styles.controlButton, styles.zoomButton)}
              >
                <Scan size={15} strokeWidth={2.25} className={styles.zoomIcon} />
              </Button>
            </div>
          </TooltipTrigger>
          <TooltipContent side="top" className={styles.tooltipContent}>
            Fit to View
            <TooltipArrow className={styles.tooltipArrow} />
          </TooltipContent>
        </Tooltip>
      )}

      <Tooltip>
        <TooltipTrigger asChild>
          <div className={styles.iconWrapper}>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <div>
                  <Button
                    variant="ghost"
                    size="icon"
                    className={clsx(styles.controlButton)}
                    aria-label="Export"
                  >
                    <Download size={14} />
                  </Button>
                </div>
              </DropdownMenuTrigger>
              <DropdownMenuContent className={styles.dropdownContent}>
                {exportFormats.map((format) => (
                  <DropdownMenuItem
                    key={format.value}
                    onClick={() => onExport(format.value as ExportFormat)}
                  >
                    {format.label}
                  </DropdownMenuItem>
                ))}
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </TooltipTrigger>
        <TooltipContent side="top" className={styles.tooltipContent}>
          Export
          <TooltipArrow className={styles.tooltipArrow} />
        </TooltipContent>
      </Tooltip>
    </div>
  );
};

export default DiagramControlBar;
