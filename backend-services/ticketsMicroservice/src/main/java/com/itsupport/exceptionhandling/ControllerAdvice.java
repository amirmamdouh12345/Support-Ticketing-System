package com.itsupport.exceptionhandling;


import com.itsupport.dtos.ExceptionBody;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException runtimeException){
        ExceptionBody exceptionBody= new ExceptionBody();
        exceptionBody.setMessage(runtimeException.getMessage());
        exceptionBody.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        exceptionBody.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        return ResponseEntity.internalServerError().body(exceptionBody);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException runtimeException){
        ExceptionBody exceptionBody= new ExceptionBody();
        exceptionBody.setMessage(runtimeException.getMessage());
        exceptionBody.setStatus(HttpStatus.BAD_REQUEST.value());
        exceptionBody.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        return ResponseEntity.badRequest().body(exceptionBody);
    }
}
