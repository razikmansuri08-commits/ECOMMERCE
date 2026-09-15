package com.rmtech.ecom.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmtech.ecom.DTOS.Error_ResponseDto;
import com.rmtech.ecom.Exception.JwtTokenExpiredException;
import com.rmtech.ecom.Exception.JwtTokenInvalidException;
import com.rmtech.ecom.Service.TokenBlacklist_Service;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null || path.isBlank()) {
            path = request.getRequestURI();
        }
        // Let permitAll endpoints through without touching Redis/JWT so that
        // infrastructure outages can never turn them into 401s.
        return path.startsWith("/public/")
                || path.equals("/auth/login")
                || path.equals("/auth/refresh")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator/health")
                || path.equals("/")
                || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                if (token.isBlank()) {
                    throw new JwtTokenInvalidException("Token is invalid");
                }

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
        } catch (JwtTokenExpiredException ex) {
            writeError(response, request, ex.getMessage(), "TOKEN_EXPIRED", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (JwtTokenInvalidException ex) {
            writeError(response, request, ex.getMessage(), "TOKEN_INVALID", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (ExpiredJwtException ex) {
            writeError(response, request, "Token is expired", "TOKEN_EXPIRED", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT validation failed for {}: {}", request.getRequestURI(), ex.getMessage());
            writeError(response, request, "Token is invalid", "TOKEN_INVALID", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (AuthenticationException ex) {
            authenticationEntryPoint.commence(request, response, ex);
        } catch (AccessDeniedException ex)
        {
            writeError(response, request, "You do not have permission to perform this action.", "FORBIDDEN", HttpServletResponse.SC_FORBIDDEN);
        } catch (RuntimeException ex) {
            // Infrastructure failure (e.g. Redis/DB down). Never mask as 401.
            log.warn("JwtFilter infrastructure failure for {}: {}", request.getRequestURI(), ex.toString());
            writeError(response, request, "Authentication service temporarily unavailable", "SERVICE_UNAVAILABLE", HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    private void writeError(HttpServletResponse response, HttpServletRequest request, String message, String code, int status) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        Error_ResponseDto er = new Error_ResponseDto(
                LocalDateTime.now(),
                message,
                code,
                status,
                request.getRequestURI());
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(er));
    }
}
