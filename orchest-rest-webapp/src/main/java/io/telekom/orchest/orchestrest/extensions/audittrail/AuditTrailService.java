package io.telekom.orchest.orchestrest.extensions.audittrail;

import io.telekom.orchest.orchestrest.extensions.audittrail.model.AuditTrailEntry;
import io.telekom.orchest.orchestrest.extensions.audittrail.repository.AuditTrailRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Service layer for recording and querying audit trail entries. */
@Service
@RequiredArgsConstructor
public class AuditTrailService {

  private final AuditTrailRepository repository;

  public void record(AuditTrailEntry entry) {
    repository.save(entry);
  }

  public List<AuditTrailEntry> getByUser(String userEmail, int limit) {
    return repository.findByUserEmail(userEmail, limit);
  }

  public List<AuditTrailEntry> getByTimeRange(OffsetDateTime from, OffsetDateTime to, int limit) {
    return repository.findByTimeRange(from, to, limit);
  }

  public List<AuditTrailEntry> getByPath(
      String path, OffsetDateTime from, OffsetDateTime to, int limit) {
    return repository.findByPath(path, from, to, limit);
  }
}
