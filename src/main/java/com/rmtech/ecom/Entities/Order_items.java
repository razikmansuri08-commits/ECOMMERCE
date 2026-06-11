package com.rmtech.ecom.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Order_items
{

    @Id
    @GeneratedValue
    private Long order_item_id;

    @ManyToOne
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Orders order;

    private int quantity;
}
