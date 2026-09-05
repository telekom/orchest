import { X } from 'lucide-react';
import React, { useCallback, useRef, useState } from 'react';
import styles from './EmailTagInput.module.css';

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

interface Props {
  emails: string[];
  onChange: (emails: string[]) => void;
  placeholder?: string;
  required?: boolean;
}

export const EmailTagInput: React.FC<Props> = ({ emails, onChange, placeholder = 'Add email...', required }) => {
  const [input, setInput] = useState('');
  const [error, setError] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  const addEmail = useCallback((raw: string) => {
    const email = raw.trim().toLowerCase();
    if (!email) return;
    if (!EMAIL_REGEX.test(email)) {
      setError(`"${email}" is not a valid email`);
      return;
    }
    if (emails.includes(email)) {
      setError(`"${email}" already added`);
      return;
    }
    setError('');
    onChange([...emails, email]);
    setInput('');
  }, [emails, onChange]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault();
      addEmail(input);
    }
    if (e.key === 'Backspace' && !input && emails.length > 0) {
      onChange(emails.slice(0, -1));
    }
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text');
    const parts = pasted.split(/[,;\s]+/).filter(Boolean);
    const valid: string[] = [];
    for (const part of parts) {
      const email = part.trim().toLowerCase();
      if (EMAIL_REGEX.test(email) && !emails.includes(email)) {
        valid.push(email);
      }
    }
    if (valid.length) onChange([...emails, ...valid]);
  };

  const handleBlur = () => {
    if (input.trim()) addEmail(input);
  };

  const removeEmail = (email: string) => {
    onChange(emails.filter(e => e !== email));
  };

  return (
    <div className={styles.wrapper}>
      <div className={styles.container} onClick={() => inputRef.current?.focus()}>
        {emails.map(email => (
          <span key={email} className={styles.tag}>
            {email}
            <button type="button" className={styles.removeBtn} onClick={() => removeEmail(email)}>
              <X size={12} />
            </button>
          </span>
        ))}
        <input
          ref={inputRef}
          className={styles.input}
          value={input}
          onChange={e => { setInput(e.target.value); setError(''); }}
          onKeyDown={handleKeyDown}
          onPaste={handlePaste}
          onBlur={handleBlur}
          placeholder={emails.length === 0 ? placeholder : ''}
          required={required && emails.length === 0}
        />
      </div>
      {error && <span className={styles.error}>{error}</span>}
    </div>
  );
};
