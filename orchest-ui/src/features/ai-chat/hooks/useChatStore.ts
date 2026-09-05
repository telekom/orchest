import { create } from 'zustand';
import { persist, type StateStorage } from 'zustand/middleware';
import { v4 as uuid } from 'uuid';
import type { ChatMessage, Conversation } from '../types/chat.types';
import { DEFAULT_CONVERSATION_TITLE } from '../constants/chat.constants';

// ponytail: debounced storage prevents localStorage writes from blocking main thread during streaming
const debouncedStorage: StateStorage = {
  getItem: (name) => localStorage.getItem(name),
  setItem: (name, value) => {
    if (debouncedStorage._timer) clearTimeout(debouncedStorage._timer);
    debouncedStorage._timer = window.setTimeout(() => {
      localStorage.setItem(name, value);
    }, 1000);
  },
  removeItem: (name) => localStorage.removeItem(name),
  _timer: null as ReturnType<typeof setTimeout> | null,
};

type ConversationStatus = 'idle' | 'streaming' | 'error';

interface ChatStore {
  conversations: Conversation[];
  activeConversationId: string | null;
  statusMap: Record<string, ConversationStatus>;
  createConversation: () => string;
  deleteConversation: (id: string) => void;
  renameConversation: (id: string, title: string) => void;
  setActiveConversation: (id: string | null) => void;
  addMessage: (conversationId: string, message: Omit<ChatMessage, 'id' | 'timestamp'>) => string;
  updateMessage: (conversationId: string, messageId: string, content: string) => void;
  setStatus: (conversationId: string, status: ConversationStatus) => void;
  getActiveConversation: () => Conversation | null;
}

export const useChatStore = create<ChatStore>()(
  persist(
    (set, get) => ({
      conversations: [],
      activeConversationId: null,
      statusMap: {},

      createConversation: () => {
        const id = uuid();
        const now = new Date().toISOString();
        const conversation: Conversation = {
          id,
          title: DEFAULT_CONVERSATION_TITLE,
          messages: [],
          createdAt: now,
          updatedAt: now,
        };
        set((state) => ({
          conversations: [conversation, ...state.conversations],
          activeConversationId: id,
        }));
        return id;
      },

      deleteConversation: (id) => {
        set((state) => {
          const filtered = state.conversations.filter((c) => c.id !== id);
          const newActive = state.activeConversationId === id
            ? (filtered[0]?.id ?? null)
            : state.activeConversationId;
          return { conversations: filtered, activeConversationId: newActive };
        });
      },

      renameConversation: (id, title) => {
        set((state) => ({
          conversations: state.conversations.map((c) =>
            c.id === id ? { ...c, title, updatedAt: new Date().toISOString() } : c
          ),
        }));
      },

      setActiveConversation: (id) => {
        set({ activeConversationId: id });
      },

      addMessage: (conversationId, message) => {
        const msgId = uuid();
        const fullMessage: ChatMessage = {
          ...message,
          id: msgId,
          timestamp: new Date().toISOString(),
        };
        set((state) => ({
          conversations: state.conversations.map((c) => {
            if (c.id !== conversationId) return c;
            const updated = {
              ...c,
              messages: [...c.messages, fullMessage],
              updatedAt: new Date().toISOString(),
            };
            if (c.messages.length === 0 && message.role === 'user') {
              updated.title = message.content.slice(0, 50) + (message.content.length > 50 ? '...' : '');
            }
            return updated;
          }),
        }));
        return msgId;
      },

      updateMessage: (conversationId, messageId, content) => {
        set((state) => ({
          conversations: state.conversations.map((c) => {
            if (c.id !== conversationId) return c;
            return {
              ...c,
              messages: c.messages.map((m) =>
                m.id === messageId ? { ...m, content } : m
              ),
              updatedAt: new Date().toISOString(),
            };
          }),
        }));
      },

      setStatus: (conversationId, status) => {
        set((state) => ({
          statusMap: { ...state.statusMap, [conversationId]: status },
        }));
      },

      getActiveConversation: () => {
        const { conversations, activeConversationId } = get();
        return conversations.find((c) => c.id === activeConversationId) ?? null;
      },
    }),
    {
      name: 'orchest-ai-chat',
      storage: debouncedStorage,
      partialize: (state) => ({
        conversations: state.conversations,
        activeConversationId: state.activeConversationId,
      }),
    }
  )
);
