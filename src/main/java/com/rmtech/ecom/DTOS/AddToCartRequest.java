package com.rmtech.ecom.DTOS;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public class AddToCartRequest {
    private int quantity;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Positive
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
