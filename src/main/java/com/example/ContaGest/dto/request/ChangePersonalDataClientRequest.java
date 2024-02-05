package com.example.ContaGest.dto.request;

import lombok.Getter;

@Getter
public class ChangePersonalDataClientRequest {

        private String name;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String token;

}
