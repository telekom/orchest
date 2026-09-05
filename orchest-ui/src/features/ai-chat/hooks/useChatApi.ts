import { chatService, type ChatType } from '@/api/domains/chat';
import { toast } from '@/design-system/components/ui/sonner';
import { useCallback, useRef } from 'react';
import { useChatStore } from './useChatStore';

function parseSSELines(chunk: string): string[] {
  const tokens: string[] = [];
  const lines = chunk.split('\n');

  for (const line of lines) {
    if (line.startsWith('data:')) {
      const payload = line.slice(5).trim();
      if (payload === '[DONE]') continue;
      try {
        const parsed = JSON.parse(payload);
        if (typeof parsed === 'string') {
          tokens.push(parsed);
        } else if (parsed?.content) {
          tokens.push(parsed.content);
        } else if (parsed?.delta) {
          tokens.push(parsed.delta);
        }
      } catch {
        // Raw text token (not JSON-wrapped)
        if (payload) tokens.push(payload);
      }
    }
  }

  return tokens;
}

export function useChatApi() {
  const addMessage = useChatStore((s) => s.addMessage);
  const updateMessage = useChatStore((s) => s.updateMessage);
  const setStatus = useChatStore((s) => s.setStatus);
  const abortRef = useRef<AbortController | null>(null);

  const sendMessage = useCallback(async (conversationId: string, userMessage: string, chatType?: ChatType) => {
    addMessage(conversationId, { role: 'user', content: userMessage, chatType });
    setStatus(conversationId, 'streaming');

    const assistantMsgId = addMessage(conversationId, { role: 'assistant', content: '' });
    const controller = new AbortController();
    abortRef.current = controller;
    let buffer = '';

    try {
      const stream = await chatService.analyseStream(
        { userMessage, chatType },
        controller.signal,
      );

      const reader = stream.getReader();
      const decoder = new TextDecoder();
      let flushScheduled = false;

      // ponytail: throttle DOM updates to ~60fps to keep main thread free for navigation
      const flushBuffer = () => {
        flushScheduled = false;
        updateMessage(conversationId, assistantMsgId, buffer);
      };

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value, { stream: true });
        const tokens = parseSSELines(chunk);

        for (const token of tokens) {
          buffer += token;
        }

        if (!flushScheduled) {
          flushScheduled = true;
          requestAnimationFrame(flushBuffer);
        }
      }

      // Final flush to ensure all content is rendered
      updateMessage(conversationId, assistantMsgId, buffer);
      setStatus(conversationId, 'idle');
    } catch (error) {
      if (controller.signal.aborted) {
        if (buffer) updateMessage(conversationId, assistantMsgId, buffer);
        setStatus(conversationId, 'idle');
        return;
      }
      const errorMsg = error instanceof Error ? error.message : 'Failed to get response';
      toast.error(errorMsg);
      updateMessage(conversationId, assistantMsgId, 'Sorry, I encountered an error. Please try again.');
      setStatus(conversationId, 'error');
    } finally {
      abortRef.current = null;
    }
  }, [addMessage, updateMessage, setStatus]);

  const abort = useCallback(() => {
    abortRef.current?.abort();
  }, []);

  return { sendMessage, abort };
}
