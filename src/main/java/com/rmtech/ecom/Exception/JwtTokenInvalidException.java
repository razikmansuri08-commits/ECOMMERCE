package com.rmtech.ecom.Exception;

import javax.naming.AuthenticationException;

public class JwtTokenInvalidException extends AuthenticationException{
    public JwtTokenInvalidException(String message) {
        super(message);
    }
}
