package io.telekom.orchest.enginecore.bpmn.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessInstanceRepository;
import io.telekom.orchest.api.core.model.bpmn.PIState;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests ProcessInstanceService retrieval, active-state filtering, and persistence operations. */
@ExtendWith(MockitoExtension.class)
class ProcessInstanceServiceTest {

  @Mock private ProcessInstanceRepository processInstanceRepository;

  private ProcessInstanceService service;

  @BeforeEach
  void setUp() {
    service = new ProcessInstanceService(processInstanceRepository);
  }

  // ========================================================================================
  // getInstanceById
  // ========================================================================================

  @Test
  void getInstanceById_delegatesToRepository() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    when(processInstanceRepository.getById("pi-1")).thenReturn(Optional.of(instance));

    Optional<ProcessInstance> result = service.getInstanceById("pi-1");

    assertTrue(result.isPresent());
    assertEquals("pi-1", result.get().getProcessInstanceId());
    verify(processInstanceRepository).getById("pi-1");
  }

  @Test
  void getInstanceById_returnsEmptyWhenNotFound() {
    when(processInstanceRepository.getById("pi-missing")).thenReturn(Optional.empty());

    Optional<ProcessInstance> result = service.getInstanceById("pi-missing");

    assertTrue(result.isEmpty());
  }

  // ========================================================================================
  // getProcessInstanceIfActive
  // ========================================================================================

  @Test
  void getProcessInstanceIfActive_returnsInstanceWhenActive() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setState(PIState.RUNNING);
    when(processInstanceRepository.getById("pi-1")).thenReturn(Optional.of(instance));

    Optional<ProcessInstance> result = service.getProcessInstanceIfActive("pi-1");

    assertTrue(result.isPresent());
    assertEquals(PIState.RUNNING, result.get().getState());
  }

  @Test
  void getProcessInstanceIfActive_returnsEmptyForCompletedInstance() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setState(PIState.COMPLETED);
    when(processInstanceRepository.getById("pi-1")).thenReturn(Optional.of(instance));

    Optional<ProcessInstance> result = service.getProcessInstanceIfActive("pi-1");

    assertTrue(result.isEmpty());
  }

  @Test
  void getProcessInstanceIfActive_returnsEmptyForCancelledInstance() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setState(PIState.CANCELLED);
    when(processInstanceRepository.getById("pi-1")).thenReturn(Optional.of(instance));

    Optional<ProcessInstance> result = service.getProcessInstanceIfActive("pi-1");

    assertTrue(result.isEmpty());
  }

  @Test
  void getProcessInstanceIfActive_returnsEmptyForTerminatedInstance() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setState(PIState.TERMINATED);
    when(processInstanceRepository.getById("pi-1")).thenReturn(Optional.of(instance));

    Optional<ProcessInstance> result = service.getProcessInstanceIfActive("pi-1");

    assertTrue(result.isEmpty());
  }

  @Test
  void getProcessInstanceIfActive_returnsEmptyWhenNotFound() {
    when(processInstanceRepository.getById("pi-missing")).thenReturn(Optional.empty());

    Optional<ProcessInstance> result = service.getProcessInstanceIfActive("pi-missing");

    assertTrue(result.isEmpty());
  }

  @Test
  void getProcessInstanceIfActive_returnsInstanceForStartedState() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setState(PIState.STARTED);
    when(processInstanceRepository.getById("pi-1")).thenReturn(Optional.of(instance));

    Optional<ProcessInstance> result = service.getProcessInstanceIfActive("pi-1");

    assertTrue(result.isPresent());
  }

  @Test
  void getProcessInstanceIfActive_returnsInstanceForIncidentState() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setState(PIState.INCIDENT);
    when(processInstanceRepository.getById("pi-1")).thenReturn(Optional.of(instance));

    Optional<ProcessInstance> result = service.getProcessInstanceIfActive("pi-1");

    assertTrue(result.isPresent());
  }

  @Test
  void getProcessInstanceIfActive_returnsInstanceForHoldState() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setState(PIState.HOLD);
    when(processInstanceRepository.getById("pi-1")).thenReturn(Optional.of(instance));

    Optional<ProcessInstance> result = service.getProcessInstanceIfActive("pi-1");

    assertTrue(result.isPresent());
  }

  @Test
  void getProcessInstanceIfActive_returnsInstanceForFailedState() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setState(PIState.FAILED);
    when(processInstanceRepository.getById("pi-1")).thenReturn(Optional.of(instance));

    Optional<ProcessInstance> result = service.getProcessInstanceIfActive("pi-1");

    assertTrue(result.isPresent());
  }

  // ========================================================================================
  // save
  // ========================================================================================

  @Test
  void save_delegatesToRepository() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    when(processInstanceRepository.save(instance)).thenReturn(instance);

    ProcessInstance result = service.save(instance);

    assertSame(instance, result);
    verify(processInstanceRepository).save(instance);
  }

  @Test
  void save_propagatesRuntimeExceptionFromRepository() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    when(processInstanceRepository.save(instance))
        .thenThrow(new RuntimeException("persist failed"));

    assertThrows(RuntimeException.class, () -> service.save(instance));
  }

  // ========================================================================================
  // getProcessInstanceRepository via @Getter
  // ========================================================================================

  @Test
  void getProcessInstanceRepository_returnsInjectedRepository() {
    assertSame(processInstanceRepository, service.getProcessInstanceRepository());
  }
}
