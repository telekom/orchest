import { variablesService } from "@/api/domains";
import { Input } from "@/design-system/components/ui/input";
import { Skeleton } from "@/design-system/components/ui/skeleton";
import { toast } from "@/design-system/components/ui/sonner";
import {
    Table,
    TableBody,
    TableHead,
    TableHeader,
    TableRow,
} from "@/design-system/components/ui/table";
import { EmptyState } from "@/shared/components/EmptyState/EmptyState";
import { VariableAction } from "@/shared/enums";
import { useApiMutation, useCopyToClipboard, useSearchFilter } from "@/shared/hooks";
import { Button } from "@/design-system/components/ui/button";
import clsx from "clsx";
import { Database, Plus, RefreshCw, Search } from "lucide-react";
import React, { useState } from "react";
import { ProcessVariable } from "../../types/processInstance";
import { EditVariableModal } from "../process-variables/components/EditVariableModal/EditVariableModal";
import ProcessVariableRow from "../process-variables/components/ProcessVariableRow/ProcessVariableRow";
import styles from "./ProcessVariables.module.css";

interface ProcessVariablesProps {
  variables: ProcessVariable[];
  isLoading: boolean;
  instanceId: string;
  fetchInstance: () => void;
  readOnly?: boolean;
}

const ACTION_LABELS: Record<VariableAction, string> = {
  [VariableAction.DELETE]: 'delete',
  [VariableAction.UPDATE]: 'update',
  [VariableAction.ADD]: 'add',
};

interface TableColumn {
  label: string;
  width?: string;
  align?: 'center' | 'right';
  rounded?: 'tl' | 'tr';
}

const TABLE_COLUMNS: TableColumn[] = [
  { label: 'Name', rounded: 'tl' },
  { label: 'Type', align: 'center' },
  { label: 'Scope', align: 'center' },
  { label: 'Value' },
  { label: '', width: '1px', align: 'right', rounded: 'tr' },
];


const ProcessVariables: React.FC<ProcessVariablesProps> = ({
  variables,
  isLoading,
  instanceId,
  fetchInstance,
  readOnly = false,
}) => {
  const [searchTerm, setSearchTerm] = useState("");
  const [expandedVariables, setExpandedVariables] = useState<Record<string, boolean>>({});
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);

  const { copyToClipboard: copyText, copiedItems } = useCopyToClipboard({
    successMessage: 'Variable copied to clipboard'
  });

  const toggleExpand = (name: string) => {
    setExpandedVariables((prev) => ({ ...prev, [name]: !prev[name] }));
  };

  const refreshVariables = () => {
    fetchInstance();
    toast.success("Variables refreshed");
  };

  const filteredVariables = useSearchFilter({
    data: variables,
    searchTerm,
    searchFields: ['name', 'value', 'type', 'scope'],
  });

  const { mutateAsync: variableOperation } = useApiMutation(
    async ({ action, variable }: {
      action: VariableAction;
      variable: { name: string; value: unknown; type?: string; scope?: string };
    }) => {
      if (!instanceId || instanceId.trim() === "") {
        throw new Error("Instance ID is required");
      }

      await variablesService.modifyVariables({
        action,
        processInstanceId: instanceId,
        variables: { [variable.name]: variable.value },
      });

      return { action, variable };
    },
    {
      onSuccess: ({ action, variable }) => {
        if (action === VariableAction.ADD) {
          setIsAddModalOpen(false);
        }
        const actionLabel = ACTION_LABELS[action];
        toast.success(`Variable '${variable.name}' ${actionLabel}d successfully`);
        fetchInstance();
      },
      onError: (_error, { action, variable }) => {
        const actionLabel = ACTION_LABELS[action];
        toast.error(`Failed to ${actionLabel} variable '${variable.name}'`);
      },
      showSuccessToast: false,
    }
  );

  const deleteVariable = (variable: ProcessVariable) => {
    variableOperation({ action: VariableAction.DELETE, variable });
  };

  if (isLoading) {
    return (
      <div className={styles.loadingContainer}>
        <div className={styles.loadingHeader}>
          <Skeleton className={styles.loadingSearchBar} />
          <Skeleton className={styles.loadingButton} />
        </div>
        <Skeleton className={styles.loadingTable} />
      </div>
    );
  }

  const searchAndActions = (
    <div className={styles.searchAndActions}>
      <div className={styles.searchWrapper}>
        <Search className={styles.searchIcon} />
        <Input
          placeholder="Search variables..."
          className={styles.searchInput}
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>
      <div className={styles.actions}>
        <Button
          variant="ghost"
          size="icon"
          onClick={refreshVariables}
          aria-label="Refresh variables"
          className={styles.refreshButton}
        >
          <RefreshCw size={14} />
        </Button>
        {!readOnly && (
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setIsAddModalOpen(true)}
            aria-label="Add Variable"
            className={styles.addButton}
          >
            <Plus size={14} />
          </Button>
        )}
      </div>
    </div>
  );

  if (!variables.length) {
    return (
      <div className={styles.container}>
        {searchAndActions}
        <EmptyState
          icon={<Database />}
          title="No process variables found"
        />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      {searchAndActions}

      {filteredVariables.length === 0 ? (
        <EmptyState
          icon={<Search />}
          title="No variables match your search"
        />
      ) : (
        <div className={styles.tableContainer}>
          <Table>
            <TableHeader>
              <TableRow className={styles.tableRow}>
                {TABLE_COLUMNS.map((col, index) => (
                  <TableHead
                    key={`${col.label}-${index}`}
                    className={clsx(
                      styles.tableHeader,
                      styles.tableHeaderCell,
                      col.align === 'center' && styles.alignCenter,
                      col.align === 'right' && styles.alignRight,
                      col.rounded === 'tl' && styles.roundedTl,
                      col.rounded === 'tr' && styles.roundedTr,
                      col.width === '1px' && styles.widthPx
                    )}
                  >
                    {col.label}
                  </TableHead>
                ))}
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredVariables.map((variable) => (
                <ProcessVariableRow
                  key={variable.name}
                  variable={variable}
                  isExpanded={expandedVariables[variable.name] || false}
                  onToggleExpand={toggleExpand}
                  onCopyToClipboard={copyText}
                  onDeleteVariable={deleteVariable}
                  readOnly={readOnly}
                  variableOperation={variableOperation}
                  copiedItems={copiedItems}
                />
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      <EditVariableModal
        variable={{
          name: "",
          value: "",
          type: "string",
          scope: "global",
        }}
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        variableOperation={variableOperation}
      />
    </div>
  );
};

export default React.memo(ProcessVariables);
