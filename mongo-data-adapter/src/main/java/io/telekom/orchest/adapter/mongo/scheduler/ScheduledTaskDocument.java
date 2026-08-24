package io.telekom.orchest.adapter.mongo.scheduler;

import io.telekom.orchest.scheduling.api.TaskState;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Mongo persistence model for a {@link io.telekom.orchest.scheduling.api.ScheduledTask}.
 *
 * <p>Schema-light by design: the {@code payload} is stored opaque so that adding new task types
 * never requires a migration. Indexes are declared in {@code
 * MongoIndicesConfiguration#createScheduledTaskIndexes()}.
 *
 * <p>The {@code @Version} field is the optimistic-concurrency token used by the runtime to detect
 * lease theft; Spring Data bumps it on every {@code save}, but the lease/complete/fail paths in
 * {@link MongoScheduledTaskStore} use {@code findAndModify} with an explicit {@code version ==
 * expected} guard so they remain safe even when the mapper is bypassed.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "scheduledTasks")
public class ScheduledTaskDocument {

  @Id private String id;

  private String type;
  private String businessKey;
  private String payload;

  private Instant triggerAt;

  private TaskState state;

  private int attempts;
  private int maxAttempts;

  private String ownerId;
  private Instant leaseUntil;

  private String lastError;

  @Version private Long version;

  private Instant createdAt;
  private Instant updatedAt;
}
