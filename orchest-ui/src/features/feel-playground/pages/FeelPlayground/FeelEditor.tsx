import { defaultKeymap, history, historyKeymap } from "@codemirror/commands";
import { json as jsonLang } from "@codemirror/lang-json";
import { bracketMatching } from "@codemirror/language";
import { EditorState, Extension } from "@codemirror/state";
import {
  EditorView,
  highlightActiveLine,
  highlightActiveLineGutter,
  keymap,
  lineNumbers,
} from "@codemirror/view";
import React, { useEffect, useRef } from "react";
import {
  feelHighlightExtension,
  feelLanguage,
} from "../../language/feelLanguage";
import styles from "./FeelEditor.module.css";

export type FeelEditorLanguage = "feel" | "json";

interface FeelEditorProps {
  value: string;
  onChange: (value: string) => void;
  language: FeelEditorLanguage;
  isDark: boolean;
  readOnly?: boolean;
  placeholder?: string;
}

const FeelEditor: React.FC<FeelEditorProps> = ({
  value,
  onChange,
  language,
  isDark,
  readOnly = false,
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const viewRef = useRef<EditorView | null>(null);
  const onChangeRef = useRef(onChange);
  const isInternalRef = useRef(false);

  useEffect(() => {
    onChangeRef.current = onChange;
  }, [onChange]);

  useEffect(() => {
    if (!containerRef.current) return;

    const updateListener = EditorView.updateListener.of((update) => {
      if (update.docChanged && !isInternalRef.current) {
        onChangeRef.current(update.state.doc.toString());
      }
    });

    const langExt: Extension =
      language === "feel" ? feelLanguage : jsonLang();

    const extensions: Extension[] = [
      lineNumbers(),
      highlightActiveLine(),
      highlightActiveLineGutter(),
      history(),
      bracketMatching(),
      langExt,
      feelHighlightExtension(isDark),
      keymap.of([...defaultKeymap, ...historyKeymap]),
      EditorView.lineWrapping,
      EditorState.readOnly.of(readOnly),
      updateListener,
    ];

    const view = new EditorView({
      state: EditorState.create({ doc: value, extensions }),
      parent: containerRef.current,
    });

    viewRef.current = view;
    return () => {
      view.destroy();
      viewRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [language, isDark, readOnly]);

  useEffect(() => {
    const view = viewRef.current;
    if (!view) return;
    const current = view.state.doc.toString();
    if (current === value) return;
    isInternalRef.current = true;
    view.dispatch({
      changes: { from: 0, to: current.length, insert: value },
    });
    queueMicrotask(() => {
      isInternalRef.current = false;
    });
  }, [value]);

  return <div ref={containerRef} className={styles.editor} />;
};

export default FeelEditor;
