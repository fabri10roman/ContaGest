package com.example.ContaGest.dto.request;

import com.example.ContaGest.model.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String ci;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
}
