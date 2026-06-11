package com.rmtech.ecom.Exception;

public class ProductNotFoundException extends RuntimeException{
 public ProductNotFoundException(String message) {
        super(message);
    }
}
