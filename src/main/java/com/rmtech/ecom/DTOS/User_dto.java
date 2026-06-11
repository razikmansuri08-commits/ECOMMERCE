package com.rmtech.ecom.DTOS;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class User_dto
{
    private Long id;
    private String name;
    private String email;
    private List<String>Roles=new ArrayList<>();
}
