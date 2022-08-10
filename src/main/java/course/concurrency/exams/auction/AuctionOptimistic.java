package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private static final Bid DEFAULT_BID = new Bid(0L, 0L, 0L);

    private Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicReference<Bid> latestBid = new AtomicReference(DEFAULT_BID);

    public boolean propose(Bid bid) {
        Bid latest;
        do {
            latest = latestBid.get();
            if (bid.price <= latest.price)
                return false;
        } while (!latestBid.compareAndSet(latest, bid));
        notifier.sendOutdatedMessage(latest);
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
