package com.rmtech.ecom.Service;

import com.rmtech.ecom.DTOS.Cart_Dto;
import com.rmtech.ecom.DTOS.Cart_ItemsDto;
import com.rmtech.ecom.Entities.Cart;
import com.rmtech.ecom.Entities.Cart_Items;
import com.rmtech.ecom.Entities.Product;
import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.Exception.CartNotFoundException;
import com.rmtech.ecom.Exception.MethodArgumentInvalid;
import com.rmtech.ecom.Exception.ProductNotFoundException;
import com.rmtech.ecom.Exception.UserNotFoundException;
import com.rmtech.ecom.Repositories.Cart_Repo;
import com.rmtech.ecom.Repositories.Product_Repo;
import com.rmtech.ecom.Repositories.User_Repo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class Cart_Service
{
    private final Cart_Repo cr;
    private final Product_Repo pr;
    private final User_Repo ur;
    public Cart_Service(Cart_Repo cr,Product_Repo pr,User_Repo ur)
    {
        this.cr = cr;
        this.pr = pr;
        this.ur = ur;
    }

    @Transactional
    public Cart_Dto add_itm(String username, Long productId, int quantity)
    {
        if (quantity <= 0) {
            throw new MethodArgumentInvalid("quantity must be greater than zero");
        }
        Cart cart = getUserCart(username);
          Product product = pr.findById(productId).orElseThrow(() -> new ProductNotFoundException("product not found"));
          Optional<Cart_Items> existingItem = cart.getCart_items()
                  .stream()
                  .filter(item -> item.getProduct().getId().equals(productId))
                  .findFirst();


       if (existingItem.isPresent()){
           existingItem.get().setQuantity(
                   existingItem.get().getQuantity() + quantity);
       }
       else {
           Cart_Items item = new Cart_Items();
           item.setProduct(product);
           item.setQuantity(quantity);
           item.setCart(cart);
           cart.getCart_items().add(item);
       }
      Cart savedcart=cr.save(cart);
       return convertoDto(savedcart);
    }
    private Cart getUserCart(String username) {
        User user = ur.findbyusername(username);
        if (user == null) {
            throw new UserNotFoundException("user not found");
        }

        Cart cart = user.getCart();
        if (cart == null) {
            throw new CartNotFoundException("cart not found");
        }

        return cr.findById(cart.getCartId())
                .orElseThrow(() -> new CartNotFoundException("cart not found"));
    }
    public Cart_Dto get_crt(String username)
    {
        return convertoDto(getUserCart(username));
    }

    @Transactional
    public void delete_itm(String username,Long productId)
    {
        Cart cart=getUserCart(username);
         if(!pr.existsById(productId))
             throw new ProductNotFoundException("product not found");
        cart.getCart_items().removeIf(item -> item.getProduct().getId().equals(productId));
        cr.save(cart);
    }
    public void clear_crt(String username)
    {
        Cart cart=getUserCart(username);
        cart.getCart_items().clear();
        cr.save(cart);
    }

    private Cart_Dto convertoDto(Cart cart)
    {
        Cart_Dto cartDto = new Cart_Dto();
        cartDto.setId(cart.getCartId());
        List<Cart_ItemsDto> itemsDto = cart.getCart_items()
                .stream().map(
                        cartItems -> {
                            Cart_ItemsDto cartItemsDto = new Cart_ItemsDto();
                            cartItemsDto.setProduct_id(cartItems.getProduct().getId());
                            cartItemsDto.setQuantity(cartItems.getQuantity());
                            cartItemsDto.setProduct_name(cartItems.getProduct().getName());
                            return cartItemsDto;
                        }).toList();
        cartDto.setItems(itemsDto);
        return cartDto;
    }
}
