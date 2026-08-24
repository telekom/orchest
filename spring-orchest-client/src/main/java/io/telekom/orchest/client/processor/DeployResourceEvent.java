package io.telekom.orchest.client.processor;

import org.springframework.context.ApplicationEvent;

/**
 * Application event published when resources should be scanned and deployed. Triggered during
 * application startup by {@link
 * io.telekom.orchest.client.configuration.ClientInitiatorConfiguration}.
 */
public class DeployResourceEvent extends ApplicationEvent {
  /**
   * Create a new DeployResourceEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public DeployResourceEvent(Object source) {
    super(source);
  }
}
