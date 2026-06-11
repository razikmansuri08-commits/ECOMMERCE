package com.rmtech.ecom.Exception;

import com.rmtech.ecom.Config.JwtAuthEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.naming.AuthenticationException;

public class JwtTokenExpiredException extends AuthenticationException{
    public JwtTokenExpiredException(String message) {
        super(message);
    }
}
