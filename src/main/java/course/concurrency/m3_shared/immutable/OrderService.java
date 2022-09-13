package course.concurrency.m3_shared.immutable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class OrderService {

    private final Map<Long, Order> currentOrders = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(0L);

    private long nextId() {
        return nextId.getAndIncrement();
    }

    public long createOrder(List<Item> items) {
        if (items == null || items.isEmpty()) throw new NullPointerException();
        long id = nextId();
        Order order = new Order(id, items);
        currentOrders.put(id, order);
        return id;
    }

    public void updatePaymentInfo(long orderId, PaymentInfo paymentInfo) {
        Order updated = currentOrders.computeIfPresent(orderId,
                (key, oldValue) -> oldValue.withPaymentInfo(paymentInfo));
        if (updated != null && updated.readyForDelivery()) deliver(updated);
    }

    public void setPacked(long orderId) {
        Order updated = currentOrders.computeIfPresent(orderId,
                (key, oldValue) -> oldValue.withPacked(true));
        if (updated != null && updated.readyForDelivery()) deliver(updated);
    }

    private void deliver(Order order) {
        /* ... */
        currentOrders.put(order.getId(), order.withStatus(Order.Status.DELIVERED));
    }

    public boolean isDelivered(long orderId) {
        return currentOrders.get(orderId).isDelivered();
    }
}
