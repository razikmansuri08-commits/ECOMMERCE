package com.rmtech.ecom.DTOS;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserPageResponse {

    private List<User_dto> user_dtos;

    private int currentPage;

    private int totalPages;

    private long totalItems;

}
