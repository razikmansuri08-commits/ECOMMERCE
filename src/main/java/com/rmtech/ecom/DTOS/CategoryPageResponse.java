package com.rmtech.ecom.DTOS;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class CategoryPageResponse implements Serializable {

    private List<CategoryDto> categoryDtos;

    private int currentPage;

    private int totalPages;

    private long totalItems;

}
