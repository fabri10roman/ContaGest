package com.example.ContaGest.controller;

import com.example.ContaGest.dto.ChangePasswordRequest;
import com.example.ContaGest.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/password")
@RequiredArgsConstructor
public class PasswordController {

    PasswordService passwordService;

    @PatchMapping("/change")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, Principal connectedUser){
        passwordService.changePassword(request,connectedUser);
        return ResponseEntity.ok().build();
    }
}
