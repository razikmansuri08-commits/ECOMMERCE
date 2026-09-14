package com.rmtech.ecom.DTOS;

public record OrderCreatedEvent(
        String orderId,
        Long userId,
        String username,
        Double totalAmount
) {
}
