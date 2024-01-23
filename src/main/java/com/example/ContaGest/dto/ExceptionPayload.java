package com.example.ContaGest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExceptionPayload {
    private final int status;
    private final String title;
    private final String message;
}
