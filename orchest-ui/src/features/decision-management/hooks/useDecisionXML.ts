import { decisionInstanceService } from '@/api/domains';
import { queryKeys } from '@/shared/constants/queryKeys';
import { useApiQuery } from '@/shared/hooks';
import { useEffect, useState } from 'react';

interface UseDecisionXMLOptions {
  decisionId: string | null;
  version: string | null;
}

interface UseDecisionXMLResult {
  dmnXml: string;
  isLoading: boolean;
  error: Error | null;
  refetch: () => void;
}

export function useDecisionXML({
  decisionId,
  version,
}: UseDecisionXMLOptions): UseDecisionXMLResult {
  const [dmnXml, setDmnXml] = useState<string>('');
  const shouldFetch = !!(decisionId && version);

  const {
    data: decisionXMLData,
    isLoading,
    error,
    refetch,
  } = useApiQuery<{ resourceXML: string } | null>(
    queryKeys.decisionInstances.xml(decisionId || '', version || ''),
    async () => {
      if (!shouldFetch) return null;
      return await decisionInstanceService.getDecisionXML(
        decisionId!,
        parseInt(version!)
      );
    },
    { enabled: shouldFetch, showErrorToast: false }
  );

  // Update XML when data changes or filters change
  useEffect(() => {
    if (decisionXMLData?.resourceXML) {
      setDmnXml(decisionXMLData.resourceXML);
    } else {
      setDmnXml('');
    }
  }, [decisionXMLData, decisionId, version]);

  return {
    dmnXml,
    isLoading,
    error,
    refetch,
  };
}
