package io.telekom.orchest.orchestrest.extensions.audittrail.repository;

import io.telekom.orchest.orchestrest.extensions.audittrail.model.AuditTrailEntry;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/** MongoDB repository for persisting and querying audit trail entries. */
@Component
@RequiredArgsConstructor
public class AuditTrailRepository {

  private final MongoTemplate mongoTemplate;

  public AuditTrailEntry save(AuditTrailEntry entry) {
    return mongoTemplate.save(entry);
  }

  public List<AuditTrailEntry> findByUserEmail(String userEmail, int limit) {
    Query query =
        Query.query(Criteria.where("userEmail").is(userEmail))
            .with(Sort.by(Sort.Direction.DESC, "createdAt"))
            .limit(limit);
    return mongoTemplate.find(query, AuditTrailEntry.class);
  }

  public List<AuditTrailEntry> findByTimeRange(OffsetDateTime from, OffsetDateTime to, int limit) {
    Query query =
        Query.query(Criteria.where("createdAt").gte(from).lte(to))
            .with(Sort.by(Sort.Direction.DESC, "createdAt"))
            .limit(limit);
    return mongoTemplate.find(query, AuditTrailEntry.class);
  }

  public List<AuditTrailEntry> findByPath(
      String path, OffsetDateTime from, OffsetDateTime to, int limit) {
    Query query =
        Query.query(Criteria.where("path").is(path).and("createdAt").gte(from).lte(to))
            .with(Sort.by(Sort.Direction.DESC, "createdAt"))
            .limit(limit);
    return mongoTemplate.find(query, AuditTrailEntry.class);
  }
}
