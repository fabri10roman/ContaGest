package com.example.ContaGest.controller;

import com.example.ContaGest.dto.ChangePasswordRequest;
import com.example.ContaGest.dto.ForgotPasswordConfirmRequest;
import com.example.ContaGest.dto.ForgotPasswordRequest;
import com.example.ContaGest.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @PatchMapping("/change")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, Principal connectedUser){
        passwordService.changePassword(request,connectedUser);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/password-forgot")
    public String forgotPassword(@RequestBody ForgotPasswordRequest request){
        return passwordService.forgotPassword(request);
    }

    @PostMapping("/confirm-forgot-password")
    public ResponseEntity<?> confirmForgotPassword(@RequestParam("token")String token,@RequestBody ForgotPasswordConfirmRequest request){
        return ResponseEntity.ok(passwordService.confirmForgotPassword(token,request));
    }
}
