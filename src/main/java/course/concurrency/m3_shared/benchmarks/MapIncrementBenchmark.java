package course.concurrency.m3_shared.benchmarks;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class MapIncrementBenchmark {

    private static final int THREADS = 1;
    private static final int KEYS = 1000;

    private final Map<String, Long> longMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> atomicLongMap = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> longAdderMap = new ConcurrentHashMap<>();

    private static String getKey() {
        return "key" + ThreadLocalRandom.current().nextInt(KEYS);
    }

    @Setup(Level.Iteration)
    public void doSetup() {
        longMap.clear();
        atomicLongMap.clear();
        longAdderMap.clear();
    }

    @Benchmark
    @Threads(THREADS)
    public void incrementLongMap() {
        var key = getKey();
        longMap.merge(key, 1L, (oldValue, newValue) -> oldValue + 1L);
    }

    @Benchmark
    @Threads(THREADS)
    public void incrementAtomicLongMap() {
        var key = getKey();
        atomicLongMap.putIfAbsent(key, new AtomicLong());
        atomicLongMap.get(key).incrementAndGet();
    }

    @Benchmark
    @Threads(THREADS)
    public void incrementLongAdderMap() {
        var key = getKey();
        longAdderMap.putIfAbsent(key, new LongAdder());
        longAdderMap.get(key).increment();
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(MapIncrementBenchmark.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
