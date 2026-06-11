package com.rmtech.ecom.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmtech.ecom.DTOS.Error_ResponseDto;
import com.rmtech.ecom.Exception.JwtTokenExpiredException;
import com.rmtech.ecom.Exception.JwtTokenInvalidException;
import com.rmtech.ecom.Service.TokenBlacklist_Service;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;


@Component
public class JwtFilter extends OncePerRequestFilter
{

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklist_Service tbs;
    private final JwtAuthEntryPoint authenticationEntryPoint;
    private final ObjectMapper objectMapper;

    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, TokenBlacklist_Service tbs, JwtAuthEntryPoint authenticationEntryPoint, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tbs = tbs;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                if (tbs.isBlacklisted(token)) {
                    throw new JwtTokenExpiredException("Token is expired");
                }

                String username = jwtUtil.getUsernameFromToken(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtUtil.validateToken(token, userDetails.getUsername())) {
                        UsernamePasswordAuthenticationToken authtoken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authtoken);
                    } else {
                        throw new JwtTokenInvalidException("Token is invalid");
                    }
                }
            }
            filterChain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            authenticationEntryPoint.commence(request, response, ex);
        } catch (JwtTokenExpiredException ex) {
            Error_ResponseDto er = new Error_ResponseDto
                    (LocalDateTime.now(),
                            ex.getMessage(),
                            "TOKEN_EXPIRED",
                            401,
                            request.getRequestURI());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(er));
        } catch (JwtTokenInvalidException ex) {

            Error_ResponseDto er = new Error_ResponseDto
                    (LocalDateTime.now(),
                            ex.getMessage(),
                            "TOKEN_INVALID",
                            401,
                                request.getRequestURI());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(er));
        }
        catch (AccessDeniedException ex)
        {
            Error_ResponseDto er=new Error_ResponseDto
                    (LocalDateTime.now(),
                            "You do not have permission to perform this action.",
                            "FORBIDDEN",
                            403,
                            request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(er));
        }
    }
}
