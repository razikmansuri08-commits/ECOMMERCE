package com.rmtech.ecom.DTOS;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class Order_Dto
{
    private String id;

    private List<Order_ItemsDto> items=new ArrayList<>();
}
