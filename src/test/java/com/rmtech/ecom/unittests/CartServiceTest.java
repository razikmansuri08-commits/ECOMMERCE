package com.rmtech.ecom.unittests;

import com.rmtech.ecom.DTOS.Cart_Dto;
import com.rmtech.ecom.Entities.Cart;
import com.rmtech.ecom.Entities.Cart_Items;
import com.rmtech.ecom.Entities.Product;
import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.Exception.CartNotFoundException;
import com.rmtech.ecom.Exception.ProductNotFoundException;
import com.rmtech.ecom.Exception.UserNotFoundException;
import com.rmtech.ecom.Repositories.Cart_Repo;
import com.rmtech.ecom.Repositories.Product_Repo;
import com.rmtech.ecom.Repositories.User_Repo;
import com.rmtech.ecom.Service.Cart_Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private Cart_Repo cartRepo;

    @Mock
    private Product_Repo productRepo;

    @Mock
    private User_Repo userRepo;

    @InjectMocks
    private Cart_Service cartService;

    @Test
    void shouldAddNewItemToCart() {
        Cart cart = cart(1L);
        User user = userWithCart(cart);
        Product product = product(10L, "Phone");

        when(userRepo.findbyusername("razik")).thenReturn(user);
        when(cartRepo.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepo.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepo.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart_Dto result = cartService.add_itm("razik", 10L, 2);

        assertEquals(1, result.getItems().size());
        assertEquals(10L, result.getItems().getFirst().getProduct_id());
        assertEquals("Phone", result.getItems().getFirst().getProduct_name());
        assertEquals(2, result.getItems().getFirst().getQuantity());

        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepo).save(captor.capture());
        assertSame(cart, captor.getValue());
        assertSame(cart, cart.getCart_items().getFirst().getCart());
    }

    @Test
    void shouldIncreaseQuantityWhenItemAlreadyExists() {
        Product product = product(10L, "Phone");
        Cart cart = cart(1L);
        Cart_Items existingItem = new Cart_Items();
        existingItem.setProduct(product);
        existingItem.setQuantity(3);
        existingItem.setCart(cart);
        cart.getCart_items().add(existingItem);

        when(userRepo.findbyusername("razik")).thenReturn(userWithCart(cart));
        when(cartRepo.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepo.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepo.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart_Dto result = cartService.add_itm("razik", 10L, 2);

        assertEquals(1, result.getItems().size());
        assertEquals(5, result.getItems().getFirst().getQuantity());
        verify(cartRepo).save(cart);
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(userRepo.findbyusername("missing")).thenReturn(null);

        assertThrows(
                UserNotFoundException.class,
                () -> cartService.get_crt("missing")
        );

        verifyNoInteractions(cartRepo, productRepo);
    }

    @Test
    void shouldThrowWhenUserHasNoCart() {
        User user = new User();
        user.setName("razik");
        when(userRepo.findbyusername("razik")).thenReturn(user);

        assertThrows(
                CartNotFoundException.class,
                () -> cartService.get_crt("razik")
        );

        verifyNoInteractions(cartRepo, productRepo);
    }

    @Test
    void shouldThrowWhenProductDoesNotExistOnAdd() {
        Cart cart = cart(1L);
        when(userRepo.findbyusername("razik")).thenReturn(userWithCart(cart));
        when(cartRepo.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepo.findById(10L)).thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> cartService.add_itm("razik", 10L, 1)
        );

        verify(cartRepo, never()).save(any());
    }

    @Test
    void shouldDeleteItemFromCart() {
        Product product = product(10L, "Phone");
        Cart cart = cart(1L);
        Cart_Items item = new Cart_Items();
        item.setProduct(product);
        item.setQuantity(2);
        item.setCart(cart);
        cart.getCart_items().add(item);

        when(userRepo.findbyusername("razik")).thenReturn(userWithCart(cart));
        when(cartRepo.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepo.existsById(10L)).thenReturn(true);

        cartService.delete_itm("razik", 10L);

        assertTrue(cart.getCart_items().isEmpty());
        verify(cartRepo).save(cart);
    }

    @Test
    void shouldClearCart() {
        Cart cart = cart(1L);
        Cart_Items item = new Cart_Items();
        item.setProduct(product(10L, "Phone"));
        item.setQuantity(2);
        cart.getCart_items().add(item);

        when(userRepo.findbyusername("razik")).thenReturn(userWithCart(cart));
        when(cartRepo.findById(1L)).thenReturn(Optional.of(cart));

        cartService.clear_crt("razik");

        assertTrue(cart.getCart_items().isEmpty());
        verify(cartRepo).save(cart);
    }

    private static Cart cart(Long id) {
        Cart cart = new Cart();
        cart.setCartId(id);
        return cart;
    }

    private static Product product(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        return product;
    }

    private static User userWithCart(Cart cart) {
        User user = new User();
        user.setName("razik");
        user.setCart(cart);
        cart.setUser(user);
        return user;
    }
}
