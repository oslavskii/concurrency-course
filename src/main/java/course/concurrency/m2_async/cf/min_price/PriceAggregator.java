package course.concurrency.m2_async.cf.min_price;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
        var slaInMillis = 3000 - shopIds.size();
        var waitTill = System.currentTimeMillis() + slaInMillis;
        var pricesCF = shopIds.stream()
                .map(shopId -> CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shopId)))
                .collect(toList());
        return pricesCF.parallelStream()
                .map(cf -> {
                    try {
                        return cf.get(waitTill - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                    } catch (Exception ex) {
                        return Double.NaN;
                    }
                })
                .filter(it -> !it.isNaN())
                .mapToDouble(it -> it)
                .min()
                .orElse(Double.NaN);
    }
}
