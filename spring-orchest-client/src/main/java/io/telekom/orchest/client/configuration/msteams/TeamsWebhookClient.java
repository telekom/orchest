package io.telekom.orchest.client.configuration.msteams;

import feign.Headers;
import feign.RequestLine;

/** Feign client for posting JSON payloads to an MS Teams incoming webhook. */
public interface TeamsWebhookClient {
  /**
   * Posts a message card payload to the configured Teams channel.
   *
   * @param payload the JSON message card body
   * @return the response from the webhook endpoint
   */
  @RequestLine("POST")
  @Headers("Content-Type: application/json")
  String sendMessageToChannel(String payload);
}
