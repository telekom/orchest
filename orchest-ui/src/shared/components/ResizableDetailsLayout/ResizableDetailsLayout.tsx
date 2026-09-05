
import {
    ResizableHandle,
    ResizablePanel,
    ResizablePanelGroup,
} from "@/design-system/components/ui/resizable";
import styles from './ResizableDetailsLayout.module.css';
import React, { useEffect, useRef, useState } from "react";

interface ResizableDetailsLayoutProps {
  diagramSlot: React.ReactNode;
  contentSlot: React.ReactNode;
  storageKey: string;
  defaultDiagramSize?: number;
  minDiagramSize?: number;
  maxDiagramSize?: number;
}

/**
 * Wait until the layout container has a real height before mounting
 * react-resizable-panels. Mounting at height 0 (e.g. during route transition)
 * leaves panels unable to scroll until a full remount/hard reload.
 */
const useLayoutReady = () => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;

    const markReady = () => {
      if (el.clientHeight > 0) {
        setReady(true);
        return true;
      }
      return false;
    };

    if (markReady()) return;

    const observer = new ResizeObserver(() => {
      if (markReady()) {
        observer.disconnect();
      }
    });
    observer.observe(el);

    return () => observer.disconnect();
  }, []);

  return { containerRef, ready };
};

export const ResizableDetailsLayout: React.FC<
  ResizableDetailsLayoutProps
> = ({
  diagramSlot,
  contentSlot,
  storageKey,
  defaultDiagramSize = 40,
  minDiagramSize = 20,
  maxDiagramSize = 70,
}) => {
  const { containerRef, ready } = useLayoutReady();

  return (
    <div ref={containerRef} className={styles.container}>
      {ready ? (
        <ResizablePanelGroup
          direction="vertical"
          className={styles.panelGroup}
        >
          <ResizablePanel
            defaultSize={defaultDiagramSize}
            minSize={minDiagramSize}
            maxSize={maxDiagramSize}
            className={styles.diagramPanel}
          >
            <div className={styles.diagramContent}>{diagramSlot}</div>
          </ResizablePanel>

          <ResizableHandle
            withHandle
            className={styles.resizeHandle}
          />

          <ResizablePanel
            defaultSize={100 - defaultDiagramSize}
            minSize={100 - maxDiagramSize}
            className={styles.contentPanel}
          >
            <div className={styles.contentPanelContent}>{contentSlot}</div>
          </ResizablePanel>
        </ResizablePanelGroup>
      ) : null}
    </div>
  );
};

export default ResizableDetailsLayout;
