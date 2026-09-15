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
import java.util.Locale;

@Service
public class User_Service
{
    private final User_Repo ur;
    private final Order_Service os;
    private final PasswordEncoder passwordEncoder;

    public User_Service(User_Repo ur, Order_Service os, PasswordEncoder passwordEncoder) {
        this.os = os;
        this.passwordEncoder = passwordEncoder;
        this.ur = ur;
    }

    @Transactional
    public User_dto create_us(UserRequest_Dto userdto)
    {
        // Case-insensitive email check
        if (ur.existsByEmailIgnoreCase(userdto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        // Case-insensitive name check
        if(ur.existsByUsernameIgnoreCase(userdto.getName())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        
        User user = new User();
        user.setRoles(List.of(UserRoles.USER));
        user.setName(userdto.getName().trim());
        user.setEmail(userdto.getEmail().trim().toLowerCase(Locale.ROOT));
        user.setPassword(passwordEncoder.encode(userdto.getPassword()));
        
        Cart cart = new Cart();
        user.setCart(cart);
        cart.setUser(user);
        
        User savedUser = ur.save(user);
        return convertToDTO(savedUser);
    }

    public User_dto get_us(String username)
    {
        User user = ur.findbyusername(username);
        if (user == null)
            throw new UserNotFoundException("User not found");
        return convertToDTO(user);
    }

    public UserPageResponse getallusers(Pageable pageable)
    {
        Page<User> userPage = ur.findAll(pageable);
        List<User_dto> userdtos = userPage.getContent()
                .stream().map(this::convertToDTO).toList();
        UserPageResponse response = new UserPageResponse();
        response.setUser_dtos(userdtos);
        response.setCurrentPage(userPage.getNumber());
        response.setTotalPages(userPage.getTotalPages());
        response.setTotalItems(userPage.getTotalElements());
        return response;
    }

    public User_dto get_us_by_id(Long id)
    {
        User user = ur.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Transactional
    public User_dto update_us(String username, UserUpdate_Dto dto)
    {
        User us = ur.findbyusername(username);
        if (us == null)
            throw new UserNotFoundException("User not found");
        
        if (dto.getName() != null && !dto.getName().equals(us.getName())) {
            String normalizedName = dto.getName().trim();
            if(ur.existsByUsernameIgnoreCase(normalizedName)) {
                throw new UserAlreadyExistsException("Username already exists");
            }
            us.setName(normalizedName);
        }
        
        if (dto.getEmail() != null && !dto.getEmail().equals(us.getEmail())) {
            String normalizedEmail = dto.getEmail().trim().toLowerCase(Locale.ROOT);
            if(ur.existsByEmailIgnoreCase(normalizedEmail)) {
                throw new EmailAlreadyExistsException("Email already exists");
            }
            us.setEmail(normalizedEmail);
        }
        
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            us.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        
        User savedUser = ur.save(us);
        return convertToDTO(savedUser);
    }

    @Transactional
    public void delete_us(String name)
    {
        User user = ur.findbyusername(name);
        if (user == null)
            throw new UserNotFoundException("User not found");
        
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
        User user = ur.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
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
