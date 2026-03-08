package com.interview.demo.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * OBSERVER PATTERN – Listener 2: Inventory Update
 * Độc lập với EmailListener, cùng lắng nghe OrderCreatedEvent
 */
@Slf4j
@Component
public class InventoryUpdateListener {

    @EventListener
    @Async
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("[INVENTORY] Reserving stock for order #{} with {} items",
                event.getOrder().getOrderCode(),
                event.getOrder().getItems().size());

        event.getOrder().getItems().forEach(item ->
            log.info("[INVENTORY] Reserved {} units of product '{}'",
                    item.getQuantity(),
                    item.getProduct().getName())
        );
    }
}
