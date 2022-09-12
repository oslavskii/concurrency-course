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
        var order = currentOrders.get(orderId);
        order.setPaymentInfo(paymentInfo);
        if (order.readyForDelivery()) deliver(order);
    }

    public void setPacked(long orderId) {
        var order = currentOrders.get(orderId);
        order.setPacked(true);
        if (order.readyForDelivery()) deliver(order);
    }

    private synchronized void deliver(Order order) {
        if (order.isDelivered()) return;
        /* ... */
        order.setStatus(Order.Status.DELIVERED);
    }

    public synchronized boolean isDelivered(long orderId) {
        return currentOrders.get(orderId).isDelivered();
    }
}
