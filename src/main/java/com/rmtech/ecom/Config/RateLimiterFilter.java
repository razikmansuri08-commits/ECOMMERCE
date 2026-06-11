package com.rmtech.ecom.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmtech.ecom.DTOS.Error_ResponseDto;
import com.rmtech.ecom.Exception.TooManyRequestsException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    public RateLimiterFilter(RateLimiterService rateLimiterService, ObjectMapper objectMapper) {
        this.rateLimiterService = rateLimiterService;
        this.objectMapper = objectMapper;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            String path = request.getServletPath();
            if (path == null || path.isBlank()) {
                path = request.getRequestURI();
            }

            String clientIdentifier = getClientIdentifier(request);
            if (rateLimiterService.isAllowed(request.getMethod(), path, clientIdentifier)) {
                filterChain.doFilter(request, response);
            } else {
                throw new TooManyRequestsException("Rate limit exceeded");
            }
        } catch (TooManyRequestsException ex) {
            Error_ResponseDto er = new Error_ResponseDto
                    (LocalDateTime.now(),
                            ex.getMessage(),
                            "TOO_MANY_REQUESTS",
                            429,
                            request.getRequestURI());
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(er));
        }
    }
    private String getClientIdentifier(
            HttpServletRequest request
    )
    {
        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if(auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken))
        {
            return "USER:" + auth.getName();
        }

        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && !ip.isBlank()) {
            ip = ip.split(",")[0].trim();
        }

        if(ip == null || ip.isBlank())
            ip = request.getRemoteAddr();

        return "IP:" + ip;
    }
}
