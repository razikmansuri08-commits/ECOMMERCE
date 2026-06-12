package com.rmtech.ecom.DTOS;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class User_dto
{
    private Long id;
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @UniqueElements
    private String email;
    private List<String>Roles=new ArrayList<>();
}
