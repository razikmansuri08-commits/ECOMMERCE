package com.rmtech.ecom.eventlisteners;

import com.rmtech.ecom.DTOS.OrderCreatedEvent;
import com.rmtech.ecom.Service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class OrderNotificationListener {
    private final EmailService emailService;

    public OrderNotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            log.info("Processing order confirmation email for order: {}, user: {}", 
                    event.orderId(), event.username());
            
            emailService.sendOrderConfirmation(
                    event.email(),
                    event.orderId(),
                    event.totalAmount()
            );
            
            log.info("Order confirmation email sent successfully for order: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order: {}", 
                    event.orderId(), e);
            // Log the error but don't rethrow - the order is already placed
            // In production, you might want to queue this for retry
        }
    }
}
