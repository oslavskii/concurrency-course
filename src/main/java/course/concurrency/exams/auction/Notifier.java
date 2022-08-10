package course.concurrency.exams.auction;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Notifier {

    private final ExecutorService pool = Executors.newWorkStealingPool(2);

    public void sendOutdatedMessage(Bid bid) {
        CompletableFuture.runAsync(this::imitateSending, pool);
    }

    private void imitateSending() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}
    }

    public void shutdown() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(100, MILLISECONDS))
                pool.shutdownNow();
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
