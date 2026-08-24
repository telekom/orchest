package io.telekom.orchest.adapter.mongo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.adapter.mongo.mapper.ProcessInstanceMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoProcessInstanceRepository;
import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Unit tests for {@link MongoProcessInstanceRepositoryAdapter} verifying save, getById, and list
 * operations with both static and dynamic process flows.
 */
@ExtendWith(MockitoExtension.class)
class MongoProcessInstanceRepositoryAdapterTest {

  @Mock private MongoProcessInstanceRepository processInstanceRepository;

  @Mock private MongoProcessDefinitionRepositoryAdapter mongoProcessDefinitionRepositoryAdapter;

  @Mock
  private MongoDynamicProcessDefinitionRepositoryAdapter
      mongoDynamicProcessDefinitionRepositoryAdapter;

  @Mock private ProcessInstanceMapper processInstanceMapper;

  @Mock private MongoTemplate mongoTemplate;

  private MongoProcessInstanceRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter =
        new MongoProcessInstanceRepositoryAdapter(
            processInstanceRepository,
            mongoProcessDefinitionRepositoryAdapter,
            mongoDynamicProcessDefinitionRepositoryAdapter,
            mongoTemplate,
            processInstanceMapper);
  }

  // ============================================================
  // save - delegates and returns mapped domain object
  // ============================================================

  @Test
  @DisplayName("save delegates to repository and returns mapped domain object")
  void save_delegatesToRepositoryAndReturnsMappedDomainObject() {
    ProcessInstance domainInput = new ProcessInstance("pi-100", "pd-200", 1);

    io.telekom.orchest.adapter.mongo.model.ProcessInstance mongoDocument =
        io.telekom.orchest.adapter.mongo.model.ProcessInstance.builder()
            .processInstanceId("pi-100")
            .processDefinitionId("pd-200")
            .version(1)
            .build();

    io.telekom.orchest.adapter.mongo.model.ProcessInstance savedDocument =
        io.telekom.orchest.adapter.mongo.model.ProcessInstance.builder()
            .id("mongo-id-1")
            .processInstanceId("pi-100")
            .processDefinitionId("pd-200")
            .version(1)
            .build();

    ProcessInstance domainResult = new ProcessInstance("pi-100", "pd-200", 1);
    domainResult.setId("mongo-id-1");

    when(processInstanceMapper.toDocument(domainInput)).thenReturn(mongoDocument);
    when(processInstanceRepository.save(mongoDocument)).thenReturn(savedDocument);
    when(processInstanceMapper.toDomain(savedDocument)).thenReturn(domainResult);

    ProcessInstance result = adapter.save(domainInput);

    assertNotNull(result);
    assertEquals("pi-100", result.getProcessInstanceId());
    assertEquals("pd-200", result.getProcessDefinitionId());
    assertEquals("mongo-id-1", result.getId());
    verify(processInstanceRepository).save(mongoDocument);
  }

  // ============================================================
  // getById - delegates and maps correctly
  // ============================================================

  @Test
  @DisplayName("getById delegates to repository and maps to domain with static process definition")
  void getById_delegatesAndMaps_staticProcess() {
    io.telekom.orchest.adapter.mongo.model.ProcessInstance mongoDoc =
        io.telekom.orchest.adapter.mongo.model.ProcessInstance.builder()
            .processInstanceId("pi-200")
            .processDefinitionId("pd-300")
            .version(2)
            .dynamicFlow(false)
            .build();

    ProcessInstance domainObj = new ProcessInstance("pi-200", "pd-300", 2);
    domainObj.setDynamicFlow(false);

    ProcessDefinition processDefinition = new ProcessDefinition();

    when(processInstanceRepository.findByProcessInstanceId("pi-200"))
        .thenReturn(Optional.of(mongoDoc));
    when(processInstanceMapper.toDomain(mongoDoc)).thenReturn(domainObj);
    when(mongoProcessDefinitionRepositoryAdapter.getByIdAndVersion("pd-300", 2))
        .thenReturn(Optional.of(processDefinition));

    Optional<ProcessInstance> result = adapter.getById("pi-200");

    assertTrue(result.isPresent());
    assertEquals("pi-200", result.get().getProcessInstanceId());
    verify(processInstanceRepository).findByProcessInstanceId("pi-200");
    verify(mongoProcessDefinitionRepositoryAdapter).getByIdAndVersion("pd-300", 2);
  }

  @Test
  @DisplayName("getById returns empty optional when not found")
  void getById_notFound_returnsEmpty() {
    when(processInstanceRepository.findByProcessInstanceId("non-existent"))
        .thenReturn(Optional.empty());

    Optional<ProcessInstance> result = adapter.getById("non-existent");

    assertFalse(result.isPresent());
  }

  // ============================================================
  // getById - dynamic flow sets dynamic process definition
  // ============================================================

  @Test
  @DisplayName("getById with dynamic flow sets dynamic process definition")
  void getById_dynamicFlow_setsDynamicProcessDefinition() {
    io.telekom.orchest.adapter.mongo.model.ProcessInstance mongoDoc =
        io.telekom.orchest.adapter.mongo.model.ProcessInstance.builder()
            .processInstanceId("pi-300")
            .processDefinitionId("pd-400")
            .version(1)
            .dynamicFlow(true)
            .build();

    ProcessInstance domainObj = new ProcessInstance("pi-300", "pd-400", 1);
    domainObj.setDynamicFlow(true);

    when(processInstanceRepository.findByProcessInstanceId("pi-300"))
        .thenReturn(Optional.of(mongoDoc));
    when(processInstanceMapper.toDomain(mongoDoc)).thenReturn(domainObj);
    when(mongoDynamicProcessDefinitionRepositoryAdapter.getByProcessInstanceId("pi-300"))
        .thenReturn(null);

    Optional<ProcessInstance> result = adapter.getById("pi-300");

    assertTrue(result.isPresent());
    verify(mongoDynamicProcessDefinitionRepositoryAdapter).getByProcessInstanceId("pi-300");
    verify(mongoProcessDefinitionRepositoryAdapter, never()).getByIdAndVersion(any(), any());
  }

  // ============================================================
  // getProcessInstances
  // ============================================================

  @Test
  @DisplayName("getProcessInstances returns mapped list of all instances")
  void getProcessInstances_returnsMappedList() {
    io.telekom.orchest.adapter.mongo.model.ProcessInstance mongoDoc1 =
        io.telekom.orchest.adapter.mongo.model.ProcessInstance.builder()
            .processInstanceId("pi-a")
            .processDefinitionId("pd-x")
            .build();
    io.telekom.orchest.adapter.mongo.model.ProcessInstance mongoDoc2 =
        io.telekom.orchest.adapter.mongo.model.ProcessInstance.builder()
            .processInstanceId("pi-b")
            .processDefinitionId("pd-y")
            .build();

    ProcessInstance domain1 = new ProcessInstance("pi-a", "pd-x", 1);
    ProcessInstance domain2 = new ProcessInstance("pi-b", "pd-y", 1);

    when(processInstanceRepository.findAll()).thenReturn(List.of(mongoDoc1, mongoDoc2));
    when(processInstanceMapper.toDomain(mongoDoc1)).thenReturn(domain1);
    when(processInstanceMapper.toDomain(mongoDoc2)).thenReturn(domain2);

    List<ProcessInstance> result = adapter.getProcessInstances();

    assertEquals(2, result.size());
    assertEquals("pi-a", result.get(0).getProcessInstanceId());
    assertEquals("pi-b", result.get(1).getProcessInstanceId());
  }
}
