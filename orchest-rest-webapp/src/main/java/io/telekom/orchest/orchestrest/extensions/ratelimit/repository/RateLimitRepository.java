package io.telekom.orchest.orchestrest.extensions.ratelimit.repository;

import io.telekom.orchest.orchestrest.extensions.ratelimit.model.RateLimit;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/** MongoDB repository for rate-limit configurations. */
@Component
@RequiredArgsConstructor
public class RateLimitRepository {

  private final MongoTemplate mongoTemplate;

  public Optional<RateLimit> findByProcessId(String processId) {
    return Optional.ofNullable(
        mongoTemplate.findOne(
            Query.query(Criteria.where("processId").is(processId)), RateLimit.class));
  }

  public List<RateLimit> findAll() {
    return mongoTemplate.findAll(RateLimit.class);
  }

  public RateLimit save(RateLimit rateLimit) {
    return mongoTemplate.save(rateLimit);
  }
}
