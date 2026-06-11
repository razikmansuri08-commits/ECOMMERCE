package com.rmtech.ecom.DTOS;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class CategoryTreeDto implements Serializable {

    private Long id;
    private String name;
    private List<CategoryTreeDto> children;

}
