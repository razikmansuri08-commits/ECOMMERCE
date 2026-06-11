package com.rmtech.ecom.integrationtests;

import com.rmtech.ecom.Entities.Category;
import com.rmtech.ecom.Entities.Product;
import com.rmtech.ecom.Repositories.Category_Repo;
import com.rmtech.ecom.Repositories.Product_Repo;
import com.rmtech.ecom.Repositories.User_Repo;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class PublicController_IT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private User_Repo userRepository;

    @Autowired
    private Product_Repo productRepository;

    @Autowired
    private Category_Repo categoryRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void clearCache() {
        cacheManager.getCache("products").clear();
    }
    @Test
    void shouldCreateUser() throws Exception {
        String body = """
        {
            "name": "Razik",
            "email": "razik@example.com",
            "password": "password123"
        }
        """;

        mockMvc.perform(post("/public/user")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Razik"))
                .andExpect(jsonPath("$.email").value("razik@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));

        assertTrue(userRepository.existsByname("Razik"));
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        String body = """
        {
            "name": "Razik",
            "email": "duplicate@example.com",
            "password": "password123"
        }
        """;

        mockMvc.perform(post("/public/user")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        String duplicateBody = """
        {
            "name": "Other",
            "email": "duplicate@example.com",
            "password": "password123"
        }
        """;

        mockMvc.perform(post("/public/user")
                        .contentType(APPLICATION_JSON)
                        .content(duplicateBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void shouldReturnConflictWhenUsernameAlreadyExists() throws Exception {
        String body = """
        {
            "name": "Razik",
            "email": "razik@example.com",
            "password": "password123"
        }
        """;

        mockMvc.perform(post("/public/user")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        String duplicateBody = """
        {
            "name": "Razik",
            "email": "other@example.com",
            "password": "password123"
        }
        """;

        mockMvc.perform(post("/public/user")
                        .contentType(APPLICATION_JSON)
                        .content(duplicateBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void shouldReturnPaginatedProducts() throws Exception {
        Category category = category("Electronics", null);
        product("Phone", 100.0, category);
        product("Laptop", 500.0, category);

        mockMvc.perform(get("/public/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldReturnEmptyProductPage() throws Exception {
        mockMvc.perform(get("/public/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void shouldReturnProductById() throws Exception {
        Category category = category("Electronics", null);
        Product product = product("Phone", 100.0, category);

        mockMvc.perform(get("/public/product/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name").value("Phone"))
                .andExpect(jsonPath("$.category").value("Electronics"));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        mockMvc.perform(get("/public/product/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void shouldReturnExpensiveProducts() throws Exception {
        Category category = category("Electronics", null);
        product("Phone", 100.0, category);
        product("Laptop", 500.0, category);

        Long id=category.getId();
        mockMvc.perform(get("/public/expensiveproducts")
                        .param("categoryid", String.valueOf(id))
                        .param("minPrice", "200")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(1))
                .andExpect(jsonPath("$.products[0].name").value("Laptop"));
    }

    @Test
    void shouldReturnCheapProducts() throws Exception {
        Category category = category("Electronics", null);
        product("Phone", 100.0, category);
        product("Laptop", 500.0, category);

        mockMvc.perform(get("/public/cheapproducts")
                        .param("categoryid", category.getId().toString())
                        .param("maxPrice", "200")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(1))
                .andExpect(jsonPath("$.products[0].name").value("Phone"));
    }

    @Test
    void shouldReturnParentCategorizedProducts() throws Exception {
        Category parent = category("Electronics", null);
        product("Phone", 100.0, parent);

        mockMvc.perform(get("/public/parentcategorizedproducts")
                        .param("parentcategoryid", parent.getId().toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(1))
                .andExpect(jsonPath("$.products[0].category").value("Electronics"));
    }

    @Test
    void shouldReturnNotFoundForChildCategoryInParentCategorizedProducts() throws Exception {
        Category parent = category("Electronics", null);
        Category child = category("Mobiles", parent);

        mockMvc.perform(get("/public/parentcategorizedproducts")
                        .param("parentcategoryid", child.getId().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    private Category category(String name, Category parent) {
        Category category = new Category();
        category.setName(name);
        category.setParentCategory(parent);
        return categoryRepository.save(category);
    }

    private Product product(String name, double price, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setCategory(category);
        return productRepository.save(product);
    }
}
