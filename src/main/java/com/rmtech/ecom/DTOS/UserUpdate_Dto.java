package com.rmtech.ecom.DTOS;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

@Getter
@Setter
public class UserUpdate_Dto
{
    @NotBlank(message = "Username is required")
    @Size(max = 30,min=3)
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "Username must contain only letters"
    )
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @UniqueElements
    private String email;

    @NotBlank(message = "Password is required")
    @Size(max = 25, message = "Password must not exceed 25 characters")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;


}
