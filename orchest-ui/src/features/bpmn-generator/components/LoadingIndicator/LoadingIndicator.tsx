import { useTimeout } from "@/shared/hooks";
import React, { useState } from "react";
import styles from "./LoadingIndicator.module.css";

const LoadingIndicator: React.FC = () => {
  const [showTimeout, setShowTimeout] = useState(false);

  useTimeout(() => setShowTimeout(true), 15000);

  return (
    <div className={styles.messageContainer}>
      <div className={styles.messageBubble}>
        <div className={styles.flexContainer}>
          <span className={styles.thinkingText}>Thinking</span>
          <span className={styles.dots}>
            <span className={styles.dot}>.</span>
            <span className={styles.dot}>.</span>
            <span className={styles.dot}>.</span>
          </span>
        </div>
        {showTimeout && (
          <div className={styles.timeoutMessage}>
            This is taking longer than usual...
          </div>
        )}
      </div>
    </div>
  );
};

export default React.memo(LoadingIndicator);
