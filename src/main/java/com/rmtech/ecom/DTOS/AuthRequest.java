package com.rmtech.ecom.DTOS;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AuthRequest implements Serializable {
    private String username;
    private String password;
}
