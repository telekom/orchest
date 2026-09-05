import {
  feelPlaygroundService,
  type FeelEvaluationDTO,
  type FeelValidationDTO,
} from "@/api/domains";
import { useDebounce } from "@/shared/hooks";
import AppLayout from "@/shared/layouts/AppLayout";
import { useIsDarkMode } from "@/shared/stores/uiStore";
import clsx from "clsx";
import { AlertCircle, CheckCircle2, Loader2, Play } from "lucide-react";
import React, { useEffect, useMemo, useRef, useState } from "react";
import FeelEditor from "./FeelEditor";
import styles from "./FeelPlayground.module.css";

type ValidationState =
  | { status: "idle" }
  | { status: "validating" }
  | { status: "valid" }
  | { status: "invalid"; error: string };

type ContextState =
  | { status: "valid"; parsed: Record<string, unknown> }
  | { status: "invalid"; error: string };

const DEFAULT_EXPRESSION = `if (age >= 18 and income >= 30000 and creditScore >= 650 and hasJob = true and debtToIncomeRatio < 0.4)
then "Approved" else "Denied"`;

const DEFAULT_CONTEXT = `{
  "age": 25,
  "income": 45000,
  "creditScore": 700,
  "hasJob": true,
  "debtToIncomeRatio": 0.35
}`;

const parseContext = (raw: string): ContextState => {
  const trimmed = raw.trim();
  if (!trimmed) return { status: "valid", parsed: {} };
  try {
    const parsed = JSON.parse(trimmed);
    if (parsed === null || typeof parsed !== "object" || Array.isArray(parsed)) {
      return { status: "invalid", error: "Context must be a JSON object" };
    }
    return { status: "valid", parsed: parsed as Record<string, unknown> };
  } catch (err) {
    return {
      status: "invalid",
      error: err instanceof Error ? err.message : "Invalid JSON",
    };
  }
};

const formatResult = (dto: FeelEvaluationDTO | null): string => {
  if (!dto) return "";
  if (dto.success === false) return dto.error ?? "Evaluation failed";
  if (dto.error) return dto.error;
  if (dto.result === undefined || dto.result === null) {
    return JSON.stringify(dto.result);
  }
  if (typeof dto.result === "string") {
    return JSON.stringify(dto.result);
  }
  try {
    return JSON.stringify(dto.result, null, 2);
  } catch {
    return String(dto.result);
  }
};

const FeelPlayground: React.FC = () => {
  const isDark = useIsDarkMode();

  const [expression, setExpression] = useState<string>(DEFAULT_EXPRESSION);
  const [contextText, setContextText] = useState<string>(DEFAULT_CONTEXT);
  const [validation, setValidation] = useState<ValidationState>({ status: "idle" });
  const [evaluation, setEvaluation] = useState<FeelEvaluationDTO | null>(null);
  const [isEvaluating, setIsEvaluating] = useState(false);
  const [evaluateError, setEvaluateError] = useState<string | null>(null);

  const debouncedExpression = useDebounce(expression, 500);
  const validateSeqRef = useRef(0);

  const contextState = useMemo(() => parseContext(contextText), [contextText]);

  useEffect(() => {
    const trimmed = debouncedExpression.trim();
    if (!trimmed) {
      setValidation({ status: "idle" });
      return;
    }
    const seq = ++validateSeqRef.current;
    setValidation({ status: "validating" });
    feelPlaygroundService
      .validate(trimmed)
      .then((res: FeelValidationDTO) => {
        if (seq !== validateSeqRef.current) return;
        if (res.valid === false) {
          setValidation({
            status: "invalid",
            error: res.error ?? "Invalid FEEL expression",
          });
        } else {
          setValidation({ status: "valid" });
        }
      })
      .catch((err) => {
        if (seq !== validateSeqRef.current) return;
        setValidation({
          status: "invalid",
          error: err instanceof Error ? err.message : "Validation failed",
        });
      });
  }, [debouncedExpression]);

  const canEvaluate =
    expression.trim().length > 0 &&
    contextState.status === "valid" &&
    !isEvaluating;

  const handleEvaluate = async () => {
    if (!canEvaluate) return;
    setIsEvaluating(true);
    setEvaluateError(null);
    try {
      const variables =
        contextState.status === "valid" ? contextState.parsed : {};
      const dto = await feelPlaygroundService.evaluate(
        expression.trim(),
        variables,
      );
      setEvaluation(dto);
    } catch (err) {
      setEvaluation(null);
      setEvaluateError(
        err instanceof Error ? err.message : "Evaluation request failed",
      );
    } finally {
      setIsEvaluating(false);
    }
  };

  const expressionStatus = (
    <ExpressionStatusIcon validation={validation} />
  );

  const contextStatus =
    contextState.status === "valid" ? (
      <CheckCircle2 className={styles.statusOk} size={18} />
    ) : (
      <AlertCircle className={styles.statusBad} size={18} />
    );

  const resultStatus = evaluation
    ? evaluation.success === false || evaluation.error
      ? <AlertCircle className={styles.statusBad} size={18} />
      : <CheckCircle2 className={styles.statusOk} size={18} />
    : null;

  const resultText = evaluateError
    ? evaluateError
    : evaluation
    ? formatResult(evaluation)
    : "";

  return (
    <AppLayout>
      <div className={styles.page}>
        <header className={styles.header}>
          <div>
            <h1 className={styles.title}>FEEL Playground</h1>
            <p className={styles.subtitle}>
              Validate and evaluate FEEL expressions against a JSON context.
            </p>
          </div>
          <button
            type="button"
            className={styles.evaluateButton}
            onClick={handleEvaluate}
            disabled={!canEvaluate}
          >
            {isEvaluating ? (
              <Loader2 size={16} className={styles.spinner} aria-hidden />
            ) : (
              <Play size={16} aria-hidden />
            )}
            {isEvaluating ? "Evaluating…" : "Evaluate"}
          </button>
        </header>

        <div className={styles.layout}>
          <section className={clsx(styles.panel, styles.expressionPanel)}>
            <div className={styles.panelHeader}>
              <span className={styles.panelTitle}>FEEL expression</span>
              <span className={styles.panelStatus}>{expressionStatus}</span>
            </div>
            <div className={styles.panelBody}>
              <FeelEditor
                value={expression}
                onChange={setExpression}
                language="feel"
                isDark={isDark}
              />
            </div>
            {validation.status === "invalid" && (
              <div className={styles.panelFooter}>
                <span className={styles.errorText}>{validation.error}</span>
              </div>
            )}
          </section>

          <section className={clsx(styles.panel, styles.contextPanel)}>
            <div className={styles.panelHeader}>
              <span className={styles.panelTitle}>Context</span>
              <span className={styles.panelStatus}>{contextStatus}</span>
            </div>
            <div className={styles.panelBody}>
              <FeelEditor
                value={contextText}
                onChange={setContextText}
                language="json"
                isDark={isDark}
              />
            </div>
            {contextState.status === "invalid" && (
              <div className={styles.panelFooter}>
                <span className={styles.errorText}>{contextState.error}</span>
              </div>
            )}
          </section>

          <section className={clsx(styles.panel, styles.resultPanel)}>
            <div className={styles.panelHeader}>
              <span className={styles.panelTitle}>Result</span>
              <span className={styles.panelStatus}>{resultStatus}</span>
            </div>
            <div className={styles.panelBody}>
              <pre className={styles.resultText}>{resultText}</pre>
            </div>
          </section>
        </div>
      </div>
    </AppLayout>
  );
};

const ExpressionStatusIcon: React.FC<{ validation: ValidationState }> = ({
  validation,
}) => {
  if (validation.status === "validating") {
    return <Loader2 size={18} className={styles.spinner} aria-label="Validating" />;
  }
  if (validation.status === "valid") {
    return <CheckCircle2 className={styles.statusOk} size={18} aria-label="Valid" />;
  }
  if (validation.status === "invalid") {
    return <AlertCircle className={styles.statusBad} size={18} aria-label="Invalid" />;
  }
  return null;
};

export default React.memo(FeelPlayground);
