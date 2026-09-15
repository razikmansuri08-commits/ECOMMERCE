package com.rmtech.ecom.DTOS;

public record OrderStatusUpdatedEvent(
        String orderId,
        Long userId,
        String username,
        String email,
        String previousStatus,
        String newStatus,
        java.time.LocalDateTime updatedAt
) {
    public OrderStatusUpdatedEvent {
        if (updatedAt == null) {
            updatedAt = java.time.LocalDateTime.now();
        }
    }
}
