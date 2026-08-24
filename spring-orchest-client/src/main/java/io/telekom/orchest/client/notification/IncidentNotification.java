package io.telekom.orchest.client.notification;

/**
 * Immutable data carrier for incident notification details sent to alerting channels.
 *
 * @param processInstanceId the affected process instance
 * @param correlationId the correlation identifier for tracing
 * @param environment the runtime environment (e.g., DEV, PROD)
 * @param message the incident description or stack trace
 */
public record IncidentNotification(
    String processInstanceId, String correlationId, String environment, String message) {}
