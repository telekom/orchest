import type { RateLimit } from "@/api/domains";

export function validateForm(data: RateLimit): string[] {
  const errors = [];

  if (!data.processId.trim()) {
    errors.push("Process name is required");
  } else if (data.processId.length < 2) {
    errors.push("Process name must be at least 2 characters");
  }

  if (data.windowDuration < 1) {
    errors.push("Window duration must be at least 1 second");
  } else if (data.windowDuration > 3600) {
    errors.push("Window duration cannot exceed 3600 seconds (1 hour)");
  }

  if (data.allowedSize < 1) {
    errors.push("Allowed size must be at least 1");
  } else if (data.allowedSize > 100) {
    errors.push("Allowed size cannot exceed 100");
  }

  return errors;
}

export function createNewRateLimit(): RateLimit {
  return {
    id: `temp-${Date.now()}`,
    windowDuration: 30,
    allowedSize: 75,
    processId: "",
    enabled: true,
    switchToNewCamunda: false,
  };
}
