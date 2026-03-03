package com.project.oms.scheduler;

import com.project.oms.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusScheduler.class);

    private final OrderService orderService;

    public OrderStatusScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedRate = 15 * 1000)
    public void promotePendingOrders() {
        int updatedCount = orderService.autoPromotePendingToProcessing();
        if (updatedCount > 0) {
            log.info("Auto-promoted {} orders from PENDING to PROCESSING", updatedCount);
        }
    }
}

