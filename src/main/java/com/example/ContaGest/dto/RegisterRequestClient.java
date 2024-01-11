package com.example.ContaGest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestClient {

    private String lastname;
    private Integer number;
    private String name;
    private String email;
    private String userCI;
    private String accountCI;
}
