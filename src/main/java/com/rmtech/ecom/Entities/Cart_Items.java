package com.rmtech.ecom.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Cart_Items
{
    @Id
    @GeneratedValue
    private Long cart_items_id;

    @ManyToOne
    private Product product;

    @ManyToOne
    private Cart cart;

    private int quantity;

}
