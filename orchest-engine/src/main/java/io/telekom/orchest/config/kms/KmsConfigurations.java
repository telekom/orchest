package io.telekom.orchest.config.kms;

import de.telekom.solutions.kmsclient.EnableKMS;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class enabling KMS integration for the engine module. Activating {@code @EnableKMS}
 * triggers the setup of KMS clients and related beans.
 */
@Configuration
@EnableKMS
public class KmsConfigurations {}
