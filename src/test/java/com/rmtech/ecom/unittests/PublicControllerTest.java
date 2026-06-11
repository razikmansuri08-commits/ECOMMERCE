package com.rmtech.ecom.unittests;

import com.rmtech.ecom.Controllers.Public_Controllers;
import com.rmtech.ecom.DTOS.ProductPageResponse;
import com.rmtech.ecom.DTOS.Product_dto;
import com.rmtech.ecom.DTOS.UserRequest_Dto;
import com.rmtech.ecom.DTOS.User_dto;
import com.rmtech.ecom.Service.Cart_Service;
import com.rmtech.ecom.Service.Order_Service;
import com.rmtech.ecom.Service.Product_Service;
import com.rmtech.ecom.Service.User_Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicControllerTest {

    @Mock
    private User_Service userService;

    @Mock
    private Cart_Service cartService;

    @Mock
    private Product_Service productService;

    @Mock
    private Order_Service orderService;

    @InjectMocks
    private Public_Controllers publicControllers;

    @Test
    void shouldCreateUserWithCreatedStatus() {
        UserRequest_Dto request = new UserRequest_Dto();
        User_dto dto = new User_dto();
        dto.setName("Razik");

        when(userService.create_us(request)).thenReturn(dto);

        ResponseEntity<User_dto> response = publicControllers.create_user(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void shouldReturnProductById() {
        Product_dto dto = new Product_dto();
        dto.setId(1L);
        when(productService.get_prod(1L)).thenReturn(dto);

        ResponseEntity<?> response = publicControllers.get_product(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void shouldReturnPagedProducts() {
        ProductPageResponse page = new ProductPageResponse();
        page.setProducts(List.of(new Product_dto()));
        when(productService.getallProducts(any(Pageable.class))).thenReturn(page);

        ResponseEntity<?> response = publicControllers.get_all_products(Pageable.ofSize(10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
        verify(productService).getallProducts(any(Pageable.class));
    }
}
