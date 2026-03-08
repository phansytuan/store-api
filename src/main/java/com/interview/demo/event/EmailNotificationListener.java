package com.interview.demo.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * OBSERVER PATTERN – Listener 1: Email Notification
 *
 * @Async: chạy bất đồng bộ, không block request của user.
 * Cần @EnableAsync trên DemoApplication.
 */
@Slf4j
@Component
public class EmailNotificationListener {

    @EventListener
    @Async
    public void onOrderCreated(OrderCreatedEvent event) {
        // Giả lập gửi email (thực tế inject EmailService)
        log.info("[EMAIL] Sending order confirmation to: {} for order #{}",
                event.getUser().getEmail(),
                event.getOrder().getOrderCode());
        log.info("[EMAIL] Email sent successfully!");
    }
}
