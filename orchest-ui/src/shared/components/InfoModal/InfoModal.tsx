import React from "react";
import { StandardModal, StandardModalProps } from "@/shared/components/StandardModal/StandardModal";
import { ModalBanner, ModalBannerType } from "@/shared/components/ModalBanner/ModalBanner";
import styles from "./InfoModal.module.css";

export interface InfoModalProps extends Omit<StandardModalProps, 'children' | 'showFooter' | 'onConfirm'> {
  bannerType?: ModalBannerType;
  bannerTitle?: string;
  bannerMessage?: React.ReactNode;
  children: React.ReactNode;
}

export const InfoModal: React.FC<InfoModalProps> = ({
  isOpen,
  onClose,
  title,
  description,
  size = "md",
  bannerType = "info",
  bannerTitle,
  bannerMessage,
  children,
  cancelText = "Close",
  contentClassName,
  contentWrapperClassName,
  maxHeight,
}) => {
  return (
    <StandardModal
      isOpen={isOpen}
      onClose={onClose}
      title={title}
      description={description}
      size={size}
      showFooter={true}
      cancelText={cancelText}
      contentClassName={contentClassName}
      contentWrapperClassName={contentWrapperClassName}
      maxHeight={maxHeight}
    >
      <div className={styles.container}>
        {bannerMessage && (
          <ModalBanner
            type={bannerType}
            title={bannerTitle}
            message={bannerMessage}
          />
        )}
        {children}
      </div>
    </StandardModal>
  );
};
