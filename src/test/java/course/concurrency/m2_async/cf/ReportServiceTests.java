package course.concurrency.m2_async.cf;

import course.concurrency.m2_async.cf.report.ReportServiceCF;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ReportServiceTests {
    static {
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("---");
    }

    //    private ReportServiceExecutors reportService = new ReportServiceExecutors();
    private ReportServiceCF reportService = new ReportServiceCF();

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                arguments("newCachedThreadPool", Executors.newCachedThreadPool()),
                arguments("newSingleThreadExecutor", Executors.newSingleThreadExecutor()),
                arguments("newFixedThreadPool(2)", Executors.newFixedThreadPool(2)),
                arguments("newFixedThreadPool(4)", Executors.newFixedThreadPool(4)),
                arguments("newFixedThreadPool(8)", Executors.newFixedThreadPool(8)),
                arguments("newFixedThreadPool(16)", Executors.newFixedThreadPool(16)),
                arguments("newFixedThreadPool(32)", Executors.newFixedThreadPool(32)),
                arguments("newFixedThreadPool(64)", Executors.newFixedThreadPool(64)),
                arguments("new ForkJoinPool()", new ForkJoinPool())

//                arguments("newFixedThreadPool(`16`)", Executors.newFixedThreadPool(16)),
//                arguments("newFixedThreadPool(`18`)", Executors.newFixedThreadPool(18)),
//                arguments("newFixedThreadPool(`20`)", Executors.newFixedThreadPool(20)),
//                arguments("newFixedThreadPool(`22`)", Executors.newFixedThreadPool(22)),
//                arguments("newFixedThreadPool(`24`)", Executors.newFixedThreadPool(24)),
//                arguments("newFixedThreadPool(`26`)", Executors.newFixedThreadPool(26)),
//                arguments("newFixedThreadPool(`28`)", Executors.newFixedThreadPool(28)),
//                arguments("newFixedThreadPool(`30`)", Executors.newFixedThreadPool(30)),
//                arguments("newFixedThreadPool(`32`)", Executors.newFixedThreadPool(32))
        );
    }

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    public void testMultipleTasks(String option, ExecutorService ex) throws InterruptedException {
        ReportServiceCF.executor = ex;
        int poolSize = Runtime.getRuntime().availableProcessors() * 3;
        int iterations = 5;

        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        for (int i = 0; i < poolSize; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException ignored) {}
                for (int it = 0; it < iterations; it++) {
                    reportService.getReport();
                }
            });
        }

        long start = System.currentTimeMillis();
        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
        long end = System.currentTimeMillis();

        if (ex instanceof ThreadPoolExecutor)
            System.out.println(option + " | " + ((ThreadPoolExecutor) ex).getLargestPoolSize() + " | " + (end - start));
        else
            System.out.println(option + " | " + (end - start));
    }
}
