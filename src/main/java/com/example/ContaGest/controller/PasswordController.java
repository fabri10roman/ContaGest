package com.example.ContaGest.controller;

import com.example.ContaGest.dto.ChangePasswordRequest;
import com.example.ContaGest.dto.ForgotPasswordConfirmRequest;
import com.example.ContaGest.dto.ForgotPasswordRequest;
import com.example.ContaGest.dto.ResponsePayload;
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
    @ResponseBody
    public ResponsePayload changePassword(@RequestBody ChangePasswordRequest request, Principal connectedUser){
        return passwordService.changePassword(request,connectedUser);

    }
    @PostMapping("/password-forgot")
    @ResponseBody
    public ResponsePayload forgotPassword(@RequestBody ForgotPasswordRequest request){
        return passwordService.forgotPassword(request);
    }

    @PostMapping("/confirm-forgot-password")
    @ResponseBody
    public ResponsePayload confirmForgotPassword(@RequestParam("token")String token,@RequestBody ForgotPasswordConfirmRequest request){
        return passwordService.confirmForgotPassword(token,request);
    }
}
