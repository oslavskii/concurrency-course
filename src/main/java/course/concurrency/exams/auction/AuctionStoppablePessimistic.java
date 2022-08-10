package course.concurrency.exams.auction;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private static final Bid DEFAULT_BID = new Bid(0L, 0L, 0L);

    private Notifier notifier;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid = DEFAULT_BID;
    private volatile boolean finished = false;

    public boolean propose(Bid bid) {
        if (finished)
            return false;

        if (bid.price > latestBid.price) {
            var outdated =  proposeExclusively(bid);
            if (outdated != null) {
                notifier.sendOutdatedMessage(outdated);
                return true;
            }
        }
        return false;
    }

    private synchronized Bid proposeExclusively(Bid bid) {
        var outdated = latestBid;
        if (bid.price > outdated.price) {
            latestBid = bid;
            return outdated;
        }
        return null;
    }

    public Bid getLatestBid() {
        return latestBid;
    }

    public Bid stopAuction() {
        finished = true;
        return latestBid;
    }
}
