import type { FeelEvaluationDTO } from "@/api/domains";

/** Strip Zeebe's leading `=` FEEL marker when present. */
export const normalizeFeelExpression = (raw: string): string => {
  const trimmed = raw.trim();
  if (trimmed.startsWith("=")) return trimmed.slice(1).trim();
  return trimmed;
};

export const formatFeelEvaluationResult = (dto: FeelEvaluationDTO): string => {
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
