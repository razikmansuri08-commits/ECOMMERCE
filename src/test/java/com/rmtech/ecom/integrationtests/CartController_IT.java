package com.rmtech.ecom.integrationtests;

import com.rmtech.ecom.Entities.Cart;
import com.rmtech.ecom.Entities.Cart_Items;
import com.rmtech.ecom.Entities.Product;
import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.Repositories.Cart_Repo;
import com.rmtech.ecom.Repositories.Product_Repo;
import com.rmtech.ecom.Repositories.User_Repo;
import com.rmtech.ecom.Service.Cart_Service;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class CartController_IT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private User_Repo userRepository;

    @Autowired
    private Product_Repo productRepo;

    @Autowired
    private Cart_Repo cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(username = "name")
    void shouldAddProductToCartSuccessfully() throws Exception {

        User user=new User();
        user.setName("name");
        user.setEmail("test@gmail.com");
        user.setPassword(passwordEncoder.encode("password"));
        Cart cart=new Cart();
        cart.setUser(user);
        user.setCart(cart);
        cartRepository.save(cart);
        userRepository.save(user);

        Product product=new Product();
        product.setName("product");
        product.setPrice(100.0);
        productRepo.save(product);

        ;

        String requestBody = """
{
    "quantity":2
}
""";

        mockMvc.perform(
                post("/user/cart/product/" + product.getId())
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
        Cart updatedCart =
                cartRepository.findById(cart.getCartId())
                        .orElseThrow();
        assertEquals(
                1,
                updatedCart.getCart_items().size()
        );
        Cart_Items item =
                updatedCart.getCart_items().get(0);

        assertEquals(
                product.getId(),
                item.getProduct().getId()
        );
        assertEquals(
                2,
                item.getQuantity()
        );
        assertEquals(
                100.0,
                item.getProduct().getPrice()
        );
    }

    @Test
    @WithMockUser(username = "name")
    void shouldIncreaseQuantityWhenAddingSameProductAgain() throws Exception {
        User user = userWithCart("name");
        Product product = product("product");

        String requestBody = """
        {
            "quantity":2
        }
        """;

        mockMvc.perform(post("/user/cart/product/" + product.getId())
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        mockMvc.perform(post("/user/cart/product/" + product.getId())
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(4));

        Cart updatedCart = cartRepository.findById(user.getCart().getCartId()).orElseThrow();
        assertEquals(1, updatedCart.getCart_items().size());
        assertEquals(4, updatedCart.getCart_items().get(0).getQuantity());
    }

    @Test
    @WithMockUser(username = "name")
    void shouldReturnNotFoundWhenAddingMissingProduct() throws Exception {
        userWithCart("name");

        String requestBody = """
        {
            "quantity":2
        }
        """;

        mockMvc.perform(post("/user/cart/product/999")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "missing")
    void shouldReturnNotFoundWhenCartUserDoesNotExist() throws Exception {
        Product product = product("product");

        String requestBody = """
        {
            "quantity":2
        }
        """;

        mockMvc.perform(post("/user/cart/product/" + product.getId())
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "name")
    void shouldReturnNotFoundWhenUserHasNoCart() throws Exception {
        User user = new User();
        user.setName("name");
        user.setEmail("name@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);

        Product product = product("product");

        String requestBody = """
        {
            "quantity":2
        }
        """;

        mockMvc.perform(post("/user/cart/product/" + product.getId())
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "name")
    void shouldRemoveProductFromCartSuccessfully() throws Exception {
        Product product=new Product();
        product.setName("product");
        product.setPrice(100.0);
        productRepo.save(product);

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

        mockMvc.perform(
                        delete("/user/cart/product/" + product.getId())
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().is2xxSuccessful());

        Cart updatedCart =
                cartRepository.findById(cart.getCartId())
                        .orElseThrow();
        assertEquals(
                0,
                updatedCart.getCart_items().size()
        );
    }

    @Test
    @WithMockUser(username = "name")
    void shouldReturnNotFoundWhenRemovingMissingProduct() throws Exception {
        userWithCart("name");

        mockMvc.perform(delete("/user/cart/product/999")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "name")
    void shouldKeepCartUnchangedWhenRemovingProductThatIsNotInCart() throws Exception {
        User user = userWithCart("name");
        Product product = product("product");

        mockMvc.perform(delete("/user/cart/product/" + product.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Cart updatedCart = cartRepository.findById(user.getCart().getCartId()).orElseThrow();
        assertEquals(0, updatedCart.getCart_items().size());
    }

    @Test
    @WithMockUser(username = "name")
    void shouldFetchCart() throws Exception {
        Product product = product("product");
        User user = userWithCart("name");
        Cart_Items item = new Cart_Items();
        item.setProduct(product);
        item.setQuantity(2);
        item.setCart(user.getCart());
        user.getCart().setCart_items(new ArrayList<>(List.of(item)));
        cartRepository.save(user.getCart());

        mockMvc.perform(get("/user/cart/cart")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].product_name").value("product"));
    }

    @Test
    @WithMockUser(username = "name")
    void shouldFetchEmptyCart() throws Exception {
        userWithCart("name");

        mockMvc.perform(get("/user/cart/cart")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    @WithMockUser(username = "name")
    void shouldClearCartSuccessfully() throws Exception {
        Product product=new Product();
        product.setName("product");
        product.setPrice(100.0);
        productRepo.save(product);

        Product product1=new Product();
        product1.setName("product1");
        product1.setPrice(100.0);
        productRepo.save(product1);

        User user=new User();
        user.setName("name");
        user.setEmail("test@gmail.com");
        user.setPassword(passwordEncoder.encode("password"));

        Cart cart=new Cart();
        cart.setUser(user);
        user.setCart(cart);

        Cart_Items item1=new Cart_Items();
        item1.setProduct(product1);
        item1.setQuantity(2);
        item1.setCart(cart);

        Cart_Items item2=new Cart_Items();
        item2.setProduct(product);
        item2.setQuantity(2);
        item2.setCart(cart);

        cart.setCart_items( new ArrayList<>(List.of(item1,item2)));



        cartRepository.save(cart);
        userRepository.save(user);

        mockMvc.perform(
                delete("/user/cart/cart")
                        .contentType(APPLICATION_JSON)
        ).andExpect(status().is2xxSuccessful());

        Cart updatedCart =
                cartRepository.findById(cart.getCartId())
                        .orElseThrow();
        assertEquals(
                0,
                updatedCart.getCart_items().size()
        );
    }

    @Test
    @WithMockUser(username = "name")
    void shouldClearAlreadyEmptyCart() throws Exception {
        User user = userWithCart("name");

        mockMvc.perform(delete("/user/cart/cart")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Cart updatedCart = cartRepository.findById(user.getCart().getCartId()).orElseThrow();
        assertEquals(0, updatedCart.getCart_items().size());
    }

    @Test
    void shouldDenyUnauthenticatedCartAccess() throws Exception {
        mockMvc.perform(get("/user/cart/cart"))
                .andExpect(status().isUnauthorized());
    }

    private Product product(String name) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(100.0);
        return productRepo.save(product);
    }

    private User userWithCart(String name) {
        User user = new User();
        user.setName(name);
        user.setEmail(name + "@example.com");
        user.setPassword(passwordEncoder.encode("password"));

        Cart cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);

        cartRepository.save(cart);
        return userRepository.save(user);
    }

}

