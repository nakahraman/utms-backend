package com.utms.backend.exception;

import com.utms.backend.model.record.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {

        ErrorResponse body = new ErrorResponse(
                ex.getCode(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(body);
    }

    @RestControllerAdvice
    public class ValidationExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public Map<String, String> handleValidationErrors(MethodArgumentNotValidException ex) {

            return ex.getBindingResult().getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage,
                            (a, b) -> a
                    ));
        }
    }


 /*   @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {

        ErrorResponse body = new ErrorResponse(
                "SYS-500",
                "Beklenmeyen bir hata oluştu. Lütfen daha sonra tekrar deneyiniz.",
                LocalDateTime.now()
        );

        return ResponseEntity.internalServerError().body(body);
    }

  */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // Hatayı konsola (IntelliJ terminaline) yazdır
        ex.printStackTrace();

        // Hata mesajını Swagger'da doğrudan görmek için mesajı ErrorResponse'a koy
        return ResponseEntity.internalServerError().body(
                new ErrorResponse("SYS-500", ex.getMessage(), LocalDateTime.now())
        );
    }
}



