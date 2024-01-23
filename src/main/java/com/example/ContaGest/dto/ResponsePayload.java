package com.example.ContaGest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ResponsePayload {
    private final int status = 200;
    private final String title = "OK";
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Object> data;
}
