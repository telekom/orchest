import React from 'react';

interface TelekomLogoMarkProps {
  size?: number;
  className?: string;
  /** `square` = magenta tile with white T (navbar). `letter` = magenta T only, no background. */
  variant?: 'square' | 'letter';
}

const TELEKOM_T_PATH =
  'M22.67,35.28h-6.67v-6.6h6.67v6.6Zm-6.67-21.78v11.22h2v-.33c0-5.28,3-8.58,8.67-8.58h.33v23.76c0,3.3-1.33,4.62-4.67,4.62h-1v2.31h17.33v-2.31h-1c-3.33,0-4.67-1.32-4.67-4.62V15.81h.33c5.67,0,8.67,3.3,8.67,8.58v.33h2V13.5H16Zm21.33,21.78h6.67v-6.6h-6.67v6.6Z';

/** Telekom T mark — square badge (navbar) or letter-only magenta T. */
export const TelekomLogoMark: React.FC<TelekomLogoMarkProps> = ({
  size = 24,
  className,
  variant = 'square',
}) => {
  if (variant === 'letter') {
    return (
      <svg
        width={size}
        height={size}
        viewBox="0 0 60 60"
        xmlns="http://www.w3.org/2000/svg"
        role="img"
        aria-label="Telekom"
        className={className}
      >
        <path d={TELEKOM_T_PATH} fill="#E20074" />
      </svg>
    );
  }

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 60 60"
      xmlns="http://www.w3.org/2000/svg"
      role="img"
      aria-label="Telekom"
      className={className}
    >
      <rect width="60" height="60" fill="#E20074" />
      <path d={TELEKOM_T_PATH} fill="#fff" />
    </svg>
  );
};
