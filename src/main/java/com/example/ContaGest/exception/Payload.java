package com.example.ContaGest.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Payload {
    private final int status;
    private final String title;
    private final String message;
}
