import { decisionDefinitionService } from "@/api/domains";
import { EvaluateDecisionResult, MatchedRule } from "@/api/types/orchest-api";
import { toast } from "@/design-system/components/ui/sonner";
import JsonEditor from "@/features/process-management/components/process-variables/components/JsonEditor/JsonEditor";
import { StandardModal } from "@/shared/components/StandardModal/StandardModal";
import { logger } from "@/shared/utils/logger";
import React, { useCallback, useEffect, useState } from "react";
import styles from "./DmnEvaluateModal.module.css";

// ─── Types ───────────────────────────────────────────────────────────────────

interface DmnEvaluateModalProps {
  isOpen: boolean;
  onClose: () => void;
  decisionId: string;
  version: number;
}

type Phase = "input" | "result";

// ─── Helpers ─────────────────────────────────────────────────────────────────

const DEFAULT_INPUT = "{}";

const tryParseJson = (raw: string): Record<string, unknown> | null => {
  try {
    const parsed = JSON.parse(raw);
    if (typeof parsed === "object" && parsed !== null && !Array.isArray(parsed)) {
      const obj = parsed as Record<string, unknown>;
      
      // Vanilla-jsoneditor in tree mode may wrap the content under a "New item" key
      // when initializing with an empty object. Unwrap if that's the case.
      if (
        Object.keys(obj).length === 1 &&
        "New item" in obj &&
        typeof obj["New item"] === "object" &&
        obj["New item"] !== null
      ) {
        return obj["New item"] as Record<string, unknown>;
      }
      
      return obj;
    }
    return null;
  } catch {
    return null;
  }
};

const formatJson = (value: Record<string, unknown>): string =>
  JSON.stringify(value, null, 2);

// ─── Sub-components ───────────────────────────────────────────────────────────

interface MatchedRulesTableProps {
  rules: MatchedRule[];
}

const MatchedRulesTable: React.FC<MatchedRulesTableProps> = ({ rules }) => {
  if (rules.length === 0) {
    return <p className={styles.noMatchText}>No rules matched.</p>;
  }
  return (
    <div className={styles.rulesTable}>
      {rules.map((rule) => (
        <div key={rule.id} className={styles.ruleRow}>
          <div className={styles.ruleHeader}>
            <span className={styles.ruleNumber}>Rule #{rule.ruleNumber}</span>
            {rule.description && (
              <span className={styles.ruleDescription}>{rule.description}</span>
            )}
          </div>
          <div className={styles.ruleEntries}>
            <div className={styles.ruleEntryGroup}>
              <span className={styles.entryLabel}>Inputs</span>
              <div className={styles.entryTags}>
                {rule.inputEntries.map((entry, i) => (
                  <code key={i} className={styles.entryTag}>{entry}</code>
                ))}
              </div>
            </div>
            <div className={styles.ruleEntryGroup}>
              <span className={styles.entryLabel}>Outputs</span>
              <div className={styles.entryTags}>
                {rule.outputEntries.map((entry, i) => (
                  <code key={i} className={styles.entryTag}>{entry}</code>
                ))}
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

interface JsonBlockProps {
  label: string;
  value: Record<string, unknown>;
}

const JsonBlock: React.FC<JsonBlockProps> = ({ label, value }) => (
  <div className={styles.jsonSection}>
    <span className={styles.jsonLabel}>{label}</span>
    <pre className={styles.jsonPre}>{formatJson(value)}</pre>
  </div>
);

// ─── Main Component ──────────────────────────────────────────────────────────

export const DmnEvaluateModal: React.FC<DmnEvaluateModalProps> = ({
  isOpen,
  onClose,
  decisionId,
  version,
}) => {
  // when the decision definition is loaded we need to pull the real
  // evaluation id out of `resourceMetadata.decisionIds` (if available).
  const [metadataDecisionId, setMetadataDecisionId] = useState<string | null>(null);
  const [metadataLoaded, setMetadataLoaded] = useState(false);

  const [inputValue, setInputValue] = useState<string>(DEFAULT_INPUT);
  const [inputError, setInputError] = useState<string>("");
  const [isLoading, setIsLoading] = useState(false);
  const [phase, setPhase] = useState<Phase>("input");
  const [result, setResult] = useState<EvaluateDecisionResult | null>(null);

  // ── Handlers ──────────────────────────────────────────────────────────────

  const handleInputChange = useCallback((value: string) => {
    setInputValue(value);
    setInputError("");
  }, []);

  const handleEvaluate = useCallback(async () => {
    // Parse and normalize input (unwraps "New item" wrapper if present from tree mode)
    const parsed = tryParseJson(inputValue);
    if (parsed === null) {
      setInputError("Invalid JSON – please enter a valid JSON object.");
      return;
    }

    setIsLoading(true);
    setInputError("");

    try {
      const idToUse = metadataDecisionId || decisionId;
      // Wrap input variables under a top-level key for the payload structure
      const wrappedVariables = {
        splittingAttributes: parsed,
      };
      const response = await decisionDefinitionService.evaluateDecision({
        decisionId: idToUse,
        version,
        inputVariables: wrappedVariables,
      });
      setResult(response.data);
      setPhase("result");
    } catch (err) {
      logger.error("Decision evaluation failed", err);
      toast.error("Evaluation failed. Please check the input and try again.");
    } finally {
      setIsLoading(false);
    }
  }, [inputValue, decisionId, version, metadataDecisionId]);

  const handleReset = useCallback(() => {
    setPhase("input");
    setResult(null);
    setInputError("");
  }, []);

  const handleClose = useCallback(() => {
    setPhase("input");
    setResult(null);
    setInputValue(DEFAULT_INPUT);
    setInputError("");
    setMetadataDecisionId(null);
    setMetadataLoaded(false);
    onClose();
  }, [onClose]);

  // ── Footer ─────────────────────────────────────────────────────────────────

  const footer =
    phase === "input" ? undefined : (
      <div className={styles.resultFooter}>
        <button className={styles.resetBtn} onClick={handleReset} type="button">
          Evaluate Again
        </button>
        <button className={styles.closeBtn} onClick={handleClose} type="button">
          Close
        </button>
      </div>
    );

  // ── Render ─────────────────────────────────────────────────────────────────

  // load real decision id from the definition when modal opens
  useEffect(() => {
    if (!isOpen) {
      return;
    }

    // Reset input when modal opens to ensure clean state
    setInputValue(DEFAULT_INPUT);
    setInputError("");
    setMetadataLoaded(false);

    decisionDefinitionService
      .getDecisionDefinitionByVersion(decisionId, version)
      .then((def) => {
        const metaId =
          def.resourceMetadata?.decisionIds?.[0] || null;
        setMetadataDecisionId(metaId);
      })
      .catch((err) => {
        logger.error('Failed to fetch decision definition metadata', err);
        // fallback to using passed id
        setMetadataDecisionId(null);
      })
      .finally(() => {
        setMetadataLoaded(true);
      });
  }, [isOpen, decisionId, version]);

  return (
    <StandardModal
      isOpen={isOpen}
      onClose={handleClose}
      title="Evaluate Decision"
      description={
        phase === "input"
          ? `Enter input variables for "${decisionId}" (v${version}) as JSON object.` +
              (!metadataLoaded
                ? " (fetching evaluation ID...)"
                : metadataDecisionId
                ? ` The evaluation will use ID "${metadataDecisionId}".`
                : "")
          : `Evaluation result for "${decisionId}" (v${version})`
      }
      size="3xl"
      contentClassName={styles.modalContent}
      confirmText="Evaluate"
      onConfirm={phase === "input" ? handleEvaluate : undefined}
      confirmLoading={isLoading}
      confirmDisabled={isLoading || !decisionId || !metadataLoaded}
      cancelText="Cancel"
      customFooter={phase === "result" ? footer : undefined}
      maxHeight="85vh"
      contentWrapperClassName={phase === "input" ? "contentWrapperObjectEdit" : ""}
    >
      {phase === "input" && (
        <div className={styles.inputPhase}>
          <JsonEditor
            value={inputValue}
            onChange={handleInputChange}
            mode="text"
            height="100%"
          />
          {inputError && <p className={styles.inputError}>{inputError}</p>}
        </div>
      )}

      {phase === "result" && result && (
        <div className={styles.resultPhase}>
          <section className={styles.resultSection}>
            <h4 className={styles.sectionTitle}>
              Matched Rules
              <span className={styles.badge}>{result.matchedRules.length}</span>
            </h4>
            <MatchedRulesTable rules={result.matchedRules} />
          </section>

          <section className={styles.resultSection}>
            <h4 className={styles.sectionTitle}>Output Variables</h4>
            <JsonBlock label="Output" value={result.outputVariables} />
          </section>

          <section className={styles.resultSection}>
            <h4 className={styles.sectionTitle}>Input Variables</h4>
            <JsonBlock label="Input" value={result.inputVariables} />
          </section>
        </div>
      )}
    </StandardModal>
  );
};
