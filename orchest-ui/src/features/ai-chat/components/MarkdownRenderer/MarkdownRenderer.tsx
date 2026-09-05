import DOMPurify from 'dompurify';
import React, { useCallback, useEffect, useRef } from 'react';
import styles from './MarkdownRenderer.module.css';

interface MarkdownRendererProps {
  content: string;
}

function escapeHtml(text: string): string {
  return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function parseMarkdown(text: string): string {
  // Code blocks (fenced)
  let html = text.replace(/```(\w*)\n([\s\S]*?)```/g, (_m, lang, code) => {
    const escaped = escapeHtml(code.trim());
    const langLabel = lang ? `<div class="${styles.codeHeader}"><span class="${styles.codeLang}">${lang}</span></div>` : '';
    return `<div class="${styles.codeBlockWrapper}">${langLabel}<pre class="${styles.codeBlock}"><code>${escaped}</code></pre></div>`;
  });

  // Inline code
  html = html.replace(/`([^`]+)`/g, `<code class="${styles.inlineCode}">$1</code>`);

  // Tables (simple markdown tables)
  html = html.replace(/^(\|.+\|)\n(\|[-| :]+\|)\n((?:\|.+\|\n?)+)/gm, (_m, headerRow, _sep, bodyRows) => {
    const headers = headerRow.split('|').filter((c: string) => c.trim()).map((c: string) => `<th class="${styles.th}">${c.trim()}</th>`).join('');
    const rows = bodyRows.trim().split('\n').map((row: string) => {
      const cells = row.split('|').filter((c: string) => c.trim()).map((c: string) => `<td class="${styles.td}">${c.trim()}</td>`).join('');
      return `<tr>${cells}</tr>`;
    }).join('');
    return `<div class="${styles.tableWrapper}"><table class="${styles.table}"><thead><tr>${headers}</tr></thead><tbody>${rows}</tbody></table></div>`;
  });

  // Blockquotes
  html = html.replace(/^> (.+)$/gm, `<blockquote class="${styles.blockquote}">$1</blockquote>`);
  // Merge consecutive blockquotes
  html = html.replace(/(<\/blockquote>\s*<blockquote class="[^"]*">)/g, '<br />');

  // Headings
  html = html.replace(/^### (.+)$/gm, `<h3 class="${styles.h3}">$1</h3>`);
  html = html.replace(/^## (.+)$/gm, `<h2 class="${styles.h2}">$1</h2>`);
  html = html.replace(/^# (.+)$/gm, `<h1 class="${styles.h1}">$1</h1>`);

  // Bold & italic & strikethrough & highlight
  html = html.replace(/\*\*(.+?)\*\*/g, `<strong class="${styles.bold}">$1</strong>`);
  html = html.replace(/\*(.+?)\*/g, `<em>$1</em>`);
  html = html.replace(/~~(.+?)~~/g, `<del class="${styles.strikethrough}">$1</del>`);
  html = html.replace(/==(.+?)==/g, `<mark class="${styles.highlight}">$1</mark>`);

  // Unordered lists
  html = html.replace(/^[-*] (.+)$/gm, `<li class="${styles.listItem}">$1</li>`);
  // Ordered lists
  html = html.replace(/^\d+\. (.+)$/gm, `<li class="${styles.orderedItem}">$1</li>`);

  // Wrap consecutive <li> in <ul>/<ol>
  html = html.replace(/((<li class="[^"]*">.*?<\/li>\s*)+)/g, (match) => {
    const isOrdered = match.includes(styles.orderedItem);
    const tag = isOrdered ? 'ol' : 'ul';
    return `<${tag} class="${styles.list}">${match}</${tag}>`;
  });

  // Horizontal rule
  html = html.replace(/^---$/gm, `<hr class="${styles.hr}" />`);

  // Links
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, `<a href="$2" target="_blank" rel="noopener" class="${styles.link}">$1</a>`);

  // Paragraphs
  html = html
    .split(/\n{2,}/)
    .map((block) => {
      const trimmed = block.trim();
      if (!trimmed) return '';
      if (/^<(div|pre|h[1-6]|ul|ol|hr|blockquote|table)/.test(trimmed)) return trimmed;
      return `<p class="${styles.paragraph}">${trimmed.replace(/\n/g, '<br />')}</p>`;
    })
    .join('');

  return DOMPurify.sanitize(html);
}

export const MarkdownRenderer: React.FC<MarkdownRendererProps> = React.memo(({ content }) => {
  if (!content) return null;

  const html = parseMarkdown(content);

  return (
    <div className={styles.markdown}>
      <div dangerouslySetInnerHTML={{ __html: html }} />
      <CodeCopyButtons />
    </div>
  );
});

MarkdownRenderer.displayName = 'MarkdownRenderer';

const CodeCopyButtons: React.FC = () => {
  const containerRef = useRef<HTMLSpanElement>(null);

  const handleCopy = useCallback((codeEl: Element) => {
    navigator.clipboard.writeText(codeEl.textContent || '');
    const btn = codeEl.closest(`.${styles.codeBlockWrapper}`)?.querySelector(`.${styles.copyBtn}`);
    if (btn) {
      btn.classList.add(styles.copied);
      setTimeout(() => btn.classList.remove(styles.copied), 2000);
    }
  }, []);

  useEffect(() => {
    const parent = containerRef.current?.closest(`.${styles.markdown}`);
    if (!parent) return;

    const wrappers = parent.querySelectorAll(`.${styles.codeBlockWrapper}`);
    wrappers.forEach((wrapper) => {
      if (wrapper.querySelector(`.${styles.copyBtn}`)) return;
      const btn = document.createElement('button');
      btn.className = styles.copyBtn;
      btn.title = 'Copy code';
      btn.innerHTML = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>`;
      const codeEl = wrapper.querySelector('code');
      if (codeEl) btn.onclick = () => handleCopy(codeEl);
      wrapper.appendChild(btn);
    });
  });

  return <span ref={containerRef} style={{ display: 'none' }} />;
};
