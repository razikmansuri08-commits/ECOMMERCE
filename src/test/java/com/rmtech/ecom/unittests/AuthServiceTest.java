package com.rmtech.ecom.unittests;

import com.rmtech.ecom.Config.JwtUtil;
import com.rmtech.ecom.DTOS.AuthRequest;
import com.rmtech.ecom.DTOS.AuthResponse;
import com.rmtech.ecom.DTOS.RefreshTokenRequest;
import com.rmtech.ecom.Entities.RefreshToken;
import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.Repositories.User_Repo;
import com.rmtech.ecom.Service.AuthService;
import com.rmtech.ecom.Service.LoginAttemptService;
import com.rmtech.ecom.Service.RefreshTokenService;
import com.rmtech.ecom.Service.TokenBlacklist_Service;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private  PasswordEncoder passwordEncoder;
    @Mock
    private  JwtUtil jwtUtil;
    @Mock
    private  UserDetailsService userDetailsService;
    @Mock
    private  TokenBlacklist_Service tokenBlacklistService;
    @Mock
    private  RefreshTokenService refreshTokenService;
    @Mock
    private User_Repo userRepo;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthService authService;



    @Test
    void shouldReturnAuthResponseWhenLoginIsSuccessful() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("username");
        authRequest.setPassword("password");

        User user=new User();
        user.setName("username");
        user.setPassword("password");

        when(loginAttemptService.isLocked("username")).thenReturn(false);
        when(passwordEncoder.matches("password","hashedPassword")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("username")).thenReturn(org.springframework.security.core.userdetails.User.builder()
                .username(user.getName())
                .password("hashedPassword")
                .build());


        when(jwtUtil.generateToken("username")).thenReturn("accessToken");
        when(refreshTokenService.createRefreshToken("username")).thenReturn("refreshToken");

        AuthResponse authResponse = authService.login(authRequest);

        assertEquals("accessToken", authResponse.getAccesstoken());
        assertEquals("refreshToken", authResponse.getRefreshToken());

        verify(userDetailsService)
                .loadUserByUsername("username");

        verify(jwtUtil)
                .generateToken("username");

        verify(refreshTokenService)
                .createRefreshToken("username");
    }

    @Test
    void shouldReturnAuthResponseWhenRefreshIsSuccessful() {

        LocalDateTime expiry=LocalDateTime.now().plusMinutes(60);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setExpiryDate(expiry);

        User user = new User();
        user.setId(1L);
        user.setName("username");
        refreshToken.setUser(user);



        when(refreshTokenService.findByRawToken("refreshToken")).thenReturn(Optional.of(refreshToken));

        when(userRepo.findbyid(1L)).thenReturn(user);
        when(jwtUtil.generateToken("username")).thenReturn("accessToken");
        when(refreshTokenService.createRefreshToken("username")).thenReturn("newrefreshToken");




        AuthResponse authResponse = authService.refresh("refreshToken");

        assertEquals("accessToken", authResponse.getAccesstoken());
        assertEquals("newrefreshToken", authResponse.getRefreshToken());

        verify(jwtUtil).generateToken("username");
        verify(refreshTokenService).createRefreshToken("username");
        verify(refreshTokenService).deleteRefreshToken(refreshToken);
        verifyNoMoreInteractions(jwtUtil,refreshTokenService);
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenIsInvalid()
    {
        when(refreshTokenService.findByRawToken("badToken"))
                .thenReturn(empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.refresh(
                        "badToken"
                )
        );
    }

    @Test
    void shouldLogoutSuccessfully() {

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);

        LocalDateTime expiry = LocalDateTime.now().plusMinutes(60);

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refreshToken");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer accessToken");
        when(refreshTokenService.findByRawToken("refreshToken")).thenReturn(Optional.of(refreshToken));
        when(jwtUtil.extractExpiration("accessToken")).thenReturn(expiry);

        authService.logout(request,refreshTokenRequest);
        verify(tokenBlacklistService).addTokenToBlacklist("accessToken",expiry );
        verify(refreshTokenService).deleteRefreshToken(refreshToken);
    }

    @Test
    void shouldThrowExceptionWhenHeaderMissing() {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        RefreshTokenRequest dto =
                new RefreshTokenRequest();

        dto.setRefreshToken("refreshToken");

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.logout(request, dto)
        );
    }

    @Test
    void shouldLogoutEvenWhenRefreshTokenDoesNotExist()
    {
        LocalDateTime expiry =
                LocalDateTime.now().plusMinutes(60);

        RefreshTokenRequest dto =
                new RefreshTokenRequest();

        dto.setRefreshToken("invalidToken");

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer accessToken");

        when(refreshTokenService.findByRawToken("invalidToken"))
                .thenReturn(Optional.empty());


        when(jwtUtil.extractExpiration("accessToken"))
                .thenReturn(expiry);


        assertThrows(
                IllegalArgumentException.class,
                () -> authService.logout(request, dto)
        );

        verify(refreshTokenService, never())
                .deleteRefreshToken(any());

        verify(tokenBlacklistService)
                .addTokenToBlacklist(
                        "accessToken",
                        expiry
                );
    }


}
