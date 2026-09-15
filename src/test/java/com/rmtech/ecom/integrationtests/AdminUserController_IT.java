package com.rmtech.ecom.integrationtests;

import com.rmtech.ecom.Repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.rmtech.ecom.DTOS.Order_Dto;
import com.rmtech.ecom.Entities.*;
import com.rmtech.ecom.Repositories.*;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.experimental.categories.CategoryValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static com.rmtech.ecom.Entities.OrderStatus.PENDING;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.List;
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class AdminUserController_IT {


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
    private Product_Repo productRepository;

    @Autowired
    private Cart_Repo cartRepository;
//    @Autowired
//    private CacheManager cacheManager;
//
//    @BeforeEach
//    void clearCache() {
//        cacheManager.getCache("ParentCategory").clear();
//    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_all_users() throws Exception {

        User user=new User();
        user.setName("user");
        user.setPassword("password");
        user.setEmail("user@gmail.com");

        User user1=new User();
        user1.setName("user1");
        user1.setPassword("password");
        user1.setEmail("user1@gmail.com");

        User user2=new User();
        user2.setName("user2");
        user2.setPassword("password");
        user2.setEmail("user2@gmail.com");

        userRepository.save(user);
        userRepository.save(user1);
        userRepository.save(user2);

        mockMvc.perform(get("/admin/users")
                .param("page", "0")
                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_dtos").isArray())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void should_forbid_non_admin_from_users_endpoint() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_deny_unauthenticated_admin_users_endpoint() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_empty_users_page() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_dtos.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_category_tree() throws Exception {

        Category parent = new Category();
        parent.setName("Electronics");

        Category android = new Category();
        android.setName("Android");
        android.setParentCategory(parent);

        Category laptop = new Category();
        laptop.setName("Laptop");
        laptop.setParentCategory(parent);


        Category mobile = new Category();
        mobile.setName("Mobile");
        mobile.setParentCategory(android);


        android.setSubCategories(List.of(mobile));
        parent.setSubCategories(List.of(laptop, android));
        categoryRepo.save(mobile);
        categoryRepo.save(android);
        categoryRepo.save(laptop);


        categoryRepo.save(parent);

        mockMvc.perform(
                        get("/admin/categorytree/{id}", parent.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id")
                        .value(parent.getId()))

                .andExpect(jsonPath("$.name")
                        .value("Electronics"))

                .andExpect(jsonPath("$.children.length()")
                        .value(2))

                .andExpect(jsonPath("$.children[1].name")
                        .value("Android"))

                .andExpect(jsonPath("$.children[0].name")
                        .value("Laptop"))
                .andExpect(
                        jsonPath("$.children[1].children[0].name")
                                .value("Mobile")
                );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_not_found_when_category_tree_is_missing() throws Exception {
        mockMvc.perform(get("/admin/categorytree/{id}", 999L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_all_categories() throws Exception {

        Category category = new Category();
        category.setName("Electronics");
        categoryRepo.save(category);

        Category category1 = new Category();
        category1.setName("Clothing");
        categoryRepo.save(category1);

        Category category2 = new Category();
        category2.setName("Kitchen");
        categoryRepo.save(category2);

        mockMvc.perform(get("/admin/categories")
                .param("page", "0")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryDtos").isArray())
                .andExpect(jsonPath("$.categoryDtos.length()").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_empty_categories_page() throws Exception {
        mockMvc.perform(get("/admin/categories")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryDtos.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_create_root_category() throws Exception {

        String requestBody = """
        {
            "name":"Electronics"
        }
        """;

        mockMvc.perform(post("/admin/category")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.parent_id").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_create_child_category() throws Exception {
        Category parent = new Category();
        parent.setName("Electronics");
        categoryRepo.save(parent);

        String requestBody = """
        {
            "name":"Mobiles",
            "parent_id":"%s"
        }
        """.formatted(parent.getId());

        mockMvc.perform(post("/admin/category")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mobiles"))
                .andExpect(jsonPath("$.parent_id").value(parent.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_server_error_when_category_name_is_duplicate() throws Exception {
        Category category = new Category();
        category.setName("Electronics");
        categoryRepo.save(category);

        String requestBody = """
        {
            "name":"Electronics"
        }
        """;

        mockMvc.perform(post("/admin/category")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_server_error_when_parent_category_is_missing() throws Exception {
        String requestBody = """
        {
            "name":"Mobiles",
            "parent_id":"999"
        }
        """;

        mockMvc.perform(post("/admin/category")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_create_product() throws Exception {

        Category category = new Category();
        category.setName("Electronics");
        categoryRepo.save(category);

        String requestBody = """
                {
                "name": "Product",
                "price": 100.0,
                "categoryid": "%s"
                }""".formatted(category.getId());

        mockMvc.perform(post("/admin/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.name").value("Product"))
                .andExpect(jsonPath("$.price").value(100.0))
                .andExpect(jsonPath("$.category").value(category.getName()));

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_get_user_by_id() throws Exception {
        User user = new User();
        user.setName("user");
        user.setPassword("password");
        user.setEmail("user@gmail.com");
        userRepository.save(user);

        mockMvc.perform(get("/admin/user/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value("user"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return_not_found_when_user_id_is_missing() throws Exception {
        mockMvc.perform(get("/admin/user/{userId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_delete_user_by_id() throws Exception {
        User user = new User();
        user.setName("user");
        user.setPassword("password");
        user.setEmail("user@gmail.com");
        userRepository.save(user);

        mockMvc.perform(delete("/admin/user/{userId}", user.getId()))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsById(user.getId()));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void should_update_current_admin() throws Exception {
        User admin = new User();
        admin.setName("admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setEmail("admin@gmail.com");
        userRepository.save(admin);

        String requestBody = """
        {
            "name":"updatedadmin",
            "email":"updatedadmin@gmail.com",
            "password":"newpassword123"
        }
        """;

        mockMvc.perform(patch("/admin/admin")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updatedadmin"))
                .andExpect(jsonPath("$.email").value("updatedadmin@gmail.com"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void should_delete_current_admin() throws Exception {
        User admin = new User();
        admin.setName("admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setEmail("admin@gmail.com");
        userRepository.save(admin);

        mockMvc.perform(delete("/admin/admin"))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsByUsernameIgnoreCase("admin"));
    }

}
