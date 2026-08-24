package io.telekom.orchest.client.configuration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.api.core.request.WorkerRegistryRequest;
import io.telekom.orchest.client.OrchestProperties;
import io.telekom.orchest.client.annotations.JobMethodInfo;
import io.telekom.orchest.client.clients.processstate.ProcessStateScheduler;
import io.telekom.orchest.client.listener.DynamicKafkaConsumer;
import io.telekom.orchest.client.model.ProcessState;
import io.telekom.orchest.client.processor.DeployResourceEvent;
import io.telekom.orchest.client.processor.EventProducer;
import jakarta.annotation.PostConstruct;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * Configuration class responsible for initializing the OrchesT client at application startup. Tasks
 * include:
 *
 * <ul>
 *   <li>Registering workers with the engine.
 *   <li>Publishing deployment events for embedded resources.
 *   <li>Starting Kafka consumers for task processing.
 * </ul>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ClientInitiatorConfiguration {

  private final ApplicationContext applicationContext;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final DynamicKafkaConsumer dynamicKafkaConsumer;
  private final OrchestProperties orchestProperties;
  private final Map<String, JobMethodInfo> jobWorkerMethods;
  private final EventProducer eventProducer;
  private final KubernetesNamespaceProvider kubernetesNamespaceProvider;

  @Autowired(required = false)
  private ProcessStateScheduler processStateScheduler;

  /**
   * Listener for {@link ApplicationReadyEvent}. Triggers the initialization logic once the Spring
   * context is fully loaded.
   *
   * @param event The application ready event.
   */
  @EventListener
  public void onApplicationStarted(ApplicationReadyEvent event) {
    if (!orchestProperties.isEnableWorkers()) {
      return;
    }
    Set<WorkerRegistryRequest.WorkerInfo> serviceTaskIds = new HashSet<>();
    jobWorkerMethods.forEach(
        (jobName, jobMethodInfo) -> {
          serviceTaskIds.add(
              new WorkerRegistryRequest.WorkerInfo(jobName, jobMethodInfo.isCommonWorker()));
        });
    Optional<String> k8Namespace = kubernetesNamespaceProvider.getNameSpace();
    eventProducer.sendWorkerRegistryUpdateEvent(
        new WorkerRegistryRequest(
            k8Namespace.orElse(applicationContext.getApplicationName()), serviceTaskIds));

    // Deploy Resource like BPMN, DMN if any
    applicationEventPublisher.publishEvent(new DeployResourceEvent(this));

    boolean haveCommonWorkers =
        jobWorkerMethods.values().stream().anyMatch(JobMethodInfo::isCommonWorker);

    Map<String, ProcessState> processStateMap = new HashMap<>();
    if (processStateScheduler != null) {
      processStateMap.putAll(processStateScheduler.fetchProcessStateMap());
    }

    // Start all consumers, respecting process state
    if (dynamicKafkaConsumer != null) {
      orchestProperties
          .getProcessIds()
          .forEach(
              processId -> {
                String topic = KafkaUtils.getClientWorkerEventTopic(processId);
                ProcessState processState = processStateMap.get(processId);
                boolean consumerIsDisabled =
                    processState != null
                        && processState.getStatus() != null
                        && Boolean.FALSE.equals(processState.getStatus());
                if (!consumerIsDisabled) {
                  dynamicKafkaConsumer.registerWorkerConsumer(
                      topic, orchestProperties.getWorkerThreadCount());
                } else {
                  log.info(
                      "Consumer for processId={} started in paused state (disabled via process state)",
                      processId);
                }
              });

      if (haveCommonWorkers) {
        dynamicKafkaConsumer.registerWorkerConsumer(
            KafkaUtils.getClientCommonWorkerEventTopic(), orchestProperties.getWorkerThreadCount());
      }
    }
    log.info("worker registry update event sent for workers: {}", serviceTaskIds);
  }

  /** Post-construction initialization. Can be used to set log levels or other internal setup. */
  @PostConstruct
  public void init() {
    //        setClassLogLevel("org.apache.kafka", Level.ERROR);
    //        setClassLogLevel("org.springframework.kafka", Level.ERROR);
  }

  /**
   * Helper to dynamically change log levels for specific classes.
   *
   * @param className The fully qualified class name.
   * @param level The desired log level.
   */
  public void setClassLogLevel(String className, Level level) {
    Logger logger = (Logger) LoggerFactory.getLogger(className);
    logger.setLevel(level);
  }
}
