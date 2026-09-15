package com.rmtech.ecom.integrationtests;

import com.rmtech.ecom.Entities.OrderStatus;
import com.rmtech.ecom.Entities.Orders;
import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.Repositories.Order_Repo;
import com.rmtech.ecom.Repositories.User_Repo;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class UserController_IT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private User_Repo userRepository;

    @Autowired
    private Order_Repo orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(username = "name")
    void shouldReturnCurrentUser() throws Exception {
        user("name", "name@example.com");

        mockMvc.perform(get("/user/register"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.email").value("name@example.com"));
    }

    @Test
    @WithMockUser(username = "missing")
    void shouldReturnNotFoundWhenCurrentUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/user/register"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void shouldDenyUnauthenticatedCurrentUserRequest() throws Exception {
        mockMvc.perform(get("/user/register"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "name")
    void shouldUpdateCurrentUserAndEncodePassword() throws Exception {
        User user = user("name", "name@example.com");

        String body = """
        {
            "name": "newname",
            "email": "new@example.com",
            "password": "newpassword123"
        }
        """;

        mockMvc.perform(patch("/user/update")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("newname"))
                .andExpect(jsonPath("$.email").value("new@example.com"));

        User updated = userRepository.findbyusername("newname");
        assertNotEquals("newpassword123", updated.getPassword());
    }

    @Test
    @WithMockUser(username = "name")
    void shouldDeleteCurrentUser() throws Exception {
        user("name", "name@example.com");

        mockMvc.perform(delete("/user/delete"))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsByUsernameIgnoreCase("name"));
    }

    @Test
    @WithMockUser(username = "name")
    void shouldReturnOwnOrderStatus() throws Exception {
        User user = user("name", "name@example.com");
        Orders order = new Orders();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        mockMvc.perform(get("/user/getorderstatus")
                        .contentType(APPLICATION_JSON)
                        .content(order.getOrderId()))
                .andExpect(status().isOk())
                .andExpect(content().string("\"PENDING\""));
    }

    @Test
    @WithMockUser(username = "name")
    void shouldReturnNotFoundForMissingOrderStatus() throws Exception {
        user("name", "name@example.com");

        mockMvc.perform(get("/user/getorderstatus")
                        .contentType(APPLICATION_JSON)
                        .content("missing-order"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    private User user(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password"));
        return userRepository.save(user);
    }
}
