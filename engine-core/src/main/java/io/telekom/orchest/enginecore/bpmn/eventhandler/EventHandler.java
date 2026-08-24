package io.telekom.orchest.enginecore.bpmn.eventhandler;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Generic interface for handling events in the OrchesT Engine.
 *
 * @param <T> The type of event to handle.
 * @param <R> The type of execution return object.
 */
public interface EventHandler<T, R> {

  R execute(T request);

  default void handle(T request) {
    handle(request, t -> {}, (t, r) -> {}, (t, throwable) -> {});
  }

  default void handle(T request, Consumer<T> preProcessor) {
    handle(request, preProcessor, (t, r) -> {}, (t, throwable) -> {});
  }

  default void handle(T request, BiConsumer<T, R> postProcessor) {
    handle(request, t -> {}, postProcessor, (t, throwable) -> {});
  }

  default void handle(T request, Consumer<T> preProcessor, BiConsumer<T, R> postProcessor) {
    handle(request, preProcessor, postProcessor, (t, throwable) -> {});
  }

  default void handle(
      T request,
      Consumer<T> preProcessor,
      BiConsumer<T, R> postProcessor,
      BiConsumer<T, Throwable> afterCompletion) {

    Throwable throwable = null;

    try {
      preProcessor.accept(request);
      R response = execute(request);
      postProcessor.accept(request, response);
    } catch (Throwable ex) {
      throwable = ex;
      throw ex;
    } finally {
      afterCompletion.accept(request, throwable);
    }
  }
}
