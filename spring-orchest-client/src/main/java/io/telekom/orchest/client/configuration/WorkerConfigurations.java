package io.telekom.orchest.client.configuration;

import static io.telekom.orchest.adapter.kafka.client.KafkaConstant.ORCHEST_SPRING_CLIENT_CONSUMER_FACTORY_BEAN_NAME;

import io.telekom.orchest.adapter.kafka.client.KafkaEncryptionClient;
import io.telekom.orchest.client.OrchestProperties;
import io.telekom.orchest.client.annotations.JobClient;
import io.telekom.orchest.client.annotations.JobMethodInfo;
import io.telekom.orchest.client.annotations.JobWorker;
import io.telekom.orchest.client.annotations.Worker;
import io.telekom.orchest.client.listener.DynamicKafkaConsumer;
import io.telekom.orchest.client.notification.IncidentNotifier;
import io.telekom.orchest.client.processor.EventProducer;
import io.telekom.orchest.client.processor.OrchestJobWorkerAnnotationProcessor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

/**
 * Configuration class for setting up OrchesT workers. Scans for {@link Worker} and {@link
 * JobWorker} annotations to register job processing methods. Configures the annotation processor
 * and dynamic Kafka consumer.
 */
@Configuration
@RequiredArgsConstructor
public class WorkerConfigurations {

  /**
   * Scans the application context for beans annotated with {@link Worker} and methods annotated
   * with {@link JobWorker}. Registers these methods as job handlers.
   *
   * @param applicationContext The Spring application context.
   * @return A map of worker types to their corresponding method information.
   */
  @Bean
  public Map<String, JobMethodInfo> jobWorkerMethods(ApplicationContext applicationContext) {
    Map<String, JobMethodInfo> jobMethods = new ConcurrentHashMap<>();
    Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Worker.class);
    for (Object bean : beans.values()) {
      for (Method method : AopUtils.getTargetClass(bean).getDeclaredMethods()) {
        JobWorker annotation = method.getAnnotation(JobWorker.class);
        if (annotation != null) {
          List<String> types = Arrays.stream(annotation.type()).toList();
          if (!types.isEmpty()) {
            types.forEach(
                workerName -> {
                  jobMethods.put(
                      workerName,
                      JobMethodInfo.builder()
                          .bean(bean)
                          .enabled(annotation.enabled())
                          .beanName(null)
                          .method(method)
                          .logWorker(annotation.logWorker())
                          .logVariables(annotation.logVariables())
                          .isCommonWorker(annotation.commonWorker())
                          .build());
                });
          } else {
            // methodName if workerType is not given
            jobMethods.put(
                method.getName(),
                JobMethodInfo.builder()
                    .bean(bean)
                    .enabled(annotation.enabled())
                    .beanName(null)
                    .method(method)
                    .logWorker(annotation.logWorker())
                    .logVariables(annotation.logVariables())
                    .isCommonWorker(annotation.commonWorker())
                    .build());
          }
        }
      }
    }
    return jobMethods;
  }

  /**
   * Creates the processor responsible for invoking registered job worker methods when events are
   * received.
   *
   * @param jobWorkerMethods The registry of job worker methods.
   * @param jobClient The client interface.
   * @param eventProducer The event producer.
   * @param incidentNotifier The incident notifier for sending alerts.
   * @param orchestProperties Properties.
   * @return The annotation processor.
   */
  @Bean
  public OrchestJobWorkerAnnotationProcessor orchestJobWorkerAnnotationProcessor(
      Map<String, JobMethodInfo> jobWorkerMethods,
      JobClient jobClient,
      EventProducer eventProducer,
      IncidentNotifier incidentNotifier,
      OrchestProperties orchestProperties) {
    return new OrchestJobWorkerAnnotationProcessor(
        jobWorkerMethods, jobClient, eventProducer, incidentNotifier, orchestProperties);
  }

  /**
   * Creates a dynamic Kafka consumer that listens to topics corresponding to registered workers.
   *
   * @param orchestSpringClientKafkaListenerContainerFactory The listener container factory.
   * @param orchestJobWorkerAnnotationProcessor The job worker processor.
   * @param kafkaEncryptionClient The encryption client.
   * @return The dynamic Kafka consumer.
   */
  @Bean
  @ConditionalOnProperty(value = "orchest.enableWorkers", havingValue = "true")
  public DynamicKafkaConsumer dynamicKafkaConsumer(
      @Qualifier(ORCHEST_SPRING_CLIENT_CONSUMER_FACTORY_BEAN_NAME)
          ConcurrentKafkaListenerContainerFactory<String, String>
              orchestSpringClientKafkaListenerContainerFactory,
      OrchestJobWorkerAnnotationProcessor orchestJobWorkerAnnotationProcessor,
      KafkaEncryptionClient kafkaEncryptionClient) {
    return new DynamicKafkaConsumer(
        orchestSpringClientKafkaListenerContainerFactory,
        orchestJobWorkerAnnotationProcessor,
        kafkaEncryptionClient);
  }
}
