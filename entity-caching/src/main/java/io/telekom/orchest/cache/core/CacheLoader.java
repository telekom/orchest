package io.telekom.orchest.cache.core;

import java.util.Collection;

/**
 * Strategy for fetching the full set of entities to cache.
 *
 * <p>The library deliberately does not depend on a specific MongoTemplate instance. Host
 * applications supply a loader that closes over whichever template (blocking or reactive) they
 * already configure.
 *
 * @param <E> source entity type as stored in Mongo
 */
@FunctionalInterface
public interface CacheLoader<E> {
  /**
   * Loads all entities from the backing data source.
   *
   * @return the full collection of entities to cache
   */
  Collection<E> load();
}
