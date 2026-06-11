package com.rmtech.ecom.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class Cart
{
    @Id
    @GeneratedValue
    private Long cartId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL,orphanRemoval = true,fetch = FetchType.EAGER)
    private List<Cart_Items> cart_items=new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "user_id",unique = true)
    private User user;
}
