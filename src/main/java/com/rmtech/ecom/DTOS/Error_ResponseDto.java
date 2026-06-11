package com.rmtech.ecom.DTOS;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class Error_ResponseDto
{
    public Error_ResponseDto(LocalDateTime timestamp,String message,String error,int status,String path)
    {
        this.timestamp=timestamp;
        this.message = message;
        this.error=error;
        this.status=status;
        this.path=path;
    }

    public LocalDateTime timestamp;
    public String message;
    public String error;
    public int status;
    public String path;


}
