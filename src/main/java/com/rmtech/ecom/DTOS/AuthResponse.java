package com.rmtech.ecom.DTOS;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    public AuthResponse(String accesstoken,String refreshToken) {
        this.accesstoken = accesstoken;
        this.refreshToken=refreshToken;
    }
    public String accesstoken;
    public String refreshToken;
}
