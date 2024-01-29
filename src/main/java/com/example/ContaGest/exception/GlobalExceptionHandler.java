package com.example.ContaGest.exception;


import com.example.ContaGest.dto.ExceptionPayload;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException e){
        ExceptionPayload exceptionPayload = new ExceptionPayload(HttpStatus.NOT_FOUND.value(),HttpStatus.NOT_FOUND.getReasonPhrase(),e.getMessage());
        return new ResponseEntity<>(exceptionPayload, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UsernameNotFoundException e){
        ExceptionPayload exceptionPayload = new ExceptionPayload(HttpStatus.NOT_FOUND.value(),HttpStatus.NOT_FOUND.getReasonPhrase(),e.getMessage());
        return new ResponseEntity<>(exceptionPayload, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleTokenExpiredException(ExpiredJwtException e){
        String message = e.getMessage();
        ExceptionPayload exceptionPayload;
        if (message != null && !message.contains("Allowed clock skew")) {
            exceptionPayload = new ExceptionPayload(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),e.getMessage());
        } else {
            exceptionPayload = new ExceptionPayload(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),"Token Expired");
        }
        return new ResponseEntity<>(exceptionPayload, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<?> handleSignatureException(SignatureException e){
        String message = e.getMessage();
        ExceptionPayload exceptionPayload;
        if (message != null && !message.contains("JWT signature does not match locally computed signature")) {
            exceptionPayload = new ExceptionPayload(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),e.getMessage());
        } else {
            exceptionPayload = new ExceptionPayload(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),"Invalid Token");
        }
        return new ResponseEntity<>(exceptionPayload, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException e){
        ExceptionPayload exceptionPayload = new ExceptionPayload(HttpStatus.INTERNAL_SERVER_ERROR.value(),HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),e.getMessage());
        return new ResponseEntity<>(exceptionPayload, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException e){
        ExceptionPayload exceptionPayload = new ExceptionPayload(HttpStatus.UNAUTHORIZED.value(),HttpStatus.UNAUTHORIZED.getReasonPhrase(),e.getMessage());
        return new ResponseEntity<>(exceptionPayload, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(ConflictExcepcion.class)
    public ResponseEntity<?> handleConflictException(ConflictExcepcion e){
        ExceptionPayload exceptionPayload = new ExceptionPayload(HttpStatus.CONFLICT.value(),HttpStatus.CONFLICT.getReasonPhrase(),e.getMessage());
        return new ResponseEntity<>(exceptionPayload, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException e){
        ExceptionPayload exceptionPayload = new ExceptionPayload(HttpStatus.BAD_REQUEST.value(),HttpStatus.BAD_REQUEST.getReasonPhrase(),e.getMessage());
        return new ResponseEntity<>(exceptionPayload, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabledException(DisabledException e){
        ExceptionPayload exceptionPayload = new ExceptionPayload(HttpStatus.FORBIDDEN.value(),HttpStatus.FORBIDDEN.getReasonPhrase(),e.getMessage());
        return new ResponseEntity<>(exceptionPayload, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<?> handleLockedException(LockedException e){
        ExceptionPayload exceptionPayload = new ExceptionPayload(HttpStatus.FORBIDDEN.value(),HttpStatus.FORBIDDEN.getReasonPhrase(),e.getMessage());
        return new ResponseEntity<>(exceptionPayload, HttpStatus.FORBIDDEN);
    }

}
