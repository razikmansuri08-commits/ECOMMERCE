package com.rmtech.ecom.integrationtests;


import com.rmtech.ecom.DTOS.Order_Dto;
import com.rmtech.ecom.Entities.*;
import com.rmtech.ecom.Repositories.*;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.experimental.categories.CategoryValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static com.rmtech.ecom.Entities.OrderStatus.PENDING;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class AdminOrderController_IT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Inventory_Repo inventoryRepo;

    @Autowired
    private Category_Repo categoryRepo;

    @Autowired
    private Order_Repo orderRepository;

    @Autowired
    private User_Repo userRepository;

    @Autowired
    private Product_Repo productRepo;

    @Autowired
    private Cart_Repo cartRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("orders").clear();
        cacheManager.getCache("products").clear();
        cacheManager.getCache("categories").clear();
    }
    @Test
    @WithMockUser(username = "user", roles = "USER")
    void should_return_paginated_orders() throws Exception {

        User user=new User();
        user.setName("user");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRoles(List.of(UserRoles.USER));
        userRepository.save(user);



        Orders order1=new Orders();
        Orders order2=new Orders();
        Orders order3=new Orders();

        order1.setUser(user);
        order2.setUser(user);
        order3.setUser(user);


        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        mockMvc.perform(
                        get("/user/orders/orders")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").isArray())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void should_forbid_non_admin_from_admin_order_status() throws Exception {
        mockMvc.perform(get("/admin/getorderstatus")
                        .contentType(APPLICATION_JSON)
                        .content("orderid"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_order_status() throws Exception {

        User user = new User();
        userRepository.save(user);

        Orders order = new Orders();
        order.setUser(user);
        order.setStatus(PENDING);
        orderRepository.save(order);

        mockMvc.perform(
                        get("/admin/getorderstatus")
                                .contentType(APPLICATION_JSON)
                                .content(order.getOrderId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("\"PENDING\""));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_not_found_for_missing_order_status() throws Exception {
        mockMvc.perform(
                        get("/admin/getorderstatus")
                                .contentType(APPLICATION_JSON)
                                .content("missing-order")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_update_order_to_CONFIRMED() throws Exception {

        User user = new User();
        userRepository.save(user);

        Orders order = new Orders();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        orderRepository.save(order);

        String body = """
        {
            "orderId":"%s",
            "status":"CONFIRMED"
        }
        """.formatted(order.getOrderId());

        mockMvc.perform(
                        patch("/admin/setorderstatus")
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("\"CONFIRMED\""));

        Orders updated =
                orderRepository.findByOrderId(order.getOrderId());

        assertEquals(OrderStatus.CONFIRMED,
                updated.getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_update_order_to_PROCESSING() throws Exception {
        Orders order = order(OrderStatus.CONFIRMED);

        String body = """
        {
            "orderId":"%s",
            "status":"PROCESSING"
        }
        """.formatted(order.getOrderId());

        mockMvc.perform(
                        patch("/admin/setorderstatus")
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("\"PROCESSING\""));

        assertEquals(OrderStatus.PROCESSING,
                orderRepository.findByOrderId(order.getOrderId()).getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_update_order_to_SHIPPED_and_set_timestamp() throws Exception {
        Orders order = order(OrderStatus.PROCESSING);

        String body = """
        {
            "orderId":"%s",
            "status":"SHIPPED"
        }
        """.formatted(order.getOrderId());

        mockMvc.perform(
                        patch("/admin/setorderstatus")
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("\"SHIPPED\""));

        Orders updated = orderRepository.findByOrderId(order.getOrderId());
        assertEquals(OrderStatus.SHIPPED, updated.getStatus());
        assertNotNull(updated.getShippedAt());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_update_order_to_DELIVERED_and_set_timestamp() throws Exception {
        Orders order = order(OrderStatus.SHIPPED);

        String body = """
        {
            "orderId":"%s",
            "status":"DELIVERED"
        }
        """.formatted(order.getOrderId());

        mockMvc.perform(
                        patch("/admin/setorderstatus")
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("\"DELIVERED\""));

        Orders updated = orderRepository.findByOrderId(order.getOrderId());
        assertEquals(OrderStatus.DELIVERED, updated.getStatus());
        assertNotNull(updated.getDeliveredAt());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_cancel_order_and_restore_stock() throws Exception {
        Product product = product("product", 100.0, null);
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(10);
        inventoryRepo.save(inventory);

        Orders order = order(OrderStatus.CONFIRMED);
        Order_items item = new Order_items();
        item.setProduct(product);
        item.setQuantity(2);
        item.setOrder(order);
        order.getOrder_items().add(item);
        orderRepository.save(order);

        String body = """
        {
            "orderId":"%s",
            "status":"CANCELLED"
        }
        """.formatted(order.getOrderId());

        mockMvc.perform(
                        patch("/admin/setorderstatus")
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("\"CANCELLED\""));

        assertEquals(12, inventoryRepo.findByProductId(product.getId()).getQuantity());
        assertNotNull(orderRepository.findByOrderId(order.getOrderId()).getCancelledAt());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_bad_request_when_order_status_is_same() throws Exception {
        Orders order = order(OrderStatus.PENDING);

        String body = """
        {
            "orderId":"%s",
            "status":"PENDING"
        }
        """.formatted(order.getOrderId());

        mockMvc.perform(
                        patch("/admin/setorderstatus")
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_bad_request_for_invalid_order_status_transition() throws Exception {
        Orders order = order(OrderStatus.DELIVERED);

        String body = """
        {
            "orderId":"%s",
            "status":"PROCESSING"
        }
        """.formatted(order.getOrderId());

        mockMvc.perform(
                        patch("/admin/setorderstatus")
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_not_found_when_updating_missing_order_status() throws Exception {
        String body = """
        {
            "orderId":"missing-order",
            "status":"CONFIRMED"
        }
        """;

        mockMvc.perform(
                        patch("/admin/setorderstatus")
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void shouldcreate_product() throws Exception {


        Category category=new Category();
        category.setName("category");
        category.setParentCategory(null);
        categoryRepo.save(category);

        String body="""
        {
                "name":"product",
                "price":100.0,
                "categoryid":"%s"
        }""".formatted(category.getId());

        mockMvc.perform(post("/admin/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("product"))
                .andExpect(jsonPath("$.price").value(100.0))
                .andExpect(jsonPath("$.category").value("category"))
                .andExpect(jsonPath("$.parentcategory").value(Matchers.nullValue()));
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void should_return_bad_request_when_creating_invalid_product() throws Exception {
        String body = """
        {
            "name":"",
            "price":0,
            "categoryid":0
        }
        """;

        mockMvc.perform(post("/admin/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void should_return_not_found_when_creating_product_with_missing_category() throws Exception {
        String body = """
        {
            "name":"product",
            "price":100.0,
            "categoryid":999
        }
        """;

        mockMvc.perform(post("/admin/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void should_update_product() throws Exception {
        Category category = new Category();
        category.setName("category");
        categoryRepo.save(category);
        Product product = product("product", 100.0, category);

        String body = """
        {
            "name":"updated",
            "price":150.0
        }
        """;

        mockMvc.perform(patch("/admin/product/" + product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated"))
                .andExpect(jsonPath("$.price").value(150.0));
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void should_return_not_found_when_updating_missing_product() throws Exception {
        String body = """
        {
            "name":"updated",
            "price":150.0
        }
        """;

        mockMvc.perform(patch("/admin/product/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void should_delete_product() throws Exception {
        Category category = new Category();
        category.setName("category");
        categoryRepo.save(category);
        Product product = product("product", 100.0, category);

        mockMvc.perform(delete("/admin/product/" + product.getId()))
                .andExpect(status().isNoContent());

        assertFalse(productRepo.existsById(product.getId()));
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void should_return_not_found_when_deleting_missing_product() throws Exception {
        mockMvc.perform(delete("/admin/product/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_Add_Stock() throws Exception {

        Product product=new Product();
        product.setName("product");
        product.setPrice(100.0);

        Inventory inventory=new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(10);
        productRepo.save(product);
        inventoryRepo.save(inventory);

        assertEquals(10, inventory.getQuantity());

        mockMvc
                .perform(post("/admin/addstock/"+product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("10")
                )
                .andExpect(status().isOk());

        assertEquals(20, inventory.getQuantity());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_not_found_when_adding_stock_for_missing_inventory() throws Exception {
        Product product = product("product", 100.0, null);

        mockMvc
                .perform(post("/admin/addstock/"+product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("10")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_server_error_when_adding_negative_stock() throws Exception {
        Product product = product("product", 100.0, null);
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(10);
        inventoryRepo.save(inventory);

        mockMvc
                .perform(post("/admin/addstock/"+product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("-1")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_Remove_Stock() throws Exception {

        Product product=new Product();
        product.setName("product");
        product.setPrice(100.0);

        Inventory inventory=new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(10);
        productRepo.save(product);
        inventoryRepo.save(inventory);

        assertEquals(10, inventory.getQuantity());

        mockMvc
                .perform(post("/admin/removestock/"+product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("10")
                )
                .andExpect(status().isOk());

        assertEquals(0, inventory.getQuantity());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_bad_request_when_removing_more_than_available_stock() throws Exception {
        Product product = product("product", 100.0, null);
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(5);
        inventoryRepo.save(inventory);

        mockMvc
                .perform(post("/admin/removestock/"+product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("10")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_not_found_when_removing_stock_for_missing_inventory() throws Exception {
        Product product = product("product", 100.0, null);

        mockMvc
                .perform(post("/admin/removestock/"+product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("10")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    private Orders order(OrderStatus status) {
        User user = new User();
        user.setName("user" + System.nanoTime());
        user.setEmail(user.getName() + "@example.com");
        user.setPassword("password");
        userRepository.save(user);

        Orders order = new Orders();
        order.setUser(user);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    private Product product(String name, double price, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setCategory(category);
        return productRepo.save(product);
    }

}
