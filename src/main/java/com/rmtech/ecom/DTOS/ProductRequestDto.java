package com.rmtech.ecom.DTOS;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ProductRequestDto implements Serializable {


    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name must not exceed 150 characters")
    private String name;

    @NotNull(message = "Price is required")
    @Positive
    private double price;

    @NotNull(message = "Category id is required")
    @Positive
    private Long categoryid;

    @NotNull(message = "Initial stock quantity is required")
    @Positive
    private int initialStock;
}
