package com.interview.demo.event;

import com.interview.demo.entity.Order;
import com.interview.demo.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * OBSERVER PATTERN – Spring Application Events
 *
 * Publisher (OrderService) không cần biết ai lắng nghe.
 * Loose coupling – thêm listener mới không ảnh hưởng service.
 */
@Getter
public class OrderCreatedEvent extends ApplicationEvent {

    private final Order order;
    private final User  user;

    public OrderCreatedEvent(Object source, Order order, User user) {
        super(source);
        this.order = order;
        this.user  = user;
    }
}
