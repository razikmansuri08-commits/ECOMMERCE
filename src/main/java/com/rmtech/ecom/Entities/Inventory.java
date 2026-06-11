package com.rmtech.ecom.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Inventory {

    @Id
    @GeneratedValue
    private Long Inventory_id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="product_id",unique = true)
    private Product product;

    private int quantity;

    private int reservedQuantity;

    @Version
    private Long version;
}
