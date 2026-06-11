package com.rmtech.ecom.DTOS;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class Cart_Dto
{
        private Long id;
        private List<Cart_ItemsDto> items;
}
