package com.example.ContaGest.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientResponse {
    Integer id;
    String ci;
    String name;
    String lastname;
    String email;
    String phoneNumber;
}
