package io.telekom.orchest.scheduling.runtime;

import io.telekom.orchest.scheduling.api.TaskHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Maps a task {@code type} to its {@link TaskHandler}. Registration is one-shot at startup —
 * dispatchers look up handlers on every dispatch, so this is on the hot path and must be cheap and
 * lock-free.
 */
@Slf4j
public class TaskHandlerRegistry {

  private final Map<String, TaskHandler> handlers = new HashMap<>();
  private volatile Map<String, TaskHandler> view = Collections.emptyMap();

  /**
   * Register a handler for a logical task type. Throws if the type is already registered — silent
   * overwrites have caused production bugs in similar systems.
   */
  public synchronized TaskHandlerRegistry register(String type, TaskHandler handler) {
    if (handlers.putIfAbsent(type, handler) != null) {
      throw new IllegalStateException("Duplicate handler registered for task type: " + type);
    }
    view = Map.copyOf(handlers);
    log.info("Registered task handler for type='{}'", type);
    return this;
  }

  /** Auto-register a handler that exposes its own type via {@link TaskHandler#type()}. */
  public TaskHandlerRegistry register(TaskHandler handler) {
    return register(handler.type(), handler);
  }

  /**
   * Look up the handler for the given task type.
   *
   * @param type logical task type
   * @return the registered handler, or empty if none is registered
   */
  public Optional<TaskHandler> find(String type) {
    return Optional.ofNullable(view.get(type));
  }
}
