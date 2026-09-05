import { useIsDarkMode } from "@/shared/stores/uiStore";
import React, { useEffect, useRef } from "react";
import { Content, JSONEditor, Mode } from "vanilla-jsoneditor";

interface JSONEditorConstructor {
  new (config: { target: HTMLElement; props: Record<string, unknown> }): {
    destroy: () => void;
    set: (content: Content) => void;
    updateProps: (props: Record<string, unknown>) => void;
  };
}

type JSONEditorInstance = ReturnType<JSONEditorConstructor['prototype']['constructor']>;

interface JsonEditorProps {
  value: string;
  onChange?: (value: string) => void;
  readOnly?: boolean;
  mode?: "tree" | "text" | "table";
  className?: string;
  height?: string;
  maxHeight?: string;
}

const JsonEditor: React.FC<JsonEditorProps> = ({
  value,
  onChange,
  readOnly = false,
  mode = "tree",
  className = "",
  height = "500px",
  maxHeight,
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const editorRef = useRef<JSONEditorInstance | null>(null);
  const isInternalChangeRef = useRef(false);
  const onChangeRef = useRef(onChange);
  const isDarkMode = useIsDarkMode();

  // Keep onChange ref updated
  useEffect(() => {
    onChangeRef.current = onChange;
  }, [onChange]);

  // Initialize editor once
  useEffect(() => {
    if (!containerRef.current) return;

    let initialContent: Content;
    try {
      const parsed = JSON.parse(value);
      initialContent = { json: parsed };
    } catch {
      initialContent = { text: value };
    }

    const handleChange = (updatedContent: Content) => {
      if (!onChangeRef.current) return;

      isInternalChangeRef.current = true;

      let stringValue: string;
      try {
        if ("json" in updatedContent && updatedContent.json !== undefined) {
          stringValue = JSON.stringify(updatedContent.json, null, 2);
        } else if ("text" in updatedContent && typeof updatedContent.text === "string") {
          stringValue = updatedContent.text;
        } else {
          stringValue = "{}";
        }
        onChangeRef.current(stringValue);
      } catch {
        onChangeRef.current(value);
      }

      setTimeout(() => {
        isInternalChangeRef.current = false;
      }, 0);
    };

    editorRef.current = new (JSONEditor as unknown as JSONEditorConstructor)({
      target: containerRef.current,
      props: {
        content: initialContent,
        readOnly,
        mode: mode as Mode,
        mainMenuBar: !readOnly,
        navigationBar: !readOnly,
        statusBar: !readOnly,
        onChange: handleChange,
      },
    });

    return () => {
      if (editorRef.current) {
        editorRef.current.destroy();
        editorRef.current = null;
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps -- Initialization effect runs once; mode/readOnly/value updates handled by separate effects
  }, []);

  // Update content when value changes externally
  useEffect(() => {
    if (!editorRef.current || isInternalChangeRef.current) return;

    try {
      const parsed = JSON.parse(value);
      editorRef.current.set({ json: parsed });
    } catch {
      editorRef.current.set({ text: value });
    }
  }, [value]);

  // Update readOnly mode
  useEffect(() => {
    if (!editorRef.current) return;
    editorRef.current.updateProps({ readOnly });
  }, [readOnly]);

  // Update mode
  useEffect(() => {
    if (!editorRef.current) return;
    editorRef.current.updateProps({ mode: mode as Mode });
  }, [mode]);


  // Determine wrapper style based on height prop:
  // - Explicit height (e.g., "500px"): Apply height and optional maxHeight
  // - "auto": Apply maxHeight only (for table row expansion)
  // - "100%" or undefined: No inline styles (rely on parent flex layout)
  const wrapperStyle =
    height && height !== '100%' && height !== 'auto'
      ? { height, maxHeight }
      : height === 'auto' && maxHeight
      ? { maxHeight }
      : undefined;

  return (
    <div className={`json-editor-wrapper ${className}`} style={wrapperStyle}>
      <div
        ref={containerRef}
        className={`json-editor-container ${isDarkMode ? "jse-theme-dark" : ""}`}
      />
    </div>
  );
};

export default JsonEditor;
