package com.rmtech.ecom.Controllers;

import com.rmtech.ecom.DTOS.AddToCartRequest;
import com.rmtech.ecom.DTOS.Cart_Dto;
import com.rmtech.ecom.DTOS.Cart_ItemsDto;
import com.rmtech.ecom.Entities.Cart;
import com.rmtech.ecom.Service.Cart_Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/cart")
public class Cart_Controller
{
    private final Cart_Service cs;

    public Cart_Controller(Cart_Service cs) {
        this.cs = cs;
    }

    @PostMapping("/product/{productId}")
    public ResponseEntity<?> add_item(@PathVariable Long productId,@Valid @RequestBody AddToCartRequest request)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Cart_Dto cartDto=cs.add_itm(username, productId, request.getQuantity());

        return ResponseEntity.status(HttpStatus.OK).body(cartDto);
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<?> delete_item(@PathVariable Long productId)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        cs.delete_itm(username, productId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/cart")
    public ResponseEntity<?> fetch_cart()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Cart_Dto cartDto = cs.get_crt(username);

        return ResponseEntity.status(HttpStatus.OK).body(cartDto);

    }
    @DeleteMapping("/cart")
    public ResponseEntity<?> clear_cart()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        cs.clear_crt(username);
        return ResponseEntity.noContent().build();
    }
}
