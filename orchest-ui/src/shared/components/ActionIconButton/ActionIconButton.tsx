import { Button, type ButtonProps } from "@/design-system/components/ui/button";
import { AlertTriangle, Ban, CirclePlus, CircleX, Download, Eye, EyeOff, Filter, Pencil, Play, Plus, RefreshCw, Rewind, RotateCcw, Trash2, X } from "lucide-react";
import React from "react";
import { Link } from "react-router-dom";

type ColorVariant = "danger" | "success" | "warning" | "info" | "default";

interface ActionIconButtonProps {
  icon: string;
  onClick?: (e: React.MouseEvent<HTMLButtonElement>) => void | Promise<void>;
  /** When set, renders as a real link so open-in-new-tab works natively */
  to?: string;
  title: string;
  className?: string;
  variant?: ColorVariant;
  disabled?: boolean;
}

const variantToButtonVariant: Record<ColorVariant, ButtonProps["variant"]> = {
  danger: "destructive",
  success: "success",
  warning: "warning",
  info: "outline",
  default: "ghost",
};

const iconMap: Record<string, React.ReactNode> = {
  "refresh": <RefreshCw size={14} />,
  "refresh-type-standard": <RefreshCw size={14} />,
  "rotate-ccw": <RotateCcw size={14} />,
  "rewind": <Rewind size={14} />,
  "close": <X size={14} />,
  "circle-close": <CircleX size={14} />,
  "delete": <Trash2 size={14} />,
  "download": <Download size={14} />,
  "filter": <Filter size={14} />,
  "visibility-on": <Eye size={14} />,
  "visibility-off": <EyeOff size={14} />,
  "edit": <Pencil size={14} />,
  "edit-type-standard": <Pencil size={14} />,
  "play": <Play size={14} />,
  "ban": <Ban size={14} />,
  "circle-add": <CirclePlus size={14} />,
  "add": <Plus size={14} />,
  "plus": <Plus size={14} />,
  "alert-triangle": <AlertTriangle size={14} />,
};

export const ActionIconButton = React.forwardRef<HTMLButtonElement, ActionIconButtonProps>(
  ({ icon, onClick, to, title, className, variant = "default", disabled }, ref) => {
    const iconElement = iconMap[icon] || <CirclePlus size={14} />;
    const buttonVariant = variantToButtonVariant[variant];

    if (to) {
      return (
        <Button
          asChild
          variant={buttonVariant}
          size="icon"
          aria-label={title}
          title={title}
          disabled={disabled}
          className={className}
        >
          <Link
            to={to}
            onClick={(e) => e.stopPropagation()}
            aria-label={title}
            title={title}
          >
            {iconElement}
          </Link>
        </Button>
      );
    }

    return (
      <Button
        ref={ref}
        onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
          e.stopPropagation();
          onClick?.(e);
        }}
        variant={buttonVariant}
        size="icon"
        aria-label={title}
        title={title}
        disabled={disabled}
        className={className}
      >
        {iconElement}
      </Button>
    );
  }
);

ActionIconButton.displayName = "ActionIconButton";
