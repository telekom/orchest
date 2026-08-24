package io.telekom.cache.blocking;

import static org.assertj.core.api.Assertions.assertThat;

import io.telekom.orchest.cache.blocking.GenericMongoCache;
import io.telekom.orchest.cache.core.CacheDefinition;
import io.telekom.orchest.cache.core.CacheHooks;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** Tests for {@link GenericMongoCache} covering refresh, fail-safe, concurrency, and hooks. */
class GenericMongoCacheTest {

  record Item(String id, String value) {}

  private CacheDefinition<String, Item, Item> defWith(AtomicInteger loadCount, List<Item> data) {
    return CacheDefinition.<String, Item, Item>builder()
        .name("items")
        .loader(
            () -> {
              loadCount.incrementAndGet();
              return data;
            })
        .keyExtractor(Item::id)
        .refreshInterval(Duration.ofMinutes(1))
        .failSafe(true)
        .build();
  }

  @Test
  void readsReflectLatestSnapshotAfterRefresh() {
    AtomicInteger loads = new AtomicInteger();
    List<Item> initial = List.of(new Item("a", "1"), new Item("b", "2"));
    GenericMongoCache<String, Item, Item> cache =
        new GenericMongoCache<>(defWith(loads, initial), Runnable::run);

    cache.refreshBlocking();
    assertThat(cache.size()).isEqualTo(2);
    assertThat(cache.get("a")).contains(new Item("a", "1"));
  }

  @Test
  void failingRefreshRetainsPreviousSnapshot() {
    AtomicInteger calls = new AtomicInteger();
    CacheDefinition<String, Item, Item> def =
        CacheDefinition.<String, Item, Item>builder()
            .name("items")
            .loader(
                () -> {
                  int n = calls.incrementAndGet();
                  if (n == 1) return List.of(new Item("a", "v"));
                  throw new RuntimeException("boom");
                })
            .keyExtractor(Item::id)
            .failSafe(true)
            .build();

    GenericMongoCache<String, Item, Item> cache = new GenericMongoCache<>(def, Runnable::run);

    cache.refreshBlocking();
    assertThat(cache.size()).isEqualTo(1);

    cache.refreshBlocking();
    // Previous snapshot preserved despite the failure.
    assertThat(cache.size()).isEqualTo(1);
    assertThat(cache.get("a")).isPresent();
  }

  @Test
  void readsAreNonBlockingDuringRefresh() throws Exception {
    CountDownLatch loaderEntered = new CountDownLatch(1);
    CountDownLatch loaderRelease = new CountDownLatch(1);
    List<Item> seed = List.of(new Item("a", "v0"));

    CacheDefinition<String, Item, Item> def =
        CacheDefinition.<String, Item, Item>builder()
            .name("items")
            .loader(
                () -> {
                  loaderEntered.countDown();
                  try {
                    loaderRelease.await(5, TimeUnit.SECONDS);
                  } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                  }
                  return List.of(new Item("a", "v1"));
                })
            .keyExtractor(Item::id)
            .build();

    GenericMongoCache<String, Item, Item> cache = new GenericMongoCache<>(def, Runnable::run);

    // Seed the cache through reflection-free path: evict to empty, then do a synchronous
    // load of initial data via a one-shot loader. Simpler: call reloadOnDemand with
    // a different factory. Easiest: just invoke another cache with a non-blocking loader
    // first to establish v0.
    CacheDefinition<String, Item, Item> seedDef =
        CacheDefinition.<String, Item, Item>builder()
            .name("items")
            .loader(() -> seed)
            .keyExtractor(Item::id)
            .build();
    GenericMongoCache<String, Item, Item> warm = new GenericMongoCache<>(seedDef, Runnable::run);
    warm.refreshBlocking();
    assertThat(warm.get("a")).contains(new Item("a", "v0"));

    // Kick a blocking refresh from another thread; the loader waits on the latch.
    ExecutorService exec = Executors.newSingleThreadExecutor();
    exec.submit(cache::refreshBlocking);
    assertThat(loaderEntered.await(2, TimeUnit.SECONDS)).isTrue();

    // Reads return immediately (no lock contention, empty snapshot for this instance).
    long before = System.nanoTime();
    cache.get("a");
    cache.getAll();
    cache.size();
    long elapsedMs = (System.nanoTime() - before) / 1_000_000;
    assertThat(elapsedMs).isLessThan(50);

    loaderRelease.countDown();
    exec.shutdown();
    assertThat(exec.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
    assertThat(cache.get("a")).contains(new Item("a", "v1"));
  }

  @Test
  void overlappingRefreshesAreSerialized() throws Exception {
    AtomicInteger concurrentEntries = new AtomicInteger();
    AtomicInteger maxConcurrent = new AtomicInteger();
    AtomicInteger totalLoads = new AtomicInteger();

    CacheDefinition<String, Item, Item> def =
        CacheDefinition.<String, Item, Item>builder()
            .name("items")
            .loader(
                () -> {
                  int now = concurrentEntries.incrementAndGet();
                  maxConcurrent.accumulateAndGet(now, Math::max);
                  totalLoads.incrementAndGet();
                  try {
                    Thread.sleep(50);
                  } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                  }
                  concurrentEntries.decrementAndGet();
                  return List.of(new Item("a", "v"));
                })
            .keyExtractor(Item::id)
            .build();

    ExecutorService exec = Executors.newFixedThreadPool(8);
    GenericMongoCache<String, Item, Item> cache = new GenericMongoCache<>(def, exec);

    CountDownLatch start = new CountDownLatch(1);
    for (int i = 0; i < 20; i++) {
      exec.submit(
          () -> {
            try {
              start.await();
            } catch (InterruptedException ignored) {
            }
            cache.refreshBlocking();
          });
    }
    start.countDown();
    exec.shutdown();
    assertThat(exec.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

    assertThat(maxConcurrent.get()).isEqualTo(1);
    assertThat(totalLoads.get()).isGreaterThan(0);
  }

  @Test
  void hooksFireAroundRefresh() {
    Set<String> events = ConcurrentHashMap.newKeySet();
    CacheHooks<String, Item> hooks =
        new CacheHooks<>() {
          @Override
          public void beforeRefresh(String n) {
            events.add("before");
          }

          @Override
          public void afterRefresh(String n, Map<String, Item> s, long d) {
            events.add("after:" + s.size());
          }

          @Override
          public void onRefreshError(String n, Throwable t) {
            events.add("error");
          }
        };

    CacheDefinition<String, Item, Item> def =
        CacheDefinition.<String, Item, Item>builder()
            .name("items")
            .loader(() -> List.of(new Item("a", "1"), new Item("b", "2")))
            .keyExtractor(Item::id)
            .hooks(hooks)
            .build();

    GenericMongoCache<String, Item, Item> cache = new GenericMongoCache<>(def, Runnable::run);
    cache.refreshBlocking();

    assertThat(events).contains("before", "after:2");
    assertThat(events).doesNotContain("error");
  }

  @Test
  void filterAndTransformAreApplied() {
    CacheDefinition<String, Item, String> def =
        CacheDefinition.<String, Item, String>builder()
            .name("items")
            .loader(() -> List.of(new Item("a", "keep"), new Item("b", "drop")))
            .filter(i -> !i.value().equals("drop"))
            .transform(i -> i.id() + ":" + i.value())
            .keyExtractor(v -> v.split(":")[0])
            .build();

    GenericMongoCache<String, Item, String> cache = new GenericMongoCache<>(def, Runnable::run);
    cache.refreshBlocking();

    assertThat(cache.size()).isEqualTo(1);
    assertThat(cache.get("a")).contains("a:keep");
    assertThat(cache.get("b")).isEmpty();
  }

  @Test
  void evictClearsSnapshotUntilNextRefresh() {
    GenericMongoCache<String, Item, Item> cache =
        new GenericMongoCache<>(
            defWith(new AtomicInteger(), List.of(new Item("a", "1"))), Runnable::run);
    cache.refreshBlocking();
    assertThat(cache.size()).isEqualTo(1);

    cache.evict();
    assertThat(cache.size()).isZero();

    cache.refreshBlocking();
    assertThat(cache.size()).isEqualTo(1);
  }
}
