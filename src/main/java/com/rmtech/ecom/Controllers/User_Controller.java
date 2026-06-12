package com.rmtech.ecom.Controllers;
import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.DTOS.UserUpdate_Dto;
import com.rmtech.ecom.Service.User_Service;
import com.rmtech.ecom.DTOS.User_dto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;


@RestController
@RequestMapping("/user")
public class User_Controller {
    private final User_Service uss;

    public User_Controller(User_Service uss) {
        this.uss = uss;
    }

    @GetMapping("/register")
    public ResponseEntity<?> get_user()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User_dto userDto = uss.get_us(username);

        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }

    @PatchMapping("/update")
    public ResponseEntity<?> update_user(@RequestBody UserUpdate_Dto user)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User_dto updated_user = uss.update_us(username, user);

        return ResponseEntity.status(HttpStatus.OK).body(updated_user);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete_user()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        uss.delete_us(username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getorderstatus")
    public ResponseEntity<?> get_status( @Valid @RequestBody String orderId)
    {
        return ResponseEntity.ok(uss.getOrderStatus(orderId));
    }

}




