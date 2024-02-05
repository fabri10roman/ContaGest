package com.example.ContaGest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterClientRequest {

    private String lastname;
    private String phoneNumber;
    private String name;
    private String email;
    private String ci;
    private String token;
}
