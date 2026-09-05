import { Card } from '@/design-system/components/ui/card';
import { Input } from '@/design-system/components/ui/input';
import { Button } from '@/design-system/components/ui/button';
import { Select } from '@/design-system/components/ui/select';
import clsx from 'clsx';
import { Bell } from 'lucide-react';
import { type FC, useCallback, useState } from 'react';
import styles from './NotificationTriggerCard.module.css';

interface NotificationTriggerCardProps {
  onTriggerNotification: (type: string, message: string, channel: string) => void;
}

const TYPE_OPTIONS = [
  { id: 'info', label: 'Information', value: 'info' },
  { id: 'success', label: 'Success', value: 'success' },
  { id: 'warning', label: 'Warning', value: 'warning' },
  { id: 'error', label: 'Error', value: 'error' },
] as const;

const CHANNEL_OPTIONS = [
  { id: 'toast', label: 'Toast', value: 'toast' },
  { id: 'banner', label: 'Banner', value: 'banner' },
  { id: 'alert', label: 'Alert Dialog', value: 'alert' },
] as const;

const EXAMPLE_PRESETS = [
  { type: 'success', message: 'Operation completed successfully', label: 'Success' },
  { type: 'error', message: 'Failed to save data', label: 'Error' },
  { type: 'info', message: 'Process is running', label: 'Info' },
] as const;

export const NotificationTriggerCard: FC<NotificationTriggerCardProps> = ({
  onTriggerNotification
}) => {
  const [notificationType, setNotificationType] = useState('info');
  const [notificationChannel, setNotificationChannel] = useState('toast');
  const [notificationMessage, setNotificationMessage] = useState('');

  const handleTrigger = useCallback(() => {
    if (!notificationMessage) return;
    onTriggerNotification(notificationType, notificationMessage, notificationChannel);
  }, [notificationType, notificationMessage, notificationChannel, onTriggerNotification]);

  const handleTypeChange = useCallback((value: string) => {
    
    if (value) setNotificationType(value);
  }, []);

  const handleChannelChange = useCallback((value: string) => {
    
    if (value) setNotificationChannel(value);
  }, []);

  const applyPreset = useCallback((type: string, message: string) => {
    setNotificationType(type);
    setNotificationMessage(message);
  }, []);

  return (
    <Card className={styles.card}>
      <div className={styles.cardContent}>
        <h2 className={styles.title}>
          Notification Trigger
        </h2>

        <div className={styles.sectionsContainer}>
          <div className={clsx(styles.section, styles.sectionGrid)}>
            <div>
              <p className={styles.fieldLabel}>Type</p>
              <Select
                label=""
                value={notificationType}
                items={TYPE_OPTIONS}
                onValueChange={handleTypeChange}
                size="sm"
                className={styles.odsSelect}
              />
            </div>

            <div>
              <p className={styles.fieldLabel}>Channel</p>
              <Select
                label=""
                value={notificationChannel}
                items={CHANNEL_OPTIONS}
                onValueChange={handleChannelChange}
                size="sm"
                className={styles.odsSelect}
              />
            </div>
          </div>

          <div className={clsx(styles.section, styles.sectionRow)}>
            <div className={styles.inputWrapper}>
              <p className={styles.fieldLabel}>Message</p>
              <Input
                value={notificationMessage}
                onChange={(e) => setNotificationMessage(e.target.value)}
                className={styles.input}
                placeholder="Enter notification message"
              />
            </div>
            <div className={styles.buttonWrapper}>
              <button
                type="button"
                onClick={handleTrigger}
                disabled={!notificationMessage}
                className={clsx(
                  styles.triggerButton,
                  notificationMessage && styles.triggerButtonEnabled
                )}
                aria-label="Trigger notification"
              >
                <Bell className={styles.icon} />
              </button>
            </div>
          </div>

          <div className={styles.section}>
            <p className={styles.examplesLabel}>Examples</p>
            <div className={styles.examplesContainer}>
              {EXAMPLE_PRESETS.map(({ type, message, label }) => (
                <Button
                  key={type}
                  variant="ghost"
                  size="sm"
                  className={styles.exampleButton}
                  onClick={() => applyPreset(type, message)}
                >
                  {label}
                </Button>
              ))}
            </div>
          </div>
        </div>
      </div>
    </Card>
  );
};
