package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicMarkableReference<Bid> latestBid = new AtomicMarkableReference<>(null, false);

    public boolean propose(Bid bid) {
        Bid latest;
        do {
            latest = latestBid.getReference();
            if (latest != null && bid.price <= latest.price)
                return false;
        } while (latestBid.compareAndSet(latest, bid, false, false));
        if (latest != null)
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
