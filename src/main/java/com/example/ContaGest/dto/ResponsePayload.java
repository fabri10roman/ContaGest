package com.example.ContaGest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ResponsePayload {
    private int status;
    private String title;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Object> data;
}
