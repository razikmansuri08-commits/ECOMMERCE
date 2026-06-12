package com.rmtech.ecom.Controllers;

import com.rmtech.ecom.DTOS.*;
import com.rmtech.ecom.Entities.Category;
import com.rmtech.ecom.Entities.OrderStatus;
import com.rmtech.ecom.Entities.Product;
import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.Exception.ProductNotFoundException;
import com.rmtech.ecom.Exception.UserNotFoundException;
import com.rmtech.ecom.Service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin")
public class Admin_Controllers
{


    private final User_Service us;
    private final Product_Service ps;
    private final Category_Service cs;
    private final Order_Service os;
    private final Inventory_Service is;

    public Admin_Controllers(User_Service us, Product_Service ps, Category_Service cs, Order_Service os, Inventory_Service is) {
        this.us = us;
        this.cs = cs;
        this.ps = ps;
        this.os = os;
        this.is = is;
    }



    @GetMapping("/users")
    public ResponseEntity<?> get_users(@PageableDefault(size = 10) Pageable pageable)
    {
        return ResponseEntity.ok(us.getallusers(pageable));
    }


    @PostMapping("/category")
    public ResponseEntity<?> create_category(@RequestBody CategoryDto categorydto)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(cs.createCategory(categorydto));
    }


    @GetMapping("/categorytree/{id}")
    public ResponseEntity<?> get_category_tree(@PathVariable Long id)
    {
        return ResponseEntity.ok(cs.getcat_tree(id));
    }
    @GetMapping("/categories")
    public ResponseEntity<?> get_categories(@PageableDefault(size = 10) Pageable pageable)
    {
        return ResponseEntity.ok(cs.getallcategories(pageable));
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> get_user(@PathVariable Long userId)
    {
        User_dto userdto = us.get_us_by_id(userId);
        return ResponseEntity.status(HttpStatus.OK).body(userdto);
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> delete_user(@PathVariable Long userId)
    {
        us.delete_us_by_id(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/product")
    public ResponseEntity<?> create_product( @RequestBody ProductRequestDto pr)
    {
            Product_dto productdto=ps.create_prod(pr);
        return ResponseEntity.status(HttpStatus.CREATED).body(productdto);
    }

    @GetMapping("/orders")
    public ResponseEntity<?> get_all_orders(@PageableDefault(size = 10) Pageable pageable)
    {
        return ResponseEntity.ok(os.get_ords(pageable));
    }

    @PatchMapping("/product/{id}")
    public ResponseEntity<?> update_product(@PathVariable Long id, @RequestBody ProductUpdate_Dto prod)
    {
    Product_dto updated_prod = ps.update_prod(id, prod);

    return ResponseEntity.status(HttpStatus.OK).body(updated_prod);
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<?> delete_product(@PathVariable Long id)
    {
        ps.delete_prod(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin")

    public ResponseEntity<?> update_admin( @RequestBody UserUpdate_Dto user)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User_dto updated_admin = us.update_us(username, user);

        return ResponseEntity.status(HttpStatus.OK).body(updated_admin);
    }

    @PatchMapping("/setorderstatus")
    public ResponseEntity<?> update_status( @RequestBody UpdateOrderStatusDto req)
    {

        return ResponseEntity.status(HttpStatus.OK).body(os.updateStatus(req.getOrderId(), req.getStatus()));
    }

    @GetMapping("/getorderstatus")
    public ResponseEntity<?> get_status( @RequestBody String id)
    {
        return ResponseEntity.ok(os.getOrderStatus(id));
    }

    @DeleteMapping("/admin")
    public ResponseEntity<?> delete_admin()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        us.delete_us(username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/addstock/{id}")
    public ResponseEntity<?> addStock(@PathVariable Long id,@RequestBody int quantity)
    {
        is.addstock(id,quantity);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/removestock/{id}")
    public ResponseEntity<?> removeStock(@PathVariable Long id,@RequestBody int quantity)
    {
        is.removestock(id,quantity);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


}
