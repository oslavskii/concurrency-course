package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private static final Bid DEFAULT_BID = new Bid(0L, 0L, 0L);

    private Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicMarkableReference<Bid> latestBid = new AtomicMarkableReference<>(DEFAULT_BID, false);

    public boolean propose(Bid bid) {
        Bid latest;
        do {
            latest = latestBid.getReference();
            if (bid.price <= latest.price || latestBid.isMarked())
                return false;
        } while (!latestBid.compareAndSet(latest, bid, false, false));
        notifier.sendOutdatedMessage(latest);
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.getReference();
    }

    public Bid stopAuction() {
        Bid latest;
        do {
            if (latestBid.isMarked())
                return latestBid.getReference();
            latest = latestBid.getReference();
        } while (!latestBid.attemptMark(latest, true));
        return latest;
    }
}
