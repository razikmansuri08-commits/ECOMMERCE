package com.rmtech.ecom.DTOS;


import com.rmtech.ecom.Entities.Product;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order_ItemsDto
{
    private Long order_item_id;

    private String  product;


   private int quantity;

}
