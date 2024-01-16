package com.example.ContaGest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ForgotPasswordRequest {
    String email;
    String role;
}
