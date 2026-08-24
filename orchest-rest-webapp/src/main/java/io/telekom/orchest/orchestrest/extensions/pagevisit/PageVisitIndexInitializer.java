package io.telekom.orchest.orchestrest.extensions.pagevisit;

import io.telekom.orchest.orchestrest.extensions.pagevisit.model.PageVisit;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

/** Creates required MongoDB indexes for the PageVisit collection on application startup. */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "orchest.page-visits", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PageVisitIndexInitializer {

  private final MongoTemplate mongoTemplate;

  @PostConstruct
  public void ensureIndexes() {
    IndexOperations ops = mongoTemplate.indexOps(PageVisit.class);

    // Unique constraint: one document per (url, userId, date) — drives the upsert deduplication
    ops.createIndex(
        new Index()
            .on("url", Sort.Direction.ASC)
            .on("userId", Sort.Direction.ASC)
            .on("date", Sort.Direction.ASC)
            .unique()
            .named("url_user_date_unique"));

    // Query index: fast $match on date + url for the aggregations
    ops.createIndex(
        new Index().on("date", Sort.Direction.ASC).on("url", Sort.Direction.ASC).named("date_url"));

    log.info("PageVisit indexes ensured");
  }
}
