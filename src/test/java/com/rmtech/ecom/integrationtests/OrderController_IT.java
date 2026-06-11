package com.rmtech.ecom.integrationtests;

import com.rmtech.ecom.DTOS.Order_Dto;
import com.rmtech.ecom.Entities.*;
import com.rmtech.ecom.Repositories.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.List;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class OrderController_IT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Inventory_Repo inventoryRepo;
    @Autowired
    private Order_Repo orderRepository;

    @Autowired
    private User_Repo userRepository;

    @Autowired
    private Product_Repo productRepo;

    @Autowired
    private Cart_Repo cartRepository;

    @Test
    @WithMockUser(username = "name")
    public void testPlaceOrder() throws Exception {

        Product product=new Product();
        product.setName("product");
        product.setPrice(100.0);
        productRepo.save(product);

        Inventory inventory=new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(10);
        inventory.setProduct(product);
        inventoryRepo.save(inventory);

        User user=new User();
        user.setName("name");
        user.setEmail("test@gmail.com");
        user.setPassword(passwordEncoder.encode("password"));

        Cart cart=new Cart();
        cart.setUser(user);
        user.setCart(cart);

        Cart_Items cartItem=new Cart_Items();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setCart(cart);
        cart.setCart_items( new ArrayList<>(List.of(cartItem)));

        cartRepository.save(cart);
        userRepository.save(user);

        mockMvc.perform(post("/user/orders/placeorder")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        List<Orders> order = orderRepository.findByuserId(user.getId());

        assertEquals(1, order.size());
        Orders ord=order.get(0);
        assertEquals(ord.getOrder_items().get(0).getProduct(), product);
        assertEquals(OrderStatus.PENDING,ord.getStatus());
        assertEquals(2,ord.getOrder_items().get(0).getQuantity());
        assertEquals(8, inventoryRepo.findByProductId(product.getId()).getQuantity());

        Cart updatedCart =
                cartRepository.findById(
                        cart.getCartId()
                ).orElseThrow();

        assertTrue(
                updatedCart.getCart_items().isEmpty()
        );
    }

    @Test
    @WithMockUser(username = "name")
    void shouldReturnServerErrorWhenPlacingOrderWithEmptyCart() throws Exception {
        User user = user("name");
        Cart cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);
        cartRepository.save(cart);
        userRepository.save(user);

        mockMvc.perform(post("/user/orders/placeorder")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @WithMockUser(username = "name")
    void shouldReturnNotFoundWhenPlacingOrderForMissingUser() throws Exception {
        mockMvc.perform(post("/user/orders/placeorder")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "name")
    void shouldReturnNotFoundWhenInventoryIsMissing() throws Exception {
        Product product = product("product");
        User user = user("name");
        Cart cart = cartWithItem(user, product, 2);
        cartRepository.save(cart);
        userRepository.save(user);

        mockMvc.perform(post("/user/orders/placeorder")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "name")
    void shouldReturnBadRequestWhenStockIsInsufficient() throws Exception {
        Product product = product("product");
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(1);
        inventoryRepo.save(inventory);

        User user = user("name");
        Cart cart = cartWithItem(user, product, 2);
        cartRepository.save(cart);
        userRepository.save(user);

        mockMvc.perform(post("/user/orders/placeorder")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }


    @Test
    @WithMockUser(username = "name")
    public void testCancelOrder() throws Exception {

        Product product=new Product();
        product.setName("product");
        product.setPrice(100.0);
        productRepo.save(product);

        Inventory inventory=new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(10);
        inventory.setProduct(product);
        inventoryRepo.save(inventory);

        User user=new User();
        user.setName("name");
        user.setEmail("test@gmail.com");
        user.setPassword(passwordEncoder.encode("password"));

        Order_items order_items=new Order_items();
        order_items.setProduct(product);
        order_items.setQuantity(2);
        order_items.setOrder(new Orders());
        order_items.setOrder(new Orders());

       Orders order=new Orders();
       order.setUser(user);
       order.setStatus(OrderStatus.PENDING);
       order.getOrder_items().add(order_items);
       order_items.setOrder(order);

        userRepository.save(user);
        orderRepository.save(order);
        mockMvc.perform(delete("/user/orders/order/"+order.getOrderId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        assertTrue(inventoryRepo.findByProductId(product.getId()).getQuantity() == 12);
        assertTrue(orderRepository.findByOrderId(order.getOrderId()) == null);
    }

    @Test
    @WithMockUser(username = "name")
    void shouldReturnNotFoundWhenCancellingMissingOrder() throws Exception {
        user("name");

        mockMvc.perform(delete("/user/orders/order/missing-order")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "name")
    void shouldReturnNotFoundWhenCancellingAnotherUsersOrder() throws Exception {
        user("name");
        User other = user("other");
        Orders order = new Orders();
        order.setUser(other);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        mockMvc.perform(delete("/user/orders/order/" + order.getOrderId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "name")
    void shouldReturnBadRequestWhenCancellingDeliveredOrder() throws Exception {
        Product product = product("product");
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(10);
        inventoryRepo.save(inventory);

        User user = user("name");
        Orders order = order(user, OrderStatus.DELIVERED, product, 2);

        mockMvc.perform(delete("/user/orders/order/" + order.getOrderId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    @WithMockUser(username = "name")
    void should_return_order() throws Exception {

        User user =new User();
        user.setName("name");
        userRepository.save(user);

        Orders order=new Orders();
        order.setUser(user);
        orderRepository.save(order);

        mockMvc.perform(get("/user/orders/order/"+order.getOrderId())
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getOrderId()));
    }

    @Test
    @WithMockUser(username = "name")
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        user("name");

        mockMvc.perform(get("/user/orders/order/missing-order")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "name")
    void shouldReturnNotFoundWhenOrderBelongsToAnotherUser() throws Exception {
        user("name");
        User other = user("other");
        Orders order = new Orders();
        order.setUser(other);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        mockMvc.perform(get("/user/orders/order/" + order.getOrderId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "name")
    void shouldReturnPaginatedOrders() throws Exception {
        User user = user("name");
        order(user, OrderStatus.PENDING, null, 0);
        order(user, OrderStatus.PENDING, null, 0);
        order(user, OrderStatus.PENDING, null, 0);

        mockMvc.perform(get("/user/orders/orders")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(2))
                .andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldDenyUnauthenticatedOrderAccess() throws Exception {
        mockMvc.perform(post("/user/orders/placeorder"))
                .andExpect(status().isUnauthorized());
    }

    private User user(String name) {
        User user = new User();
        user.setName(name);
        user.setEmail(name + "@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        return userRepository.save(user);
    }

    private Product product(String name) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(100.0);
        return productRepo.save(product);
    }

    private Cart cartWithItem(User user, Product product, int quantity) {
        Cart cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);

        Cart_Items cartItem = new Cart_Items();
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setCart(cart);
        cart.setCart_items(new ArrayList<>(List.of(cartItem)));
        return cart;
    }

    private Orders order(User user, OrderStatus status, Product product, int quantity) {
        Orders order = new Orders();
        order.setUser(user);
        order.setStatus(status);
        if (product != null) {
            Order_items item = new Order_items();
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setOrder(order);
            order.getOrder_items().add(item);
        }
        return orderRepository.save(order);
    }


}
