package com.rmtech.ecom.Config;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class TokenHashUtil {

    public String hash(String token) {

        try {

            MessageDigest md =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    md.digest(token.getBytes());

            return HexFormat.of()
                    .formatHex(hash);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }
}
