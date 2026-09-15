package com.rmtech.ecom.Service;

import com.rmtech.ecom.Entities.Inventory;
import com.rmtech.ecom.Exception.InsufficientStockException;
import com.rmtech.ecom.Exception.InventoryNotFoundException;
import com.rmtech.ecom.Exception.MethodArgumentInvalid;
import com.rmtech.ecom.Repositories.Inventory_Repo;
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
    public void decrease_stock(Long product_id, int quantity) {
        if (quantity < 0) {
            throw new MethodArgumentInvalid("Quantity cannot be negative");
        }
        Inventory inventory = inventory_Repo.findByProductId(product_id);
        if (inventory == null) {
            throw new InventoryNotFoundException("Inventory not found for product: " + product_id);
        }

        if (inventory.getQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock. Available: " + inventory.getQuantity() + ", Requested: " + quantity);
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory_Repo.save(inventory);
    }

    @Transactional
    public void increase_stock(Long product_id, int quantity) {
        if (quantity < 0) {
            throw new MethodArgumentInvalid("Quantity cannot be negative");
        }
        Inventory inventory = inventory_Repo.findByProductId(product_id);
        if (inventory == null) {
            throw new InventoryNotFoundException("Inventory not found for product: " + product_id);
        }
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory_Repo.save(inventory);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void addstock(Long product_id, int quantity) {
        if (quantity < 0) {
            throw new MethodArgumentInvalid("Quantity cannot be negative");
        }
        Inventory inventory = inventory_Repo.findByProductId(product_id);
        if (inventory == null) {
            throw new InventoryNotFoundException("Inventory not found for product: " + product_id);
        }
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory_Repo.save(inventory);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void removestock(Long product_id, int quantity) {
        if (quantity < 0) {
            throw new MethodArgumentInvalid("Quantity cannot be negative");
        }

        Inventory inventory = inventory_Repo.findByProductId(product_id);
        if (inventory == null) {
            throw new InventoryNotFoundException("Inventory not found for product: " + product_id);
        }
        if (inventory.getQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock to remove. Available: " + inventory.getQuantity());
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory_Repo.save(inventory);
    }

    public int getStockQuantity(Long productId) {
        Inventory inventory = inventory_Repo.findByProductId(productId);
        if (inventory == null) {
            throw new InventoryNotFoundException("Inventory not found for product: " + productId);
        }
        return inventory.getQuantity();
    }

    public Inventory getInventoryByProductId(Long productId) {
        return inventory_Repo.findByProductId(productId);
    }
}
