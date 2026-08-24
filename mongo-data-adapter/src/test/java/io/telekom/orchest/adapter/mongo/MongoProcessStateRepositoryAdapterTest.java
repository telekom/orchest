package io.telekom.orchest.adapter.mongo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.telekom.orchest.adapter.mongo.mapper.ProcessStateMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoProcessStateRepository;
import io.telekom.orchest.api.core.adapters.data.model.ProcessState;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link MongoProcessStateRepositoryAdapter} verifying save, findByProcessId,
 * findAll, and deleteByProcessId operations.
 */
@ExtendWith(MockitoExtension.class)
class MongoProcessStateRepositoryAdapterTest {

  @Mock private MongoProcessStateRepository repository;

  @Mock private ProcessStateMapper mapper;

  private MongoProcessStateRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new MongoProcessStateRepositoryAdapter(repository, mapper);
  }

  @Test
  @DisplayName("save delegates to repository and returns mapped domain object")
  void save_delegatesToRepositoryAndReturnsMappedDomainObject() {
    ProcessState domainInput = new ProcessState(null, "pd-1", true);

    io.telekom.orchest.adapter.mongo.model.ProcessState mongoDocument =
        new io.telekom.orchest.adapter.mongo.model.ProcessState(null, "pd-1", true);
    io.telekom.orchest.adapter.mongo.model.ProcessState savedDocument =
        new io.telekom.orchest.adapter.mongo.model.ProcessState("mongo-id", "pd-1", true);
    ProcessState domainResult = new ProcessState("mongo-id", "pd-1", true);

    when(repository.findByProcessId("pd-1")).thenReturn(Optional.empty());
    when(mapper.toDocument(domainInput)).thenReturn(mongoDocument);
    when(repository.save(mongoDocument)).thenReturn(savedDocument);
    when(mapper.toDomain(savedDocument)).thenReturn(domainResult);

    ProcessState result = adapter.save(domainInput);

    assertEquals("mongo-id", result.getId());
    assertEquals("pd-1", result.getProcessId());
    assertTrue(result.isStatus());
    verify(repository).save(mongoDocument);
  }

  @Test
  @DisplayName("findByProcessId delegates to repository and maps to domain")
  void findByProcessId_delegatesAndMaps() {
    io.telekom.orchest.adapter.mongo.model.ProcessState mongoDoc =
        new io.telekom.orchest.adapter.mongo.model.ProcessState("mongo-id", "pd-2", false);
    ProcessState domainObj = new ProcessState("mongo-id", "pd-2", false);

    when(repository.findByProcessId("pd-2")).thenReturn(Optional.of(mongoDoc));
    when(mapper.toDomain(mongoDoc)).thenReturn(domainObj);

    Optional<ProcessState> result = adapter.findByProcessId("pd-2");

    assertTrue(result.isPresent());
    assertEquals("pd-2", result.get().getProcessId());
    verify(repository).findByProcessId("pd-2");
  }

  @Test
  @DisplayName("findAll maps all documents to domain")
  void findAll_mapsAllDocuments() {
    io.telekom.orchest.adapter.mongo.model.ProcessState mongoDoc =
        new io.telekom.orchest.adapter.mongo.model.ProcessState("mongo-id", "pd-3", true);
    ProcessState domainObj = new ProcessState("mongo-id", "pd-3", true);

    when(repository.findAll()).thenReturn(List.of(mongoDoc));
    when(mapper.toDomain(mongoDoc)).thenReturn(domainObj);

    List<ProcessState> result = adapter.findAll();

    assertEquals(1, result.size());
    assertEquals("pd-3", result.getFirst().getProcessId());
    verify(repository).findAll();
  }

  @Test
  @DisplayName("deleteByProcessId removes document and returns mapped domain object")
  void deleteByProcessId_removesAndReturnsDomain() {
    io.telekom.orchest.adapter.mongo.model.ProcessState mongoDoc =
        new io.telekom.orchest.adapter.mongo.model.ProcessState("mongo-id", "pd-5", true);
    ProcessState domainObj = new ProcessState("mongo-id", "pd-5", true);

    when(repository.findByProcessId("pd-5")).thenReturn(Optional.of(mongoDoc));
    when(mapper.toDomain(mongoDoc)).thenReturn(domainObj);

    Optional<ProcessState> result = adapter.deleteByProcessId("pd-5");

    assertTrue(result.isPresent());
    assertEquals("pd-5", result.get().getProcessId());
    verify(repository).delete(mongoDoc);
  }

  @Test
  @DisplayName("deleteByProcessId returns empty when not found")
  void deleteByProcessId_notFound_returnsEmpty() {
    when(repository.findByProcessId("missing")).thenReturn(Optional.empty());

    Optional<ProcessState> result = adapter.deleteByProcessId("missing");

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("save reuses existing id when processId already exists")
  void save_existingProcessId_reusesId() {
    ProcessState domainInput = new ProcessState(null, "pd-6", false);

    io.telekom.orchest.adapter.mongo.model.ProcessState existingDocument =
        new io.telekom.orchest.adapter.mongo.model.ProcessState("mongo-id-6", "pd-6", true);
    io.telekom.orchest.adapter.mongo.model.ProcessState updateDocument =
        new io.telekom.orchest.adapter.mongo.model.ProcessState("mongo-id-6", "pd-6", false);
    io.telekom.orchest.adapter.mongo.model.ProcessState savedDocument =
        new io.telekom.orchest.adapter.mongo.model.ProcessState("mongo-id-6", "pd-6", false);
    ProcessState savedDomain = new ProcessState("mongo-id-6", "pd-6", false);

    when(repository.findByProcessId("pd-6")).thenReturn(Optional.of(existingDocument));
    when(mapper.toDocument(domainInput)).thenReturn(updateDocument);
    when(repository.save(updateDocument)).thenReturn(savedDocument);
    when(mapper.toDomain(savedDocument)).thenReturn(savedDomain);

    ProcessState result = adapter.save(domainInput);

    assertEquals("mongo-id-6", result.getId());
    assertEquals(false, result.isStatus());
    verify(repository).save(updateDocument);
  }
}
