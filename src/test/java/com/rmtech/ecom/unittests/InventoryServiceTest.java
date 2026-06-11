package com.rmtech.ecom.unittests;

import com.rmtech.ecom.Entities.Inventory;
import com.rmtech.ecom.Exception.InsufficientStockException;
import com.rmtech.ecom.Exception.InventoryNotFoundException;
import com.rmtech.ecom.Repositories.Inventory_Repo;
import com.rmtech.ecom.Service.Inventory_Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private Inventory_Repo inventoryRepo;

    @InjectMocks
    private Inventory_Service inventoryService;

    @Test
    void shouldDecreaseStock() {
        Inventory inventory = inventory(10);
        when(inventoryRepo.findByProductId(1L)).thenReturn(inventory);

        inventoryService.decrease_stock(1L, 3);

        assertEquals(7, inventory.getQuantity());
    }

    @Test
    void shouldIncreaseStock() {
        Inventory inventory = inventory(10);
        when(inventoryRepo.findByProductId(1L)).thenReturn(inventory);

        inventoryService.increase_stock(1L, 4);

        assertEquals(14, inventory.getQuantity());
    }

    @Test
    void shouldAddStockForAdminOperation() {
        Inventory inventory = inventory(10);
        when(inventoryRepo.findByProductId(1L)).thenReturn(inventory);

        inventoryService.addstock(1L, 5);

        assertEquals(15, inventory.getQuantity());
    }

    @Test
    void shouldRemoveStockForAdminOperation() {
        Inventory inventory = inventory(10);
        when(inventoryRepo.findByProductId(1L)).thenReturn(inventory);

        inventoryService.removestock(1L, 4);

        assertEquals(6, inventory.getQuantity());
    }

    @Test
    void shouldThrowWhenInventoryDoesNotExist() {
        when(inventoryRepo.findByProductId(1L)).thenReturn(null);

        assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.decrease_stock(1L, 1)
        );
    }

    @Test
    void shouldThrowWhenStockIsInsufficient() {
        Inventory inventory = inventory(2);
        when(inventoryRepo.findByProductId(1L)).thenReturn(inventory);

        assertThrows(
                InsufficientStockException.class,
                () -> inventoryService.decrease_stock(1L, 3)
        );
    }

    @Test
    void shouldThrowWhenQuantityIsNegative() {
        assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.increase_stock(1L, -1)
        );
    }

    private static Inventory inventory(int quantity) {
        Inventory inventory = new Inventory();
        inventory.setQuantity(quantity);
        return inventory;
    }
}
