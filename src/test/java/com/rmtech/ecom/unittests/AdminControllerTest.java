package com.rmtech.ecom.unittests;

import com.rmtech.ecom.Controllers.Admin_Controllers;
import com.rmtech.ecom.DTOS.*;
import com.rmtech.ecom.Entities.OrderStatus;
import com.rmtech.ecom.Service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private User_Service userService;

    @Mock
    private Product_Service productService;

    @Mock
    private Category_Service categoryService;

    @Mock
    private Order_Service orderService;

    @Mock
    private Inventory_Service inventoryService;

    @InjectMocks
    private Admin_Controllers adminControllers;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "password")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateCategoryWithCreatedStatus() {
        CategoryDto request = new CategoryDto();
        CategoryDto responseDto = new CategoryDto();
        when(categoryService.createCategory(request)).thenReturn(responseDto);

        ResponseEntity<?> response = adminControllers.create_category(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(responseDto, response.getBody());
    }

    @Test
    void shouldCreateProductWithCreatedStatus() {
        ProductRequestDto request = new ProductRequestDto();
        Product_dto responseDto = new Product_dto();
        when(productService.create_prod(request)).thenReturn(responseDto);

        ResponseEntity<?> response = adminControllers.create_product(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(responseDto, response.getBody());
    }

    @Test
    void shouldUpdateProduct() {
        ProductUpdate_Dto request = new ProductUpdate_Dto();
        Product_dto responseDto = new Product_dto();
        when(productService.update_prod(1L, request)).thenReturn(responseDto);

        ResponseEntity<?> response = adminControllers.update_product(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseDto, response.getBody());
    }

    @Test
    void shouldDeleteProduct() {
        ResponseEntity<?> response = adminControllers.delete_product(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productService).delete_prod(1L);
    }

    @Test
    void shouldUpdateCurrentAdmin() {
        UserUpdate_Dto request = new UserUpdate_Dto();
        User_dto responseDto = new User_dto();
        when(userService.update_us("admin", request)).thenReturn(responseDto);

        ResponseEntity<?> response = adminControllers.update_admin(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseDto, response.getBody());
    }

    @Test
    void shouldUpdateOrderStatus() {
        UpdateOrderStatusDto request = new UpdateOrderStatusDto();
        request.setOrderId("order-1");
        request.setStatus(OrderStatus.CONFIRMED);
        when(orderService.updateStatus("order-1", OrderStatus.CONFIRMED))
                .thenReturn(OrderStatus.CONFIRMED);

        ResponseEntity<?> response = adminControllers.update_status(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(OrderStatus.CONFIRMED, response.getBody());
    }

    @Test
    void shouldAddStock() {
        ResponseEntity<?> response = adminControllers.addStock(1L, 5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(inventoryService).addstock(1L, 5);
    }

    @Test
    void shouldRemoveStock() {
        ResponseEntity<?> response = adminControllers.removeStock(1L, 3);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(inventoryService).removestock(1L, 3);
    }
}
