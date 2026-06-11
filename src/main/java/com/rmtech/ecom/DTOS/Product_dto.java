package com.rmtech.ecom.DTOS;

import com.rmtech.ecom.Entities.Category;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Product_dto implements Serializable {
    private String name;
    private String category;
    private String parentcategory;
    private Long id;
    private double price;

}
