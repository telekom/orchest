package io.telekom.orchest.orchestrest.extensions.pagevisit.repository;

import io.telekom.orchest.orchestrest.extensions.pagevisit.model.PageVisit;
import io.telekom.orchest.orchestrest.extensions.pagevisit.model.PageVisitStats;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

/** MongoDB repository for recording and aggregating page visit data. */
@Component
@RequiredArgsConstructor
public class PageVisitRepository {

  private final MongoTemplate mongoTemplate;

  private static final AggregationOptions DISK_USE =
      AggregationOptions.builder().allowDiskUse(true).build();

  // Upsert: insert the (url, userId, date) document only if it doesn't exist yet.
  // The compound unique index on (url, userId, date) guarantees at-most-one record per combination.
  public void recordVisit(String url, String userId, LocalDate date) {
    Query query =
        Query.query(Criteria.where("url").is(url).and("userId").is(userId).and("date").is(date));
    Update update =
        new Update()
            .setOnInsert("url", url)
            .setOnInsert("userId", userId)
            .setOnInsert("date", date);
    mongoTemplate.upsert(query, update, PageVisit.class);
  }

  public List<PageVisitStats> getUniqueUserCountPerUrl(LocalDate date) {
    Aggregation agg =
        Aggregation.newAggregation(
                Aggregation.match(Criteria.where("date").is(date)),
                Aggregation.group("url").count().as("uniqueUsers"),
                Aggregation.project("uniqueUsers").and("_id").as("url").andExclude("_id"),
                Aggregation.sort(Sort.by(Sort.Order.desc("uniqueUsers"))))
            .withOptions(DISK_USE);
    return mongoTemplate.aggregate(agg, PageVisit.class, PageVisitStats.class).getMappedResults();
  }

  public List<PageVisitStats> getUniqueUserCountForUrl(String url, LocalDate from, LocalDate to) {
    Aggregation agg =
        Aggregation.newAggregation(
                Aggregation.match(Criteria.where("url").is(url).and("date").gte(from).lte(to)),
                Aggregation.group("url", "date").count().as("uniqueUsers"),
                Aggregation.project("uniqueUsers")
                    .and("_id.url")
                    .as("url")
                    .and("_id.date")
                    .as("date")
                    .andExclude("_id"),
                Aggregation.sort(Sort.by(Sort.Order.asc("date"))))
            .withOptions(DISK_USE);
    return mongoTemplate.aggregate(agg, PageVisit.class, PageVisitStats.class).getMappedResults();
  }
}
