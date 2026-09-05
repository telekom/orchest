import { Button } from '@/design-system/components/ui/button';
import clsx from 'clsx';
import { MessageSquarePlus, Pencil, Plus, Trash2 } from 'lucide-react';
import React, { useCallback, useState } from 'react';
import { useChatStore } from '../../hooks/useChatStore';
import styles from './ChatSidebar.module.css';

export const ChatSidebar: React.FC = () => {
  const conversations = useChatStore((s) => s.conversations);
  const activeId = useChatStore((s) => s.activeConversationId);
  const createConversation = useChatStore((s) => s.createConversation);
  const setActive = useChatStore((s) => s.setActiveConversation);
  const deleteConversation = useChatStore((s) => s.deleteConversation);
  const renameConversation = useChatStore((s) => s.renameConversation);

  const [editingId, setEditingId] = useState<string | null>(null);
  const [editValue, setEditValue] = useState('');

  const handleNew = useCallback(() => {
    createConversation();
  }, [createConversation]);

  const handleStartRename = useCallback((id: string, currentTitle: string) => {
    setEditingId(id);
    setEditValue(currentTitle);
  }, []);

  const handleFinishRename = useCallback(() => {
    if (editingId && editValue.trim()) {
      renameConversation(editingId, editValue.trim());
    }
    setEditingId(null);
  }, [editingId, editValue, renameConversation]);

  return (
    <div className={styles.sidebar}>
      <div className={styles.header}>
        <span className={styles.title}>Chats</span>
        <Button variant="ghost" size="icon" onClick={handleNew} title="New chat">
          <Plus size={16} />
        </Button>
      </div>

      <div className={styles.list}>
        {conversations.length === 0 && (
          <div className={styles.empty}>
            <MessageSquarePlus size={20} />
            <span>No conversations yet</span>
          </div>
        )}
        {conversations.map((conv) => (
          <div
            key={conv.id}
            className={clsx(styles.item, activeId === conv.id && styles.itemActive)}
            onClick={() => setActive(conv.id)}
          >
            {editingId === conv.id ? (
              <input
                className={styles.renameInput}
                value={editValue}
                onChange={(e) => setEditValue(e.target.value)}
                onBlur={handleFinishRename}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') handleFinishRename();
                  if (e.key === 'Escape') setEditingId(null);
                }}
                autoFocus
                onClick={(e) => e.stopPropagation()}
              />
            ) : (
              <>
                <span className={styles.itemTitle}>{conv.title}</span>
                <div className={styles.itemActions}>
                  <button
                    className={styles.itemAction}
                    onClick={(e) => { e.stopPropagation(); handleStartRename(conv.id, conv.title); }}
                    title="Rename"
                  >
                    <Pencil size={12} />
                  </button>
                  <button
                    className={styles.itemAction}
                    onClick={(e) => { e.stopPropagation(); deleteConversation(conv.id); }}
                    title="Delete"
                  >
                    <Trash2 size={12} />
                  </button>
                </div>
              </>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};
