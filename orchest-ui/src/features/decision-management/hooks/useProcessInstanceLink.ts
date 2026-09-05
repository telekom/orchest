import { processInstanceService } from '@/api/domains';
import { handleApiOperation } from '@/shared/utils/apiErrorUtils';
import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';

export const useProcessInstanceLink = () => {
  const navigate = useNavigate();

  const handleProcessInstanceClick = useCallback(
    async (e: React.MouseEvent, processInstanceId: string) => {
      e.stopPropagation();

      // Let the browser handle new-tab / modified clicks on the real href
      if (e.metaKey || e.ctrlKey || e.shiftKey || e.altKey || e.button !== 0) {
        return;
      }

      e.preventDefault();

      const result = await handleApiOperation(
        () => processInstanceService.getProcessInstance(processInstanceId),
        {
          operation: "fetch process instance",
          errorMessage: `Process instance "${processInstanceId}" not found`,
          logError: true,
        }
      );

      if (result.success) {
        navigate(`/processes/${processInstanceId}`);
      }
    },
    [navigate]
  );

  return { handleProcessInstanceClick };
};
