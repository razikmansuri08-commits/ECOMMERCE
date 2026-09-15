package com.rmtech.ecom.Config;

import com.resend.Resend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    @Value("${resend.api-key:${RESEND_API_KEY:}}")
    private String apiKey;

    @Bean
    public Resend resendClient() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("resend.api-key (or RESEND_API_KEY) must be set to send email");
        }
        return new Resend(apiKey);
    }
}
