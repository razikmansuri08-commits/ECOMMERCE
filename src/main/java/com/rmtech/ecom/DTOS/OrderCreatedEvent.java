package com.rmtech.ecom.DTOS;

public record OrderCreatedEvent(
        String orderId,
        Long userId,
        String username,
        String email,
        Double totalAmount,
        java.time.LocalDateTime createdAt
) {
    public OrderCreatedEvent {
        if (createdAt == null) {
            createdAt = java.time.LocalDateTime.now();
        }
    }
}
