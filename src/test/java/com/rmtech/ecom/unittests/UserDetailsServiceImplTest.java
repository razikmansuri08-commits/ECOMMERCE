package com.rmtech.ecom.unittests;

import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.Entities.UserRoles;
import com.rmtech.ecom.Exception.BadCredentialsException;
import com.rmtech.ecom.Exception.UserNotFoundException;
import com.rmtech.ecom.Repositories.User_Repo;
import com.rmtech.ecom.Service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private User_Repo userRepo;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void shouldLoadUserDetailsWithAuthorities() {
        User user = new User();
        user.setName("razik");
        user.setPassword("encoded-password");
        user.setRoles(List.of(UserRoles.USER, UserRoles.ADMIN));

        when(userRepo.findbyusername("razik")).thenReturn(user);

        UserDetails result = userDetailsService.loadUserByUsername("razik");

        assertEquals("razik", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
        assertTrue(
                result.getAuthorities()
                        .stream()
                        .anyMatch(authority -> authority.getAuthority().equals("USER"))
        );
        assertTrue(
                result.getAuthorities()
                        .stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ADMIN"))
        );
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(userRepo.findbyusername("missing")).thenReturn(null);

        assertThrows(
                BadCredentialsException.class,
                () -> userDetailsService.loadUserByUsername("missing")
        );
    }
}
