package com.rmtech.ecom.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmtech.ecom.DTOS.Error_ResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    public JwtAuthEntryPoint(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.setContentType("application/json");

        Error_ResponseDto errorResponse =
                new Error_ResponseDto(
                        LocalDateTime.now(),
                        authException.getMessage(),
                        "UNAUTHORIZED",
                        401,
                        request.getRequestURI()
                );


        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
