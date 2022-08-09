package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicReference<Bid> latestBid = new AtomicReference(null);

    public boolean propose(Bid bid) {
        Bid latest;
        do {
            latest = latestBid.get();
            if (latest != null && bid.price <= latest.price)
                return false;
        } while (!latestBid.compareAndSet(latest, bid));
        if (latest != null)
            notifier.sendOutdatedMessage(latest);
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
