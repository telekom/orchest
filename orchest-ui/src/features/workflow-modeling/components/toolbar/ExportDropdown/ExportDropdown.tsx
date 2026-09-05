import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/design-system/components/ui/dropdown-menu";
import { ExportFormat } from "@/shared/enums";
import { Button } from "@/design-system/components/ui/button";
import clsx from "clsx";
import React from "react";
import styles from "./ExportDropdown.module.css";

interface ExportDropdownProps {
  exportFormats: ReadonlyArray<{ readonly value: string; readonly label: string }>;
  onExport: (format: ExportFormat) => void | Promise<void>;
}

/**
 * Export dropdown menu for exporting diagrams in different formats
 * Displays available export formats (SVG, PNG, etc.)
 */
export const ExportDropdown: React.FC<ExportDropdownProps> = ({
  exportFormats,
  onExport,
}) => {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <div>
          <Button
            variant="ghost"
            size="sm"
            label="Export"
            className={clsx(styles.button)}
          />
        </div>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start">
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
  );
};
