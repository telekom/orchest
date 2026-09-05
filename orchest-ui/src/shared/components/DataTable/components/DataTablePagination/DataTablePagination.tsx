import { FilterDropdown } from "@/shared/components/FilterDropdown/FilterDropdown";
import { Button } from "@/design-system/components/ui/button";
import clsx from "clsx";
import styles from "./DataTablePagination.module.css";
import type { PaginationConfig } from "./types";

interface DataTablePaginationProps {
  pagination: PaginationConfig;
  loading: boolean;
}

export function DataTablePagination({ pagination, loading }: DataTablePaginationProps) {
  const startIndex = pagination.currentPage * pagination.pageSize + 1;
  const endIndex = Math.min((pagination.currentPage + 1) * pagination.pageSize, pagination.totalElements);
  const pageSizeOptions = pagination.pageSizeOptions || [10, 25, 50, 100];

  return (
    <div className={styles.container}>
      <div className={styles.grid}>
        <div className={styles.info}>
          <span>
            {startIndex}-{endIndex} of {pagination.totalElements}
          </span>
        </div>

        <div className={styles.controls}>
          <Button
            variant="ghost"
            size="sm"
            buttonType="iconOnly"
            buttonIcon="arrow-left-type-standard"
            onClick={() => pagination.onPageChange?.('prev')}
            disabled={loading || pagination.currentPage === 0}
            className={clsx(styles.navButton)}
            aria-label="Previous page"
          />

          <span className={styles.pageInfo}>
            {pagination.currentPage + 1} / {pagination.totalPages}
          </span>

          <Button
            variant="ghost"
            size="sm"
            buttonType="iconOnly"
            buttonIcon="arrow-right-type-standard"
            onClick={() => pagination.onPageChange?.('next')}
            disabled={loading || pagination.currentPage + 1 >= pagination.totalPages}
            className={clsx(styles.navButton)}
            aria-label="Next page"
          />
        </div>

        <div className={styles.pageSizeContainer}>
          <span className={styles.pageSizeLabel}>
            Per page:
          </span>
          <FilterDropdown
            label=""
            value={pagination.pageSize.toString()}
            onValueChange={(val) => pagination.onPageSizeChange?.(parseInt(val, 10))}
            disabled={loading}
            options={pageSizeOptions.map(size => ({
              value: size.toString(),
              label: size.toString()
            }))}
            className={styles.pageSizeDropdown}
            triggerClassName={styles.pageSizeTrigger}
            maxWidth="40px"
            placeholder=""
          />
        </div>
      </div>
    </div>
  );
}
