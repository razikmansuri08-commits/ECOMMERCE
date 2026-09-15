package com.rmtech.ecom.eventlisteners;

import com.rmtech.ecom.DTOS.OrderCreatedEvent;
import com.rmtech.ecom.DTOS.OrderStatusUpdatedEvent;
import com.rmtech.ecom.Entities.AuditLog.AuditAction;
import com.rmtech.ecom.Service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class AuditEventListener {

    private final AuditLogService auditLogService;

    public AuditEventListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Async("auditExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            log.debug("Auditing order creation: {}", event.orderId());

            auditLogService.logAsync(
                    AuditAction.PLACE_ORDER,
                    "Order",
                    event.orderId(),
                    String.format("Order placed by %s. Total amount: ₹%.2f", 
                            event.username(), event.totalAmount()),
                    event.username(),
                    true,
                    null
            );

            log.debug("Audit log created for order: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to create audit log for order creation: {}", 
                    event.orderId(), e);
        }
    }

    @Async("auditExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleOrderStatusUpdated(OrderStatusUpdatedEvent event) {
        try {
            log.debug("Auditing order status update: {} from {} to {}", 
                    event.orderId(), event.previousStatus(), event.newStatus());

            auditLogService.logAsync(
                    AuditAction.UPDATE_STATUS,
                    "Order",
                    event.orderId(),
                    String.format("Order status changed from %s to %s by admin",
                            event.previousStatus(), event.newStatus()),
                    event.username(),
                    true,
                    null
            );

            log.debug("Audit log created for order status update: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to create audit log for order status update: {}", 
                    event.orderId(), e);
        }
    }
}
