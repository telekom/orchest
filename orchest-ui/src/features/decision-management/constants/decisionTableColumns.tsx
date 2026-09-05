import { DECISION_TRANSLATIONS } from '@/features/decision-management/constants/translations';
import { DateCell, StatusBadge, type Column } from '@/shared/components';
import { SmartText } from '@/shared/components/SmartText/SmartText';
import commonStyles from '@/shared/styles/common.module.css';
import { DecisionInstance } from '@/shared/types';
import React from 'react';

// Memoized text cell components
// eslint-disable-next-line react-refresh/only-export-components
const PrimaryTextCell: React.FC<{ text: string }> = React.memo(({ text }) => (
  <SmartText text={text} className={commonStyles.textPrimary} maxWidth="100%" showTooltip />
));
PrimaryTextCell.displayName = 'PrimaryTextCell';

// eslint-disable-next-line react-refresh/only-export-components
const SecondaryTextCell: React.FC<{ text: string }> = React.memo(({ text }) => (
  <SmartText text={text} className={commonStyles.textSecondary} maxWidth="100%" showTooltip />
));
SecondaryTextCell.displayName = 'SecondaryTextCell';

interface GetDecisionColumnsOptions {
  canView: boolean;
  handleProcessInstanceClick: (e: React.MouseEvent<HTMLAnchorElement>, id: string) => void;
}

export function getDecisionColumns({
  canView,
  handleProcessInstanceClick,
}: GetDecisionColumnsOptions): Column<DecisionInstance>[] {
  const baseColumns: Column<DecisionInstance>[] = [
    {
      key: "decisionInstanceId",
      header: "ID",
      render: (row) => <PrimaryTextCell text={row.decisionInstanceId} />,
      className: `text-table-cell ${commonStyles.textPrimary}`,
    },
    {
      key: "decisionId",
      header: "Decision ID",
      render: (row) => <PrimaryTextCell text={row.decisionId} />,
      className: `text-table-cell ${commonStyles.textPrimary}`,
    },
    {
      key: "version",
      header: "Version",
      render: (row) => <PrimaryTextCell text={String(row.version)} />,
      className: `text-table-cell ${commonStyles.textPrimary}`,
      align: "center",
    },
    {
      key: "state",
      header: "Status",
      render: (row) => <StatusBadge status={row.state || 'Unknown'} />,
      align: "center",
    },
  ];

  if (canView) {
    baseColumns.push(
      {
        key: "processInstanceId",
        header: "Process Instance ID",
        render: (row) => (
          <a
            href={`/processes/${row.processInstanceId}`}
            className={commonStyles.link}
            onClick={(e) => handleProcessInstanceClick(e, row.processInstanceId)}
            aria-label={DECISION_TRANSLATIONS.ARIA_PROCESS_INSTANCE}
          >
            <SmartText
              text={row.processInstanceId}
              maxWidth="100%"
              showTooltip
            />
          </a>
        ),
        className: `text-table-cell ${commonStyles.textSecondary}`,
      },
      {
        key: "executedAt",
        header: "Execution Date",
        render: (row) => (
          <DateCell
            date={row.executedAt}
            label="Execution Date"
            format="compact"
          />
        ),
        className: `text-table-cell ${commonStyles.textSecondary}`,
      }
    );
  }

  return baseColumns;
}
