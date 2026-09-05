import { aiService as api, type ChatMessage } from "@/api/external";
import { toast } from "@/design-system/components/ui/sonner";
import { Textarea } from "@/design-system/components/ui/textarea";
import { ErrorBanner } from "@/shared/components/ErrorBanner/ErrorBanner";
import { TOAST_MESSAGES, VALIDATION_LIMITS } from "@/shared/constants";
import { readStreamToString, readStreamWithCallback } from "@/shared/utils";
import { logger } from "@/shared/utils/logger";
import { Button } from "@/design-system/components/ui/button";
import clsx from "clsx";
import React, { useReducer, useRef } from "react";
import Intent from "../../constants/Intent";
import { CHAT_ACTIONS } from "../../constants/chatActions";
import type { BpmnJsonData, BpmnProcess } from "../../types/bpmn.types";
import LoadingIndicator from "../LoadingIndicator/LoadingIndicator";
import MessageCard from "../MessageCard/MessageCard";
import ModelPicker from "../ModelPicker/ModelPicker";
import styles from "./BpmnGeneratorChat.module.css";

interface Props {
  onBpmnXmlReceived: (xml: string) => void;
  onBpmnJsonReceived: (json: BpmnJsonData) => void;
  onDownload: () => void;
  isDownloadReady: boolean;
  process: BpmnProcess;
}

interface ChatState {
  isLoading: boolean;
  messages: ChatMessage[];
  currentInput: string;
  selectedModel: string;
  hasError: boolean;
}

type ChatAction =
  | { type: typeof CHAT_ACTIONS.SET_LOADING; loading: boolean }
  | { type: typeof CHAT_ACTIONS.ADD_MESSAGE; message: ChatMessage }
  | { type: typeof CHAT_ACTIONS.APPEND_TO_LAST_MESSAGE; chunk: string }
  | { type: typeof CHAT_ACTIONS.SET_MESSAGES; messages: ChatMessage[] }
  | { type: typeof CHAT_ACTIONS.SET_INPUT; input: string }
  | { type: typeof CHAT_ACTIONS.SET_MODEL; model: string }
  | { type: typeof CHAT_ACTIONS.SET_ERROR; hasError: boolean }
  | { type: typeof CHAT_ACTIONS.RESET };

const chatReducer = (state: ChatState, action: ChatAction): ChatState => {
  switch (action.type) {
    case CHAT_ACTIONS.SET_LOADING:
      return { ...state, isLoading: action.loading };
    case CHAT_ACTIONS.ADD_MESSAGE:
      return { ...state, messages: [...state.messages, action.message] };
    case CHAT_ACTIONS.APPEND_TO_LAST_MESSAGE:
      const lastMessage = state.messages[state.messages.length - 1];
      if (lastMessage?.role === 'assistant') {
        return {
          ...state,
          messages: [
            ...state.messages.slice(0, -1),
            { ...lastMessage, content: lastMessage.content + action.chunk }
          ]
        };
      }
      return {
        ...state,
        messages: [...state.messages, { role: 'assistant', content: action.chunk }]
      };
    case CHAT_ACTIONS.SET_MESSAGES:
      return { ...state, messages: action.messages };
    case CHAT_ACTIONS.SET_INPUT:
      return { ...state, currentInput: action.input };
    case CHAT_ACTIONS.SET_MODEL:
      return { ...state, selectedModel: action.model };
    case CHAT_ACTIONS.SET_ERROR:
      return { ...state, hasError: action.hasError };
    case CHAT_ACTIONS.RESET:
      return { ...initialChatState, selectedModel: state.selectedModel };
    default:
      return state;
  }
};

const initialChatState: ChatState = {
  isLoading: false,
  messages: [],
  currentInput: '',
  selectedModel: '',
  hasError: false,
};

const ChatInterface: React.FC<Props> = ({
  onBpmnXmlReceived,
  onBpmnJsonReceived,
  onDownload,
  isDownloadReady,
  process,
}) => {
  const [state, dispatch] = useReducer(chatReducer, initialChatState);
  const messageContainerRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messageContainerRef.current?.scrollTo(0, messageContainerRef.current.scrollHeight);
  };

  const reset = () => {
    dispatch({ type: CHAT_ACTIONS.RESET });
    onBpmnXmlReceived("");
    onBpmnJsonReceived({});
  };

  const determineIntent = async (updatedMessages: ChatMessage[]) => {
    try {
      const data = await api.determineIntent(updatedMessages, state.selectedModel);
      return data.intent;
    } catch (err) {
      logger.error("Intent error:", err);
      dispatch({ type: CHAT_ACTIONS.SET_ERROR, hasError: true });
      return null;
    }
  };

  const talk = async (
    proc: BpmnProcess | BpmnJsonData,
    model: string,
    isFinal: boolean,
    updatedMessages: ChatMessage[]
  ) => {
    try {
      const stream = await api.generateBpmnFromChat(updatedMessages, proc, model, isFinal);
      if (!stream) throw new Error("Failed to get stream");

      await readStreamWithCallback(stream, (chunk) => {
        dispatch({ type: CHAT_ACTIONS.APPEND_TO_LAST_MESSAGE, chunk });
        scrollToBottom();
      });
    } catch (err) {
      logger.error("Talk error:", err);
      dispatch({ type: CHAT_ACTIONS.SET_ERROR, hasError: true });
    }
  };

  const modify = async (
    proc: BpmnProcess | BpmnJsonData,
    model: string,
    updatedMessages: ChatMessage[]
  ) => {
    try {
      const stream = await api.modifyBpmn(updatedMessages, proc, model);
      if (!stream) throw new Error("Failed to get stream");

      const jsonString = await readStreamToString(stream);
      if (jsonString) {
        try {
          return JSON.parse(jsonString);
        } catch (e) {
          logger.error("Failed to parse JSON:", e);
        }
      }
      return null;
    } catch (err) {
      logger.error("Modify error:", err);
      dispatch({ type: CHAT_ACTIONS.SET_ERROR, hasError: true });
      return null;
    }
  };

  const handleMessageSubmit = async () => {
    if (!state.currentInput.trim()) return;

    if (!state.selectedModel) {
      toast.error(TOAST_MESSAGES.ERROR.MODEL_SELECTION_REQUIRED);
      return;
    }

    if (state.currentInput.length > VALIDATION_LIMITS.MAX_MESSAGE_LENGTH) {
      toast.error(TOAST_MESSAGES.ERROR.MESSAGE_TOO_LONG);
      return;
    }

    dispatch({ type: CHAT_ACTIONS.SET_ERROR, hasError: false });
    const updatedMessages: ChatMessage[] = [
      ...state.messages,
      { content: state.currentInput, role: "user" },
    ];
    dispatch({ type: CHAT_ACTIONS.SET_MESSAGES, messages: updatedMessages });
    dispatch({ type: CHAT_ACTIONS.SET_INPUT, input: '' });
    scrollToBottom();

    const intent = await determineIntent(updatedMessages);
    if (!intent) return;

    if (intent === Intent.TALK) {
      await talk(process, state.selectedModel, false, updatedMessages);
    } else if (intent === Intent.MODIFY) {
      dispatch({ type: CHAT_ACTIONS.SET_LOADING, loading: true });
      scrollToBottom();
      const result = await modify(process, state.selectedModel, updatedMessages);
      if (result) {
        onBpmnXmlReceived(result.bpmn_xml);
        onBpmnJsonReceived(result.bpmn_json);
        await talk(result.bpmn_json, state.selectedModel, true, updatedMessages);
      }
      dispatch({ type: CHAT_ACTIONS.SET_LOADING, loading: false });
    } else {
      logger.error("Unknown intent:", intent);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.shiftKey && e.key === "Enter") {
      e.preventDefault();
      const start = e.currentTarget.selectionStart;
      const newInput = state.currentInput.slice(0, start) + "\n" + state.currentInput.slice(start);
      dispatch({ type: CHAT_ACTIONS.SET_INPUT, input: newInput });
    } else if (e.key === "Enter") {
      e.preventDefault();
      handleMessageSubmit();
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div className={styles.headerTitle}>
          <span className={styles.title}>BPMN Assistant</span>
        </div>
        <div className={styles.headerActions}>
          <Button
            onClick={reset}
            disabled={state.isLoading || state.messages.length === 0}
            variant="ghost"
            size="sm"
            buttonType="iconOnly"
            buttonIcon="refresh"
            className={styles.refreshButton}
            aria-label="Reset chat"
          />
          {isDownloadReady && (
            <Button
              onClick={onDownload}
              disabled={state.isLoading}
              variant="ghost"
              size="sm"
              buttonType="iconOnly"
              buttonIcon="download"
              className={styles.odsIconButton}
              aria-label="Download BPMN"
            />
          )}
          <ModelPicker onSelectModel={(model) => dispatch({ type: CHAT_ACTIONS.SET_MODEL, model })} />
        </div>
      </div>

      <div className={styles.messageArea} ref={messageContainerRef}>
        {state.messages.length > 0 ? (
          <>
            {state.messages.map((msg, i) => (
              <MessageCard key={i} role={msg.role} content={msg.content} />
            ))}
            {state.isLoading && <LoadingIndicator />}
          </>
        ) : (
          <>
          
            <MessageCard
              role="assistant"
              content="Welcome to BPMN Assistant! I can help you understand and create BPMN processes. Let's start by discussing your BPMN needs or creating a new process from scratch. How would you like to begin?"
            />
          </>
        )}

        {state.hasError && (
          <ErrorBanner message="An error occurred while processing your request. Please try again." />
        )}
      </div>

      <div className={styles.inputArea}>
        <div className={styles.inputWrapper}>
          <Textarea
            placeholder="Message BPMN Assistant..."
            value={state.currentInput}
            onChange={(e) => dispatch({ type: CHAT_ACTIONS.SET_INPUT, input: e.target.value })}
            onKeyDown={handleKeyDown}
            disabled={state.isLoading}
            className={styles.textarea}
            rows={4}
          />
          <div className={styles.sendButtonWrapper}>
            <Button
              onClick={handleMessageSubmit}
              disabled={state.isLoading || !state.currentInput.trim()}
              variant="ghost"
              size="sm"
              buttonType="iconOnly"
              buttonIcon="message-outgoing"
              className={clsx(styles.sendButton)}
              aria-label="Send message"
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default ChatInterface;
