package com.rmtech.ecom.unittests;

import com.rmtech.ecom.DTOS.UserPageResponse;
import com.rmtech.ecom.DTOS.UserRequest_Dto;
import com.rmtech.ecom.DTOS.UserUpdate_Dto;
import com.rmtech.ecom.DTOS.User_dto;
import com.rmtech.ecom.Entities.Cart;
import com.rmtech.ecom.Entities.Orders;
import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.Entities.UserRoles;
import com.rmtech.ecom.Exception.EmailAlreadyExistsException;
import com.rmtech.ecom.Exception.UserAlreadyExistsException;
import com.rmtech.ecom.Exception.UserNotFoundException;
import com.rmtech.ecom.Repositories.User_Repo;
import com.rmtech.ecom.Service.Order_Service;
import com.rmtech.ecom.Service.User_Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private User_Repo userRepo;

    @Mock
    private Order_Service orderService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private User_Service userService;

    @Test
    void shouldCreateUserWithDefaultRoleEncodedPasswordAndCart() {
        UserRequest_Dto request = new UserRequest_Dto();
        request.setName("Razik");
        request.setEmail("razik@example.com");
        request.setPassword("password123");

        when(userRepo.existsByEmail("razik@example.com")).thenReturn(false);
        when(userRepo.existsByname("Razik")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User_dto result = userService.create_us(request);

        assertEquals("Razik", result.getName());
        assertEquals("razik@example.com", result.getEmail());
        assertEquals(List.of("USER"), result.getRoles());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("encoded-password", saved.getPassword());
        assertEquals(List.of(UserRoles.USER), saved.getRoles());
        assertNotNull(saved.getCart());
        assertSame(saved, saved.getCart().getUser());
    }

    @Test
    void shouldCreateUserWithRequestedRoles() {
        UserRequest_Dto request = new UserRequest_Dto();
        request.setName("Admin");
        request.setEmail("admin@example.com");
        request.setPassword("password123");
        request.setRole("ADMIN");

        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User_dto result = userService.create_us(request);

        assertEquals(List.of("ADMIN"), result.getRoles());
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        UserRequest_Dto request = new UserRequest_Dto();
        request.setEmail("razik@example.com");
        when(userRepo.existsByEmail("razik@example.com")).thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.create_us(request)
        );

        verify(userRepo, never()).save(any());
    }

    @Test
    void shouldThrowWhenUsernameAlreadyExists() {
        UserRequest_Dto request = new UserRequest_Dto();
        request.setName("Razik");
        request.setEmail("razik@example.com");
        when(userRepo.existsByEmail("razik@example.com")).thenReturn(false);
        when(userRepo.existsByname("Razik")).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.create_us(request)
        );

        verify(userRepo, never()).save(any());
    }

    @Test
    void shouldUpdateUserAndEncodePassword() {
        User user = user(1L, "Razik", "old@example.com");
        user.setPassword("old-password");
        UserUpdate_Dto update = new UserUpdate_Dto();
        update.setName("R M");
        update.setEmail("new@example.com");
        update.setPassword("new-password");

        when(userRepo.findbyusername("Razik")).thenReturn(user);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

        User_dto result = userService.update_us("Razik", update);

        assertEquals("R M", result.getName());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("encoded-new-password", user.getPassword());
        verify(userRepo).save(user);
    }

    @Test
    void shouldThrowWhenUpdatingMissingUser() {
        when(userRepo.findbyusername("missing")).thenReturn(null);

        assertThrows(
                UserNotFoundException.class,
                () -> userService.update_us("missing", new UserUpdate_Dto())
        );
    }

    @Test
    void shouldReturnPagedUsers() {
        User user = user(1L, "Razik", "razik@example.com");
        when(userRepo.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1));

        UserPageResponse result = userService.getallusers(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalItems());
        assertEquals("Razik", result.getUser_dtos().getFirst().getName());
    }

    @Test
    void shouldDeleteUserAndDetachRelationships() {
        User user = user(1L, "Razik", "razik@example.com");
        Cart cart = new Cart();
        Orders order = new Orders();
        user.setCart(cart);
        user.setOrder(List.of(order));
        cart.setUser(user);
        order.setUser(user);

        when(userRepo.findbyusername("Razik")).thenReturn(user);

        userService.delete_us("Razik");

        assertNull(cart.getUser());
        assertNull(order.getUser());
        verify(userRepo).delete(user);
    }

    @Test
    void shouldGetUserById() {
        User user = user(1L, "Razik", "razik@example.com");
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        User_dto result = userService.get_us_by_id(1L);

        assertEquals("Razik", result.getName());
    }

    private static User user(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setRoles(List.of(UserRoles.USER));
        return user;
    }
}
