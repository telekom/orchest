package io.telekom.orchest.cache.core;

import java.util.function.Function;

/**
 * Extracts the cache key from an entity. Declared as a named type (rather than plain {@code
 * Function}) so Spring bean resolution can distinguish it from generic functions.
 */
@FunctionalInterface
public interface KeyExtractor<K, V> extends Function<V, K> {}
