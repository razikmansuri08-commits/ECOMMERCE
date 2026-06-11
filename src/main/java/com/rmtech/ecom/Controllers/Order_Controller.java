package com.rmtech.ecom.Controllers;

import com.rmtech.ecom.DTOS.Order_Dto;
import com.rmtech.ecom.DTOS.Order_ItemsDto;
import com.rmtech.ecom.Exception.OrderNotFoundException;
import com.rmtech.ecom.Service.Order_Service;
import com.rmtech.ecom.Entities.Orders;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user/orders")
public class Order_Controller
{
    private final
    Order_Service os;


    public Order_Controller(Order_Service os) {
        this.os = os;

    }

    @PostMapping("/placeorder")
    public ResponseEntity<?> place_order()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Order_Dto orderDto=os.place_ord(username);

        return ResponseEntity.status(200).body(orderDto);
    }

    @GetMapping("/orders")
    public ResponseEntity<?> get_all_orders(@PageableDefault(size = 10) Pageable pageable)
    {
       return ResponseEntity.status(200).body(os.get_UserOrds(pageable));
    }


    @GetMapping("/order/{orderId}")//rem
    public ResponseEntity<?> get_order(@PathVariable String orderId)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Order_Dto orderDto=os.get_ord(orderId,username);
        return ResponseEntity.status(200).body(orderDto);
    }

    @DeleteMapping("/order/{orderid}")
    public ResponseEntity<?> cancel_order(@PathVariable String orderid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        if (os.cancel_ord(orderid, username))
        {
            return ResponseEntity.status(200).body("Order cancelled successfully");
        }
        else
        {
            return ResponseEntity.status(404).body("Order not found");
        }
    }

}
