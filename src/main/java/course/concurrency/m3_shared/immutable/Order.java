package course.concurrency.m3_shared.immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static course.concurrency.m3_shared.immutable.Order.Status.NEW;

public class Order {

    public enum Status { NEW, IN_PROGRESS, DELIVERED }

    private final Long id;
    private final List<Item> items;
    private volatile PaymentInfo paymentInfo;
    private volatile boolean isPacked;
    private volatile Status status;

    public Order(Long id, List<Item> items) {
        this.id = id;
        this.items = new ArrayList<>(items);
        this.status = NEW;
    }

    public boolean readyForDelivery() {
        return paymentInfo != null && isPacked;
    }

    public Long getId() {
        return id;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(PaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
        this.status = Status.IN_PROGRESS;
    }

    public boolean isPacked() {
        return isPacked;
    }

    public void setPacked(boolean packed) {
        isPacked = packed;
        this.status = Status.IN_PROGRESS;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isDelivered() {
        return status.equals(Order.Status.DELIVERED);
    }
}
