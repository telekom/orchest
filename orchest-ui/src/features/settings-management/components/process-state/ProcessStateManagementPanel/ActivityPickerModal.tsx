import { processDefinitionService } from '@/api/domains';
import { Combobox } from '@/design-system/components/ui/combobox';
import { StandardModal } from '@/shared/components/StandardModal/StandardModal';
import { useDiagramViewer } from '@/shared/diagram/hooks/useDiagramViewer';
import { useApiQuery } from '@/shared/hooks';
import BpmnNavigatedViewer from 'bpmn-js/lib/NavigatedViewer';
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import styles from './ActivityPickerModal.module.css';

export interface ActivityPickerResult {
  activityIds: string[];
  version: number;
  enabled: boolean;
}

interface ActivityPickerModalProps {
  isOpen: boolean;
  onClose: () => void;
  processDefinitionId: string;
  version: number;
  onSubmit: (result: ActivityPickerResult) => void;
  submitting?: boolean;
}

const SELECTABLE_TYPES = new Set([
  'bpmn:Task',
  'bpmn:UserTask',
  'bpmn:ServiceTask',
  'bpmn:SendTask',
  'bpmn:ReceiveTask',
  'bpmn:ManualTask',
  'bpmn:BusinessRuleTask',
  'bpmn:ScriptTask',
  'bpmn:CallActivity',
  'bpmn:SubProcess',
]);

const HIGHLIGHT_CLASS = 'activity-picker-selected';

export const ActivityPickerModal: React.FC<ActivityPickerModalProps> = ({
  isOpen,
  onClose,
  processDefinitionId,
  version: initialVersion,
  onSubmit,
  submitting = false,
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [pickerVersion, setPickerVersion] = useState(initialVersion);
  const [pickerEnabled, setPickerEnabled] = useState(true);
  const [readyXml, setReadyXml] = useState('');

  // Fetch available versions for this process
  const { data: versionsResponse, isLoading: loadingVersions } = useApiQuery(
    ['activity-picker-versions', processDefinitionId],
    () => processDefinitionService.getProcessDefinitions({ size: 1000 }),
    { enabled: isOpen && !!processDefinitionId, staleTime: 60000 }
  );

  const versionOptions = useMemo(() => {
    const content = versionsResponse?.content ?? versionsResponse?.data ?? [];
    if (!Array.isArray(content)) return [];
    const versions = content
      .filter((d: Record<string, unknown>) => {
        const id = String(d.definitionId || d.processDefinitionId || d.id || '');
        return id === processDefinitionId;
      })
      .map((d: Record<string, unknown>) => Number(d.version))
      .filter((v: number) => v > 0);
    return [...new Set(versions)]
      .sort((a, b) => b - a)
      .map((v) => ({ label: `Version ${v}`, value: String(v) }));
  }, [versionsResponse, processDefinitionId]);

  const { data: diagramXml = '', isLoading } = useApiQuery(
    ['activity-picker-diagram', processDefinitionId, pickerVersion],
    async () => {
      const def = await processDefinitionService.getProcessDefinition(processDefinitionId, pickerVersion);
      return def?.resourceXML || '';
    },
    { enabled: isOpen && !!processDefinitionId && pickerVersion > 0, staleTime: 60000 }
  );

  // Wait for container to have dimensions before feeding XML to the viewer
  useEffect(() => {
    if (!diagramXml || !isOpen) {
      setReadyXml('');
      return;
    }

    let cancelled = false;
    const check = () => {
      if (cancelled) return;
      const el = containerRef.current;
      if (el && el.offsetWidth > 0 && el.offsetHeight > 0) {
        setReadyXml(diagramXml);
      } else {
        requestAnimationFrame(check);
      }
    };
    requestAnimationFrame(check);

    return () => { cancelled = true; };
  }, [diagramXml, isOpen]);

  const { viewer, isLoaded } = useDiagramViewer({
    containerRef,
    xml: readyXml,
    engine: 'bpmn',
    ViewerClass: BpmnNavigatedViewer as unknown as new (options?: Record<string, unknown>) => import('@/shared/diagram/types').DiagramViewer,
    useFlowSkin: true,
  });

  // Apply/remove highlights
  const applyHighlights = useCallback((ids: string[]) => {
    if (!viewer || !isLoaded) return;
    const canvas = viewer.get('canvas') as { addMarker?: (id: string, cls: string) => void; removeMarker?: (id: string, cls: string) => void };
    const elementRegistry = viewer.get('elementRegistry');
    const allElements = elementRegistry.getAll?.() ?? [];

    for (const el of allElements) {
      canvas.removeMarker?.(el.id, HIGHLIGHT_CLASS);
    }
    for (const id of ids) {
      canvas.addMarker?.(id, HIGHLIGHT_CLASS);
    }
  }, [viewer, isLoaded]);

  useEffect(() => {
    applyHighlights(selectedIds);
  }, [selectedIds, applyHighlights]);

  // Click handler on diagram elements
  useEffect(() => {
    if (!viewer || !isLoaded) return;
    const eventBus = viewer.get('eventBus');

    const handleClick = (...args: unknown[]) => {
      const event = args[0] as { element?: { id: string; type: string } };
      const element = event?.element;
      if (!element || !SELECTABLE_TYPES.has(element.type)) return;

      setSelectedIds((prev) =>
        prev.includes(element.id)
          ? prev.filter((id) => id !== element.id)
          : [...prev, element.id]
      );
    };

    eventBus.on('element.click', handleClick);
    return () => { eventBus.off('element.click', handleClick); };
  }, [viewer, isLoaded]);

  // Reset selection when version changes (new diagram loaded)
  useEffect(() => {
    setSelectedIds([]);
  }, [pickerVersion]);

  const handleRemoveChip = useCallback((id: string) => {
    setSelectedIds((prev) => prev.filter((x) => x !== id));
  }, []);

  const handleConfirm = useCallback(() => {
    onSubmit({ activityIds: selectedIds, version: pickerVersion, enabled: pickerEnabled });
  }, [selectedIds, pickerVersion, pickerEnabled, onSubmit]);

  return (
    <StandardModal
      isOpen={isOpen}
      onClose={onClose}
      title="Select Activities from Diagram"
      description="Click on activities in the BPMN diagram to select them. Selected activities will be added for state management."
      size="4xl"
      onConfirm={handleConfirm}
      confirmText={`Add ${selectedIds.length} Activit${selectedIds.length === 1 ? 'y' : 'ies'}`}
      confirmDisabled={selectedIds.length === 0}
      confirmLoading={submitting}
    >
      <div>
        {/* Version & State controls */}
        <div className={styles.controlsRow}>
          <div className={styles.controlGroup}>
            <span className={styles.controlLabel}>Version</span>
            <Combobox
              items={versionOptions}
              value={String(pickerVersion)}
              onSelect={(val) => setPickerVersion(Number(val) || 1)}
              placeholder="Select version..."
              searchPlaceholder="Search versions..."
              emptyMessage="No versions found."
              loading={loadingVersions}
              className={styles.versionSelect}
            />
          </div>
          <div className={styles.controlGroup}>
            <span className={styles.controlLabel}>Initial State</span>
            <div className={styles.stateToggleGroup}>
              <button
                type="button"
                className={`${styles.stateOption} ${pickerEnabled ? styles.stateOptionActiveSelected : ''}`}
                onClick={() => setPickerEnabled(true)}
              >
                <span className={`${styles.stateDot} ${styles.dotActive}`} />
                Active
              </button>
              <button
                type="button"
                className={`${styles.stateOption} ${!pickerEnabled ? styles.stateOptionDisabledSelected : ''}`}
                onClick={() => setPickerEnabled(false)}
              >
                <span className={`${styles.stateDot} ${styles.dotDisabled}`} />
                Disabled
              </button>
            </div>
          </div>
        </div>

        <p className={styles.hint}>Click on task/activity nodes to select or deselect them.</p>

        {isLoading ? (
          <div className={styles.loadingState}>Loading BPMN diagram...</div>
        ) : !diagramXml ? (
          <div className={styles.errorState}>No diagram available for this process version.</div>
        ) : (
          <div className={styles.diagramContainer}>
            <div ref={containerRef} className={styles.diagramCanvas} />
          </div>
        )}

        {selectedIds.length > 0 && (
          <>
            <div className={styles.selectionInfo}>
              <span><span className={styles.selectionCount}>{selectedIds.length}</span> activit{selectedIds.length === 1 ? 'y' : 'ies'} selected</span>
            </div>
            <div className={styles.selectedChips}>
              {selectedIds.map((id) => (
                <span key={id} className={styles.chip}>
                  {id}
                  <button type="button" className={styles.chipRemove} onClick={() => handleRemoveChip(id)} title="Remove">
                    &times;
                  </button>
                </span>
              ))}
            </div>
          </>
        )}

        {!pickerEnabled && selectedIds.length > 0 && (
          <p className={styles.warningText}>
            Selected activities will be added as disabled — they will be skipped during execution.
          </p>
        )}
      </div>
    </StandardModal>
  );
};
