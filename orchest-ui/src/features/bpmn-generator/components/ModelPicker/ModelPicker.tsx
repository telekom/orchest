import { aiService } from "@/api/external";
import { useApiQuery } from "@/shared/hooks";
import { Select } from "@/design-system/components/ui/select";
import React, { useCallback, useEffect, useMemo, useState } from "react";
import styles from "./ModelPicker.module.css";

const Models = Object.freeze({
  GPT_4_1_MINI: "gpt-4.1-mini",
  GPT_4_1: "gpt-4.1",
  O4_MINI: "o4-mini",
  SONNET_4: "claude-sonnet-4-20250514",
  OPUS_4: "claude-opus-4-20250514",
  GEMINI_2_5_PRO: "gemini/gemini-2.5-pro-preview-03-25",
  GEMINI_2_5_FLASH: "gemini/gemini-2.5-flash-preview-04-17",
  LLAMA_4_MAVERICK: "fireworks_ai/accounts/fireworks/models/llama4-maverick-instruct-basic",
  QWEN_3_235B: "fireworks_ai/accounts/fireworks/models/qwen3-235b-a22b",
  DEEPSEEK_V3: "fireworks_ai/accounts/fireworks/models/deepseek-v3",
  DEEPSEEK_R1: "fireworks_ai/accounts/fireworks/models/deepseek-r1",
});

const Providers = Object.freeze({
  OPENAI: "openai",
  ANTHROPIC: "anthropic",
  GOOGLE: "google",
  FIREWORKS_AI: "fireworks_ai",
});

type ModelOption = {
  value: string;
  title: string;
  provider: string;
};

type Props = {
  onSelectModel: (model: string) => void;
};

const ALL_MODELS: ModelOption[] = [
  { value: Models.GPT_4_1, title: "GPT-4.1", provider: Providers.OPENAI },
  { value: Models.GPT_4_1_MINI, title: "GPT-4.1 mini", provider: Providers.OPENAI },
  { value: Models.O4_MINI, title: "o4-mini", provider: Providers.OPENAI },
  { value: Models.SONNET_4, title: "Claude Sonnet 4", provider: Providers.ANTHROPIC },
  { value: Models.OPUS_4, title: "Claude Opus 4", provider: Providers.ANTHROPIC },
  { value: Models.GEMINI_2_5_FLASH, title: "Gemini 2.5 Flash", provider: Providers.GOOGLE },
  { value: Models.GEMINI_2_5_PRO, title: "Gemini 2.5 Pro", provider: Providers.GOOGLE },
  { value: Models.LLAMA_4_MAVERICK, title: "Llama 4 Maverick", provider: Providers.FIREWORKS_AI },
  { value: Models.QWEN_3_235B, title: "Qwen 3", provider: Providers.FIREWORKS_AI },
  { value: Models.DEEPSEEK_V3, title: "Deepseek V3", provider: Providers.FIREWORKS_AI },
  { value: Models.DEEPSEEK_R1, title: "Deepseek R1", provider: Providers.FIREWORKS_AI },
];

const PROVIDER_DEFAULT_MODELS: Record<string, string> = {
  [Providers.OPENAI]: Models.GPT_4_1,
  [Providers.ANTHROPIC]: Models.SONNET_4,
  [Providers.GOOGLE]: Models.GEMINI_2_5_PRO,
  [Providers.FIREWORKS_AI]: Models.LLAMA_4_MAVERICK,
};

const getDefaultModelForProviders = (providers: string[]): string | null => {
  for (const provider of [Providers.OPENAI, Providers.ANTHROPIC, Providers.GOOGLE, Providers.FIREWORKS_AI]) {
    if (providers.includes(provider)) {
      return PROVIDER_DEFAULT_MODELS[provider];
    }
  }
  return null;
};

const ModelPicker: React.FC<Props> = ({ onSelectModel }) => {
  const [selectedModel, setSelectedModel] = useState("");

  const selectModel = useCallback((model: string) => {
    setSelectedModel(model);
    onSelectModel(model);
  }, [onSelectModel]);

  const handleModelChange = (value: string) => {
    
    if (value) selectModel(value);
  };

  // Fetch available providers and filter models
  const { data: availableModels = [] } = useApiQuery(
    ['ai-providers'],
    async () => {
      const data = await aiService.getAvailableProviders();
      const providers = Object.keys(data).filter((provider) => data[provider]);
      return ALL_MODELS.filter((model) => providers.includes(model.provider));
    },
    {
      staleTime: 60000, // Provider configuration rarely changes
      showErrorToast: true,
    }
  );

  // Auto-select default model when providers are loaded
  useEffect(() => {
    if (!selectedModel && availableModels.length > 0) {
      const providers = availableModels.map(m => m.provider);
      const defaultModel = getDefaultModelForProviders(providers);
      if (defaultModel) {
        selectModel(defaultModel);
      }
    }
  }, [availableModels, selectedModel, selectModel]);

  // Convert models to ODS format
  const odsModelOptions = useMemo(() => {
    return availableModels.map(model => ({
      id: model.value,
      value: model.value,
      label: model.title,
    }));
  }, [availableModels]);

  return (
    <div className={styles.container}>
      <Select
        label=""
        value={selectedModel || ""}
        items={odsModelOptions}
        onValueChange={handleModelChange}
        size="sm"
        className={styles.odsSelect}
      />
    </div>
  );
};

export default React.memo(ModelPicker);
