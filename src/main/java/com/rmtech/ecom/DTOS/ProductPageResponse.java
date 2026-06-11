package com.rmtech.ecom.DTOS;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ProductPageResponse implements Serializable {

    private List<Product_dto> products;

    private int currentPage;

    private int totalPages;

    private long totalItems;


}
