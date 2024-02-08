package com.example.ContaGest.controller;

import com.example.ContaGest.dto.request.ChangePasswordRequest;
import com.example.ContaGest.dto.request.ForgotPasswordConfirmRequest;
import com.example.ContaGest.dto.request.ForgotPasswordRequest;
import com.example.ContaGest.dto.ResponsePayload;
import com.example.ContaGest.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @PatchMapping()
    @ResponseBody
    public ResponsePayload changePassword(@RequestBody ChangePasswordRequest request){
        return passwordService.changePassword(request);

    }
    @PostMapping("/forgot")
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
