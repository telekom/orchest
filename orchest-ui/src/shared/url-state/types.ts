export interface UrlFieldDef<T> {
  parse: (raw: string) => T;
  serialize: (value: T) => string | undefined;
  isEmpty?: (value: T) => boolean;
}

export type UrlStateSchema<T extends Record<string, unknown>> = {
  [K in keyof T]: UrlFieldDef<T[K]>;
};

export type UrlHistoryMode = 'push' | 'replace';
