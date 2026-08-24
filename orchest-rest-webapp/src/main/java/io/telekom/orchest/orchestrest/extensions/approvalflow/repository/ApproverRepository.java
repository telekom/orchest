package io.telekom.orchest.orchestrest.extensions.approvalflow.repository;

import io.telekom.orchest.orchestrest.extensions.approvalflow.model.Approver;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/** MongoDB repository for managing approver configurations per process definition. */
@Component
@RequiredArgsConstructor
public class ApproverRepository {

  private final MongoTemplate mongoTemplate;

  public Optional<Approver> findApproverByProcessId(String processId) {
    Approver approver =
        mongoTemplate.findOne(
            Query.query(Criteria.where("processId").is(processId)), Approver.class);
    if (approver != null) {
      return Optional.of(approver);
    }
    return Optional.empty();
  }

  public List<Approver> findAllDefinitionsForApprover(String approver) {
    return mongoTemplate.find(
        Query.query(Criteria.where("approvers").is(approver)), Approver.class);
  }

  public Optional<Approver> findApproverById(String approverId) {
    return Optional.ofNullable(mongoTemplate.findById(approverId, Approver.class));
  }

  public Approver save(Approver approver) {
    return mongoTemplate.save(approver);
  }
}
