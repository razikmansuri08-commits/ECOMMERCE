package com.rmtech.ecom.Service;


import com.rmtech.ecom.Entities.Inventory;
import com.rmtech.ecom.Repositories.Inventory_Repo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

@Service
    public class InventoryConcurrencyTestSupportService {

    private final Inventory_Repo inventoryRepository;

    public InventoryConcurrencyTestSupportService(
            Inventory_Repo inventoryRepository) {

        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public void updateInventory(
            Long productId,
            int amount,
            CountDownLatch loadedLatch,
            CountDownLatch continueLatch) {

        Inventory inventory =
                inventoryRepository.findByProductId(productId);

        inventory.setQuantity(
                inventory.getQuantity() + amount
        );

        // Both transactions have now read
        // the same version.
        loadedLatch.countDown();

        try {

            continueLatch.await();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(e);
        }

        // Hibernate dirty checking will flush
        // when this transaction commits.
    }
}
