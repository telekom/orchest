import { useTimeout } from "@/shared/hooks";
import clsx from "clsx";
import DOMPurify from "dompurify";
import React, { useState } from "react";
import styles from "./MessageCard.module.css";

interface MessageCardProps {
  role: "user" | "assistant";
  content: string;
}

const MessageCard: React.FC<MessageCardProps> = ({ role, content }) => {
  const [isVisible, setIsVisible] = useState(false);

  useTimeout(() => setIsVisible(true), 10);

  const roleDisplay = role === "user" ? "You" : "BPMN Assistant";

  const formattedContent = DOMPurify.sanitize(
    content
      .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
      .replace(/\n- /g, "<br>• ")
      .replace(/\n/g, "<br>"),
    {
      ALLOWED_TAGS: ['strong', 'br'],
      ALLOWED_ATTR: [],
    }
  );

  return (
    <div
      className={clsx(
        styles.messageContainer,
        role === "user" ? styles.messageUser : styles.messageAssistant,
        isVisible ? styles.fadeEnterTo : styles.fadeEnterFrom
      )}
    >
      <div className={styles.messageBubble}>
        <div className={styles.messageRole}>
          <b>{roleDisplay}</b>
        </div>
        <div
          className={styles.messageContent}
          dangerouslySetInnerHTML={{ __html: formattedContent }}
        />
      </div>
    </div>
  );
};

export default React.memo(MessageCard);
