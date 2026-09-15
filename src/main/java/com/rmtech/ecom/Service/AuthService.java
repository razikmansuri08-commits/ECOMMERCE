package com.rmtech.ecom.Service;

import com.rmtech.ecom.Config.JwtUtil;
import com.rmtech.ecom.DTOS.AuthRequest;
import com.rmtech.ecom.DTOS.AuthResponse;
import com.rmtech.ecom.DTOS.RefreshTokenRequest;
import com.rmtech.ecom.Entities.RefreshToken;
import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.Exception.JwtTokenInvalidException;
import com.rmtech.ecom.Exception.UserLOckedException;
import com.rmtech.ecom.Exception.UserNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.rmtech.ecom.Repositories.RefreshTokenRepository;
import com.rmtech.ecom.Repositories.User_Repo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklist_Service tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;
    private final User_Repo userRepository;
    private final LoginAttemptService loginAttemptService;
    public AuthService(PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsService userDetailsService, TokenBlacklist_Service tbs, RefreshTokenService rts, User_Repo userRepository, LoginAttemptService loginAttemptService) {
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tbs;
        this.refreshTokenService = rts;
        this.userRepository = userRepository;
        this.loginAttemptService = loginAttemptService;
    }

    public AuthResponse login(AuthRequest request) {

        if (loginAttemptService.isLocked(request.getUsername())) {

            Long remainingtime=loginAttemptService.getremainingLockTime(request.getUsername());
            throw new UserLOckedException("try again after " + remainingtime + " seconds");
        }
            UserDetails userDetails;
            try {
                userDetails = userDetailsService
                        .loadUserByUsername(request.getUsername());
            } catch (UsernameNotFoundException e) {
                loginAttemptService.loginFailed(request.getUsername());
                throw new com.rmtech.ecom.Exception.BadCredentialsException("Invalid credentials");
            }
            if(!passwordEncoder.matches(request.getPassword(), userDetails.getPassword()))
            {
                loginAttemptService.loginFailed(request.getUsername());
                throw new com.rmtech.ecom.Exception.BadCredentialsException("Invalid credentials");
            }

            String accessToken = jwtUtil.generateToken(userDetails.getUsername());
            String refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());
            loginAttemptService.loginSucces(request.getUsername());

            return new AuthResponse(accessToken, refreshToken);
    }

//    public AuthResponse refresh(String rawRefreshToken) {
//
//        RefreshToken oldToken = refreshTokenService
//                .findByRawToken(rawRefreshToken)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
//
//        User user = userRepository.findbyid(oldToken.getUser().getId());
//        refreshTokenService.deleteRefreshToken(oldToken);
//        String newToken = refreshTokenService.createRefreshToken(user.getName());
//
//        String accessToken = jwtUtil.generateToken(user.getName());
//
//        return new AuthResponse(accessToken, newToken);
//    }



    public void logout(HttpServletRequest httpRequest, RefreshTokenRequest refreshTokenRequest) {

        String header = httpRequest.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("No token provided");
        }

        String accessToken = header.substring(7);
        if (accessToken.isBlank()) {
            throw new IllegalArgumentException("No token provided");
        }
        try {
            tokenBlacklistService.addTokenToBlacklist(
                    accessToken,
                    jwtUtil.extractExpiration(accessToken)
            );
        } catch (io.jsonwebtoken.JwtException ex) {
            throw new JwtTokenInvalidException("Token is invalid");
        }
        RefreshToken refreshToken = refreshTokenService
                .findByRawToken(refreshTokenRequest.getRefreshToken())
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid refresh token"
                        )
                );
        refreshTokenService.deleteRefreshToken(refreshToken);

    }
}
