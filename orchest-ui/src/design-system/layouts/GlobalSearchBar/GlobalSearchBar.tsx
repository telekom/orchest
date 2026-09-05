import { processInstanceService, decisionInstanceService } from "@/api/domains";
import { Command } from "cmdk";
import clsx from "clsx";
import { GitBranch, Loader2, Search, Workflow, X } from "lucide-react";
import React, { useCallback, useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { useNavigate } from "react-router-dom";
import styles from "./GlobalSearchBar.module.css";

type FilterType = "all" | "bpmn" | "dmn";

interface SearchResult {
  id: string;
  label: string;
  type: "process" | "decision";
  state?: string;
}

export const GlobalSearchBar: React.FC = () => {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [filter, setFilter] = useState<FilterType>("all");
  const [results, setResults] = useState<SearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const abortRef = useRef<AbortController | null>(null);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === "k") {
        e.preventDefault();
        setOpen((prev) => !prev);
      }
      if (e.key === "Escape") {
        setOpen(false);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  useEffect(() => {
    if (open) {
      setTimeout(() => inputRef.current?.focus(), 0);
    } else {
      setQuery("");
      setResults([]);
    }
  }, [open]);

  useEffect(() => {
    if (query.length < 3) {
      setResults([]);
      return;
    }

    abortRef.current?.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    const debounce = setTimeout(async () => {
      setLoading(true);
      try {
        const searchResults: SearchResult[] = [];

        if (filter !== "dmn") {
          try {
            const processResponse = await processInstanceService.getProcessInstances({
              searchText: query,
              size: 10,
              page: 0,
            });
            const instances = processResponse?.content ?? processResponse?.data ?? [];
            if (Array.isArray(instances)) {
              instances.forEach((inst: Record<string, unknown>) => {
                searchResults.push({
                  id: (inst.processInstanceId ?? inst.processId ?? inst.id) as string,
                  label: `${inst.processDefinitionId || inst.processName || "Process"} — ${inst.processInstanceId ?? inst.processId ?? inst.id}`,
                  type: "process",
                  state: (inst.state ?? inst.status) as string | undefined,
                });
              });
            }
          } catch {
            // silently ignore process search errors
          }
        }

        if (filter !== "bpmn") {
          try {
            const decisionResponse = await decisionInstanceService.scrollDecisionInstances({
              searchText: query,
              from: 0,
              to: 10,
              sort: "evaluatedAt:desc",
            });
            const instances = decisionResponse?.items ?? decisionResponse?.content ?? [];
            if (Array.isArray(instances)) {
              instances.forEach((inst: Record<string, unknown>) => {
                searchResults.push({
                  id: (inst.decisionInstanceId ?? inst.id) as string,
                  label: `${inst.decisionId || inst.decisionName || "Decision"} — ${inst.decisionInstanceId ?? inst.id}`,
                  type: "decision",
                  state: (inst.state ?? inst.status) as string | undefined,
                });
              });
            }
          } catch {
            // silently ignore decision search errors
          }
        }

        if (!controller.signal.aborted) {
          setResults(searchResults);
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }, 300);

    return () => {
      clearTimeout(debounce);
      controller.abort();
    };
  }, [query, filter]);

  const handleSelect = useCallback(
    (result: SearchResult) => {
      setOpen(false);
      if (result.type === "process") {
        navigate(`/processes/${result.id}`);
      } else {
        navigate(`/decisions/${result.id}`);
      }
    },
    [navigate]
  );

  const searchPanel = open ? (
    <div className={styles.overlay} onClick={() => setOpen(false)}>
      <div className={styles.dialog} onClick={(e) => e.stopPropagation()}>
        <Command className={styles.command} shouldFilter={false}>
          <div className={styles.inputRow}>
            <Search size={16} className={styles.searchIcon} />
            <Command.Input
              ref={inputRef}
              value={query}
              onValueChange={setQuery}
              placeholder="Search process & decision instances..."
              className={styles.input}
            />
            {loading && <Loader2 size={14} className={styles.spinner} />}
            {query && !loading && (
              <button type="button" className={styles.clearBtn} onClick={() => setQuery("")}>
                <X size={14} />
              </button>
            )}
          </div>

          <div className={styles.filterRow}>
            <button
              type="button"
              className={clsx(styles.filterBtn, filter === "all" && styles.filterActive)}
              onClick={() => setFilter("all")}
            >
              All
            </button>
            <button
              type="button"
              className={clsx(styles.filterBtn, filter === "bpmn" && styles.filterActive)}
              onClick={() => setFilter("bpmn")}
            >
              <Workflow size={12} /> BPMN
            </button>
            <button
              type="button"
              className={clsx(styles.filterBtn, filter === "dmn" && styles.filterActive)}
              onClick={() => setFilter("dmn")}
            >
              <GitBranch size={12} /> DMN
            </button>
          </div>

          <Command.List className={styles.list}>
            {query.length > 0 && query.length < 3 && (
              <Command.Empty className={styles.empty}>
                Type at least 3 characters to search...
              </Command.Empty>
            )}
            {query.length >= 3 && !loading && results.length === 0 && (
              <Command.Empty className={styles.empty}>
                No instances found.
              </Command.Empty>
            )}
            {results.map((result) => (
              <Command.Item
                key={`${result.type}-${result.id}`}
                value={result.id}
                onSelect={() => handleSelect(result)}
                className={styles.resultItem}
              >
                {result.type === "process" ? (
                  <Workflow size={14} className={styles.resultIcon} />
                ) : (
                  <GitBranch size={14} className={styles.resultIcon} />
                )}
                <span className={styles.resultLabel}>{result.label}</span>
                {result.state && (
                  <span className={styles.resultState}>{result.state}</span>
                )}
              </Command.Item>
            ))}
          </Command.List>
        </Command>
      </div>
    </div>
  ) : null;

  return (
    <>
      <button
        type="button"
        className={styles.trigger}
        onClick={() => setOpen(true)}
      >
        <Search size={14} className={styles.triggerIcon} />
        <span className={styles.triggerText}>Search instances...</span>
        <kbd className={styles.kbd}>
          <span>&#8984;</span>K
        </kbd>
      </button>

      {searchPanel && createPortal(searchPanel, document.body)}
    </>
  );
};
