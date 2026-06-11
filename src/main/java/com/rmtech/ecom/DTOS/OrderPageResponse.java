package com.rmtech.ecom.DTOS;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderPageResponse {

    private List<Order_Dto> orders;

    private int currentPage;

    private int totalPages;

    private long totalItems;

}
