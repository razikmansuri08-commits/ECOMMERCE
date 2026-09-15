package com.rmtech.ecom.DTOS;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDto {
    private String id;
    private Long userId;
    private String username;
    private String type;
    private String title;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private String link;
}
