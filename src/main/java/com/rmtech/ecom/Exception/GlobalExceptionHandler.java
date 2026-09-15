package com.rmtech.ecom.Exception;

import com.rmtech.ecom.DTOS.Error_ResponseDto;
import com.rmtech.ecom.DTOS.ValidationErrorResponse_Dto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cache.CacheException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler
{
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Error_ResponseDto> handleUserNotFound(UserNotFoundException ex,HttpServletRequest request)
    {
        Error_ResponseDto er=new Error_ResponseDto
                (LocalDateTime.now(),
                 ex.getMessage(),
                "NOT_FOUND",
                404,
                        request.getRequestURI());
        return ResponseEntity.status(404).body(er);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Error_ResponseDto> handleBadCredentialsException(BadCredentialsException ex,HttpServletRequest request)
    {
        Error_ResponseDto er=new Error_ResponseDto
                (LocalDateTime.now(),
                        ex.getMessage(),
                        "BAD_REQUEST",
                        400,
                        request.getRequestURI());
        return ResponseEntity.status(400).body(er);
    }


    @ExceptionHandler(UserLOckedException.class)
    public ResponseEntity<Error_ResponseDto> handleUserLocked(UserLOckedException ex,HttpServletRequest request)
    {
        Error_ResponseDto er=new Error_ResponseDto
                (LocalDateTime.now(),
                        ex.getMessage(),
                        "BAD_REQUEST",
                        HttpStatus.LOCKED.value(),
                        request.getRequestURI());
        return ResponseEntity.status(423).body(er);
    }
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Error_ResponseDto> handleTooManyReqException(TooManyRequestsException ex,HttpServletRequest request)
    {
        Error_ResponseDto er=new Error_ResponseDto
                (LocalDateTime.now(),
                        ex.getMessage(),
                        "TOO_MANY_REQUESTS",
                        429,
                        request.getRequestURI());
        return ResponseEntity.status(429).body(er);
    }
    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<Error_ResponseDto> handleTooManyReqException(RefreshTokenException ex,HttpServletRequest request)
    {
        Error_ResponseDto er=new Error_ResponseDto
                (LocalDateTime.now(),
                        ex.getMessage(),
                        "REFRESH_TOKEN_Erequest.getRequestURI()CEPTION",
                        403,
                        request.getRequestURI());
        return ResponseEntity.status(403).body(er);
    }



    @ExceptionHandler(MethodArgumentInvalid.class)
    public ResponseEntity<?> handleValidationException(
            MethodArgumentInvalid ex,HttpServletRequest request)
    {
//        log.error("Validation failed:", ex);

        Error_ResponseDto er=new Error_ResponseDto(
                LocalDateTime.now(),
                "VALIDATION_ERROR",
                "BAD_REQUEST",
                400,
                request.getRequestURI()
        );

        return ResponseEntity.status(400).body(er);
    }

    @ExceptionHandler(CacheException.class)
    public ResponseEntity<Error_ResponseDto> handleCacheExceptionFound(CacheException ex, HttpServletRequest request)
    {
        Error_ResponseDto er=new Error_ResponseDto
                (LocalDateTime.now(),
                        ex.getMessage(),
                        "NOT_FOUND",
                        503,
                        request.getRequestURI());
        return ResponseEntity.status(503).body(er);
    }

    @ExceptionHandler(ProductNotFoundException.class)
   public ResponseEntity<Error_ResponseDto> handleProductNotFound(ProductNotFoundException ex, HttpServletRequest request)
   {
       Error_ResponseDto er=new Error_ResponseDto
               (LocalDateTime.now(),
                    ex.getMessage(),
                    "NOT_FOUND",
                    404,
               request.getRequestURI());
        return ResponseEntity.status(404).body(er);
   }
    @ExceptionHandler(CartNotFoundException.class)
   public ResponseEntity<Error_ResponseDto> handleCartNotFound(CartNotFoundException ex,HttpServletRequest request)
   {
       Error_ResponseDto er=new Error_ResponseDto
               (LocalDateTime.now(),
                    ex.getMessage(),
                    "NOT_FOUND",
                    404,
                       request.getRequestURI());
       return ResponseEntity.status(404).body(er);
   }

    @ExceptionHandler(JwtTokenExpiredException.class)
    public ResponseEntity<Error_ResponseDto> handleJwtExpiredException(JwtTokenExpiredException ex,HttpServletRequest request)
    {
        Error_ResponseDto er=new Error_ResponseDto
                (LocalDateTime.now(),
                        ex.getMessage(),
                        "TOKEN_EXPIRED",
                        401,
                        request.getRequestURI());
        return ResponseEntity.status(401).body(er);
    }

    @ExceptionHandler(JwtTokenInvalidException.class)
    public ResponseEntity<Error_ResponseDto> handleInvalidJwtException(JwtTokenInvalidException ex,HttpServletRequest request)
    {
        Error_ResponseDto er=new Error_ResponseDto
                (LocalDateTime.now(),
                        ex.getMessage(),
                        "TOKEN_INVALID",
                        401,
                        request.getRequestURI());
        return ResponseEntity.status(401).body(er);
    }

   @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Error_ResponseDto> handleOrderNotFound(OrderNotFoundException ex,HttpServletRequest request)
    {
        Error_ResponseDto er=new Error_ResponseDto
                (LocalDateTime.now(),
                        ex.getMessage(),
                        "NOT_FOUND",
                        404,
                        request.getRequestURI());
        return ResponseEntity.status(404).body(er);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Error_ResponseDto> handleUserExistException(UserAlreadyExistsException ex,HttpServletRequest request)
    {
        log.error("Unexpected exception", ex);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new Error_ResponseDto(
                        LocalDateTime.now(),
                        "User already exists",
                        "CONFLICT",
                        409,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Error_ResponseDto> handleEmailExistException(EmailAlreadyExistsException ex,HttpServletRequest request)
    {
        log.error("Unexpected exception", ex);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new Error_ResponseDto(
                        LocalDateTime.now(),
                        "Email already exists",
                        "CONFLICT",
                        409,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<Error_ResponseDto> handleEmailSendException(EmailSendException ex,HttpServletRequest request)
    {
        log.error("Email send failed", ex);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new Error_ResponseDto(
                        LocalDateTime.now(),
                        "Failed to send email. Please try again later.",
                        "EMAIL_SEND_ERROR",
                        503,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<Error_ResponseDto> handleInventoryNotFound(InventoryNotFoundException ex,HttpServletRequest request)
    {
        Error_ResponseDto er=new Error_ResponseDto
                (LocalDateTime.now(),
                        ex.getMessage(),
                        "NOT_FOUND",
                        404,
                        request.getRequestURI());
        return ResponseEntity.status(404).body(er);
    }


    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Error_ResponseDto> handleInventoryNotFound(InsufficientStockException ex,HttpServletRequest request)
    {
        Error_ResponseDto er=new Error_ResponseDto
                (LocalDateTime.now(),
                        ex.getMessage(),
                        "BAD_REQUEST",
                        400,
                        request.getRequestURI());
        return ResponseEntity.status(400).body(er);
    }
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Error_ResponseDto>
    handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex,
            HttpServletRequest request)
    {
        return ResponseEntity.status(409)
                .body(
                        new Error_ResponseDto(
                                LocalDateTime.now(),
                                "Inventory was modified by another request. Please try again.",
                                "CONFLICT",
                                409,
                                request.getRequestURI()
                        )
                );
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<Error_ResponseDto> handleInvalidOrderStatusException(InvalidOrderStatusException ex,HttpServletRequest request)
    {

        return ResponseEntity.status(400)
                .body(new Error_ResponseDto(
                        LocalDateTime.now(),
                        ex.getMessage(),
                        "BAD_REQUEST",
                        400,
                        request.getRequestURI()
                ));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error_ResponseDto> handleGlobalException(Exception ex,HttpServletRequest request)
    {
        log.error("Unexpected exception", ex);

        return ResponseEntity.status(500)
                .body(new Error_ResponseDto(
                        LocalDateTime.now(),
                        "An unexpected error occurred",
                        "INTERNAL_SERVER_ERROR",
                        500,
                        request.getRequestURI()
                ));
    }
}
