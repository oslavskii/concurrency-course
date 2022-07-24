package course.concurrency.m2_async.cf.min_price;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.lang.Double.NaN;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

public class PriceAggregator {

    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        var slaInMillis = 2900;
        var pricesCF = shopIds.stream()
                .map(shopId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return priceRetriever.getPrice(itemId, shopId);
                    } catch (Exception ex) {
                        return NaN;
                    }
                }))
                .collect(toList());
        return CompletableFuture.allOf(pricesCF.toArray(CompletableFuture[]::new))
                .completeOnTimeout(null, slaInMillis, MILLISECONDS)
                .thenApply(v -> pricesCF.stream()
                        .filter(CompletableFuture::isDone)
                        .map(CompletableFuture::join)
                        .filter(it -> !it.isNaN())
                        .mapToDouble(it -> it)
                        .min()
                        .orElse(NaN)

                ).join();
    }
}
