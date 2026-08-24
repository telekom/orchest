package io.telekom.orchest.orchestrest.extensions.approvalflow.repository;

import io.telekom.orchest.orchestrest.extensions.approvalflow.DeploymentApprovalState;
import io.telekom.orchest.orchestrest.extensions.approvalflow.model.DeploymentApproval;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

/** MongoDB repository for deployment approval requests with state-based querying. */
@Component
@RequiredArgsConstructor
public class DeploymentApprovalRepository {

  private final MongoTemplate mongoTemplate;

  public List<DeploymentApproval> findAllByReviewers(List<String> reviewers) {
    return mongoTemplate.find(
        Query.query(Criteria.where("reviewers").in(reviewers)), DeploymentApproval.class);
  }

  public Page<DeploymentApproval> findDeploymentApprovalsByState(
      DeploymentApprovalState state, Pageable pageable) {
    Query query = Query.query(Criteria.where("state").is(state));
    List<DeploymentApproval> deploymentApprovals =
        mongoTemplate.find(query.with(pageable), DeploymentApproval.class);
    return PageableExecutionUtils.getPage(
        deploymentApprovals, pageable, () -> mongoTemplate.count(query, DeploymentApproval.class));
  }

  public List<DeploymentApproval> findAllByReviewersAndStateIn(
      List<String> reviewers, Collection<DeploymentApprovalState> states) {
    Query query = Query.query(Criteria.where("reviewers").in(reviewers).and("state").in(states));
    return mongoTemplate.find(query, DeploymentApproval.class);
  }

  public Optional<DeploymentApproval> findById(String deploymentRequestId) {
    DeploymentApproval byId = mongoTemplate.findById(deploymentRequestId, DeploymentApproval.class);
    if (byId == null) {
      return Optional.empty();
    }
    return Optional.of(byId);
  }

  public void save(DeploymentApproval deploymentApproval) {
    mongoTemplate.save(deploymentApproval);
  }

  public Page<DeploymentApproval> findAll(PageRequest pageRequest) {
    List<DeploymentApproval> deploymentApprovals =
        mongoTemplate.find(new Query(), DeploymentApproval.class);
    return PageableExecutionUtils.getPage(
        deploymentApprovals,
        pageRequest,
        () -> mongoTemplate.count(new Query(), DeploymentApproval.class));
  }
}
