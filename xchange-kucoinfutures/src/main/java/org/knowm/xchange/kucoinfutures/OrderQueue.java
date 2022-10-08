package org.knowm.xchange.kucoinfutures;

import org.knowm.xchange.dto.trade.LimitOrder;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OrderQueue extends ConcurrentLinkedQueue<LimitOrder> {
    private final BigDecimal price;
    private BigDecimal volume = BigDecimal.ZERO;

    private void recalculate() {
        volume = stream().map(LimitOrder::getRemainingAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Create an OrderQueue containing one LimitOrder.
     * @param order the initial order (probably spanning multiple actual orders)
     */
    public OrderQueue(LimitOrder order) {
        offer(order);
        price = order.getLimitPrice();
        volume = order.getRemainingAmount();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public BigDecimal getVolumeBefore(final String orderId) {
        BigDecimal amount = BigDecimal.ZERO;
        for (LimitOrder order : this) {
            if (orderId.equals(order.getId())) {
                break;
            }
            amount = amount.add(order.getRemainingAmount());
        }
        return amount;
    }

    @Override
    public boolean add(LimitOrder order) {
        volume = volume.add(order.getRemainingAmount());
        return super.add(order);
    }

    @Override
    public boolean addAll(Collection<? extends LimitOrder> c) {
        boolean result = super.addAll(c);
        recalculate();
        return result;
    }

    @Override
    public boolean offer(LimitOrder order) {
        volume = volume.add(order.getRemainingAmount());
        return super.offer(order);
    }

    @Override
    public LimitOrder poll() {
        LimitOrder order = super.poll();
        if (order != null) {
            volume = volume.subtract(order.getRemainingAmount());
        }
        return order;
    }

    @Override
    public LimitOrder remove() {
        LimitOrder order = super.remove();
        volume = volume.subtract(order.getRemainingAmount());
        return order;
    }

    @Override
    public void clear() {
        super.clear();
        volume = BigDecimal.ZERO;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = super.removeAll(c);
        recalculate();
        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean result = super.retainAll(c);
        recalculate();
        return result;
    }

    @Override
    public String toString() {
        return "OrderQueue [price=" + price + ", volume=" + volume + ", size=" + size() + "]";
    }
}
