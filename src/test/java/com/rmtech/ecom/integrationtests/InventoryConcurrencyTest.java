package com.rmtech.ecom.integrationtests;

import com.rmtech.ecom.Entities.Inventory;
import com.rmtech.ecom.Entities.Product;
import com.rmtech.ecom.Repositories.Inventory_Repo;
import com.rmtech.ecom.Repositories.Product_Repo;
import com.rmtech.ecom.Service.InventoryConcurrencyTestSupportService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class InventoryConcurrencyTest {

    @Autowired
    private Inventory_Repo inventoryRepository;

    @Autowired
    private Product_Repo productRepository;

    @Autowired
    private InventoryConcurrencyTestSupportService testService;

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        executor = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() {
        executor.shutdown();
    }

    @Test
    void shouldDetectOptimisticLockConflict() throws Exception {

        // ============================
        // 1. CREATE TEST PRODUCT
        // ============================

        Product product = new Product();

        product.setName("Concurrency Test Product");
        product.setPrice(100.0);

        product = productRepository.saveAndFlush(product);


        // ============================
        // 2. CREATE TEST INVENTORY
        // ============================

        Inventory inventory = new Inventory();

        inventory.setProduct(product);
        inventory.setQuantity(100);
        inventory.setReservedQuantity(0);

        inventoryRepository.saveAndFlush(inventory);


        Long productId = product.getId();


        // ============================
        // 3. SYNCHRONIZATION
        // ============================

        CountDownLatch loadedLatch =
                new CountDownLatch(2);

        CountDownLatch continueLatch =
                new CountDownLatch(1);


        // ============================
        // 4. THREAD 1
        // ============================

        Callable<String> task1 = () -> {

            try {

                testService.updateInventory(
                        productId,
                        20,
                        loadedLatch,
                        continueLatch
                );

                return "SUCCESS";

            } catch (OptimisticLockingFailureException e) {

                return "CONFLICT";
            }
        };


        // ============================
        // 5. THREAD 2
        // ============================

        Callable<String> task2 = () -> {

            try {

                testService.updateInventory(
                        productId,
                        -30,
                        loadedLatch,
                        continueLatch
                );

                return "SUCCESS";

            } catch (OptimisticLockingFailureException e) {

                return "CONFLICT";
            }
        };


        // ============================
        // 6. START BOTH THREADS
        // ============================

        Future<String> future1 =
                executor.submit(task1);

        Future<String> future2 =
                executor.submit(task2);


        // Wait until both transactions
        // have loaded the inventory
        loadedLatch.await();


        // Release both transactions
        continueLatch.countDown();


        // ============================
        // 7. GET RESULTS
        // ============================

        String result1 = future1.get();

        String result2 = future2.get();


        // ============================
        // 8. VERIFY
        // ============================

        long successCount =
                Stream.of(result1, result2)
                        .filter("SUCCESS"::equals)
                        .count();

        long conflictCount =
                Stream.of(result1, result2)
                        .filter("CONFLICT"::equals)
                        .count();


        assertEquals(1, successCount);

        assertEquals(1, conflictCount);
    }
}