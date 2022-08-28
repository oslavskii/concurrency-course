package course.concurrency.m3_shared.collections;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toUnmodifiableSet;

public class RestaurantService {

    private final ConcurrentHashMap<String, Long> stat = new ConcurrentHashMap<>();
    private Restaurant mockRestaurant = new Restaurant("A");

    public Restaurant getByName(String restaurantName) {
        addToStat(restaurantName);
        return mockRestaurant;
    }

    public void addToStat(String restaurantName) {
        stat.merge(restaurantName, 1L, (oldValue, newValue) -> oldValue + 1L);
    }

    public Set<String> printStat() {
        return stat.entrySet()
                .stream()
                .map(it -> it.getKey() + " - " + it.getValue())
                .collect(toUnmodifiableSet());
    }
}
