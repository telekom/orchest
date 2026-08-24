package io.telekom.orchest;

import static io.telekom.orchest.adapter.kafka.model.TopicConstant.CLIENT_COMMON_WORKER_EVENT_TOPIC;
import static io.telekom.orchest.adapter.kafka.model.TopicConstant.CLIENT_WORKER_EVENT_TOPIC_PREFIX;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.micrometer.core.instrument.util.IOUtils;
import io.telekom.orchest.adapter.kafka.model.EncryptedKafkaEvent;
import io.telekom.orchest.api.JobClient;
import io.telekom.orchest.api.Worker;
import io.telekom.orchest.api.core.adapters.cipher.CipherType;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.request.*;
import io.telekom.orchest.api.core.response.MessageEventResponse;
import io.telekom.orchest.api.core.response.ProcessInvocationResponse;
import io.telekom.orchest.api.core.response.SignalEventResponse;
import io.telekom.orchest.api.core.response.UpdateVariableResponse;
import io.telekom.orchest.api.core.utils.JsonMapper;
import io.telekom.orchest.api.core.utils.StateChangeUtils;
import io.telekom.orchest.config.OrchestClientProperties;
import io.telekom.orchest.kafka.KafkaConsumerManager;
import io.telekom.orchest.kafka.kms.KafkaEncryptionClient;
import io.telekom.orchest.processor.WorkerEventProcessor;
import io.telekom.orchest.producer.EventProducer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

/**
 * Pure Java client for connecting to the OrchesT workflow engine without Spring dependencies.
 * Manages worker registration, Kafka consumers/producers, and communication with the engine.
 */
@Slf4j
public class OrchesTClient implements AutoCloseable, JobClient {

  private final KafkaConsumerManager consumerManager;
  private final EventProducer eventProducer;
  private final WorkerEventProcessor workerEventProcessor;
  private KafkaEncryptionClient kafkaEncryptionClient;
  private final AtomicBoolean running = new AtomicBoolean(true);

  private final Map<String, Worker> workerRegistry;
  private final List<WorkerRegistryRequest.WorkerInfo> workerInfos;

  private final OrchestClientProperties properties;

  /**
   * Creates a new OrchesT client with the given configuration.
   *
   * @param properties the client configuration properties
   */
  public OrchesTClient(OrchestClientProperties properties) {
    // validate and load defaults
    properties.validate();

    this.properties = properties;
    // initiate handler methods
    workerRegistry = new HashMap<>();
    workerInfos = new ArrayList<>();
    // create consumer
    consumerManager = new KafkaConsumerManager(properties);
    // create producer
    kafkaEncryptionClient = new KafkaEncryptionClient(properties);
    eventProducer = new EventProducer(properties, kafkaEncryptionClient);
    // create worker registry
    workerEventProcessor =
        new WorkerEventProcessor(this, eventProducer, properties, workerRegistry);

    // update logger
    Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.setLevel(Level.INFO);
  }

  /**
   * Registers a worker using its class simple name as the worker type.
   *
   * @param worker the worker implementation
   */
  public void registerWorker(Worker worker) {
    registerWorker(worker, false);
  }

  /**
   * Registers a worker using its class simple name as the worker type.
   *
   * @param worker the worker implementation
   * @param commonWorker true if this worker handles tasks across all process definitions
   */
  public void registerWorker(Worker worker, boolean commonWorker) {
    String workerName = worker.getClass().getSimpleName();
    workerRegistry.put(workerName, worker);
    workerInfos.add(new WorkerRegistryRequest.WorkerInfo(workerName, commonWorker));
  }

  /**
   * Registers a worker with an explicit worker type name.
   *
   * @param workerType the service task type identifier
   * @param worker the worker implementation
   */
  public void registerWorker(String workerType, Worker worker) {
    registerWorker(workerType, worker, false);
  }

  /**
   * Registers a worker with an explicit worker type name.
   *
   * @param workerType the service task type identifier
   * @param worker the worker implementation
   * @param commonWorker true if this worker handles tasks across all process definitions
   */
  public void registerWorker(String workerType, Worker worker, boolean commonWorker) {
    workerRegistry.put(workerType, worker);
    workerInfos.add(new WorkerRegistryRequest.WorkerInfo(workerType, commonWorker));
  }

  /**
   * Starts the client by registering workers with the engine and subscribing Kafka consumers to the
   * appropriate worker event topics.
   */
  public void start() {
    log.info("Starting Kafka Producer Manager");
    // send worker registry event

    eventProducer.sendWorkerRegistryUpdateEvent(
        new WorkerRegistryRequest(
            StringUtils.upperCase(properties.getClientId()).replace("-", "_"),
            new HashSet<>(workerInfos)));
    boolean haveCommonWorkers =
        workerInfos.stream().anyMatch(WorkerRegistryRequest.WorkerInfo::isCommonWorker);
    // start consumer for worker threads
    properties
        .getProcessIds()
        .forEach(
            processId -> {
              String processTopics =
                  CLIENT_WORKER_EVENT_TOPIC_PREFIX
                      + StringUtils.upperCase(processId).replace("-", "_")
                      + properties.getKafkaSuffix();
              consumerManager.registerConsumer(processTopics, this::processEvent);
            });

    if (haveCommonWorkers) {
      String topic = CLIENT_COMMON_WORKER_EVENT_TOPIC + properties.getKafkaSuffix();
      consumerManager.registerConsumer(topic, this::processEvent);
    }
  }

  private void processEvent(String key, String value) {
    EncryptedKafkaEvent encryptedKafkaEvent =
        JsonMapper.readFromJson(value, EncryptedKafkaEvent.class);
    // decrypt
    String decrypted =
        kafkaEncryptionClient.cipher(CipherType.DECRYPT, encryptedKafkaEvent.getEvent());
    WorkerEventRequest workerKafkaEvent =
        JsonMapper.readFromJson(decrypted, WorkerEventRequest.class);
    workerEventProcessor.processEvent(workerKafkaEvent);
  }

  @Override
  public void close() {
    if (running.compareAndSet(true, false)) {
      log.info("Closing OrchesT Client...");
      if (consumerManager != null) {
        consumerManager.close();
      }
      log.info("OrchesT Client closed successfully");
    }
  }

  @Override
  public void deployProcessDefinition(ResourceDeploymentRequest deploymentEvent) {
    eventProducer.sendDeploymentEvent(deploymentEvent);
  }

  @Override
  public void deployProcessDefinition(File file, int partitionCount) {
    try {
      String fileContent = IOUtils.toString(new FileInputStream(file), Charset.defaultCharset());
      eventProducer.sendDeploymentEvent(
          new ResourceDeploymentRequest(partitionCount, fileContent, false, null, false));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deployProcessDefinition(String filePath, int partitionCount) {
    try {
      Path path = Path.of(filePath);
      String fileContent = Files.readString(path);
      eventProducer.sendDeploymentEvent(
          new ResourceDeploymentRequest(partitionCount, fileContent, false, null, false));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ProcessInvocationResponse createProcessInstance(
      String processId, Integer version, Map<String, Object> variables) {
    return this.createProcessInstance(processId, null, UUID.randomUUID().toString(), variables);
  }

  @Override
  public ProcessInvocationResponse createProcessInstanceAsync(
      String processId, Integer version, Map<String, Object> variables) {
    ProcessInvocationRequest processInvocationRequest =
        new ProcessInvocationRequest(
            processId, String.valueOf(Instant.now().getNano()), version, variables, null, null);
    eventProducer.sendProcessInvocationEvent(processInvocationRequest);
    return new ProcessInvocationResponse(
        processInvocationRequest.getProcessInstanceId(), processId, version);
  }

  @Override
  public ProcessInvocationResponse createProcessInstance(
      String processId, Integer version, String processInstanceId, Map<String, Object> variables) {
    ProcessInvocationRequest processInvocationRequest =
        new ProcessInvocationRequest(processId, processInstanceId, version, variables, null, null);
    eventProducer.sendProcessInvocationEvent(processInvocationRequest);
    return new ProcessInvocationResponse(
        processInvocationRequest.getProcessInstanceId(), processId, version);
  }

  @Override
  public ProcessInvocationResponse createProcessInstance(
      String processId, String processInstanceId, Map<String, Object> variables) {
    return this.createProcessInstance(processId, null, processInstanceId, variables);
  }

  @Override
  public ProcessInvocationResponse createProcessInstance(
      String processId, Map<String, Object> variables) {
    return this.createProcessInstance(processId, null, UUID.randomUUID().toString(), variables);
  }

  @Override
  public WorkerEventRequest sendCompleteEvent(
      String processInstanceId, Map<String, Object> variables) {
    return null;
  }

  @Override
  public WorkerEventRequest throwIncidentEvent(
      String processInstanceId, Map<String, Object> variables, Throwable e) {
    return null;
  }

  @Override
  public WorkerEventRequest sendErrorEvent(
      String processInstanceId,
      Map<String, Object> variables,
      String errorMessage,
      String errorCode) {
    return null;
  }

  @Override
  public MessageEventResponse sendMessageEvent(
      String messageName, String correlationKey, Map<String, Object> variables) {
    MessageEventRequest messageEventRequest =
        new MessageEventRequest(
            UUID.randomUUID().toString(),
            messageName,
            correlationKey,
            Variables.builder().variables(variables).build(),
            List.of(StateChangeUtils.buildStateChanges(NodeState.TRIGGERED)));
    eventProducer.sendMessageEvent(messageEventRequest);
    return new MessageEventResponse(messageEventRequest.getId(), messageName, correlationKey);
  }

  @Override
  public SignalEventResponse broadcastSignal(String signalName, Map<String, Object> variables) {
    SignalEventRequest signalEventRequest =
        new SignalEventRequest(
            UUID.randomUUID().toString(),
            signalName,
            Variables.builder().variables(variables).build(),
            List.of(StateChangeUtils.buildStateChanges(NodeState.TRIGGERED)));
    eventProducer.sendSignalEvent(signalEventRequest);
    return new SignalEventResponse(signalEventRequest.getSignalId(), signalName);
  }

  @Override
  public UpdateVariableResponse updateVariable(String processInstanceId, Variables variables) {
    UpdateVariableRequest updateVariableRequest =
        new UpdateVariableRequest(processInstanceId, variables);
    eventProducer.sendUpdateVariableEvent(updateVariableRequest);
    return new UpdateVariableResponse(UUID.randomUUID().toString(), processInstanceId, variables);
  }
}
