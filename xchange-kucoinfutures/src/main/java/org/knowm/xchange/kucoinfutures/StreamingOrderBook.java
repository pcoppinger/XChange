package org.knowm.xchange.kucoinfutures;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.kucoinfutures.dto.response.OrderBookResponse;
import org.knowm.xchange.kucoinfutures.dto.ws.event.OrderBookEvent;
import org.knowm.xchange.kucoinfutures.dto.ws.event.OrderChangeEvent;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.knowm.xchange.dto.Order.OrderType.BID;

public class StreamingOrderBook extends OrderBook {

    private final Exchange exchange;
    private final KucoinMarketDataServiceRaw service;

    private final Instrument instrument;
    private final CurrencyPairMetaData cpmd;

    private final ArrayList<OrderBookEvent> cache = new ArrayList<>();

    private final AtomicLong sequence = new AtomicLong(-1L);

    private final ConcurrentSkipListMap<BigDecimal, LimitOrder> bids
            = new ConcurrentSkipListMap<>((o1, o2) -> Math.negateExact(o1.compareTo(o2)));
    private final ConcurrentSkipListMap<BigDecimal, LimitOrder> asks
            = new ConcurrentSkipListMap<>(BigDecimal::compareTo);

    private final HashMap<String, Function<OrderChangeEvent.Data, OrderBook>> changeHandlers = new HashMap<>();

    StreamingOrderBook(final Instrument instrument, Exchange exchange) {
        changeHandlers.put("open", this::open);
        changeHandlers.put("match", this::match);
        changeHandlers.put("filled", this::filled);
        changeHandlers.put("canceled", this::canceled);
        changeHandlers.put("update", this::update);

        this.instrument = instrument;
        this.exchange = exchange;
        this.cpmd = exchange.getExchangeMetaData().getCurrencyPairs().get(instrument);

        this.service = (KucoinMarketDataServiceRaw) exchange.getMarketDataService();
    }

    private void clear() {
        bids.clear();
        asks.clear();
        sequence.set(-1L);
        cache.clear();
    }

    private void bft(List<List<String>> list, Consumer<? super List<String>> action) {

        class Node {
            final int lo;
            final int hi;
            Node (int lo, int hi) {
                this.lo = lo;
                this.hi = hi;
            }
        }

        final Queue<Node> queue = new LinkedList<>();
        queue.add(new Node(0, list.size() - 1));
        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if (node.lo <= node.hi) {
                int mid = (node.lo + node.hi) >>> 1;
                action.accept(list.get(mid));
                queue.add(new Node(node.lo, mid - 1));
                queue.add(new Node(mid + 1, node.hi));
            }
        }
    }

    private void put(Order.OrderType side, List<String> entry) {
        LimitOrder order = KucoinAdapters.adaptLimitOrder(
                instrument, cpmd, side, new KucoinAdapters.PriceAndSize(entry), getTimeStamp());
        put(side, order);
    }

    private void put(Order.OrderType side, LimitOrder order) {
        ((side == BID) ? bids : asks).put(order.getLimitPrice(), order);
    }

    private void remove(Order.OrderType side, LimitOrder order) {
        ((side == BID) ? bids : asks).remove(order.getLimitPrice());
    }

    @Override
    public void update(LimitOrder order) {
        updateDate(order.getTimestamp());
        if (order.getOriginalAmount().compareTo(BigDecimal.ZERO) > 0) {
            put(order.getType(), order);
        } else {
            remove(order.getType(), order);
        }
    }

    public void initialize() {
        CompletableFuture.supplyAsync(() -> {
            try {
                OrderBookResponse res = service.getKucoinOrderBookFull(instrument);
                if (res != null) {
                    updateDate(new Date(res.getTime()));

                    bft(res.getBids(), entry -> put(BID, entry));
                    bft(res.getAsks(), entry -> put(ASK, entry));

                    synchronized(cache) {
                        sequence.set(res.getSequence());
                        cache.stream()
                                .filter(event -> sequence.compareAndSet(event.data.sequence - 1, event.data.sequence))
                                .map(event -> KucoinStreamingAdapters.toLimitOrder(exchange, instrument, event))
                                .forEach(this::update);
                        cache.clear();
                    }
                }
            } catch (IOException ex) {
                clear();
            }
            return this;
        });
    }

    /** OrderChangeEvent Handlers. */
    public OrderBook event(OrderChangeEvent event) {
        Function<OrderChangeEvent.Data, OrderBook> handler = changeHandlers.get(event.data.type);
        return (handler != null) ? handler.apply(event.data) : this;
    }

    private OrderBook open(OrderChangeEvent.Data data) {
        return this;
    }

    private OrderBook match(OrderChangeEvent.Data data) {
        return this;
    }

    private OrderBook filled(OrderChangeEvent.Data data) {
        return this;
    }

    private OrderBook canceled(OrderChangeEvent.Data data) {
        return this;
    }

    private OrderBook update(OrderChangeEvent.Data data) {
        return this;
    }

    /** OrderBookEvent Handler. */
    public OrderBook event(OrderBookEvent event) {
        synchronized(cache) {
            if (cache.size() == 0) {
                if (sequence.compareAndSet(event.data.sequence - 1, event.data.sequence)) {
                    update(KucoinStreamingAdapters.toLimitOrder(exchange, instrument, event));
                } else {
                    clear();
                    cache.add(event);
                    initialize();
                }
            } else {
                cache.add(event);
            }
        }
        return this;
    }

    public BigDecimal getBestPrice(Order.OrderType side) {
        final Map.Entry<BigDecimal, LimitOrder> entry = ((side == BID) ? bids : asks).firstEntry();
        return entry != null ? entry.getValue().getLimitPrice() : BigDecimal.ZERO;
    }

    public int getDepth(Order.OrderType side) {
        return ((side == BID) ? bids : asks).size();
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public BigDecimal getVolume(Order.OrderType side, int depth) {
        BigDecimal volume = BigDecimal.ZERO.setScale(cpmd.getBaseScale(), RoundingMode.HALF_EVEN);
        final Iterator<LimitOrder> iter = ((side == BID) ? bids : asks).values().iterator();
        while (iter.hasNext() && depth-- > 0) {
            volume = volume.add(iter.next().getRemainingAmount());
        }
        return volume;
    }

    @Override
    public String toString() {

        return "OrderBook [timestamp: "
                + getTimeStamp()
                + ", asks="
                + asks.size()
                + ", bids="
                + bids.size()
                + "]";
    }
}
