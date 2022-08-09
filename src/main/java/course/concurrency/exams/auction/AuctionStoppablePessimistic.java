package course.concurrency.exams.auction;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid;
    private volatile boolean finished = false;

    public synchronized boolean propose(Bid bid) {
        if (finished)
            return false;
        if (latestBid == null || bid.price > latestBid.price) {
            if (latestBid != null)
                notifier.sendOutdatedMessage(latestBid);
            latestBid = bid;
            return true;
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }

    public Bid stopAuction() {
        finished = true;
        return latestBid;
    }
}
