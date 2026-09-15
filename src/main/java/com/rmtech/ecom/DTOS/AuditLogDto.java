package com.rmtech.ecom.DTOS;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AuditLogDto {
    private String id;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private String performedBy;
    private LocalDateTime timestamp;
    private Boolean success;
    private String failureReason;
}
