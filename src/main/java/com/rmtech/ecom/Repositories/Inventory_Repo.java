package com.rmtech.ecom.Repositories;

import com.rmtech.ecom.Entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface Inventory_Repo extends JpaRepository<Inventory,Long> {

    @Query("SELECT i FROM Inventory i WHERE i.product.id=:productId")
    Inventory findByProductId(Long productId);
}
