package com.rmtech.ecom.unittests;

import com.rmtech.ecom.Controllers.User_Controller;
import com.rmtech.ecom.DTOS.UserUpdate_Dto;
import com.rmtech.ecom.DTOS.User_dto;
import com.rmtech.ecom.Entities.OrderStatus;
import com.rmtech.ecom.Service.User_Service;
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
class UserControllerTest {

    @Mock
    private User_Service userService;

    @InjectMocks
    private User_Controller userController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("razik", "password")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnCurrentUser() {
        User_dto dto = new User_dto();
        dto.setName("razik");
        when(userService.get_us("razik")).thenReturn(dto);

        ResponseEntity<?> response = userController.get_user();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void shouldUpdateCurrentUser() {
        UserUpdate_Dto update = new UserUpdate_Dto();
        User_dto dto = new User_dto();
        when(userService.update_us("razik", update)).thenReturn(dto);

        ResponseEntity<?> response = userController.update_user(update);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void shouldDeleteCurrentUser() {
        ResponseEntity<?> response = userController.delete_user();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).delete_us("razik");
    }

    @Test
    void shouldReturnCurrentUserOrderStatus() {
        when(userService.getOrderStatus("order-1")).thenReturn(OrderStatus.PENDING);

        ResponseEntity<?> response = userController.get_status("order-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(OrderStatus.PENDING, response.getBody());
    }
}
