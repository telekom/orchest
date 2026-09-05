import { useCallback, useEffect, useState } from "react";

export interface UseJsonFormatterOptions {
  value: string;
  type?: string;
  indent?: number;
}

export interface UseJsonFormatterReturn {
  formatted: string;
  isValid: boolean;
  parse: (jsonString: string) => unknown | null;
  stringify: (obj: unknown, indent?: number) => string;
  updateValue: (newValue: string) => void;
}

export function useJsonFormatter({
  value,
  type = "string",
  indent = 2,
}: UseJsonFormatterOptions): UseJsonFormatterReturn {
  const [formatted, setFormatted] = useState("");
  const [isValid, setIsValid] = useState(true);

  const parse = useCallback((jsonString: string): unknown | null => {
    try {
      return JSON.parse(jsonString);
    } catch {
      return null;
    }
  }, []);

  const stringify = useCallback((obj: unknown, indentSpaces: number = indent): string => {
    try {
      return JSON.stringify(obj, null, indentSpaces);
    } catch {
      return String(obj);
    }
  }, [indent]);

  const formatValue = useCallback((val: string) => {
    if (type === "object") {
      try {
        const parsed = JSON.parse(val);
        const stringified = JSON.stringify(parsed, null, indent);
        setFormatted(stringified);
        setIsValid(true);
      } catch {
        setFormatted(val);
        setIsValid(false);
      }
    } else {
      setFormatted(val);
      setIsValid(true);
    }
  }, [type, indent]);

  const updateValue = useCallback(
    (newValue: string) => {
      formatValue(newValue);
    },
    [formatValue]
  );

  useEffect(() => {
    // Format value directly to avoid callback dependency issues
    if (type === "object") {
      try {
        const parsed = JSON.parse(value);
        const stringified = JSON.stringify(parsed, null, indent);
        setFormatted(stringified);
        setIsValid(true);
      } catch {
        setFormatted(value);
        setIsValid(false);
      }
    } else {
      setFormatted(value);
      setIsValid(true);
    }
  }, [value, type, indent]);

  return {
    formatted,
    isValid,
    parse,
    stringify,
    updateValue,
  };
}
