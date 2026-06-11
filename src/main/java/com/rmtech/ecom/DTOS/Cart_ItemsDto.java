package com.rmtech.ecom.DTOS;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Cart_ItemsDto
{
    private String product_name;
    private Long product_id;
    private int quantity;

}
