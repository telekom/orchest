package io.telekom.orchest.client.configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Provides the Kubernetes namespace by reading the mounted service account secret. Returns {@link
 * java.util.Optional#empty()} when not running inside a Kubernetes pod.
 */
@Slf4j
@Component
public class KubernetesNamespaceProvider {

  private static final Path NAMESPACE_FILE =
      Path.of("/var/run/secrets/kubernetes.io/serviceaccount/namespace");

  /**
   * Reads the Kubernetes namespace from the service account file.
   *
   * @return the namespace, or empty if unavailable
   */
  public Optional<String> getNameSpace() {
    try {
      if (Files.exists(NAMESPACE_FILE)) {
        return Optional.of(Files.readString(NAMESPACE_FILE).trim());
      }
    } catch (Exception e) {
      log.debug("Failed to read Kubernetes namespace", e);
    }
    return Optional.empty();
  }
}
