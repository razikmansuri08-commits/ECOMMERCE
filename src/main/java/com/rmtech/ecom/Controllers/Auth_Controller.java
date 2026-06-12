package com.rmtech.ecom.Controllers;

import com.rmtech.ecom.Config.JwtUtil;
import com.rmtech.ecom.DTOS.AuthRequest;
import com.rmtech.ecom.DTOS.AuthResponse;
import com.rmtech.ecom.DTOS.RefreshTokenRequest;
import com.rmtech.ecom.Entities.RefreshToken;
import com.rmtech.ecom.Exception.UserNotFoundException;
import com.rmtech.ecom.Repositories.RefreshTokenRepository;
import com.rmtech.ecom.Service.AuthService;
import com.rmtech.ecom.Service.RefreshTokenService;
import com.rmtech.ecom.Service.TokenBlacklist_Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class Auth_Controller
{
    private final AuthService authService;

    public Auth_Controller(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {

        return ResponseEntity.ok(authService.login(authRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @RequestBody RefreshTokenRequest request) {

            AuthResponse response = authService.refresh(request.getRefreshToken());
            return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest httpRequest,
           @RequestBody RefreshTokenRequest refreshTokenRequest) {

            authService.logout(httpRequest, refreshTokenRequest);
            return ResponseEntity.ok("Logged out successfully");


    }


}
