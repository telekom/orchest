import { Accordion } from '@/design-system/components/ui/accordion';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { SpinnerLoader } from '@/shared/components/Loader/Loader';
import { useDeputyManagement } from '@/shared/hooks/useDeputyManagement';
import { useUIStore } from '@/shared/stores/uiStore';
import { Users } from 'lucide-react';
import React, { useCallback, useState } from 'react';
import { DeleteApproverModal } from '../DeleteApproverModal/DeleteApproverModal';
import { ProcessDeputyItem } from '../deputy-management';
import styles from './DeputyManagementView.module.css';

interface ApproverInputState {
  [definitionId: string]: string;
}

interface DeleteModalData {
  definitionId: string;
  deputyEmail: string;
  processName: string;
}

const EMPTY_DELETE_MODAL_DATA: DeleteModalData = {
  definitionId: '',
  deputyEmail: '',
  processName: '',
};

export const DeputyManagementView: React.FC = () => {
  const {
    ownedProcesses,
    isLoading,
    addDeputy,
    removeDeputy,
    isAddingDeputy,
    isRemovingDeputy,
  } = useDeputyManagement();

  const [deputyInputs, setDeputyInputs] = useState<ApproverInputState>({});
  const [deleteModalData, setDeleteModalData] = useState<DeleteModalData>(EMPTY_DELETE_MODAL_DATA);

  const isDeleteModalOpen = useUIStore((state) => state.modals.deleteApproverModal);
  const openDeleteModal = useUIStore((state) => state.openModal);
  const closeDeleteModal = useUIStore((state) => state.closeModal);

  const handleAddDeputy = useCallback(async (definitionId: string) => {
    const email = deputyInputs[definitionId]?.trim();
    if (!email) return;

    // Email validation is handled by DeputyFormInput component
    await addDeputy(definitionId, email);
    setDeputyInputs((prev) => ({ ...prev, [definitionId]: '' }));
  }, [deputyInputs, addDeputy]);

  const handleRemoveDeputy = useCallback((
    definitionId: string,
    deputyEmail: string,
    processName: string
  ) => {
    setDeleteModalData({ definitionId, deputyEmail, processName });
    openDeleteModal('deleteApproverModal');
  }, [openDeleteModal]);

  const handleConfirmDelete = useCallback(() => {
    const { definitionId, deputyEmail } = deleteModalData;
    if (definitionId && deputyEmail) {
      removeDeputy(definitionId, deputyEmail);
    }
  }, [deleteModalData, removeDeputy]);

  const handleCloseDeleteModal = useCallback(() => {
    closeDeleteModal('deleteApproverModal');
    setDeleteModalData(EMPTY_DELETE_MODAL_DATA);
  }, [closeDeleteModal]);

  const handleInputChange = useCallback((processId: string, value: string) => {
    setDeputyInputs((prev) => ({ ...prev, [processId]: value }));
  }, []);

  if (isLoading) {
    return (
      <div className={styles.loadingContainer}>
        <SpinnerLoader size="lg" text="Loading your processes..." />
      </div>
    );
  }

  if (ownedProcesses.length === 0) {
    return (
      <EmptyState
        icon={<Users className={styles.emptyStateIcon} />}
        title="No Owned Processes"
        description="You don't own any processes or decisions yet. When you create or are assigned as owner of a process, it will appear here."
        className={styles.emptyStateContainer}
      />
    );
  }

  return (
    <div className={styles.container} tabIndex={-1}>
      <div className={styles.panelHeader}>
        <h2 className={styles.panelTitle}>Process ownership</h2>
        <p className={styles.panelSubtitle}>
          Assign approvers to processes you own. Expand a process to add or
          remove deputy emails.
        </p>
      </div>

      <div className={styles.listScroll}>
        <Accordion type="multiple" className={styles.accordionWrapper}>
          {ownedProcesses.map((process) => (
            <ProcessDeputyItem
              key={process.id}
              process={process}
              inputValue={deputyInputs[process.processId] || ''}
              onInputChange={(value) => handleInputChange(process.processId, value)}
              onAddDeputy={() => handleAddDeputy(process.processId)}
              onRemoveDeputy={(deputyEmail) =>
                handleRemoveDeputy(process.id, deputyEmail, process.processId)
              }
              isAddingDeputy={isAddingDeputy}
              isRemovingDeputy={isRemovingDeputy}
            />
          ))}
        </Accordion>
      </div>

      <DeleteApproverModal
        approverEmail={deleteModalData.deputyEmail}
        processName={deleteModalData.processName}
        isOpen={isDeleteModalOpen}
        onClose={handleCloseDeleteModal}
        onConfirm={handleConfirmDelete}
      />
    </div>
  );
};
