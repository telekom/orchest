import { bracketMatching, defaultHighlightStyle, syntaxHighlighting } from "@codemirror/language";
import { xml as xmlLang } from "@codemirror/lang-xml";
import { EditorState } from "@codemirror/state";
import { EditorView, highlightActiveLine, highlightActiveLineGutter, keymap, lineNumbers } from "@codemirror/view";
import { defaultKeymap, history, historyKeymap } from "@codemirror/commands";
import React, { useEffect, useRef } from "react";
import styles from "./CodeViewer.module.css";

interface CodeViewerProps {
  xml: string;
  onXmlChange?: (xml: string) => void;
  readOnly?: boolean;
}

/**
 * CodeViewer - Displays BPMN/DMN XML in a CodeMirror editor
 * Uses @codemirror/lang-xml for XML syntax highlighting
 */
export const CodeViewer: React.FC<CodeViewerProps> = ({
  xml,
  onXmlChange,
  readOnly = false,
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const editorViewRef = useRef<EditorView | null>(null);
  const isInternalChangeRef = useRef(false);
  const onChangeRef = useRef(onXmlChange);

  // Keep callback ref fresh
  useEffect(() => {
    onChangeRef.current = onXmlChange;
  }, [onXmlChange]);

  // Initialize editor once
  useEffect(() => {
    if (!containerRef.current) return;

    const updateListener = EditorView.updateListener.of((update) => {
      if (update.docChanged && !isInternalChangeRef.current && onChangeRef.current) {
        const newXml = update.state.doc.toString();
        onChangeRef.current(newXml);
      }
    });

    const extensions = [
      lineNumbers(),
      highlightActiveLine(),
      highlightActiveLineGutter(),
      history(),
      bracketMatching(),
      syntaxHighlighting(defaultHighlightStyle),
      xmlLang(),
      keymap.of([...defaultKeymap, ...historyKeymap]),
      updateListener,
      EditorView.lineWrapping,
      EditorState.readOnly.of(readOnly),
    ];

    // Note: We keep the editor in light mode even in dark mode
    // to match the BPMN/DMN container styling

    const startState = EditorState.create({
      doc: xml,
      extensions,
    });

    const view = new EditorView({
      state: startState,
      parent: containerRef.current,
    });

    editorViewRef.current = view;

    return () => {
      view.destroy();
      editorViewRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps -- Initialization effect; xml updates handled by separate effect below
  }, [readOnly]);

  // Update content when xml changes externally
  useEffect(() => {
    const view = editorViewRef.current;
    if (!view || isInternalChangeRef.current) return;

    const currentDoc = view.state.doc.toString();
    if (currentDoc !== xml) {
      isInternalChangeRef.current = true;
      view.dispatch({
        changes: {
          from: 0,
          to: currentDoc.length,
          insert: xml,
        },
      });
      setTimeout(() => {
        isInternalChangeRef.current = false;
      }, 0);
    }
  }, [xml]);

  return (
    <div className={styles.container}>
      <div ref={containerRef} className={styles.editor} />
    </div>
  );
};
