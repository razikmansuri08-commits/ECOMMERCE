package com.rmtech.ecom.Service;

import com.rmtech.ecom.Entities.Inventory;
import com.rmtech.ecom.Exception.InsufficientStockException;
import com.rmtech.ecom.Exception.InventoryNotFoundException;
import com.rmtech.ecom.Repositories.Inventory_Repo;
import jakarta.persistence.Id;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class Inventory_Service {

    private final Inventory_Repo inventory_Repo;
    public Inventory_Service(Inventory_Repo inventory_Repo) {
        this.inventory_Repo = inventory_Repo;

    }
    @Transactional
    public void decrease_stock(Long product_id,int quantity)
    {
        if(quantity<0)
            throw new IllegalArgumentException("Quantity cannot be negative");
        Inventory inventory = inventory_Repo.findByProductId(product_id);
        if(inventory==null)
            throw new InventoryNotFoundException("Inventory not found");

        if(inventory.getQuantity()<quantity)
            throw new InsufficientStockException("Insufficient stock");
        inventory.setQuantity(inventory.getQuantity()-quantity);
    }

    @Transactional
    public void increase_stock(Long product_id,int quantity)
    {
        try {
            if (quantity < 0)
                throw new IllegalArgumentException("Quantity cannot be negative");
            Inventory inventory = inventory_Repo.findByProductId(product_id);
            if (inventory == null)
                throw new InventoryNotFoundException("Inventory not found");
            inventory.setQuantity(inventory.getQuantity() + quantity);
        }
        catch (OptimisticLockException e) {
            throw new OptimisticLockException(e);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void addstock(Long product_id,int quantity)
    {
        if(quantity<0)
            throw new IllegalArgumentException("Quantity cannot be negative");
        Inventory inventory = inventory_Repo.findByProductId(product_id);
        if(inventory==null)
            throw new InventoryNotFoundException("Inventory not found");
        inventory.setQuantity(inventory.getQuantity()+quantity);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void removestock(Long product_id,int quantity)
    {
        if(quantity<0)
            throw new IllegalArgumentException("Quantity cannot be negative");

        Inventory inventory = inventory_Repo.findByProductId(product_id);
        if(inventory==null)
            throw new InventoryNotFoundException("Inventory not found");
        if(inventory.getQuantity()<quantity)
            throw new InsufficientStockException("Insufficient stock");
        inventory.setQuantity(inventory.getQuantity()-quantity);
    }


}
