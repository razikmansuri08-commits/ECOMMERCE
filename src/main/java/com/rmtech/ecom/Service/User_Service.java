package com.rmtech.ecom.Service;
import com.rmtech.ecom.DTOS.*;
import com.rmtech.ecom.Entities.*;
import com.rmtech.ecom.Exception.EmailAlreadyExistsException;
import com.rmtech.ecom.Exception.OrderNotFoundException;
import com.rmtech.ecom.Exception.UserAlreadyExistsException;
import com.rmtech.ecom.Exception.UserNotFoundException;
import com.rmtech.ecom.Repositories.User_Repo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class User_Service
{
    private final User_Repo ur;
    private final Order_Service os;


    public User_Service(User_Repo ur, Order_Service os, PasswordEncoder passwordEncoder) {
        this.os = os;
        this.passwordEncoder = passwordEncoder;
        this.ur = ur;
    }

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User_dto create_us(UserRequest_Dto userdto)
    {
        if (ur.existsByEmail(userdto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if(ur.existsByname(userdto.getName()))
            throw new UserAlreadyExistsException("Username already exists");
        User user = new User();
        if (userdto.getRole() == null || userdto.getRole().isEmpty()) {
            user.setRoles(List.of(UserRoles.USER));
        } else if ((userdto.getRole().equals("USER"))) {
            user.setRoles(List.of(UserRoles.ADMIN));
        }
        else if(userdto.getRole().equals("ADMIN"))
            user.setRoles(List.of(UserRoles.ADMIN));
        user.setName(userdto.getName());
        user.setEmail(userdto.getEmail());
        user.setPassword(passwordEncoder.encode(userdto.getPassword()));
        Cart cart = new Cart();
        user.setCart(cart);
        cart.setUser(user);
        ur.save(user);
        return convertToDTO(user);
    }

    public User_dto get_us(String username)
    {
        User user=ur.findbyusername(username);
        if (user == null)
            throw new UserNotFoundException("user not found");
        return convertToDTO(user);
    }

    public UserPageResponse getallusers(Pageable pageable)
    {
        Page<User> userPage = ur.findAll(pageable);
        List<User_dto> userdtos  = userPage.getContent()
                .stream().map(this::convertToDTO).toList();
        UserPageResponse response=new UserPageResponse();
        response.setUser_dtos(userdtos);
        response.setCurrentPage(userPage.getNumber());
        response.setTotalPages(userPage.getTotalPages());
        response.setTotalItems(userPage.getTotalElements());
        return response;
    }

    public User_dto get_us_by_id(Long id)
    {
        User user= ur.findById(id).orElseThrow(()->new UserNotFoundException("user not found"));
        return convertToDTO(user);
    }

    @Transactional
    public User_dto update_us(String username, UserUpdate_Dto dto)
    {
        User us = ur.findbyusername(username);
        if (us == null)
            throw new UserNotFoundException("user not found");
        if (dto.getName() != null)
            us.setName(dto.getName());
        if (dto.getEmail() != null)
            us.setEmail(dto.getEmail());
        if (dto.getPassword() != null)
            us.setPassword(passwordEncoder.encode(dto.getPassword()));
        ur.save(us);

        return convertToDTO(us);
    }

    @Transactional
    public void delete_us(String name)
    {

        User user=ur.findbyusername(name);
        if (user == null)
            throw new UserNotFoundException("user not found");
        if (user.getCart() != null) {
            user.getCart().setUser(null);
        }
        for (Orders o : user.getOrder()) {
            o.setUser(null);
        }
        ur.delete(user);
    }

    @Transactional
    public void delete_us_by_id(Long id)
    {
        User user=ur.findById(id).orElseThrow(()->new UserNotFoundException("user not found"));
        if (user.getCart() != null) {
            user.getCart().setUser(null);
        }
        for (Orders o : user.getOrder()) {
            o.setUser(null);
        }
        ur.delete(user);
    }
    public OrderStatus getOrderStatus(String id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return os.getUserOrderStatus(id, username);
    }

    private User_dto convertToDTO(User user) {
        User_dto userDto = new User_dto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles().stream().map(Enum::name).toList());
        return userDto;

    }
}



