package com.rmtech.ecom.DTOS;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CategoryDto implements Serializable {

    private Long id;
    private String name;
    private Long parent_id;
}
