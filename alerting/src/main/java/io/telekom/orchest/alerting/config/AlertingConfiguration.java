package io.telekom.orchest.alerting.config;

import io.telekom.orchest.alerting.ModuleProperties;
import java.util.Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class for the alerting system. Sets up beans for different alert channels (MS
 * Teams, Outlook/SMTP) based on configuration properties.
 */
@Configuration
@EnableAsync
@EnableScheduling
@ConditionalOnProperty(value = "alerting.enabled", havingValue = "true")
public class AlertingConfiguration {

  /**
   * Provides the configuration for MS Teams alerting.
   *
   * @param moduleProperties The module properties.
   * @return The MS Teams alerting config.
   */
  @Bean
  public ModuleProperties.AlertingConfig msTeamsAlertingConfig(ModuleProperties moduleProperties) {
    return moduleProperties.getConfigs().get(ModuleProperties.AlertType.MS_TEAMS);
  }

  /**
   * Provides the configuration for MS Outlook (SMTP) alerting.
   *
   * @param moduleProperties The module properties.
   * @return The MS Outlook alerting config.
   */
  @Primary
  @Bean
  public ModuleProperties.AlertingConfig emailAlertingConfig(ModuleProperties moduleProperties) {
    return moduleProperties.getConfigs().get(ModuleProperties.AlertType.EMAIL);
  }

  /**
   * Configures the JavaMailSender bean for sending emails.
   *
   * @param moduleProperties The module properties.
   * @return The configured JavaMailSender.
   */
  @Bean
  @Primary
  @ConditionalOnProperty(value = "alerting.enabled", havingValue = "true")
  public JavaMailSender alertingJavaMailSender(ModuleProperties moduleProperties) {
    ModuleProperties.AlertingConfig msOutlookAlertingConfig = emailAlertingConfig(moduleProperties);
    if (msOutlookAlertingConfig != null && !msOutlookAlertingConfig.isEnabled()) {
      return new JavaMailSenderImpl();
    }
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    Properties props = mailSender.getJavaMailProperties();

    String protocol = "smtp";
    boolean sslEnable = false;

    // Basic configuration
    mailSender.setHost(msOutlookAlertingConfig.getHost());
    mailSender.setPort(msOutlookAlertingConfig.getPort());
    mailSender.setProtocol(protocol);

    if (msOutlookAlertingConfig.getUserName() != null) {
      mailSender.setUsername(msOutlookAlertingConfig.getUserName());
      mailSender.setPassword(msOutlookAlertingConfig.getPassword());
    } else {
      props.put("mail.smtp.auth", false);
    }

    // Additional properties
    props.put("mail.transport.protocol", protocol);

    props.put("mail.smtp.starttls.enable", false);
    props.put("mail.smtp.ssl.enable", sslEnable);

    // Connection timeout and read timeout
    props.put("mail.smtp.connectiontimeout", "10000"); // 10 seconds
    props.put("mail.smtp.timeout", "10000"); // 10 seconds
    props.put("mail.smtp.writetimeout", "10000"); // 10 seconds

    return mailSender;
  }
}
