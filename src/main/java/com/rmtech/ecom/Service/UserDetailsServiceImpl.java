package com.rmtech.ecom.Service;


import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.Exception.UserNotFoundException;
import com.rmtech.ecom.Repositories.User_Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {
    public UserDetailsServiceImpl(User_Repo ur) {
        this.ur = ur;
    }

    private final User_Repo ur;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user=ur.findbyusername(username);
        if(user==null)
        {
            throw new UserNotFoundException("user not found");

        }
        List<GrantedAuthority> authorities=user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getName())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}
