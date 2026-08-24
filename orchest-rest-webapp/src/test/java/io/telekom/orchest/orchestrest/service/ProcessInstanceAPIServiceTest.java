package io.telekom.orchest.orchestrest.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.PIState;
import io.telekom.orchest.api.core.request.BatchInstanceRequest;
import io.telekom.orchest.api.core.request.CancelInstanceRequest;
import io.telekom.orchest.api.core.request.ProcessInvocationRequest;
import io.telekom.orchest.api.core.request.RetryProcessEvent;
import io.telekom.orchest.api.core.request.UpdateInstanceRequest;
import io.telekom.orchest.api.core.response.ProcessInvocationResponse;
import io.telekom.orchest.orchestrest.api.dto.PagedRequestDTO;
import io.telekom.orchest.orchestrest.api.dto.ProcessInstanceDTO;
import io.telekom.orchest.orchestrest.api.dto.ProcessInstanceScrollDTO;
import io.telekom.orchest.orchestrest.api.dto.ProcessInstanceStats;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.configurations.LoggedInUserContext;
import io.telekom.orchest.orchestrest.event.EventProducer;
import io.telekom.orchest.orchestrest.service.processInstance.ProcessInstanceAPIService;
import io.telekom.orchest.orchestrest.service.processInstance.ProcessInstanceDataService;
import io.telekom.orchest.orchestrest.service.processdefinition.ProcessDefinitionDataService;
import io.telekom.orchest.telemetry.OrchestRestTelemetryService;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * Unit tests for {@link
 * io.telekom.orchest.orchestrest.service.processInstance.ProcessInstanceAPIService}.
 */
@ExtendWith(MockitoExtension.class)
class ProcessInstanceAPIServiceTest {

  @Mock private ProcessInstanceDataService processInstanceDataService;

  @Mock private ProcessDefinitionDataService processDefinitionDataService;

  @Mock private EventProducer eventProducer;

  @Mock private OrchestRestTelemetryService orchestRestTelemetryService;

  @Mock private LoggedInUserContext loggedInUserContext;

  @Mock
  private io.telekom.orchest.adapter.mongo.repository.MongoProcessInstanceRepository
      mongoProcessInstanceRepository;

  @Mock private io.telekom.orchest.enginecore.bpmn.service.UserTaskService userTaskService;

  @Mock
  private io.telekom.orchest.enginecore.bpmn.service.EventRegisterService eventRegisterService;

  @Mock
  private io.telekom.orchest.orchestrest.extensions.sensitivevariables
          .ProcessSensitiveVariablesService
      processSensitiveVariablesService;

  @InjectMocks private ProcessInstanceAPIService service;

  /**
   * Helper to create a ProcessInstance. ProcessInstance uses @RequiredArgsConstructor with final
   * fields (processInstanceId, processDefinitionId, version).
   */
  private ProcessInstance createProcessInstance(
      String piId, String defId, Integer version, PIState state) {
    ProcessInstance pi = new ProcessInstance(piId, defId, version);
    pi.setState(state);
    pi.setCreatedAt(OffsetDateTime.now());
    return pi;
  }

  @Nested
  @DisplayName("createProcessInstance")
  class CreateProcessInstance {

    @Test
    @DisplayName("should generate process instance ID when not provided")
    void createProcessInstance_generatesId() {
      ProcessInvocationRequest request =
          ProcessInvocationRequest.builder().processDefinitionId("test-process").version(1).build();

      when(processDefinitionDataService.getProcessDefinitionsWithVersion("test-process", 1))
          .thenReturn(
              Optional.of(
                  ProcessDefinition.builder().definitionId("test-process").version(1).build()));

      ProcessInvocationResponse response = service.createProcessInstance(request);

      assertThat(response.getProcessInstanceId()).isNotNull().isNotBlank();
      assertThat(response.getProcessId()).isEqualTo("test-process");
      assertThat(response.getVersion()).isEqualTo(1);
      verify(eventProducer).sendProcessInvocationEvent(request);
    }

    @Test
    @DisplayName("should use provided process instance ID")
    void createProcessInstance_usesProvidedId() {
      ProcessInvocationRequest request =
          ProcessInvocationRequest.builder()
              .processDefinitionId("test-process")
              .processInstanceId("custom-id")
              .version(2)
              .build();

      when(processDefinitionDataService.getProcessDefinitionsWithVersion("test-process", 2))
          .thenReturn(
              Optional.of(
                  ProcessDefinition.builder().definitionId("test-process").version(2).build()));

      ProcessInvocationResponse response = service.createProcessInstance(request);

      assertThat(response.getProcessInstanceId()).isEqualTo("custom-id");
      assertThat(response.getVersion()).isEqualTo(2);
      verify(eventProducer).sendProcessInvocationEvent(request);
    }

    @Test
    @DisplayName("should default version to -1 when null")
    void createProcessInstance_nullVersion() {
      ProcessInvocationRequest request =
          ProcessInvocationRequest.builder().processDefinitionId("test-process").build();

      when(processDefinitionDataService.getProcessDefinition("test-process"))
          .thenReturn(
              Optional.of(
                  ProcessDefinition.builder().definitionId("test-process").version(-1).build()));

      ProcessInvocationResponse response = service.createProcessInstance(request);

      assertThat(response.getVersion()).isEqualTo(-1);
    }

    @Test
    @DisplayName("should send process invocation event to Kafka")
    void createProcessInstance_sendsEvent() {
      ProcessInvocationRequest request =
          ProcessInvocationRequest.builder()
              .processDefinitionId("proc")
              .processInstanceId("pi-1")
              .version(1)
              .build();

      when(processDefinitionDataService.getProcessDefinitionsWithVersion("proc", 1))
          .thenReturn(
              Optional.of(ProcessDefinition.builder().definitionId("proc").version(1).build()));

      service.createProcessInstance(request);

      verify(eventProducer, times(1)).sendProcessInvocationEvent(request);
    }
  }

  @Nested
  @DisplayName("getProcessInstance")
  class GetProcessInstance {

    @Test
    @DisplayName("should return empty when process instance not found")
    void getProcessInstance_notFound() {
      when(processInstanceDataService.getProcessInstance("nonexistent"))
          .thenReturn(Optional.empty());

      Optional<ProcessInstanceDTO> result =
          service.getProcessInstance("nonexistent", loggedInUserContext);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should map process instance to DTO when found")
    void getProcessInstance_found() {
      ProcessDefinition processDefinition =
          ProcessDefinition.builder()
              .definitionId("proc-def")
              .definitionXML(
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" targetNamespace=\"http://test.orchest.io\"><bpmn:process id=\"proc-def\" isExecutable=\"true\"><bpmn:startEvent id=\"start\"/></bpmn:process><bpmndi:BPMNDiagram id=\"BPMNDiagram_1\"><bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"proc-def\"/></bpmndi:BPMNDiagram></bpmn:definitions>")
              .build();

      ProcessInstance instance = createProcessInstance("pi-123", "proc-def", 1, PIState.RUNNING);
      instance.setActiveNodeIds(Set.of("task-1"));
      instance.setProcessDefinition(processDefinition);
      instance.setExecutionHistory(new LinkedHashMap<>());
      instance.setVariables(Map.of("key", "value"));

      when(processInstanceDataService.getProcessInstance("pi-123"))
          .thenReturn(Optional.of(instance));

      Optional<ProcessInstanceDTO> result =
          service.getProcessInstance("pi-123", loggedInUserContext);

      assertThat(result).isPresent();
      ProcessInstanceDTO dto = result.get();
      assertThat(dto.getProcessInstanceId()).isEqualTo("pi-123");
      assertThat(dto.getProcessDefinitionId()).isEqualTo("proc-def");
      assertThat(dto.getState()).isEqualTo("RUNNING");
      assertThat(dto.getVersion()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("getProcessInstancesStats")
  class GetStats {

    @Test
    @DisplayName("should delegate to data interaction service")
    void getStats() {
      ProcessInstanceStats stats = new ProcessInstanceStats();
      stats.setCompleted(10);
      when(processInstanceDataService.getProcessInstanceStats()).thenReturn(stats);

      ProcessInstanceStats result = service.getProcessInstancesStats();

      assertThat(result.getCompleted()).isEqualTo(10);
      verify(processInstanceDataService).getProcessInstanceStats();
    }
  }

  @Nested
  @DisplayName("getProcessInstances (paginated)")
  class GetProcessInstances {

    @Test
    @DisplayName("should return mapped page of DTOs")
    void getProcessInstances_returnsMappedPage() {
      ProcessInstance instance = createProcessInstance("pi-1", "proc", 1, PIState.COMPLETED);

      Page<ProcessInstance> page = new PageImpl<>(List.of(instance), PageRequest.of(0, 10), 1);
      when(processInstanceDataService.getPageData(any(PagedRequestDTO.class))).thenReturn(page);

      PagedRequestDTO requestDTO =
          PagedRequestDTO.builder().page(0).size(10).sort("-createdAt").build();
      Page<ProcessInstanceDTO> result =
          service.getProcessInstances(requestDTO, loggedInUserContext);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().getProcessInstanceId()).isEqualTo("pi-1");
    }
  }

  @Nested
  @DisplayName("cancelInstance")
  class CancelInstance {

    @Test
    @DisplayName("should do nothing when instances list is empty")
    void cancelInstance_emptyList() {
      CancelInstanceRequest request = new CancelInstanceRequest();
      request.setInstances(Collections.emptyList());

      service.cancelInstance(request);

      verifyNoInteractions(processInstanceDataService);
    }

    @Test
    @DisplayName("should do nothing when instances list is null")
    void cancelInstance_nullList() {
      CancelInstanceRequest request = new CancelInstanceRequest();
      request.setInstances(null);

      service.cancelInstance(request);

      verifyNoInteractions(processInstanceDataService);
    }

    @Test
    @DisplayName("should cancel instances with cascading")
    void cancelInstance_smallBatch() {
      CancelInstanceRequest request = new CancelInstanceRequest();
      request.setInstances(
          List.of(
              new CancelInstanceRequest.Request("pi-1", "proc-1"),
              new CancelInstanceRequest.Request("pi-2", "proc-1")));

      when(processInstanceDataService.getProcessInstance("pi-1")).thenReturn(Optional.empty());
      when(processInstanceDataService.getProcessInstance("pi-2")).thenReturn(Optional.empty());
      when(mongoProcessInstanceRepository.findAllChildInstanceIds(any()))
          .thenReturn(Collections.emptyList());

      service.cancelInstance(request);

      verify(processInstanceDataService, times(2)).updateFirst(any(), any(), eq("processInstance"));
      verify(orchestRestTelemetryService, times(2)).incrementCancelInstanceCounter(any());
    }

    @Test
    @DisplayName("should cascade cancellation to child instances")
    void cancelInstance_cascadesToChildren() {
      CancelInstanceRequest request = new CancelInstanceRequest();
      request.setInstances(List.of(new CancelInstanceRequest.Request("pi-parent", "proc-1")));

      io.telekom.orchest.adapter.mongo.model.ProcessInstance childDoc =
          io.telekom.orchest.adapter.mongo.model.ProcessInstance.builder()
              .processInstanceId("pi-child")
              .state(PIState.RUNNING)
              .build();

      when(processInstanceDataService.getProcessInstance("pi-parent")).thenReturn(Optional.empty());
      when(processInstanceDataService.getProcessInstance("pi-child")).thenReturn(Optional.empty());
      when(mongoProcessInstanceRepository.findAllChildInstanceIds("pi-parent"))
          .thenReturn(List.of(childDoc));
      when(mongoProcessInstanceRepository.findAllChildInstanceIds("pi-child"))
          .thenReturn(Collections.emptyList());

      service.cancelInstance(request);

      // Parent + child = 2 updateFirst calls
      verify(processInstanceDataService, times(2)).updateFirst(any(), any(), eq("processInstance"));
      verify(userTaskService, times(2)).cancelTasksForProcessInstance(any());
      verify(eventRegisterService, times(2)).cleanupAllEventsForProcessInstance(any());
    }
  }

  @Nested
  @DisplayName("retryInstance")
  class RetryInstance {

    @Test
    @DisplayName("should return true when retry event is sent successfully")
    void retryInstance_success() {
      RetryProcessEvent event =
          RetryProcessEvent.builder()
              .processInstanceId("pi-123")
              .activityId("task-1")
              .previousActivityId("task-1")
              .build();

      boolean result = service.retryInstance(event);

      assertThat(result).isTrue();
      verify(eventProducer).sendRetryEvent(event);
      verify(orchestRestTelemetryService).incrementRestRetryEventCounter();
    }

    @Test
    @DisplayName("should return false when retry event fails")
    void retryInstance_failure() {
      RetryProcessEvent event =
          RetryProcessEvent.builder()
              .processInstanceId("pi-123")
              .activityId("task-1")
              .previousActivityId("task-1")
              .build();

      doThrow(new RuntimeException("Kafka unavailable")).when(eventProducer).sendRetryEvent(event);

      boolean result = service.retryInstance(event);

      assertThat(result).isFalse();
    }
  }

  @Nested
  @DisplayName("modifyInstance")
  class ModifyInstance {

    @Test
    @DisplayName("should throw 404 when process instance not found")
    void modifyInstance_notFound() {
      UpdateInstanceRequest request =
          UpdateInstanceRequest.builder()
              .processInstanceId("nonexistent")
              .fromNodeId("a")
              .toNodeId("b")
              .build();

      when(processInstanceDataService.getProcessInstance(eq("nonexistent"), any()))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.modifyInstance(request))
          .isInstanceOf(RestExceptions.class)
          .hasMessageContaining("ProcessInstance not found");
    }

    @Test
    @DisplayName("should throw exception when instance is not in INCIDENT state")
    void modifyInstance_wrongState() {
      UpdateInstanceRequest request =
          UpdateInstanceRequest.builder()
              .processInstanceId("pi-123")
              .fromNodeId("a")
              .toNodeId("b")
              .build();

      ProcessInstance instance = createProcessInstance("pi-123", "proc", 1, PIState.RUNNING);
      when(processInstanceDataService.getProcessInstance(eq("pi-123"), any()))
          .thenReturn(Optional.of(instance));

      assertThatThrownBy(() -> service.modifyInstance(request))
          .isInstanceOf(RestExceptions.class)
          .hasMessageContaining("Modification is not allowed");
    }

    @Test
    @DisplayName("should send retry event when instance is in INCIDENT state")
    void modifyInstance_success() {
      UpdateInstanceRequest request =
          UpdateInstanceRequest.builder()
              .processInstanceId("pi-123")
              .fromNodeId("a")
              .toNodeId("b")
              .build();

      ProcessInstance instance = createProcessInstance("pi-123", "proc", 1, PIState.INCIDENT);
      when(processInstanceDataService.getProcessInstance(eq("pi-123"), any()))
          .thenReturn(Optional.of(instance));

      boolean result = service.modifyInstance(request);

      assertThat(result).isTrue();
      verify(eventProducer).sendRetryEvent(any(RetryProcessEvent.class));
    }
  }

  @Nested
  @DisplayName("cancelInstancesBatch")
  class CancelInstancesBatch {

    @Test
    @DisplayName("should do nothing when IDs list is empty")
    void cancelBatch_emptyList() {
      BatchInstanceRequest request =
          BatchInstanceRequest.builder().processInstanceIds(Collections.emptyList()).build();

      service.cancelInstancesBatch(request);

      verifyNoInteractions(processInstanceDataService);
    }

    @Test
    @DisplayName("should deduplicate and process distinct IDs with cascading")
    void cancelBatch_deduplicates() {
      BatchInstanceRequest request =
          BatchInstanceRequest.builder()
              .processInstanceIds(List.of("pi-1", "pi-1", "pi-2"))
              .build();

      when(processInstanceDataService.getProcessInstance("pi-1")).thenReturn(Optional.empty());
      when(processInstanceDataService.getProcessInstance("pi-2")).thenReturn(Optional.empty());
      when(mongoProcessInstanceRepository.findAllChildInstanceIds(any()))
          .thenReturn(Collections.emptyList());

      service.cancelInstancesBatch(request);

      // 2 distinct IDs, each cancelled individually
      verify(processInstanceDataService, times(2)).updateFirst(any(), any(), eq("processInstance"));
    }
  }

  @Nested
  @DisplayName("retryInstancesBatch")
  class RetryInstancesBatch {

    @Test
    @DisplayName("should skip instances not found")
    void retryBatch_instanceNotFound() {
      BatchInstanceRequest request =
          BatchInstanceRequest.builder().processInstanceIds(List.of("pi-missing")).build();

      when(processInstanceDataService.getProcessInstance("pi-missing"))
          .thenReturn(Optional.empty());

      service.retryInstancesBatch(request);

      verify(eventProducer, never()).sendRetryEvent(any());
    }

    @Test
    @DisplayName("should skip instances not in INCIDENT state")
    void retryBatch_notIncidentState() {
      BatchInstanceRequest request =
          BatchInstanceRequest.builder().processInstanceIds(List.of("pi-1")).build();

      ProcessInstance instance = createProcessInstance("pi-1", "proc", 1, PIState.RUNNING);
      when(processInstanceDataService.getProcessInstance("pi-1")).thenReturn(Optional.of(instance));

      service.retryInstancesBatch(request);

      verify(eventProducer, never()).sendRetryEvent(any());
    }

    @Test
    @DisplayName("should send retry events for valid INCIDENT instances")
    void retryBatch_success() {
      BatchInstanceRequest request =
          BatchInstanceRequest.builder().processInstanceIds(List.of("pi-1")).build();

      ProcessInstance instance = createProcessInstance("pi-1", "proc", 1, PIState.INCIDENT);
      instance.setActiveNodeIds(Set.of("task-1"));
      when(processInstanceDataService.getProcessInstance("pi-1")).thenReturn(Optional.of(instance));

      service.retryInstancesBatch(request);

      verify(eventProducer, times(1)).sendRetryEvent(any(RetryProcessEvent.class));
      verify(orchestRestTelemetryService, times(1)).incrementRestRetryEventCounter();
    }

    @Test
    @DisplayName("should skip instances with no active nodes")
    void retryBatch_noActiveNodes() {
      BatchInstanceRequest request =
          BatchInstanceRequest.builder().processInstanceIds(List.of("pi-1")).build();

      ProcessInstance instance = createProcessInstance("pi-1", "proc", 1, PIState.INCIDENT);
      instance.setActiveNodeIds(Set.of());
      when(processInstanceDataService.getProcessInstance("pi-1")).thenReturn(Optional.of(instance));

      service.retryInstancesBatch(request);

      verify(eventProducer, never()).sendRetryEvent(any());
    }
  }

  @Nested
  @DisplayName("scrollProcessInstances")
  class ScrollProcessInstances {

    private PagedRequestDTO baseFilter() {
      return PagedRequestDTO.builder().sort("-createdAt").page(0).size(1).build();
    }

    @Test
    @DisplayName("should reject negative from offset")
    void scroll_negativeFrom() {
      assertThatThrownBy(
              () -> service.scrollProcessInstances(baseFilter(), -1, 5, loggedInUserContext))
          .isInstanceOf(RestExceptions.class)
          .hasMessageContaining("from must be >= 0");
    }

    @Test
    @DisplayName("should reject when to is not greater than from")
    void scroll_invalidRange() {
      assertThatThrownBy(
              () -> service.scrollProcessInstances(baseFilter(), 10, 10, loggedInUserContext))
          .isInstanceOf(RestExceptions.class)
          .hasMessageContaining("to must be greater than from");
    }

    @Test
    @DisplayName("should reject window larger than max")
    void scroll_windowTooLarge() {
      assertThatThrownBy(
              () -> service.scrollProcessInstances(baseFilter(), 0, 501, loggedInUserContext))
          .isInstanceOf(RestExceptions.class)
          .hasMessageContaining("scroll window");
    }

    @Test
    @DisplayName("should return empty slice when no rows at offset")
    void scroll_emptyWindow() {
      when(processInstanceDataService.getProcessInstancesSlice(any(), eq(10), eq(6)))
          .thenReturn(Collections.emptyList());

      ProcessInstanceScrollDTO result =
          service.scrollProcessInstances(baseFilter(), 10, 15, loggedInUserContext);

      assertThat(result.getContent()).isEmpty();
      assertThat(result.getFrom()).isEqualTo(10);
      assertThat(result.getTo()).isEqualTo(15);
      assertThat(result.isHasNext()).isFalse();
      verify(processInstanceDataService).getProcessInstancesSlice(any(), eq(10), eq(6));
    }

    @Test
    @DisplayName("should set hasNext when an extra matching row exists after the window")
    void scroll_hasNextWhenFetchedExtraRow() {
      List<ProcessInstance> windowPlusOne =
          Stream.generate(() -> createProcessInstance("pi-1", "def", 1, PIState.COMPLETED))
              .limit(6)
              .toList();
      when(processInstanceDataService.getProcessInstancesSlice(any(), eq(5), eq(6)))
          .thenReturn(windowPlusOne);

      ProcessInstanceScrollDTO result =
          service.scrollProcessInstances(baseFilter(), 5, 10, loggedInUserContext);

      assertThat(result.getContent()).hasSize(5);
      assertThat(result.getContent().getFirst().getProcessInstanceId()).isEqualTo("pi-1");
      assertThat(result.isHasNext()).isTrue();
      verify(processInstanceDataService).getProcessInstancesSlice(any(), eq(5), eq(6));
    }

    @Test
    @DisplayName("should clear hasNext when no row exists beyond the window")
    void scroll_noHasNextWhenNoExtraRow() {
      ProcessInstance row = createProcessInstance("pi-1", "def", 1, PIState.COMPLETED);
      List<ProcessInstance> exactWindow = Stream.generate(() -> row).limit(5).toList();
      when(processInstanceDataService.getProcessInstancesSlice(any(), eq(5), eq(6)))
          .thenReturn(exactWindow);

      ProcessInstanceScrollDTO ended =
          service.scrollProcessInstances(baseFilter(), 5, 10, loggedInUserContext);

      assertThat(ended.getContent()).hasSize(5);
      assertThat(ended.isHasNext()).isFalse();
      verify(processInstanceDataService).getProcessInstancesSlice(any(), eq(5), eq(6));
    }
  }
}
