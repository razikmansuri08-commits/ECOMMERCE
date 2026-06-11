package com.rmtech.ecom.Controllers;

import com.rmtech.ecom.DTOS.ProductPageResponse;
import com.rmtech.ecom.DTOS.Product_dto;
import com.rmtech.ecom.DTOS.UserRequest_Dto;
import com.rmtech.ecom.DTOS.User_dto;
import com.rmtech.ecom.Entities.Category;
import com.rmtech.ecom.Entities.Product;
import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.Exception.ProductNotFoundException;
import com.rmtech.ecom.Service.Cart_Service;
import com.rmtech.ecom.Service.Order_Service;
import com.rmtech.ecom.Service.Product_Service;
import com.rmtech.ecom.Service.User_Service;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public")
public class Public_Controllers
{
    private final User_Service us;
    private final Cart_Service cs;
    private final Product_Service ps;
    private final Order_Service os;

    public Public_Controllers(User_Service us, Cart_Service cs, Product_Service ps, Order_Service os) {
        this.us = us;
        this.cs = cs;
        this.ps = ps;
        this.os = os;
    }

    @Operation(summary = "Create Product")
    @PostMapping("/user")
    public ResponseEntity<User_dto> create_user(@Valid @RequestBody UserRequest_Dto userRequestDto)
    {
        User_dto userDto=us.create_us(userRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @GetMapping("/products")
    public ResponseEntity<?> get_all_products(@PageableDefault(size = 10) Pageable pageable)
    {
        ProductPageResponse page=ps.getallProducts(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/expensiveproducts")
    public ResponseEntity<?> get_exp_products(

            @RequestParam(required = true)
            Long categoryid,

            @RequestParam(required = false,defaultValue = "0.0")
            Double minPrice,

            @PageableDefault(size = 10) Pageable pageable)
    {
        ProductPageResponse page=ps.getexpensiveProducts(categoryid,minPrice,pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/cheapproducts")
    public ResponseEntity<?> get_cheap_products(

            @RequestParam(required = true)
            Long categoryid,

            @RequestParam(required = false,defaultValue = "100000.0")
            Double maxPrice,

            @PageableDefault(size = 10) Pageable pageable)
    {
        ProductPageResponse page=ps.getcheapProducts(categoryid,maxPrice,pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/parentcategorizedproducts")
    public ResponseEntity<?> get_parentcat_products(    @RequestParam(required = false)
                                                      Long parentcategoryid,

                                                  @PageableDefault(size = 10) Pageable pageable)
    {
        ProductPageResponse page=ps.getparentcategorizedProducts(parentcategoryid,pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<?> get_product(@PathVariable Long id) {

        Product_dto productdto = ps.get_prod(id);
        return ResponseEntity.ok(productdto);
    }



}
