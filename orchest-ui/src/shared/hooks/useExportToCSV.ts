import { globalErrorHandler } from "@/shared/error/globalErrorHandler";
import { toast } from "@/design-system/components/ui/sonner";
import { useCallback, useState } from "react";

export interface ExportOptions<T> {
  data: T[];
  filename: string;
  columnMapping?: Record<keyof T | string, string>;
  valueFormatter?: (key: keyof T, value: unknown) => string;
  includeTimestamp?: boolean;
}

export interface UseExportToCSVResult {
  exportToCSV: <T extends Record<string, unknown>>(
    options: ExportOptions<T>
  ) => Promise<void>;
  isExporting: boolean;
}

export function useExportToCSV(): UseExportToCSVResult {
  const [isExporting, setIsExporting] = useState(false);

  const exportToCSV = useCallback(
    async <T extends Record<string, unknown>>({
      data,
      filename,
      columnMapping,
      valueFormatter,
      includeTimestamp = true,
    }: ExportOptions<T>) => {
      if (data.length === 0) {
        toast.warning("No data to export");
        return;
      }

      setIsExporting(true);

      try {
        const firstRow = data[0];
        const columns = columnMapping
          ? Object.keys(columnMapping)
          : Object.keys(firstRow);

        const headers = columns.map((col) =>
          columnMapping ? columnMapping[col] || col : col
        );

        const escapeCSVValue = (value: unknown): string => {
          if (value === null || value === undefined) return "";

          const stringValue = String(value);

          if (
            stringValue.includes(",") ||
            stringValue.includes('"') ||
            stringValue.includes("\n")
          ) {
            return `"${stringValue.replace(/"/g, '""')}"`;
          }

          return stringValue;
        };

        const csvRows = data.map((row) =>
          columns
            .map((col) => {
              const value = row[col as keyof T];
              const formatted = valueFormatter
                ? valueFormatter(col as keyof T, value)
                : value;
              return escapeCSVValue(formatted);
            })
            .join(",")
        );

        const csvContent = [headers.join(","), ...csvRows].join("\n");

        const csvBlob = new Blob([csvContent], {
          type: "text/csv;charset=utf-8;",
        });

        const url = URL.createObjectURL(csvBlob);
        const link = document.createElement("a");

        const timestamp = includeTimestamp
          ? `-${new Date().toISOString().slice(0, 10)}`
          : "";
        const fullFilename = `${filename}${timestamp}.csv`;

        link.setAttribute("href", url);
        link.setAttribute("download", fullFilename);

        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

        URL.revokeObjectURL(url);

        toast.success(`Exported ${data.length} rows to ${fullFilename}`);
      } catch (error) {
        globalErrorHandler.handleError(
          error instanceof Error ? error : new Error(String(error)),
          'runtime',
          { operation: 'exporting to CSV' }
        );
      } finally {
        setIsExporting(false);
      }
    },
    []
  );

  return {
    exportToCSV,
    isExporting,
  };
}
