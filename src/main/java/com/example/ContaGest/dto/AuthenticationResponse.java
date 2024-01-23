package com.example.ContaGest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    private String token;
    private String CI;
    private String name;
    private String email;
    private String role;
    private Integer id;
    private String lastname;
    private Integer phoneNumber;
}
