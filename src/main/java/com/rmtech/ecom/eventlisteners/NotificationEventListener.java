package com.rmtech.ecom.eventlisteners;

import com.rmtech.ecom.DTOS.OrderCreatedEvent;
import com.rmtech.ecom.DTOS.OrderStatusUpdatedEvent;
import com.rmtech.ecom.Entities.Notification.NotificationType;
import com.rmtech.ecom.Service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            log.info("Creating in-app notification for order: {}, user: {}", 
                    event.orderId(), event.username());

            notificationService.createNotification(
                    event.userId(),
                    NotificationType.ORDER_CONFIRMATION,
                    "Order Confirmed 🎉",
                    String.format("Your order #%s has been confirmed. Total: ₹%.2f", 
                            event.orderId(), event.totalAmount()),
                    "/user/orders/order/" + event.orderId()
            );

            log.info("In-app notification created for order: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to create notification for order: {}", 
                    event.orderId(), e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleOrderStatusUpdated(OrderStatusUpdatedEvent event) {
        try {
            log.info("Creating notification for order status update: {}, user: {}", 
                    event.orderId(), event.username());

            String message = getStatusMessage(event.newStatus());
            
            notificationService.createNotification(
                    event.userId(),
                    getNotificationTypeForStatus(event.newStatus()),
                    "Order Status Updated",
                    String.format("Order #%s status changed to: %s", 
                            event.orderId(), event.newStatus()),
                    "/user/orders/order/" + event.orderId()
            );

            log.info("In-app notification created for order status update: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to create notification for order status update: {}", 
                    event.orderId(), e);
        }
    }

    private String getStatusMessage(String status) {
        return switch (status) {
            case "CONFIRMED" -> "Your order has been confirmed and will be processed soon.";
            case "PROCESSING" -> "Your order is being prepared for shipment.";
            case "SHIPPED" -> "Your order has been shipped! Track your delivery.";
            case "DELIVERED" -> "Your order has been delivered. Thank you for your purchase!";
            case "CANCELLED" -> "Your order has been cancelled.";
            default -> "Order status has been updated.";
        };
    }

    private NotificationType getNotificationTypeForStatus(String status) {
        return switch (status) {
            case "CONFIRMED" -> NotificationType.ORDER_STATUS_UPDATE;
            case "PROCESSING" -> NotificationType.ORDER_STATUS_UPDATE;
            case "SHIPPED" -> NotificationType.ORDER_STATUS_UPDATE;
            case "DELIVERED" -> NotificationType.ORDER_STATUS_UPDATE;
            case "CANCELLED" -> NotificationType.ORDER_CANCELLED;
            default -> NotificationType.SYSTEM;
        };
    }
}
