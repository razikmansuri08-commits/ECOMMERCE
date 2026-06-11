package com.rmtech.ecom.DTOS;

import com.rmtech.ecom.Entities.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusDto {
    private String orderId;
    private OrderStatus status;
}
