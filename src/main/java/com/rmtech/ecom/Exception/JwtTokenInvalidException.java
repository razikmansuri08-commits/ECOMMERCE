package com.rmtech.ecom.Exception;

import org.springframework.security.core.AuthenticationException;

public class JwtTokenInvalidException extends AuthenticationException{
    public JwtTokenInvalidException(String message) {
        super(message);
    }
}
