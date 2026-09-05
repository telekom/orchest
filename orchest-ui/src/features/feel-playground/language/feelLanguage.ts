import {
  HighlightStyle,
  StreamLanguage,
  StreamParser,
  syntaxHighlighting,
} from "@codemirror/language";
import { tags as t } from "@lezer/highlight";

const KEYWORDS = new Set([
  "if",
  "then",
  "else",
  "for",
  "in",
  "return",
  "some",
  "every",
  "satisfies",
  "and",
  "or",
  "not",
  "between",
  "instance",
  "of",
  "function",
  "external",
  "null",
  "true",
  "false",
]);

const BUILT_INS = new Set([
  "date",
  "time",
  "duration",
  "string",
  "number",
  "boolean",
  "list",
  "context",
  "decimal",
  "floor",
  "ceiling",
  "abs",
  "min",
  "max",
  "sum",
  "mean",
  "count",
  "contains",
  "matches",
  "replace",
  "substring",
]);

interface FeelState {
  inBlockComment: boolean;
}

const feelParser: StreamParser<FeelState> = {
  startState: () => ({ inBlockComment: false }),

  token(stream, state) {
    if (state.inBlockComment) {
      while (!stream.eol()) {
        if (stream.match("*/")) {
          state.inBlockComment = false;
          return "comment";
        }
        stream.next();
      }
      return "comment";
    }

    if (stream.eatSpace()) return null;

    if (stream.match("//")) {
      stream.skipToEnd();
      return "comment";
    }

    if (stream.match("/*")) {
      state.inBlockComment = true;
      return "comment";
    }

    const ch = stream.peek();

    if (ch === '"') {
      stream.next();
      let escaped = false;
      while (!stream.eol()) {
        const c = stream.next();
        if (c === '"' && !escaped) return "string";
        escaped = c === "\\" && !escaped;
      }
      return "string";
    }

    if (/\d/.test(ch ?? "")) {
      stream.eatWhile(/[\d.]/);
      return "number";
    }

    if (stream.match(/^(<=|>=|!=|==|=|<|>|\+|-|\*|\/|\.\.)/)) {
      return "operator";
    }

    if (/[(){}[\],;:?]/.test(ch ?? "")) {
      stream.next();
      return "punctuation";
    }

    if (/[A-Za-z_]/.test(ch ?? "")) {
      stream.eatWhile(/[A-Za-z0-9_]/);
      const word = stream.current();
      if (KEYWORDS.has(word)) return "keyword";
      if (word === "true" || word === "false") return "atom";
      if (word === "null") return "atom";
      if (BUILT_INS.has(word)) return "builtin";
      return "variableName";
    }

    stream.next();
    return null;
  },

  languageData: {
    commentTokens: { line: "//", block: { open: "/*", close: "*/" } },
  },
};

export const feelLanguage = StreamLanguage.define(feelParser);

export const feelHighlightStyle = HighlightStyle.define([
  { tag: t.keyword, color: "var(--feel-color-keyword, #af00db)", fontWeight: "600" },
  { tag: t.atom, color: "var(--feel-color-atom, #0070c1)" },
  { tag: t.string, color: "var(--feel-color-string, #a31515)" },
  { tag: t.number, color: "var(--feel-color-number, #098658)" },
  { tag: t.comment, color: "var(--feel-color-comment, #6a737d)", fontStyle: "italic" },
  { tag: t.operator, color: "var(--feel-color-operator, #000000)" },
  { tag: t.variableName, color: "var(--feel-color-variable, #001080)" },
  { tag: t.standard(t.variableName), color: "var(--feel-color-builtin, #267f99)" },
]);

export const feelHighlightStyleDark = HighlightStyle.define([
  { tag: t.keyword, color: "#c586c0", fontWeight: "600" },
  { tag: t.atom, color: "#569cd6" },
  { tag: t.string, color: "#ce9178" },
  { tag: t.number, color: "#b5cea8" },
  { tag: t.comment, color: "#6a9955", fontStyle: "italic" },
  { tag: t.operator, color: "#d4d4d4" },
  { tag: t.variableName, color: "#9cdcfe" },
  { tag: t.standard(t.variableName), color: "#4ec9b0" },
]);

export const feelHighlightExtension = (isDark: boolean) =>
  syntaxHighlighting(isDark ? feelHighlightStyleDark : feelHighlightStyle);
