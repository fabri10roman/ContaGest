package com.example.ContaGest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)

public class AlreadySendEmailException extends RuntimeException{
    public AlreadySendEmailException(String message) {
        super(message);
    }
}
