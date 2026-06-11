package com.rmtech.ecom.DTOS;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class ValidationErrorResponse_Dto {
    public ValidationErrorResponse_Dto(LocalDateTime timestamp, String message, String error, int status, Map<String,String> fielderrors,String path)
    {
        this.timestamp=timestamp;
        this.message = message;
        this.error=error;
        this.status=status;
        this.path=path;
        this.fielderrors=fielderrors;
    }

    public LocalDateTime timestamp;
    public String message;
    public String error;
    public int status;
        public String path;
    public Map<String,String> fielderrors;
}
