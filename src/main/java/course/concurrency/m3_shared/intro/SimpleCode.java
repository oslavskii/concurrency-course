package course.concurrency.m3_shared.intro;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
public class SimpleCode {

    private void run() {
        final int size = 50_000_000;
        Object[] objects = new Object[size];
        for (int i = 0; i < size; ++i) {
            objects[i] = new Object();
        }
    }

    @Benchmark
    @Fork(jvmArgsAppend = {"-XX:+UseTLAB"})
    @Threads(1)
    public void allocateWithTLAB() {
        run();
    }

    @Benchmark
    @Fork(jvmArgsAppend = {"-XX:-UseTLAB"})
    @Threads(1)
    public void allocateWithoutTLAB() {
        run();
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(SimpleCode.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();

    }
}
