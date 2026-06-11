package com.rmtech.ecom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableCaching
public class EcomApplication {

	public static void main(String[] args)
	{
		SpringApplication.run(EcomApplication.class, args);
	}
}
